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
package de.rwth.idsg.xsharing.router.core.routing.batch.request.distancematrix;

import de.rwth.idsg.xsharing.router.Constants.BatchConstants;
import de.rwth.idsg.xsharing.router.Constants.IVRouterConfig;
import de.rwth.idsg.xsharing.router.core.RoutingComponentsProvider;
import de.rwth.idsg.xsharing.router.core.routing.batch.BatchManager;
import de.rwth.idsg.xsharing.router.core.routing.batch.CustomBatchlet;
import de.rwth.idsg.xsharing.router.iv.request.IVRequestTuple;
import de.rwth.idsg.xsharing.router.iv.IVRouterClient;
import de.rwth.idsg.xsharing.router.iv.util.IVRequestFactory;
import de.rwth.idsg.xsharing.router.iv.model.EsriFeatureAttribute;
import de.rwth.idsg.xsharing.router.iv.model.EsriPoint;
import de.rwth.idsg.xsharing.router.iv.model.EsriPointFeature;
import de.rwth.idsg.xsharing.router.iv.model.EsriPolyLineFeature;
import de.rwth.idsg.xsharing.router.iv.model.SpatialReference;
import de.rwth.idsg.xsharing.router.iv.request.DistanceMatrixRequest;
import de.rwth.idsg.xsharing.router.iv.response.DistanceMatrixResult;
import de.rwth.idsg.xsharing.router.persistence.DatabaseException;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.LegType;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.RouteLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.util.RouteLegFactory;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.WalkingLeg;
import de.rwth.idsg.xsharing.router.persistence.repository.RouteLegRepository;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.batch.api.BatchProperty;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Performs concrete dispatching of matrix requests to IV router
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Named("MatrixRequestBatchlet")
@Slf4j
public class MatrixRequestBatchlet extends CustomBatchlet {

    @Inject private JobContext jobContext;
    @Inject private BatchManager batchManager;
    @Inject private RouteLegRepository routeLegRepository;
    @Inject private RoutingComponentsProvider provider;

    private UpdateLegProcessor updateLegProcessor;

    @Inject
    @BatchProperty(name = BatchConstants.START)
    private String start;

    @Inject
    @BatchProperty(name = BatchConstants.END)
    private String end;

    private LegType type;
    private boolean isIVRoute;
    private boolean isCartesian;

    private IVRouterClient routerClient;
    private Consumer<RouteLeg> databaseWorker;

    @PostConstruct
    public void init() {
        JobOperator operator = BatchRuntime.getJobOperator();
        Properties jobProperties = operator.getParameters(jobContext.getExecutionId());

        type = LegType.valueOf(jobProperties.getProperty((BatchConstants.PATH_TYPE)));
        isIVRoute = Boolean.valueOf(jobProperties.getProperty((BatchConstants.IVROUTE)));
        isCartesian = Boolean.valueOf(jobProperties.getProperty((BatchConstants.CARTESIAN)));

        updateLegProcessor = provider.getUpdateLegProcessor();
        routerClient = provider.getIVRouterClient();
        databaseWorker = initWorker(type);
    }

    /**
     * The decision, what to do with a RouteLeg during the computation, depends on the LegType which is set as a job
     * property when submitting the job and does not change afterwards. Set the implementation during init.
     */
    private Consumer<RouteLeg> initWorker(LegType type) {
        switch (type) {
            case WalkingLeg:
                // try update for raster point entries
                return (leg) -> updateLegProcessor.process((WalkingLeg) leg);

            default:
                return (leg) -> {
                    // try-catch because Consumer does not allow to throw checked exceptions
                    try {
                        routeLegRepository.saveRouteLeg(leg);
                    } catch (DatabaseException e) {
                        throw new RuntimeException(e);
                    }
                };
        }
    }

    @Override
    public BatchStatus processBatch() throws Exception {
        logBatchStart();
        List<DistanceMatrixRequest> requests = deserializeRequests();

        int i = 0;
        int n = Integer.parseInt(end) - Integer.parseInt(start);

        for (DistanceMatrixRequest r : requests) {
            try {
                processRequest(r);
            } catch (Exception e) {
                log.error("Exception happened while processing IVRouter request {}:{}", i, e.getMessage());
            }

            i++;
            if ((i % 10) == 0) {
                log.info("Processed matrix request {}/{} {} {} ({})", i, n, jobContext.getJobName(), jobContext.getExecutionId(), type);
            } else {
                log.debug("Processed matrix request {}/{} {} {} ({})", i, n, jobContext.getJobName(), jobContext.getExecutionId(), type);
            }

            if (shouldStop) {
                logBatchStop();
                return BatchStatus.STOPPING;
            }
        }

        logBatchFinish();
        return BatchStatus.COMPLETED;
    }

    /**
     * Actual request sending and result handling
     * if leg is walking mode the raster is updated accordingly!
     */
    private boolean processRequest(DistanceMatrixRequest request) throws DatabaseException {
        DistanceMatrixResult result = routerClient.getDistanceMatrix(request);
        if (result == null) {
            return false;
        }

        for (EsriPolyLineFeature poly : result.getValue().getFeatures()) {
            EsriFeatureAttribute att = poly.getAttributes();
            if (att.getTime() == -1 && att.getDistance() == -1) {
                // we want to skip invalid legs altogether
                continue;
            }
            RouteLeg leg = RouteLegFactory.getLeg(type, poly);
            if (leg == null) {
                // do not save invalid legs!
                continue;
            }

            databaseWorker.accept(leg);
        }
        return true;
    }

    /**
     * Get actual requests to IVRouter that were passed to batch framework.
     * Uses batch manager to obtain appropriate partitions according to provided indices
     * @return
     */
    @SuppressWarnings("unchecked")
    protected List<DistanceMatrixRequest> deserializeRequests() {
        // decide if we do cartesian or (raster) walking legs here
        int startPos = Integer.parseInt(start);
        int endPos = Integer.parseInt(end);
        long jobId = jobContext.getExecutionId();
        String mode = getVehicleClass(type);

        List<DistanceMatrixRequest> requests = new ArrayList<>();
        if (isCartesian) {
            // we need routes between all points
            List<EsriPointFeature> sourceSubList = (List<EsriPointFeature>) batchManager.getJobSourceSublist(jobId, startPos, endPos);
            EsriPoint requestPoint = buildRequestPoint(sourceSubList);
            requests.add(IVRequestFactory.getDMXRequest(mode, requestPoint, requestPoint));

        } else if (isIVRoute) {
            // can simply get target lists
            List<EsriPointFeature> sources = (List<EsriPointFeature>) batchManager.getJobSourceSublist(jobId, 0, 1);
            List<EsriPointFeature> targetSublist = batchManager.getCarTargetList(jobId, startPos, endPos);
            requests.add(IVRequestFactory.getDMXRequest(mode, buildRequestPoint(sources), buildRequestPoint(targetSublist)));

        } else {
            // general distance request involving raster points (need to extract targets first)
            List<IVRequestTuple> tuples = batchManager.getMatrixSublist(jobId, startPos, endPos);
            for (IVRequestTuple tuple : tuples) {
                if (tuple.getSource() != null && tuple.getTargets() != null && !tuple.getTargets().isEmpty()) {
                    requests.add(IVRequestFactory.getDMXRequest(mode,
                            buildRequestPoint(Collections.singletonList(tuple.getSource())),
                            buildRequestPoint(new ArrayList<>(tuple.getTargets()))));
                }
            }
        }
        return requests;
    }

    @Override
    protected void logBatch(StatusForLog status) {
        log.info("{} processing of {} {} ({}) at interval [{},{}]",
                status, jobContext.getJobName(), jobContext.getExecutionId(), type, start, end);
    }

    private static EsriPoint buildRequestPoint(List<EsriPointFeature> points) {
        return new EsriPoint(new SpatialReference(), points);
    }

    private static String getVehicleClass(LegType legType) {
        switch (legType) {
            case BikeLeg:       return IVRouterConfig.BIKE;
            case CarLeg:        return IVRouterConfig.PKW;
            case WalkingLeg:    return IVRouterConfig.PEDESTRIAN_NORMAL;
            default:            return null;
        }
    }

}
