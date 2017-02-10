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
package de.rwth.idsg.xsharing.router.core.routing;

import de.rwth.idsg.xsharing.router.AppConfiguration;
import de.rwth.idsg.xsharing.router.core.aggregation.raster.RasterManager;
import de.rwth.idsg.xsharing.router.core.routing.batch.BatchManager;
import de.rwth.idsg.xsharing.router.iv.model.EsriPointFeature;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.iv.request.IVRequestTuple;
import de.rwth.idsg.xsharing.router.iv.util.EsriFactory;
import de.rwth.idsg.xsharing.router.persistence.CarAndBikeStations;
import de.rwth.idsg.xsharing.router.persistence.DatabaseException;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.RasterPoint;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.ComputationLog;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.LegType;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.BikeStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.CarStation;
import de.rwth.idsg.xsharing.router.persistence.repository.ComputationLogRepository;
import de.rwth.idsg.xsharing.router.persistence.repository.StationRepository;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.rwth.idsg.xsharing.router.iv.util.EsriFactory.getCarRequestTuple;
import static de.rwth.idsg.xsharing.router.iv.util.EsriFactory.getIvRequestTuple;
import static de.rwth.idsg.xsharing.router.iv.util.EsriFactory.getPointFeature;
import static de.rwth.idsg.xsharing.router.utils.BasicUtils.checkNullOrEmpty;
import static de.rwth.idsg.xsharing.router.utils.BasicUtils.toGeoCoord;
import static java.util.stream.Collectors.toList;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Singleton
@Slf4j
public class RouteBuilderServiceImpl implements RouteBuilderService {

    @Inject private StationRepository stationRepository;
    @Inject private RasterManager rasterManager;
    @Inject private BatchManager batchManager;
    @Inject private ComputationLogRepository logRepository;

    private final NearestStationStore stationStore = new NearestStationStoreImpl();

    private double maxWalk;

    @PostConstruct
    private void init() {
        maxWalk = AppConfiguration.CONFIG.getMaxWalkingDistance();
    }

    /**
     * Init the raster and load data into cache.
     * Further check if there is precomputed data available and decide further proceeding
     *
     * @return true if data is present, false if new computation needs to be initiated
     */
    @Override
    public boolean prepareCore(List<RasterPoint> databasePoints, Double granularity) {
        // prepare raster (load from DB or init)
        rasterManager.manualInit(databasePoints, granularity);
        // check if there is route data present
        List<GeoCoord> ptStations = buildPTWalkingList();

        // clear any preexisting contents
        stationStore.reset();

        // prepare public transit stations
        buildPTStationsMap(ptStations);

        // check if preprocessing is necesary
        try {
            ComputationLog anyLog = logRepository.findAny();
            if (anyLog == null) {
                throw new DatabaseException("No log data found in DB. Need to run prepare step");
            }
            log.info("Found computation time stamp, assuming leg data is present. ({})", anyLog.toString());
            return true;
        } catch (DatabaseException e) {
            log.info("No leg data found, proceeding to compute new.");
            return false;
        }
    }

    @Override
    public void prepareAllWalkingLegs() {
        buildSharingWalkingLegSet();
        buildSharingDrivingLegSet();
        buildPTWalkingLegSet();
    }

    @Override
    public void prepareAllSharingLegs() {
        try {
            // get bike station list and dispatch request jobs
            List<EsriPointFeature> bikePoints = stationRepository.findAllBike()
                                                                 .stream()
                                                                 .map(EsriFactory::toEsriPointFeature)
                                                                 .collect(toList());

            batchManager.startCartesianMatrixJob(bikePoints, LegType.BikeLeg);

            // get car station list and dispatch request jobs
            List<EsriPointFeature> carPoints = stationRepository.findAllCar()
                                                                .stream()
                                                                .map(EsriFactory::toEsriPointFeature)
                                                                .collect(toList());

            batchManager.startCartesianMatrixJob(carPoints, LegType.CarLeg);

        } catch (DatabaseException e) {
            log.error("Exception when trying to fetch from DB: {}", e.getMessage());
        }
    }

    @Override
    public void buildNearestStationsMap() {
        log.info("Fetching closest stations for raster entries");
        List<RasterPoint> rasterPoints = rasterManager.getList();
        batchManager.startNeighborsJob(rasterPoints);
    }

    private void buildPTStationsMap(List<GeoCoord> points) {
        log.info("Fetching closest stations for public transport stations");

        for (GeoCoord point : points) {
            CarAndBikeStations cb = stationRepository.findAllStationsInRadius(point.getX(), point.getY(), maxWalk);
            stationStore.addBikeStations(point, cb.getBikeStations());
            stationStore.addCarStations(point, cb.getCarStations());
        }
    }

    private List<GeoCoord> buildPTWalkingList() {
        try {
            return stationRepository.findAllPT()
                                    .stream()
                                    .map(EsriFactory::toGeoCoord)
                                    .collect(toList());
        } catch (DatabaseException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<IVRequestTuple> buildTargetPoints(List<GeoCoord> ptStations) {
        if (checkNullOrEmpty(ptStations)) {
            return Collections.emptyList();
        }

        List<IVRequestTuple> targetPoints = new ArrayList<>();
        for (GeoCoord pt : ptStations) {
            EsriPointFeature sourcePoint = getPointFeature(pt);
            List<BikeStation> bikeStations = stationStore.allNearestBikeStations(pt);
            if (!bikeStations.isEmpty()) {
                IVRequestTuple bikeReq = getIvRequestTuple(sourcePoint, bikeStations);
                targetPoints.add(bikeReq);
            }
            List<CarStation> carStations = stationStore.allNearestCarStations(pt);
            if (!carStations.isEmpty()) {
                IVRequestTuple carReq = getIvRequestTuple(sourcePoint, carStations);
                targetPoints.add(carReq);
            }
        }

        return targetPoints;
    }

    private void buildPTWalkingLegSet() {
        List<GeoCoord> ptStations = buildPTWalkingList();
        List<IVRequestTuple> targetPoints = buildTargetPoints(ptStations);

        if (!targetPoints.isEmpty()) {
            batchManager.startMatrixJob(targetPoints, LegType.WalkingLeg);
        }
    }

    private void buildSharingWalkingLegSet() {
        // use sets to eliminate duplicates
        // adjusted to include all stations in radius (kNN) instead of only the closest one!
        log.info("Gathering raster point - station combinations for request.");
        List<IVRequestTuple> targetPoints = new ArrayList<>();
        for (RasterPoint point : rasterManager.getList()) {
            EsriPointFeature sourcePoint = getPointFeature(point.getCoord());

            List<BikeStation> bikeStations = point.getBikeStationsList();
            if (!bikeStations.isEmpty()) {
                IVRequestTuple bikeReq = getIvRequestTuple(sourcePoint, bikeStations);
                targetPoints.add(bikeReq);
            }

            List<CarStation> carStations = point.getCarStationsList();
            if (!carStations.isEmpty()) {
                IVRequestTuple carReq = getIvRequestTuple(sourcePoint, carStations);
                targetPoints.add(carReq);
            }
        }
        // prevent useless job executions
        if (!targetPoints.isEmpty()) {
            log.info("starting with " + targetPoints.stream()
                                                    .map(ivRequestTuple -> ivRequestTuple.getTargets().size())
                                                    .reduce(0, (a, b) -> a + b));
            batchManager.startMatrixJob(targetPoints, LegType.WalkingLeg);
        }
    }

    private void buildSharingDrivingLegSet() {
        log.info("Building CarStation - RasterPoint combinations.");
        try {
            List<CarStation> carStations = stationRepository.findAllCar();
            List<GeoCoord> rasterCoords = rasterManager.getCoordinatesList();
            // first find legs car station -> rasterpoint
            for (CarStation station : carStations) {
                EsriPointFeature sourcePoint = getPointFeature(toGeoCoord(station.getGeoPos()));
                IVRequestTuple req = getCarRequestTuple(sourcePoint, rasterCoords);
                batchManager.startIVMatrixJob(req, LegType.CarLeg);
            }
            // now find legs rasterpoint -> car station (for return trips)
            List<IVRequestTuple> targetPoints = new ArrayList<>();
            for (GeoCoord point : rasterCoords) {
                EsriPointFeature sourcePoint = getPointFeature(point);
                if (!carStations.isEmpty()) {
                    IVRequestTuple carReq = getIvRequestTuple(sourcePoint, carStations);
                    targetPoints.add(carReq);
                }
            }
            batchManager.startMatrixJob(targetPoints, LegType.CarLeg);
        } catch (DatabaseException e) {
            log.error("Could not gather car stations from database: {}", e.getMessage());
        }
    }

}
