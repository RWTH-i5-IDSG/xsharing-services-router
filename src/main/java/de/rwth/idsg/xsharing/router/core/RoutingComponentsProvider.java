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
package de.rwth.idsg.xsharing.router.core;

import de.rwth.idsg.xsharing.router.cache.RouteDataCache;
import de.rwth.idsg.xsharing.router.core.aggregation.raster.RasterManager;
import de.rwth.idsg.xsharing.router.core.routing.LegService;
import de.rwth.idsg.xsharing.router.core.routing.SharingStationType;
import de.rwth.idsg.xsharing.router.core.routing.batch.request.distancematrix.UpdateLegProcessor;
import de.rwth.idsg.xsharing.router.core.routing.serving.SharingRouterServiceImpl;
import de.rwth.idsg.xsharing.router.core.routing.strategy.StrategyDependencyContext;
import de.rwth.idsg.xsharing.router.core.routing.strategy.route.DefaultRouteBuilderStrategy;
import de.rwth.idsg.xsharing.router.core.routing.strategy.route.RouteBuilderStrategy;
import de.rwth.idsg.xsharing.router.iv.IVRouterClient;
import de.rwth.idsg.xsharing.router.iv.IVRouterClientImpl;
import de.rwth.idsg.xsharing.router.iv.IVRouterService;
import de.rwth.idsg.xsharing.router.persistence.repository.RouteLegRepository;
import de.rwth.idsg.xsharing.router.persistence.repository.StationRepository;
import de.rwth.idsg.xsharing.router.utils.StationClassUtils;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.rwth.idsg.xsharing.router.AppConfiguration.CONFIG;

/**
 * Provides some plain Java objects that do some heavy-lifting computation. Previously, these were Java EE objects
 * and we could see that the container (i.e. server) always intercepted the method calls, which added some
 * unnecessary overhead, because we call these in a tight loop.
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.01.2016
 */
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Singleton
public class RoutingComponentsProvider {

    @Inject private RouteDataCache routeCache;

    @Inject private RouteLegRepository routeLegRepository;
    @Inject private StationRepository stationRepository;
    @Inject private RasterManager rasterManager;
    @Inject private LegService legService;
    @Inject private IVRouterService ivRouterService;

    private SharingRouterServiceImpl routerService;
    private SharingRouterServiceImpl lowerBoundsRouterService;

    private IVRouterClientImpl ivRouterClient;
    private UpdateLegProcessor updateLegProcessor;

    private RouteBuilderStrategy routeBuilderStrategy;
    private RouteBuilderStrategy lowerBoundsRouteBuilderStrategy;

    @PostConstruct
    public void init() {
        initRouteBuilderStrategies();

        routerService = new SharingRouterServiceImpl(routeCache, routeBuilderStrategy);
        lowerBoundsRouterService = new SharingRouterServiceImpl(routeCache, lowerBoundsRouteBuilderStrategy);

        updateLegProcessor = new UpdateLegProcessor(rasterManager);
        ivRouterClient = new IVRouterClientImpl(ivRouterService);
    }

    public SharingRouterServiceImpl getRouterService() {
        return routerService;
    }

    public SharingRouterServiceImpl getLowerBoundsRouterService() {
        return lowerBoundsRouterService;
    }

    public UpdateLegProcessor getUpdateLegProcessor() {
        return updateLegProcessor;
    }

    public IVRouterClient getIVRouterClient() {
        return ivRouterClient;
    }

    private void initRouteBuilderStrategies() {
        String[] types = CONFIG.getSharingTypes();
        Optional<List<SharingStationType>> optional = StationClassUtils.getStationTypesList(types);

        List<SharingStationType> defaultSharingStationTypes = Collections.emptyList();
        if (optional.isPresent()) {
            defaultSharingStationTypes = optional.get();
        }

        double maxDistance = CONFIG.getMaxWalkingDistance();

        StrategyDependencyContext defaultCtx = StrategyDependencyContext.builder()
                                                                        .useLiveStatus(true)
                                                                        .routeLegRepository(routeLegRepository)
                                                                        .maxDistance(maxDistance)
                                                                        .build();

        routeBuilderStrategy =
                new DefaultRouteBuilderStrategy(
                        stationRepository, rasterManager, legService,
                        defaultSharingStationTypes, defaultCtx);

        StrategyDependencyContext lowerCtx = StrategyDependencyContext.builder()
                                                                      .useLiveStatus(false)
                                                                      .routeLegRepository(routeLegRepository)
                                                                      .maxDistance(maxDistance)
                                                                      .build();

        lowerBoundsRouteBuilderStrategy =
                new DefaultRouteBuilderStrategy(
                        stationRepository, rasterManager, legService,
                        defaultSharingStationTypes, lowerCtx);
    }
}
