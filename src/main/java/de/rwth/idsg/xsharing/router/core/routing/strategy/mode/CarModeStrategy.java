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
import de.rwth.idsg.xsharing.router.core.routing.strategy.inavailability.car.CarInavailabilityStrategy;
import de.rwth.idsg.xsharing.router.core.routing.util.RouteBuilderException;
import de.rwth.idsg.xsharing.router.persistence.DatabaseException;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.RasterPoint;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.station.CarStationTuple;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.CarLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.RouteLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.WalkingLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.CarStation;
import org.joda.time.DateTime;

import javax.ejb.EJBTransactionRolledbackException;
import java.util.List;
import java.util.ListIterator;

import static de.rwth.idsg.xsharing.router.utils.BasicUtils.checkNullOrEmpty;
import static de.rwth.idsg.xsharing.router.utils.BasicUtils.isEqualXY;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 25.05.2016
 */
public class CarModeStrategy extends AbstractModeStrategy<CarLeg> {

    private final CarInavailabilityStrategy inavailabilityStrategy;

    public CarModeStrategy(StrategyDependencyContext ctx, CarInavailabilityStrategy inavailabilityStrategy) {
        super(ctx, inavailabilityStrategy.getTotalStrategy());
        this.inavailabilityStrategy = inavailabilityStrategy;
    }

    @Override
    public Class<CarLeg> getRouteLegClass() {
        return CarLeg.class;
    }

    /**
     * START --(walk)--> STATION --(car)--> DESTINATION
     *
     * [walk is optional]
     */
    @Override
    public RouteContext findRoute(SingleMinimalRequest request,
                                  RasterPoint fromPoint,
                                  RasterPoint toPoint) throws RouteBuilderException {

        // get all sharing stations for the raster points
        List<CarStationTuple> fromTuples = fromPoint.getNearestCarStations();

        if (checkNullOrEmpty(fromTuples)) {
            // Could not find any sharing stations in range
            return RouteContext.getEmptySingleton();
        }

        // in the defined order, try finding routes using every station type
        // modified to allow toggling fallback stations

        ListIterator<CarStationTuple> stationsAtStart = getListIterator(fromTuples);

        Double userMaxWalkDistance = request.getMaxWalkDistance();
        DateTime time = request.getTime();
        boolean isArrival = request.isArrivalTime();
        int stayTime = request.getStayTime();

        while (stationsAtStart.hasNext()) {

            CarStationTuple startTuple = stationsAtStart.next();
            CarStation startStation = startTuple.getStation();

            if (!validateStation(startStation, fromPoint.getCoord(), userMaxWalkDistance)) {
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
            boolean alreadyAtStation = isEqualXY(startStation.getGeoPos(), request.getStartPoint());
            if (!alreadyAtStation) {
                WalkingLeg walkFromStart = startTuple.getLeg();
                if (walkFromStart == null) {
                    continue;
                }
                legs.addAndShift(new RouteLegWrapper(walkFromStart));
            }

            // -------------------------------------------------------------------------
            // 2. car leg
            // -------------------------------------------------------------------------

            RouteLeg carLeg;

            try {
                // do not route station to station with car, but rather directly to the destination (station-bound!)
                carLeg = super.cacheOrQuery(startStation.getGeoPos(), toPoint.getCoord());
                if (carLeg == null) {
                    continue;
                }
            } catch (DatabaseException e) {
                continue;

            } catch (EJBTransactionRolledbackException rbe) {
                throw new RouteBuilderException("FATAL: Unable to find route for " + fromPoint.getCoord().toString()
                        + "->" + toPoint.getCoord().toString() + "! The transaction was rolled back! ");
            }

            RouteLegWrapper carWrapper = new RouteLegWrapper(carLeg);

            // 2a. Check car leg availability
            //
            if (inavailabilityStrategy.overlaps(stayTime, legs, startStation, carWrapper)) {
                continue;
            }
            legs.add(carWrapper);

            // We came so far, within the while. Which means, we successfully found a route. Return it.
            return super.constructRoute(legs.getList());
        }

        // Unable to find route for from -> to
        return RouteContext.getEmptySingleton();
    }
}
