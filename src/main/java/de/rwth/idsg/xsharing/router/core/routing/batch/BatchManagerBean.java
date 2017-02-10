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
package de.rwth.idsg.xsharing.router.core.routing.batch;

import com.google.common.collect.Lists;
import de.rwth.idsg.xsharing.router.AppConfiguration;
import de.rwth.idsg.xsharing.router.Constants.BatchConstants;
import de.rwth.idsg.xsharing.router.iv.model.EsriPointFeature;
import de.rwth.idsg.xsharing.router.iv.request.IVRequestTuple;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.RasterPoint;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.LegType;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static de.rwth.idsg.xsharing.router.utils.BasicUtils.checkNullOrEmpty;

/**
 * Batch job management bean responsible for dispatching job instances and storing all input data
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Slf4j
public class BatchManagerBean implements BatchManager {

    private List<HashMap<Long, ?>> stashList;
    private HashMap<Long, List<EsriPointFeature>> sourcePointStash = new HashMap<>();
    private HashMap<Long, List<EsriPointFeature>> carTargetStash = new HashMap<>();
    private HashMap<Long, List<IVRequestTuple>> matrixStash = new HashMap<>();
    private HashMap<Long, List<RasterPoint>> rasterStash = new HashMap<>();

    private double maxWalk;

    @PostConstruct
    public void init() {
        stashList = Lists.newArrayList(sourcePointStash, carTargetStash, matrixStash, rasterStash);
        maxWalk = AppConfiguration.CONFIG.getMaxWalkingDistance();
    }

    @PreDestroy
    public void destroy() {
        JobOperator jo = BatchRuntime.getJobOperator();
        stashList.stream()
                 .map(HashMap::keySet)
                 .flatMap(Collection::stream) // join multiple sets together
                 .forEach(jo::stop); // signal every job that we are going down and they should stop
    }

    @Override
    public void startCartesianMatrixJob(List<EsriPointFeature> sourcePoints, LegType legType) {
        if (checkNullOrEmpty(sourcePoints)) {
            log.error("Will not start Cartesian Matrix job with invalid sourcePoints for leg type {}", legType);
            return;
        }
        JobOperator jo = BatchRuntime.getJobOperator();

        Properties properties = new Properties();
        properties.setProperty(BatchConstants.PATH_TYPE, legType.toString());
        // assume here that the size of both sets is roughly equivalent!
        properties.setProperty(BatchConstants.BATCH_SIZE, String.valueOf(sourcePoints.size()));
        properties.setProperty(BatchConstants.PARTITIONING, String.valueOf(false));
        properties.setProperty(BatchConstants.CARTESIAN, String.valueOf(true));

        Long jobId = jo.start(BatchConstants.MATRIX_JOB, properties);
        log.info("Dispatched Cartesian Matrix job with id {}", jobId);
        sourcePointStash.put(jobId, sourcePoints);
    }

    @Override
    public void startIVMatrixJob(IVRequestTuple request, LegType legType) {
        if (checkNullOrEmpty(request.getTargets())) {
            log.error("Will not start IV Matrix job with invalid targets for leg type {}", legType);
            return;
        }
        JobOperator jo = BatchRuntime.getJobOperator();

        Properties properties = new Properties();
        properties.setProperty(BatchConstants.PATH_TYPE, legType.toString());
        // assume here that the size of both sets is roughly equivalent!
        properties.setProperty(BatchConstants.BATCH_SIZE, String.valueOf(request.getTargets().size()));
        properties.setProperty(BatchConstants.PARTITIONING, String.valueOf(true));
        properties.setProperty(BatchConstants.IVROUTE, String.valueOf(true));

        Long jobId = jo.start(BatchConstants.MATRIX_JOB, properties);
        List<EsriPointFeature> source = new ArrayList<>();
        source.add(request.getSource());
        log.info("Dispatched IV Matrix job with id {}", jobId);
        sourcePointStash.put(jobId, source);
        carTargetStash.put(jobId, new ArrayList<>(request.getTargets()));
    }

    @Override
    public void startMatrixJob(List<IVRequestTuple> points, LegType legType) {
        if (checkNullOrEmpty(points)) {
            log.error("Will not start Matrix job with invalid points for leg type {}", legType);
            return;
        }
        JobOperator jo = BatchRuntime.getJobOperator();

        Properties properties = new Properties();
        properties.setProperty(BatchConstants.PATH_TYPE, legType.toString());
        properties.setProperty(BatchConstants.BATCH_SIZE, String.valueOf(points.size()));
        properties.setProperty(BatchConstants.PARTITIONING, String.valueOf(true));

        Long jobId = jo.start(BatchConstants.MATRIX_JOB, properties);
        log.info("Dispatched Matrix job with id {}", jobId);
        matrixStash.put(jobId, points);
    }

    @Override
    public void startNeighborsJob(List<RasterPoint> rasterPoints) {
        if (checkNullOrEmpty(rasterPoints)) {
            log.error("Will not start Neighbors job with invalid rasterPoints.");
            return;
        }
        JobOperator jo = BatchRuntime.getJobOperator();

        Properties properties = new Properties();
        properties.setProperty(BatchConstants.BATCH_SIZE, String.valueOf(rasterPoints.size()));
        properties.setProperty(BatchConstants.MAX_WALK, String.valueOf(maxWalk));

        Long jobId = jo.start(BatchConstants.NEIGHBORS_JOB, properties);
        log.info("Dispatched Neighbors job with id {}", jobId);
        rasterStash.put(jobId, rasterPoints);
    }

    @Override
    public List<?> getJobSourceSublist(Long jobId, Integer from, Integer to) {
        List<EsriPointFeature> all = sourcePointStash.get(jobId);
        if (checkNullOrEmpty(all)) {
            return Collections.emptyList();
        }
        return all.subList(Math.min(all.size(), from), Math.min(all.size(), to));
    }

    @Override
    public List<EsriPointFeature> getCarTargetList(Long jobId, Integer from, Integer to) {
        List<EsriPointFeature> t = carTargetStash.get(jobId);
        if (checkNullOrEmpty(t)) {
            return Collections.emptyList();
        }
        return t.subList(Math.min(t.size(), from), Math.min(t.size(), to));
    }

    @Override
    public List<IVRequestTuple> getMatrixSublist(Long jobId, Integer from, Integer to) {
        List<IVRequestTuple> all = matrixStash.get(jobId);
        if (checkNullOrEmpty(all)) {
            return Collections.emptyList();
        }
        return all.subList(Math.min(all.size(), from), Math.min(all.size(), to));
    }

    @Override
    public List<RasterPoint> getRasterSublist(Long jobId, Integer from, Integer to) {
        List<RasterPoint> all = rasterStash.get(jobId);
        if (checkNullOrEmpty(all)) {
            return Collections.emptyList();
        }
        return all.subList(Math.min(all.size(), from), Math.min(all.size(), to));
    }

    /**
     * Removes finished jobs from the stash
     * @param id the unique job execution id
     */
    @Override
    public void doneJob(long id) {
        for (HashMap<Long, ?> map : stashList) {
            map.remove(id);
        }
    }
}
