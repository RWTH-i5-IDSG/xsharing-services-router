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
package de.rwth.idsg.xsharing.router.core.aggregation.raster;

import de.rwth.idsg.xsharing.router.AppConfiguration;
import de.rwth.idsg.xsharing.router.persistence.MBDataService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Periodic runner used for keeping sharing station data from Mobility Broker database up to date.
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Slf4j
@Singleton
public class PeriodicStationUpdater {
    @Resource
    private ManagedScheduledExecutorService scheduler;

    @Inject private MBDataService dataService;
    @Inject private RasterManager rasterManager;

    private ScheduledFuture theFuture;
    private long interval;

    @PostConstruct
    public void init() {
        // obtain updater interval from application configuration
        interval = AppConfiguration.CONFIG.getStationInfoInterval();
    }

    /**
     * Ping every INTERVAL minutes
     */
    public ScheduledFuture create() {
        this.theFuture = scheduler.scheduleAtFixedRate(
                new StationRunner(dataService, rasterManager), 0, interval, TimeUnit.MINUTES);
        log.info("Registered runner for updating state of sharing stations. (at interval of {})", interval);
        return theFuture;
    }

    @PreDestroy
    public void destroy() {
        log.info("Cancelling the future!");
        if (theFuture != null) {
            theFuture.cancel(true);
        }
    }
}
