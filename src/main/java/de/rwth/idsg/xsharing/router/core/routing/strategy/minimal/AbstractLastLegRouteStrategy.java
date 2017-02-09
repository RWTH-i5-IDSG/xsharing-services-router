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
package de.rwth.idsg.xsharing.router.core.routing.strategy.minimal;

import de.rwth.idsg.xsharing.router.core.routing.strategy.RouteLegWrapper;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.RasterPoint;
import de.rwth.idsg.xsharing.router.core.routing.SharingStationType;
import de.rwth.idsg.xsharing.router.core.routing.request.SingleMinimalRequest;
import de.rwth.idsg.xsharing.router.core.routing.strategy.RouteContext;
import de.rwth.idsg.xsharing.router.core.routing.strategy.mode.BikeModeStrategy;
import de.rwth.idsg.xsharing.router.core.routing.strategy.mode.CarModeStrategy;
import de.rwth.idsg.xsharing.router.core.routing.strategy.mode.ModeStrategy;
import de.rwth.idsg.xsharing.router.core.routing.util.RouteBuilderException;

import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 24.05.2016
 */
abstract class AbstractLastLegRouteStrategy implements MinimalRouteStrategy {

    private final ModeStrategy bikeModeStrategy;
    private final ModeStrategy carModeStrategy;

    AbstractLastLegRouteStrategy(BikeModeStrategy bikeModeStrategy, CarModeStrategy carModeStrategy) {
        this.bikeModeStrategy = bikeModeStrategy;
        this.carModeStrategy = carModeStrategy;
    }

    @Override
    public RouteContext findRoute(SingleMinimalRequest request,
                                  RasterPoint fromPoint,
                                  RasterPoint toPoint,
                                  List<SharingStationType> queryTypes) throws RouteBuilderException {

        RouteContext carLegs = RouteContext.getEmptySingleton();
        RouteContext bikeLegs = RouteContext.getEmptySingleton();

        if (queryTypes.contains(SharingStationType.Car)) {
            carLegs = carModeStrategy.findRoute(request, fromPoint, toPoint);
        }

        if (queryTypes.contains(SharingStationType.Bike)) {
            bikeLegs = bikeModeStrategy.findRoute(request, fromPoint, toPoint);
        }

        return decideReturnList(carLegs, bikeLegs);
    }

    private RouteContext decideReturnList(RouteContext carRoute,
                                          RouteContext bikeRoute) {

        List<RouteLegWrapper> carLegs = carRoute.getLegs();
        List<RouteLegWrapper> bikeLegs = bikeRoute.getLegs();

        if (carLegs.isEmpty() && bikeLegs.isEmpty()) {
            return RouteContext.getEmptySingleton();

        } else if (carLegs.isEmpty() && !bikeLegs.isEmpty()) {
            return bikeRoute;

        } else if (!carLegs.isEmpty() && bikeLegs.isEmpty()) {
            return carRoute;

        } else {
            // both lists non-empty
            int totalCar = carRoute.getTotalDurationInSeconds();
            int totalBike = bikeRoute.getTotalDurationInSeconds();

            if (totalCar <= totalBike) {
                return carRoute;
            } else {
                return bikeRoute;
            }
        }
    }
}
