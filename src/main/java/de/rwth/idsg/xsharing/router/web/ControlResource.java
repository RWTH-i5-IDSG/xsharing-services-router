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
package de.rwth.idsg.xsharing.router.web;

import de.rwth.idsg.xsharing.router.cache.RouteDataCache;
import de.rwth.idsg.xsharing.router.cache.RouteLegCache;
import de.rwth.idsg.xsharing.router.core.CoreBootstrapper;
import de.rwth.idsg.xsharing.router.core.RoutingComponentsProvider;
import de.rwth.idsg.xsharing.router.core.aggregation.raster.PeriodicStationUpdater;
import de.rwth.idsg.xsharing.router.core.aggregation.raster.RasterManager;
import de.rwth.idsg.xsharing.router.core.messaging.debug.Caller;
import de.rwth.idsg.xsharing.router.core.messaging.debug.CompactCaller;
import de.rwth.idsg.xsharing.router.core.messaging.debug.DetailsCaller;
import de.rwth.idsg.xsharing.router.core.routing.request.CompactRequest;
import de.rwth.idsg.xsharing.router.core.routing.request.DetailsRequest;
import de.rwth.idsg.xsharing.router.core.routing.request.SingleMinimalRequest;
import de.rwth.idsg.xsharing.router.core.routing.response.CompactResponse;
import de.rwth.idsg.xsharing.router.core.routing.response.DetailsResponse;
import de.rwth.idsg.xsharing.router.core.routing.serving.SharingRouterServiceImpl;
import de.rwth.idsg.xsharing.router.core.routing.util.RouteBuilderException;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.RasterPoint;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.station.BikeStationTuple;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.station.CarStationTuple;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.Route;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteCompactRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteDetailsRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteMinimalRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.BikeStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.CarStation;
import de.rwth.idsg.xsharing.router.utils.JsonMapper;
import de.rwth.idsg.xsharing.router.utils.StatsManager;
import de.rwth.idsg.xsharing.router.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.joda.time.DateTime;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Offers control functionality over the core system
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
@Slf4j
@Path("/control")
public class ControlResource {

    @Inject private CoreBootstrapper coreBootstrapper;
    @Inject private CompactCaller compactCaller;
    @Inject private DetailsCaller detailsCaller;
    @Inject private RouteDataCache routeDataCache;
    @Inject private PeriodicStationUpdater updater;
    @Inject private RasterManager rasterManager;
    @Inject private RoutingComponentsProvider provider;

    private HashMap<String, Caller> callerMap;

    private static String gitProperties;

    private static SharingRouterServiceImpl routerService;

    @PostConstruct
    public void init() {
        routerService = provider.getRouterService();
        callerMap = new HashMap<>();
        callerMap.put(Constants.JMSConfig.COMPACT_QUEUE_NAME, compactCaller);
        callerMap.put(Constants.JMSConfig.DETAILS_QUEUE_NAME, detailsCaller);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/log")
    public Response getLog() {
        StreamingOutput stream = new LogStreamingOutput();
        return Response.ok(stream).build();
    }

    @PUT
    @Path("/log/change-level/{loggerName}/{newLevel}")
    public Response changeLogLevel(@PathParam("loggerName") String loggerName,
                                   @PathParam("newLevel") String newLevel) {

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(loggerName);

        if (loggerConfig.getName().equals(LogManager.ROOT_LOGGER_NAME)) {
            return Response.ok("Not found", MediaType.TEXT_PLAIN).build();
        }

        loggerConfig.setLevel(Level.valueOf(newLevel));
        ctx.updateLoggers();  // This causes all Loggers to refetch information from their LoggerConfig.

        return Response.ok("Done", MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("/home")
    public Response getHome() {
        XSharingStatusDTO dto = XSharingStatusDTO.builder()
                                                 .cacheStatus(routeDataCache.getCacheStatus())
                                                 .rasterGranularity(BigDecimal.valueOf(rasterManager.getGranularity()))
                                                 .lifecycleStatus(coreBootstrapper.getServerStatus())
                                                 .queueStats(StatsManager.getAll())
                                                 .routeLegCacheStats(RouteLegCache.SINGLETON.getReport())
                                                 .build();

        return Response.ok(dto, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Path("/rebuild-all/{granularity}")
    public Response rebuildAll(@PathParam("granularity") double granularity) {
        log.info("Importing data and rebuilding cache!");
        coreBootstrapper.rebuildAll(granularity);
        return Response.status(200).build();
    }

    @GET
    @Path("/cache/status")
    public Response cacheStatus() {
        log.info("Request for current status of route cache.");
        String status = routeDataCache.getCacheStatus();
        return Response.ok(status, MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("/cache/keys")
    public Response cacheKeys() {
        log.info("Request for current content of route cache.");
        String status = routeDataCache.getCacheKeys();
        return Response.ok(status, MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("/cache/clear")
    public Response clearCache() {
        log.info("Request to clear cache contents.");
        routeDataCache.clearCache();
        return Response.status(200).build();
    }

    /**
     * This is heavy on the application, since we iterate for all the raster points
     * all the attached stations. Should be used responsibly.
     */
    @GET
    @Path("/station/{providerId}/{stationId}")
    public Response getStation(@PathParam("providerId") String providerId,
                               @PathParam("stationId") String stationId) {

        try {
            for (RasterPoint point : rasterManager.getList()) {
                List<BikeStationTuple> bikes = point.getNearestBikeStations();
                for (BikeStationTuple item : bikes) {
                    BikeStation st = item.getStation();
                    if (st.getPlaceId().equals(stationId) && st.getProviderId().equals(providerId)) {
                        String response = JsonMapper.serializeOrNull(st);
                        return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
                    }
                }

                List<CarStationTuple> cars = point.getNearestCarStations();
                for (CarStationTuple item : cars) {
                    CarStation st = item.getStation();
                    if (st.getPlaceId().equals(stationId) && st.getProviderId().equals(providerId)) {
                        String response = JsonMapper.serializeOrNull(st);
                        return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
                    }
                }
            }

            return Response.ok("Error: Station not found", MediaType.TEXT_PLAIN_TYPE)
                           .build();

        } catch (Exception e) {
            log.error("Error occurred", e);
            return Response.serverError()
                           .entity(e.getMessage())
                           .type(MediaType.TEXT_PLAIN_TYPE)
                           .build();
        }
    }

    @GET
    @Path("/trip-info/{tripId}")
    public Response getTripInfo(@PathParam("tripId") String tripId) {
        try {
            RouteMinimalRepresentation route = routeDataCache.getRoute(tripId);
            if (route == null) {
                return Response.ok("Not found", MediaType.TEXT_PLAIN_TYPE).build();
            }

            RouteCompactRepresentation compactRoute = routerService.getRoutes(new CompactRequest(tripId));
            RouteDetailsRepresentation detailsRoute = routerService.getRoutes(new DetailsRequest(tripId));

            CompactResponse compactResponse = new CompactResponse(compactRoute);
            DetailsResponse detailsResponse = new DetailsResponse(detailsRoute);

            TripInfo dto = TripInfo.builder()
                                   .route(route)
                                   .compact(compactResponse)
                                   .details(detailsResponse)
                                   .build();

            return Response.ok(JsonMapper.serializeOrNull(dto), MediaType.APPLICATION_JSON_TYPE).build();

        } catch (RouteBuilderException e) {
            log.error("Exception occurred", e);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/destRequest/{destination}/{routeId}")
    public Response sendRequest(@PathParam("destination") String destination,
                                @PathParam("routeId") String routeId) {

        log.info("Sending dummy request to destination {}", destination);
        String response = callerMap.get(destination).sendRequest(Optional.ofNullable(routeId));

        return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Path("/routeRequest")
    public Response routeRequest(@QueryParam("from_x") Double fromX,
                                 @QueryParam("from_y") Double fromY,
                                 @QueryParam("to_x") Double toX,
                                 @QueryParam("to_y") Double toY) {

        log.info("Received route request for ({},{}), ({},{})", fromX, fromY, toX, toY);
        SingleMinimalRequest req =
                new SingleMinimalRequest(
                        new GeoCoord(fromX, fromY),
                        new GeoCoord(toX, toY),
                        DateTime.now(),
                        false, false, false, 0, 4000D, null);

        try {
            Route route = routerService.getRoutes(req);
            if (route == null) {
                return Response.serverError().build();
            } else {
                return Response.ok(route.toString(), MediaType.TEXT_PLAIN).build();
            }
        } catch (RouteBuilderException e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/raster-granularity")
    public Response getRasterGranularity() {
        log.debug("Received request for raster granularity");

        return Response.ok(String.format("%.6f", (double) rasterManager.getGranularity()), MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("/status")
    public Response getLifecycleStatus() {
        log.debug("Received request for lifecycle status");

        return Response.ok(coreBootstrapper.getServerStatus().toString(), MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("/commit")
    public Response getCommitDetails() {
        if (gitProperties == null) {
            loadGitPropertiesFile();
        }
        return Response.ok(gitProperties, MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("/stats")
    public Response getStatistics() {
        return Response.ok(StatsManager.getAll(), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @PUT
    @Path("/stats/reset")
    public Response resetStatistics() {
        StatsManager.reset();
        return Response.ok().build();
    }

    @PUT
    @Path("/leg-cache/clear")
    public Response clearLegCache() {
        RouteLegCache.SINGLETON.clearCache();
        return Response.ok().build();
    }

    private void loadGitPropertiesFile() {
        try {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("git.properties");
                 InputStreamReader ist = new InputStreamReader(in, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(ist)) {

                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    builder.append(line).append(System.getProperty("line.separator"));
                }
                gitProperties = builder.toString();
            }
        } catch (IOException e) {
            log.error("Exception occurred", e);
        }
    }
}
