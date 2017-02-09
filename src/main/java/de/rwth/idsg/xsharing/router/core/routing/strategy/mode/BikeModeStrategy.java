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

import de.rwth.idsg.xsharing.router.core.routing.request.SingleMinimalRequest;
import de.rwth.idsg.xsharing.router.core.routing.strategy.RouteContext;
import de.rwth.idsg.xsharing.router.core.routing.strategy.RouteLegList;
import de.rwth.idsg.xsharing.router.core.routing.strategy.RouteLegWrapper;
import de.rwth.idsg.xsharing.router.core.routing.strategy.StrategyDependencyContext;
import de.rwth.idsg.xsharing.router.core.routing.strategy.inavailability.bike.BikeInavailabilityStrategy;
import de.rwth.idsg.xsharing.router.core.routing.util.RouteBuilderException;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.persistence.DatabaseException;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.RasterPoint;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.station.BikeStationTuple;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.BikeLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.RouteLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.WalkingLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.BikeStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.SharingStation;
import de.rwth.idsg.xsharing.router.utils.WalkingLegCloner;
import de.rwth.idsg.xsharing.router.utils.BasicUtils;
import org.joda.time.DateTime;

import javax.ejb.EJBTransactionRolledbackException;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 25.05.2016
 */
public class BikeModeStrategy extends AbstractModeStrategy<BikeLeg> {

    private final BikeInavailabilityStrategy inavailabilityStrategy;

    public BikeModeStrategy(StrategyDependencyContext ctx, BikeInavailabilityStrategy inavailabilityStrategy) {
        super(ctx, inavailabilityStrategy.getTotalStrategy());
        this.inavailabilityStrategy = inavailabilityStrategy;
    }

    @Override
    public Class<BikeLeg> getRouteLegClass() {
        return BikeLeg.class;
    }

    /**
     * START --(walk)--> STATION --(bike)--> STATION --(walk)--> DESTINATION
     *
     * [walks are optional]
     */
    @Override
    public RouteContext findRoute(SingleMinimalRequest request,
                                  RasterPoint fromPoint,
                                  RasterPoint toPoint) throws RouteBuilderException {

        // get all sharing stations for the raster points
        List<BikeStationTuple> fromTuples = fromPoint.getNearestBikeStations();
        List<BikeStationTuple> toTuples = toPoint.getNearestBikeStations();

        if (BasicUtils.checkNullOrEmpty(fromTuples) || BasicUtils.checkNullOrEmpty(toTuples)) {
            // Could not find any sharing stations in range
            return RouteContext.getEmptySingleton();
        }

        // in the defined order, try finding routes using every station type
        // modified to allow toggling fallback stations

        ListIterator<BikeStationTuple> stationsAtStart = getListIterator(fromTuples);
        ListIterator<BikeStationTuple> stationsAtEnd = getListIterator(toTuples);

        Double userMaxWalkDistance = request.getMaxWalkDistance();
        DateTime time = request.getTime();
        boolean isArrival = request.isArrivalTime();
        int stayTime = request.getStayTime();

        // TODO: It does not make sense to iterate start/end stations both at the same time. For ex, if there is a
        // TODO: problem with the 1st candidate of stop stations, we should continue with the 2nd candidate of
        // TODO: stop stations, while still using the 1st of start stations.
        // TODO: But then again, we might arrive at a problematic situation: Is the route a better one with, for ex,
        // TODO: 1st start station and 100th end station, or 2nd start station and 2nd end station?
        //
        while (stationsAtStart.hasNext() && stationsAtEnd.hasNext()) {

            BikeStationTuple startTuple = stationsAtStart.next();
            BikeStation startStation = startTuple.getStation();

            BikeStationTuple endTuple = stationsAtEnd.next();
            BikeStation endStation = endTuple.getStation();

            // routes between 1 station make no sense
            // if the two closest stations are identical there is no sense in driving
            if (startStation.equals(endStation)) {
                // Unable to find route for from -> to. The closest stations are identical
                return RouteContext.getEmptySingleton();
            }

            if (!validateStation(startStation, fromPoint.getCoord(), userMaxWalkDistance)
                    || !validateStation(endStation, toPoint.getCoord(), userMaxWalkDistance)) {
                // stations in the list are ordered by their distance to the raster point. if the nth station
                // cannot be validated (because of max distance), the n+1st will not either (because further away).
                // so, no route.
                return RouteContext.getEmptySingleton();
            }

            RouteLegList legs = new RouteLegList(time, isArrival);

            // -------------------------------------------------------------------------
            // 1. walk leg
            // -------------------------------------------------------------------------

            // walk if you are not at the station
            //
            boolean alreadyAtStartStation = BasicUtils.isEqualXY(startStation.getGeoPos(), request.getStartPoint());
            if (!alreadyAtStartStation) {
                WalkingLeg walkFromStart = startTuple.getLeg();
                if (walkFromStart == null) {
                    continue;
                }
                legs.addAndShift(new RouteLegWrapper(walkFromStart));
            }

            // -------------------------------------------------------------------------
            // 2. bike leg / postpone addition to the list
            // -------------------------------------------------------------------------

            GeoCoord bsEndCoord = BasicUtils.toGeoCoord(endStation.getGeoPos());

            RouteLeg bikeLeg;

            try {
                // retrieve appropriate leg for class and add it
                bikeLeg = super.cacheOrQuery(startStation.getGeoPos(), bsEndCoord);
                if (bikeLeg == null) {
                    continue;
                }
            } catch (DatabaseException e) {
                continue;

            } catch (EJBTransactionRolledbackException rbe) {
                throw new RouteBuilderException("FATAL: Unable to find route for " + fromPoint.getCoord().toString()
                        + "->" + toPoint.getCoord().toString() + "! The transaction was rolled back! ");
            }

            // -------------------------------------------------------------------------
            // 3. walk leg / postpone addition to the list
            // -------------------------------------------------------------------------

            WalkingLeg walkToEnd = null;

            // same as above, walk to destination
            //
            boolean alreadyAtStopStation = BasicUtils.isEqualXY(endStation.getGeoPos(), request.getEndPoint());
            if (!alreadyAtStopStation) {
                walkToEnd = WalkingLegCloner.cloneAndReverse(endTuple.getLeg());
                if (walkToEnd == null) {
                    continue;
                }
            }

            // -------------------------------------------------------------------------
            // 4. post process (check availabilities)
            // -------------------------------------------------------------------------

            boolean success;

            if (walkToEnd == null) {
                success = processBikeAsLastLeg(stayTime, legs, startStation, bikeLeg);
            } else {
                success = processWalkAsLastLeg(stayTime, legs, startStation, bikeLeg, endStation, walkToEnd);
            }

            if (!success) {
                continue;
            }

            // We came so far, within the while. Which means, we successfully found a route. Return it.
            return super.constructRoute(legs.getList());
        }

        // Unable to find route for from -> to
        return RouteContext.getEmptySingleton();
    }

    /**
     * @return true     if processing succeeds
     */
    private boolean processBikeAsLastLeg(int stayTime, RouteLegList legs,
                                         SharingStation startStation, RouteLeg bikeLeg) {

        RouteLegWrapper bikeWrapper = new RouteLegWrapper(bikeLeg);

        if (inavailabilityStrategy.overlaps(stayTime, legs, startStation, bikeWrapper)) {
            return false;
        } else {
            legs.add(bikeWrapper);
            return true;
        }
    }

    /**
     * @return true     if processing succeeds
     */
    private boolean processWalkAsLastLeg(int stayTime , RouteLegList legs,
                                         SharingStation startStation, RouteLeg bikeLeg,
                                         SharingStation endStation, WalkingLeg walkToEnd) {

        RouteLegWrapper bikeWrapper = new RouteLegWrapper(bikeLeg);
        RouteLegWrapper walkWrapper = new RouteLegWrapper(walkToEnd);

        if (inavailabilityStrategy.overlaps(stayTime, legs, startStation, bikeWrapper, endStation, walkWrapper)) {
            return false;
        } else {
            legs.add(bikeWrapper);
            legs.add(walkWrapper);
            return true;
        }
    }
}
