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

import com.google.common.collect.Lists;
import de.rwth.idsg.xsharing.router.AppConfiguration;
import de.rwth.idsg.xsharing.router.core.CoreBootstrapper;
import de.rwth.idsg.xsharing.router.core.routing.ServerStatus;
import de.rwth.idsg.xsharing.router.core.routing.request.SingleMinimalRequest;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.persistence.DatabaseException;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.RasterPoint;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.station.StationTuple;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.WalkingLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.BikeStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.CarStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.SharingStation;
import de.rwth.idsg.xsharing.router.persistence.repository.RasterRepository;
import de.rwth.idsg.xsharing.router.persistence.repository.RouteLegRepository;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static de.rwth.idsg.xsharing.router.utils.BasicUtils.hasNoElements;
import static de.rwth.idsg.xsharing.router.utils.BasicUtils.isEqualXY;

/**
 * Concrete realization of the raster grid including creation and retrieval functionality
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
@Slf4j
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class RasterManagerImpl implements RasterManager {

    @Inject private RasterRepository rasterRepository;
    @Inject private RouteLegRepository legRepository;
    @Inject private CoreBootstrapper coreBootstrapper;

    // the actual map holding raster points, each associated with a hash of their coords for easy lookup
    // maybe replace this with more memory efficient data source (problem: concurrency)
    private Map<Integer, RasterPoint> points = Collections.emptyMap();

    private AppConfiguration.Raster rasterConfig;

    private final ReentrantReadWriteLock.WriteLock dbLock =  new ReentrantReadWriteLock().writeLock();
    private final ReentrantReadWriteLock.WriteLock pointsLock = new ReentrantReadWriteLock().writeLock();

    @PostConstruct
    public void init() {
        rasterConfig = AppConfiguration.CONFIG.getRaster();
    }

    public double getGranularity() {
        return rasterConfig.getGranularity();
    }

    public void setGranularity(double granularity) {
        rasterConfig.setGranularity(granularity);
    }
    
    /**
     * Calculate initial amount of raster points to prepare hashmap accordingly at creation time
     * @return calculated points count
     */
    private int getInitialPointsCount() {
        return (int) Math.round(
                ((rasterConfig.getBoundingBoxMaxLat() - rasterConfig.getBoundingBoxMinLat()) / getGranularity()) *
                ((rasterConfig.getBoundingBoxMaxLon() - rasterConfig.getBoundingBoxMinLon()) / getGranularity())
        );
    }

    /**
     * Initialize the raster grid with the desired granularity.
     * If none specified (i.e. equals 0) default from configuration is used.
     */
    @Override
    public void manualInit(List<RasterPoint> databasePoints, double manualGranularity) {
        // manually cause raster to be created to prevent later delays
        if (manualGranularity != 0) {
            setGranularity(manualGranularity);
        }

        points = new ConcurrentHashMap<>(getInitialPointsCount());
        loadRaster(databasePoints);
    }

    @Override
    public int getSize() {
        return points.size();
    }

    @Override
    public List<RasterPoint> getList() {
        return new ArrayList<>(points.values());
    }

    /**
     * Obtain partial set of the raster
     * @param from lower index
     * @param to upper index
     * @return list of the contained raster points at the provided indices, or null if invalid
     */
    @Override
    public List<RasterPoint> getSubList(int from, int to) {
        ArrayList<RasterPoint> values = new ArrayList<>(points.values());
        try {
            int size = values.size();
            if (to >= size && from < size) {
                return values.subList(from, size - 1);
            }
            return values.subList(from, to);
        } catch (IndexOutOfBoundsException ie) {
            log.error("The provided indices were invalid! from={}, to={}", from, to);
            return null;
        }
    }

    /**
     * Retrieve all coordinates of raster points in the map
     * @return list of geocoordinates in longitude, latitude format
     */
    @Override
    public List<GeoCoord> getCoordinatesList() {
        return points.values().stream().map(RasterPoint::getCoord).collect(Collectors.toList());
    }

    /**
     * get raster point by coordinate hashcode
     * @param hashCode hashcode of a GeoCoord object
     * @return found rasterpoint, or null
     */
    public RasterPoint getPoint(int hashCode) {
        return points.get(hashCode);
    }

    /**
     * get raster point by coordinate object
     * @param at GeoCoord object
     * @return found rasterpoint, or null
     */
    @Override
    public RasterPoint getPoint(GeoCoord at) {
        return this.getPoint(at.hashCode());
    }

    /**
     * Compute proposed RasterPoint coordinate for arbitrary coordinates.
     * NOTE: This does not guarantee a point to be present in the map!
     * @param lon longitude of the target coord
     * @param lat latitude of the target coord
     * @return coordinate object referencing prospective raster point
     */
    @Override
    public GeoCoord getEntryPoint(double lon, double lat) {
        double alpha = getGranularity();
        double latMin = rasterConfig.getBoundingBoxMinLat();
        double lonMin = rasterConfig.getBoundingBoxMinLon();

        double resultLat = alpha * Math.floor((lat - latMin) / alpha + 0.5) + latMin;
        double resultLon = alpha * Math.floor((lon - lonMin) / alpha + 0.5) + lonMin;

        return new GeoCoord(resultLon, resultLat);
    }

    @Override
    public RasterPoint getPointNear(double lon, double lat) {
        return getPoint(getEntryPoint(lon, lat));
    }

    /**
     * [0] : from
     * [1] : to
     */
    @Override
    public RasterPoint[] getPointNearAsArray(SingleMinimalRequest request) {
        GeoCoord from = request.getStartPoint();
        GeoCoord to = request.getEndPoint();

        RasterPoint fromPoint = getPointNear(from.getX(), from.getY());
        RasterPoint toPoint = getPointNear(to.getX(), to.getY());

        return new RasterPoint[]{fromPoint, toPoint};
    }

    /**
     * use predefined raster config
     */
    @Override
    public void createRaster() {
        createRaster(
                rasterConfig.getBoundingBoxMinLon(),
                rasterConfig.getBoundingBoxMinLat(),
                rasterConfig.getBoundingBoxMaxLon(),
                rasterConfig.getBoundingBoxMaxLat(),
                getGranularity()
        );
    }

    /**
     * Create raster according to specified parameters and save it to the database
     * @param minLon lower boundary on longitude
     * @param minLat lower boundary on latitude
     * @param maxLon upper boundary on longitude
     * @param maxLat upper boundary on latitude
     * @param alpha granularity coefficient
     */
    @Override
    public void createRaster(double minLon, double minLat, double maxLon, double maxLat, double alpha) {
        int latCount = ((int) Math.floor((maxLat - minLat) / alpha));
        int lonCount = ((int) Math.floor((maxLon - minLon) / alpha));

        pointsLock.lock();
        try {
            for (int i = 0; i <= latCount; i++) {
                for (int j = 0; j <= lonCount; j++) {
                    double lon = minLon + j * alpha;
                    double lat = minLat + i * alpha;
                    GeoCoord point = new GeoCoord(lon, lat);
                    points.put(point.hashCode(), new RasterPoint(point));
                }
            }
        } finally {
            pointsLock.unlock();
        }

        log.info("Created raster list with {} entries!", points.size());

        int batchSize = 100;

        List<RasterPoint> allPoints = new ArrayList<>(points.values());
        List<List<RasterPoint>> batchList = Lists.partition(allPoints, batchSize);

        log.info("Will insert raster list into database in {} batches each with {} size", batchList.size(), batchSize);

        for (List<RasterPoint> batch : batchList) {
            insertOneBatch(batch);
        }

        log.info("Saved raster list to database");
    }

    @Override
    public List<RasterPoint> getAllRasterPoints() {
        log.info("Attempting to load raster from database.");
        return rasterRepository.getAllRasterPoints();
    }

    /**
     * Optimistically update the sharing station availabilities contained in raster
     * @param stations list of sharing stations with updated availabilities
     */
    @Override
    public void updateStations(List<SharingStation> stations) {
        // Station objects are called by reference so we update the first occurrence
        for (SharingStation station : stations) {

            boolean update = false;
            Iterator<RasterPoint> it = points.values().iterator();

            while (!update && it.hasNext()) {

                RasterPoint p = it.next();
                Optional<? extends StationTuple> found = null;

                // Find the same known station as the one arrived after update
                //
                if (station instanceof BikeStation) {
                    found = findStation(p.getNearestBikeStations(), station);

                } else if (station instanceof CarStation) {
                    found = findStation(p.getNearestCarStations(), station);
                }

                // When found, update the availabilities
                //
                if (found != null && found.isPresent()) {
                    SharingStation presentStation = found.get().getStation();
                    presentStation.setVehicleStatusList(station.getVehicleStatusList());
                    presentStation.setAvailableCapacity(station.getAvailableCapacity());
                    presentStation.setAvailableVehicles(station.getAvailableVehicles());
                    update = true;
                }
            }
        }
    }

    private <T extends StationTuple> Optional<T> findStation(List<T> knownStations, SharingStation fromAdapterDB) {
        return knownStations.stream()
                            .filter(stat ->
                                    stat.getStation().getPlaceId().equals(fromAdapterDB.getPlaceId()) &&
                                    stat.getStation().getProviderId().equals(fromAdapterDB.getProviderId()))
                            .findAny();
    }

    /**
     * Set a computed walking leg for a raster point by retrieving the location and the appropriate station
     * @param leg the computed walking leg
     */
    @Override
    public void updateLegForRasterPoint(WalkingLeg leg) {
        // perform persistence before inserting leg into raster to ensure ids being set correctly
        try {
            leg = legRepository.saveWalkingLeg(leg);
        } catch (DatabaseException e) {
            log.error(e.getMessage());
        }

        RasterPoint rasterPoint = points.get(leg.getFrom().hashCode());
        // early exit if no raster point exists for given leg
        if (rasterPoint == null) {
            return;
        }

        updateWalkingLegForStation(rasterPoint.getNearestBikeStations(), leg);
        updateWalkingLegForStation(rasterPoint.getNearestCarStations(), leg);

        rasterPoint = saveOrUpdateInternal(rasterPoint);

        pointsLock.lock();
        try {
            points.put(rasterPoint.getCoord().hashCode(), rasterPoint);
        } finally {
            pointsLock.unlock();
        }
    }

    private <T extends StationTuple> void updateWalkingLegForStation(List<T> stations, WalkingLeg leg) {
        for (T tuple : stations) {
            if (isEqualXY(tuple.getStation().getGeoPos(), leg.getTo())) {
                tuple.setLeg(leg);
                // Assumption: A walking leg's destination can be at most 1 station within this raster.
                // Therefore, early exit
                return;
            }
        }
    }

    /**
     * Save updates to the stations associated with the raster point
     * @param point the changed raster point
     */
    @Override
    public void updateRasterPointStations(RasterPoint point) {
        saveOrUpdateInternal(point);
    }

    /**
     * retrieve all bike stations relevant to the raster point
     * @param at raster point coordinate
     * @return list of all relevant stations
     */
    @Override
    public List<BikeStation> getBikeStationsForRasterPoint(GeoCoord at) {
        // first check hashmap for stored point
        RasterPoint point = getPoint(at);
        if (point == null) {
            return Collections.emptyList();
        }
        return point.getBikeStationsList();
    }

    /**
     * retrieve all car stations relevant to the raster point
     * @param at raster point coordinate
     * @return list of all relevant stations
     */
    @Override
    public List<CarStation> getCarStationsForRasterPoint(GeoCoord at) {
        RasterPoint point = getPoint(at);
        if (point == null) {
            return Collections.emptyList();
        }
        return point.getCarStationsList();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Retrieve the raster points from DB
     */
    private void loadRaster(List<RasterPoint> databasePoints) {

        // if db is empty we need to create the raster
        if (hasNoElements(databasePoints)) {
            log.info("No raster data in DB, creating new.");
            coreBootstrapper.setServerStatus(ServerStatus.COMPUTING);
            createRaster();
            return;
        }

        pointsLock.lock();
        try {
            // refill the map with existing points
            log.info("Found {} raster entries in DB, updating local cache.", databasePoints.size());
            for (RasterPoint p : databasePoints) {
                points.put(p.getCoord().hashCode(), p);
            }
        } finally {
            pointsLock.unlock();
        }
    }

    /**
     * TODO: why do we need a lock here at all?
     */
    private RasterPoint saveOrUpdateInternal(RasterPoint rp) {
        dbLock.lock();
        try {
            return rasterRepository.saveOrUpdate(rp);
        } finally {
            dbLock.unlock();
        }
    }

    private void insertOneBatch(List<RasterPoint> p) {
        dbLock.lock();
        try {
            rasterRepository.savePointsList(p);
        } catch (DatabaseException e) {
            log.error("FATAL: Could not save raster to database: {}", e.getMessage());
        } finally {
            dbLock.unlock();
        }
    }
}
