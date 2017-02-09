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

import de.rwth.idsg.xsharing.router.core.aggregation.raster.PeriodicStationUpdater;
import de.rwth.idsg.xsharing.router.core.aggregation.ura.StationService;
import de.rwth.idsg.xsharing.router.core.routing.RouteBuilderService;
import de.rwth.idsg.xsharing.router.core.routing.ServerStatus;
import de.rwth.idsg.xsharing.router.persistence.MBDataService;
import de.rwth.idsg.xsharing.router.persistence.repository.MasterRepository;
import de.rwth.idsg.xsharing.router.persistence.repository.RasterRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTimeZone;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionManagement;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;

/**
 * Central manager bean responsible for initiating bootstrap behavior on startup
 * Also responsible for maintaining server status
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
@Slf4j
@Startup
@Singleton
@TransactionManagement
@TransactionAttribute
public class CoreBootstrapper {

    @Inject private MBDataService mbDataService;
    @Inject private StationService stationService;
    @Inject private RouteBuilderService routeBuilderService;
    @Inject private PeriodicStationUpdater periodicStationUpdater;
    @Inject private MasterRepository masterRepository;
    @Inject private RasterRepository rasterRepository;

    @Inject private Event<ServerStatus> statusChangeEvents;

    @Getter
    private ServerStatus serverStatus = ServerStatus.BOOTING;

    @PostConstruct
    public void init() {

        // Just to be extra sure
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        DateTimeZone.setDefault(DateTimeZone.UTC);

        log.info("System going for boot. Server status: {}", this.serverStatus);

        // setServerStatus(ServerStatus.BOOTING);
        // statusChangeEvents.fire(ServerStatus.BOOTING);

        // use default granularity
        triggerDataLoad(0);
    }

    public void setServerStatus(ServerStatus serverStatus) {
        log.info("Changing server status: {} -> {}", this.serverStatus, serverStatus);
        this.serverStatus = serverStatus;

        // notify also all observers about the change
        statusChangeEvents.fire(serverStatus);
    }

    @Asynchronous
    private void triggerDataLoad(double granularity) {
        CompletableFuture.supplyAsync(() -> rasterRepository.getAllRasterPoints())
                         .thenApply(dp -> routeBuilderService.prepareCore(dp, granularity))
                         .thenAccept(this::dataLoadFollowUp);
    }

    private void dataLoadFollowUp(boolean success) {
        if (success) {
            log.debug("Done loading, following up!");
            // finished loading, now accept requests
            setServerStatus(ServerStatus.SERVING);
            periodicStationUpdater.create();

        } else {
            // There is no computation log, need to start prepocessing
            importData();
            prepareBuildRouteLegs();
        }
    }

    public void computationFinished() {
        triggerDataLoad(0);
    }

    private void importData() {
        log.info("Importing from all required data sources");
        mbDataService.importMBData();
        stationService.importStopPoints();
        //TODO introduce IXSI subscriber functionality (instead of periodic updater)
//        mbDataService.subscribeAvailabilities();
    }

    private void prepareBuildRouteLegs() {
        routeBuilderService.buildNearestStationsMap();
    }

    public void rebuildAll(double granularity) {
        log.info("Initiating complete reinitialization of the system.");
        periodicStationUpdater.destroy();

        masterRepository.truncateDatabase();

        setServerStatus(ServerStatus.BOOTING);
        triggerDataLoad(granularity);
    }
}

