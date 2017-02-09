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
package de.rwth.idsg.xsharing.router.core.routing.serving;

import de.rwth.idsg.xsharing.router.cache.RouteDataCache;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteCompactRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteDetailsRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteMinimalRepresentation;
import de.rwth.idsg.xsharing.router.core.routing.request.CompactRequest;
import de.rwth.idsg.xsharing.router.core.routing.request.DetailsRequest;
import de.rwth.idsg.xsharing.router.core.routing.request.SingleMinimalRequest;
import de.rwth.idsg.xsharing.router.core.routing.strategy.route.RouteBuilderStrategy;
import de.rwth.idsg.xsharing.router.core.routing.util.RouteBuilderException;
import de.rwth.idsg.xsharing.router.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
@Slf4j
@RequiredArgsConstructor
public class SharingRouterServiceImpl implements SharingRouterService {

    private final RouteDataCache routeCache;
    private final RouteBuilderStrategy strategy;

    @Nullable
    @Override
    public RouteMinimalRepresentation getRoutesForLowerBounds(SingleMinimalRequest request) throws RouteBuilderException {
        return strategy.createRoute(request);
    }

    @Override
    @Nullable
    public RouteMinimalRepresentation getRoutes(SingleMinimalRequest request) throws RouteBuilderException {
        RouteMinimalRepresentation route = strategy.createRoute(request);

        // If the set "time" is not for departure but for arrival, we calculate the departure time from this
        // arrival time subtracting the leg duration, transfer time, etc. At the end the calculated departure time
        // might be in the past. In this case, we should not return a route.
        //
        if (route != null && route.getDeparture().isBefore(DateTimeUtils.getNowWithOffset())) {
            return null;
        }

        // save route in cache for future requests
        // null check happens withing the method
        //
        routeCache.putRoute(route);

        return route;
    }

    @Nullable
    @Override
    public RouteCompactRepresentation getRoutes(CompactRequest request) throws RouteBuilderException {
        if (request == null) {
            log.error("Invalid CompactRequest (null)!");
            return null;
        }

        RouteMinimalRepresentation route = routeCache.getRoute(request.getTripId());

        if (route == null || route.getLegs() == null) {
            log.error("Could not find route with id {} in cache!", request.getTripId());
            return null;
        }

        return strategy.getCompact(route);
    }

    @Nullable
    @Override
    public RouteDetailsRepresentation getRoutes(DetailsRequest request) throws RouteBuilderException {
        if (request == null) {
            log.error("Invalid DetailsRequest (null)!");
            return null;
        }

        RouteMinimalRepresentation route = routeCache.getRoute(request.getTripId());

        if (route == null || route.getLegs() == null) {
            log.error("Could not find route with id {} in cache!", request.getTripId());
            return null;
        }

        return strategy.getDetails(route);
    }
}
