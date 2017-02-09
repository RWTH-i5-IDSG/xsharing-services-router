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

import de.rwth.idsg.xsharing.router.persistence.MBDataService;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.SharingStation;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Runner to be executed at fixed interval (polling)
 * Updates changed Mobility Broker sharing stations
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Slf4j
public class StationRunner implements Runnable {
    private MBDataService dataService;
    private RasterManager rasterManager;

    public StationRunner(MBDataService dataService, RasterManager rasterManager) {
        this.dataService = dataService;
        this.rasterManager = rasterManager;
    }

    @Override
    public void run() {
        log.info("START - Updating stations from MB database.");
        try {
            List<SharingStation> stations = dataService.updateAvailabilities();
            rasterManager.updateStations(stations);
            log.info("DONE - Updating stations from MB database.");
        } catch (Exception e) {
            log.error("Station update failed!", e);
//            log.error("Station update failed! {}", e.getMessage());
        }
    }
}
