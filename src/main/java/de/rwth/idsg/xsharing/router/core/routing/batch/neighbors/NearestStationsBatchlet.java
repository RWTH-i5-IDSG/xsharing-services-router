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
package de.rwth.idsg.xsharing.router.core.routing.batch.neighbors;

import de.rwth.idsg.xsharing.router.Constants;
import de.rwth.idsg.xsharing.router.core.aggregation.raster.RasterManager;
import de.rwth.idsg.xsharing.router.core.routing.batch.BatchManager;
import de.rwth.idsg.xsharing.router.core.routing.batch.CustomBatchlet;
import de.rwth.idsg.xsharing.router.persistence.CarAndBikeStations;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.RasterPoint;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.station.BikeStationTuple;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.station.CarStationTuple;
import de.rwth.idsg.xsharing.router.persistence.repository.StationRepository;
import lombok.extern.slf4j.Slf4j;

import javax.batch.api.BatchProperty;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.context.JobContext;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static de.rwth.idsg.xsharing.router.utils.BasicUtils.checkNullOrEmpty;

/**
 * Batchlet implementation for obtaining the nearest stations of each station and subsequently updating the raster
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Dependent
@Named("NearestStationsBatchlet")
@Slf4j
public class NearestStationsBatchlet extends CustomBatchlet {

    @Inject private JobContext jobContext;
    @Inject private BatchManager batchManager;

    @Inject private StationRepository stationRepository;
    @Inject private RasterManager rasterManager;

    @Inject
    @BatchProperty(name = Constants.BatchConstants.START)
    private String start;

    @Inject
    @BatchProperty(name = Constants.BatchConstants.END)
    private String end;

    @Override
    public BatchStatus processBatch() throws Exception {
        logBatchStart();

        long jobId = jobContext.getExecutionId();
        JobOperator operator = BatchRuntime.getJobOperator();
        Properties props = operator.getParameters(jobId);

        Double maxWalk = Double.parseDouble(props.getProperty(Constants.BatchConstants.MAX_WALK));

        int startPos = Integer.valueOf(start);
        int endPos = Integer.valueOf(end);

        List<RasterPoint> points = batchManager.getRasterSublist(jobId, startPos, endPos);

        // find all sharing stations within walking distance of raster point!
        for (RasterPoint point : points) {
            if (hasStations(point)) {
                continue;
            }

            CarAndBikeStations cb = stationRepository.findAllStationsInRadius(point.getX(), point.getY(), maxWalk);

            List<BikeStationTuple> nearestBs = cb.getBikeStations()
                                                 .stream()
                                                 .map(station -> new BikeStationTuple(null, station))
                                                 .collect(Collectors.toList());

            List<CarStationTuple> nearestCs = cb.getCarStations()
                                                .stream()
                                                .map(station -> new CarStationTuple(null, station))
                                                .collect(Collectors.toList());

            // update raster point in db
            point.setNearestBikeStations(nearestBs);
            point.setNearestCarStations(nearestCs);
            rasterManager.updateRasterPointStations(point);

            if (shouldStop) {
                logBatchStop();
                return BatchStatus.STOPPING;
            }
        }

        logBatchFinish();
        return BatchStatus.COMPLETED;
    }

    @Override
    protected void logBatch(StatusForLog status) {
        log.info("{} processing of {} {} at interval [{},{}]",
                status, jobContext.getJobName(), jobContext.getExecutionId(), start, end);
    }

    // determine if point is fetched from database -> skip fetching closest stations
    private static boolean hasStations(RasterPoint rp) {
        boolean noStations = checkNullOrEmpty(rp.getNearestBikeStations()) && checkNullOrEmpty(rp.getNearestCarStations());
        return !noStations;
    }


}
