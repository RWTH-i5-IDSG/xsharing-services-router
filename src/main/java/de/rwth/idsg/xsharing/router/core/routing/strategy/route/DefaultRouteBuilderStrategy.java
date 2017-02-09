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
package de.rwth.idsg.xsharing.router.core.routing.strategy.route;

import de.rwth.idsg.xsharing.router.persistence.DatabaseException;
import de.rwth.idsg.xsharing.router.core.aggregation.raster.RasterManager;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.RasterPoint;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.BikeLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.CarLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.LegType;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.RouteLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.WalkingLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.factory.RouteRepresentationFactory;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.leg.LegMinimalRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteCompactRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteDetailsRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteMinimalRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.representation.StationRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.representation.StationRepresentationFactory;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.BikeStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.CarStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.SharingStation;
import de.rwth.idsg.xsharing.router.persistence.repository.StationRepository;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.core.routing.LegService;
import de.rwth.idsg.xsharing.router.core.routing.SharingStationType;
import de.rwth.idsg.xsharing.router.core.routing.request.SingleMinimalRequest;
import de.rwth.idsg.xsharing.router.core.routing.strategy.RouteContext;
import de.rwth.idsg.xsharing.router.core.routing.strategy.StrategyDependencyContext;
import de.rwth.idsg.xsharing.router.core.routing.strategy.minimal.MinimalRouteStrategyFactory;
import de.rwth.idsg.xsharing.router.core.routing.util.MinimalRouteFactory;
import de.rwth.idsg.xsharing.router.core.routing.util.RouteBuilderException;
import de.rwth.idsg.xsharing.router.core.routing.util.ThrowingFunction;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 07.03.2016
 */
@Slf4j
public class DefaultRouteBuilderStrategy implements RouteBuilderStrategy {

    private final StationRepository stationRepository;
    private final RasterManager rasterManager;
    private final List<SharingStationType> defaultSharingStationTypes;
    private final MinimalRouteStrategyFactory routeStrategyFactory;

    private final ThrowingFunction<LegMinimalRepresentation, RouteLeg> fetchForCompact;
    private final ThrowingFunction<LegMinimalRepresentation, RouteLeg> fetchForDetails;

    public DefaultRouteBuilderStrategy(StationRepository stationRepository,
                                       RasterManager rasterManager,
                                       LegService legService,
                                       List<SharingStationType> defaultSharingStationTypes,
                                       StrategyDependencyContext ctx) {
        this.stationRepository = stationRepository;
        this.rasterManager = rasterManager;
        this.defaultSharingStationTypes = defaultSharingStationTypes;
        this.routeStrategyFactory = new MinimalRouteStrategyFactory(ctx);

        this.fetchForCompact = (leg) -> legService.fetchLegForCompact(leg.getId(), leg.getType());
        this.fetchForDetails = (leg) -> legService.fetchLegForDetails(leg.getId(), leg.getType());
    }

    /**
     * !! RETURNS A ROUTE OR NULL WHEN NONE FOUND !!
     */
    @Nullable
    @Override
    public RouteMinimalRepresentation createRoute(SingleMinimalRequest request) throws RouteBuilderException {

        // lookup closest raster points at start and end positions for entry
        RasterPoint[] rasterArray = rasterManager.getPointNearAsArray(request);
        RasterPoint fromPoint = rasterArray[0];
        RasterPoint toPoint = rasterArray[1];

        if (fromPoint == null || toPoint == null) {
            // Unable to compute route for from -> to.
            // No matching raster point could be found! (query out of supported bounds?)
            return null;
        }

        // set the required modes of travel (if specified in request)
        //
        Optional<List<SharingStationType>> optionalTypes = request.getStationTypes();
        List<SharingStationType> queryTypes;

        if (optionalTypes.isPresent()) {
            queryTypes = optionalTypes.get();
            // Early exit, when we received modes that we do not support,
            // and therefore the list of station types is empty
            if (queryTypes.isEmpty()) {
                return null;
            }
        } else {
            // "modes" parameter in the request was not set. fall back to default.
            queryTypes = defaultSharingStationTypes;
        }

        // Main logic
        RouteContext sm = routeStrategyFactory.decideStrategy(request)
                                              .findRoute(request, fromPoint, toPoint, queryTypes);

        if (sm.getLegs().isEmpty()) {
            return null;
        } else {
            GeoCoord from = request.getStartPoint();
            GeoCoord to = request.getEndPoint();
            return MinimalRouteFactory.getMinRep(from, to, request.getTime(), request.isArrivalTime(), sm);
        }
    }

    @Nullable
    @Override
    public RouteCompactRepresentation getCompact(@Nonnull RouteMinimalRepresentation min) throws RouteBuilderException {
        try {
            LinkedList<RouteLeg> routeLegs = this.getRouteLegs(min, fetchForCompact);
            return RouteRepresentationFactory.getCompactRep(min, routeLegs);
        } catch (DatabaseException dbe) {
            String msg = "Unable to get compact rep. " + dbe.getMessage();
            log.error(msg);
            throw new RouteBuilderException(msg);
        }
    }

    @Nullable
    @Override
    public RouteDetailsRepresentation getDetails(@Nonnull RouteMinimalRepresentation min) throws RouteBuilderException {
        try {
            LinkedList<RouteLeg> routeLegs = this.getRouteLegs(min, fetchForDetails);
            LinkedList<StationRepresentation> stations = new LinkedList<>();
            for (RouteLeg leg : routeLegs) {
                // Bike legs are from station to station
                if (leg instanceof BikeLeg) {
                    stations.add(getStartStation(leg, BikeStation.class));
                    stations.add(getEndStation(leg, BikeStation.class));

                // Car legs are from station to destination, there is no end station!
                } else if (leg instanceof CarLeg) {
                    stations.add(getStartStation(leg, CarStation.class));
                }
            }
            return RouteRepresentationFactory.getDetailsRep(min, routeLegs, stations);
        } catch (DatabaseException dbe) {
            String msg = "Unable to get details rep. " + dbe.getMessage();
            log.error(msg);
            throw new RouteBuilderException(msg);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private StationRepresentation getStartStation(RouteLeg leg, Class<? extends SharingStation> clazz) throws DatabaseException {
        SharingStation fromStation = stationRepository.findByCoordinate(leg.getFrom().getX(),
                                                                        leg.getFrom().getY(),
                                                                        clazz);
        return StationRepresentationFactory.getRepresentation(fromStation);
    }

    private StationRepresentation getEndStation(RouteLeg leg, Class<? extends SharingStation> clazz) throws DatabaseException {
        SharingStation toStation = stationRepository.findByCoordinate(leg.getTo().getX(),
                                                                      leg.getTo().getY(),
                                                                      clazz);
        return StationRepresentationFactory.getRepresentation(toStation);
    }

    private LinkedList<RouteLeg> getRouteLegs(RouteMinimalRepresentation min,
                                              ThrowingFunction<LegMinimalRepresentation, RouteLeg> fetchFunction) throws DatabaseException {

        LinkedList<RouteLeg> routeLegs = new LinkedList<>();
        for (LegMinimalRepresentation leg : min.getLegs()) {

            RouteLeg routeLeg = fetchFunction.apply(leg);

            if (routeLeg == null) {
                throw new DatabaseException("Unable to fetch route leg.");
            }

            if (LegType.WalkingLeg.equals(leg.getType()) && checkReverse(leg, routeLeg)) {
                ((WalkingLeg) routeLeg).reverse();
            }

            routeLegs.add(routeLeg);
        }
        return routeLegs;
    }

    /**
     * Checks whether the minrep was reversed (for return trips etc)
     *
     * @param min the MinimalRepresentation of a route leg (possibly reversed)
     * @param leg the RouteLeg as retrieved from DB
     * @return true if the leg was reversed, else false
     */
    private boolean checkReverse(LegMinimalRepresentation min, RouteLeg leg) {
        return !(min.getFrom().equals(leg.getFrom()) && min.getTo().equals(leg.getTo()));
    }
}
