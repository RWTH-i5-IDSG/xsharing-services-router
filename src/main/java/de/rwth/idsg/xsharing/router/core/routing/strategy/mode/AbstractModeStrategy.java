/*
 * Copyright (C) 2015-2017 RWTH Aachen University - Information Systems - Intelligent Distributed Systems Group.
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.rwth.idsg.xsharing.router.core.routing.strategy.mode;

import com.vividsolutions.jts.geom.Point;
import de.rwth.idsg.xsharing.router.cache.RouteLegCache;
import de.rwth.idsg.xsharing.router.core.routing.strategy.RouteContext;
import de.rwth.idsg.xsharing.router.core.routing.strategy.RouteLegWrapper;
import de.rwth.idsg.xsharing.router.core.routing.strategy.StrategyDependencyContext;
import de.rwth.idsg.xsharing.router.core.routing.strategy.duration.TotalStrategy;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.persistence.DatabaseException;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.station.StationTuple;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.RouteLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.SharingStation;
import de.rwth.idsg.xsharing.router.persistence.repository.RouteLegRepository;
import de.rwth.idsg.xsharing.router.utils.BasicUtils;
import de.rwth.idsg.xsharing.router.utils.GeoCoordTuple;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 25.05.2016
 */
public abstract class AbstractModeStrategy<LEG extends RouteLeg> implements ModeStrategy<LEG> {

    private final double defaultMaxDistance;
    private final boolean useLiveStatus;
    private final RouteLegRepository routeLegRepository;
    private final TotalStrategy totalStrategy;

    AbstractModeStrategy(StrategyDependencyContext ctx, TotalStrategy totalStrategy) {
        this.useLiveStatus = ctx.isUseLiveStatus();
        this.routeLegRepository = ctx.getRouteLegRepository();
        this.defaultMaxDistance = ctx.getMaxDistance();
        this.totalStrategy = totalStrategy;
    }

    /**
     * Annotates the route with the total duration based on the legs.
     */
    RouteContext constructRoute(List<RouteLegWrapper> legs) {
        int totalDuration = totalStrategy.getTotalRouteDurationInSeconds(legs);
        double totalDistance = totalStrategy.getTotalRouteDistance(legs);

        return new RouteContext(legs, totalDuration, totalDistance);
    }

    /**
     * Cache implementation. Look in the cache first, if miss then query the database.
     *
     * We only cache Bike or Car legs, and not Walk legs! Walk legs are attached to BikeStationTuple or CarStationTuple
     * and therefore are always present in memory.
     */
    RouteLeg cacheOrQuery(Point from, GeoCoord to) throws DatabaseException {

        GeoCoordTuple key = new GeoCoordTuple(from.getX(), from.getY(), to.getX(), to.getY());

        // 1. Look in cache
        //
        RouteLeg leg = RouteLegCache.SINGLETON.getIfPresent(key);
        if (leg != null) {
            return leg;
        }

        GeoCoord fromCoord = BasicUtils.toGeoCoord(from);

        // 2. Look in db
        //
        leg = routeLegRepository.findLegByFromTo(fromCoord, to, getRouteLegClass());
        if (leg != null) {
            RouteLegCache.SINGLETON.put(key, leg);
            return leg;
        }

        // 3. All options failed. Could not find entity for the coordinates
        return null;
    }

    <TUPLE extends StationTuple> ListIterator<TUPLE> getListIterator(List<TUPLE> fromTuples) {
        if (useLiveStatus) {
            return fromTuples.listIterator();
        } else {
            List<TUPLE> out;
            if (fromTuples.isEmpty()) {
                out = Collections.emptyList();
            } else {
                out = fromTuples.subList(0, 1);
            }
            return out.listIterator();
        }
    }

    // -------------------------------------------------------------------------
    // Validation methods
    // -------------------------------------------------------------------------

    <STATION extends SharingStation> boolean validateStation(STATION station, GeoCoord start, Double userMaxDistance) {
        GeoCoord startCoord = new GeoCoord(station.getGeoPos().getX(), station.getGeoPos().getY());
        return checkMaxWalkingDistance(start, startCoord, userMaxDistance);
    }

    /**
     * Deprecated, because getAvailableCapacity() and getAvailableVehicles() always return real-time data
     * (only for the time being!) and cannot say anything about the future. But a routing request must not
     * be necessarily for the present time.
     *
     * --
     *
     * Determine whether a station can be used for sharing navigation
     * This mainly incorporates checking for available vehicles or (if it's the target station) for free slots,
     * as well as if the station is reachable (on foot)
     *
     * @param station the station to validate
     * @param start the starting location from which to head for the station
     * @param isTarget is the station source or target on the route?
     *
     * @return true if the station can be used, false if it's infeasible
     */
    @Deprecated
    <STATION extends SharingStation> boolean validateStation(STATION station, GeoCoord start, Double userMaxDistance,
                                                             boolean isTarget) {
        GeoCoord startCoord = new GeoCoord(station.getGeoPos().getX(), station.getGeoPos().getY());

        // TODO Problem: this checks if there are booking targets, but what if they are disabled/broken?
        // if live status is disabled, do not check for station capacity
        if (!useLiveStatus) {
            return checkMaxWalkingDistance(start, startCoord, userMaxDistance);
        }

        if (isTarget) {
            return getCapacityConstraint(station.getAvailableCapacity())
                    && checkMaxWalkingDistance(start, startCoord, userMaxDistance);
        } else {
            return (station.getAvailableVehicles() > 0 && checkMaxWalkingDistance(start, startCoord, userMaxDistance));
        }
    }

    /**
     * When a sharing provider does not support Place Availability in IXSI, availabileCapacity of the entity
     * can be NULL. This constraint should alter the behaviour of the routing only if the field is set.
     * Otherwise, return true (i.e. do not restrict the returned result set)
     */
    private static boolean getCapacityConstraint(Integer availabileCapacity) {
        return availabileCapacity == null || availabileCapacity > 0;
    }

    /**
     * Check whether the destination coordinate lies within the bounds of the user's walking
     * capabilities (optimistically relying on euclidean distance)
     *
     * @param from starting coordinates of the trip
     * @param to target/destination coordinates
     * @param userMaxDistance max distance specified in request
     *
     * @return true if within allowed walking distance, else false
     */
    private boolean checkMaxWalkingDistance(GeoCoord from, GeoCoord to, Double userMaxDistance) {
        double distance = BasicUtils.distFrom(from.getY(), from.getX(), to.getY(), to.getX());
        if (userMaxDistance == null) {
            return distance < defaultMaxDistance;
        } else {
            return distance < userMaxDistance;
        }
    }
}
