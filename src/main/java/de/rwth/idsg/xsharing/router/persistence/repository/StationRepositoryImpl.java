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
package de.rwth.idsg.xsharing.router.persistence.repository;

import com.google.common.collect.Lists;
import de.rwth.idsg.xsharing.router.Constants.DatabaseConstants;
import de.rwth.idsg.xsharing.router.persistence.CarAndBikeStations;
import de.rwth.idsg.xsharing.router.persistence.DatabaseException;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.BikeStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.CarStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.SharingStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.SharingStation_;
import de.rwth.idsg.xsharing.router.persistence.domain.station.transit.BusStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.transit.PTStation;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 11.06.2015
 */
@Slf4j
@Stateless
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class StationRepositoryImpl extends AbstractRepository implements StationRepository {

    @PersistenceContext(unitName = DatabaseConstants.DB_NAME_XSHARING)
    private EntityManager entityManager;

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public void saveSharingStationList(List<? extends SharingStation> stations) {

        // -------------------------------------------------------------------------
        // 1. Get all stations in order to decide whether to insert or update
        // -------------------------------------------------------------------------

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SharingStation> s = cb.createQuery(SharingStation.class);

        Root<SharingStation> station = s.from(SharingStation.class);
        CriteriaQuery<SharingStation> all = s.select(station);

        TypedQuery<SharingStation> allQuery = entityManager.createQuery(all);
        List<SharingStation> dbList = allQuery.getResultList();

        // -------------------------------------------------------------------------
        // 2. Decide to insert or update
        // -------------------------------------------------------------------------

        Map<Boolean, List<SharingStation>> partition =
                stations.stream()
                        .collect(Collectors.partitioningBy(newEntry -> {
                            for (SharingStation dbEntry : dbList) {
                                if (newEntry.isSame(dbEntry)) {
                                    // is already in db
                                    return true;
                                }
                            }
                            // not found in db
                            return false;
                        }));

        // TODO: what about stations that should be deleted?
        //
        List<SharingStation> toInsert = partition.get(Boolean.FALSE);
        List<SharingStation> toUpdate = partition.get(Boolean.TRUE);

        log.info("# of stations to insert: {}, to update: {}", toInsert.size(), toUpdate.size());

        // -------------------------------------------------------------------------
        // 3. Insert new stations
        // -------------------------------------------------------------------------

        try {
            saveList(toInsert);
        } catch (DatabaseException | EJBTransactionRolledbackException | PersistenceException e) {
            log.error("Error on insert! {}", e.getMessage());
        }

        // -------------------------------------------------------------------------
        // 4. Update existing stations
        // -------------------------------------------------------------------------

        final String query = "UPDATE SharingStation ss SET " +
                             "ss.globalId = :globalId, " +
                             "ss.geoPos = :geoPos, " +
                             "ss.capacity = :capacity, " +
                             "ss.availableCapacity = :availableCapacity, " +
                             "ss.availableVehicles = :availableVehicles, " +
                             "ss.name = :name " +
                             "WHERE ss.providerId = :providerId " +
                             "AND ss.placeId = :placeId";

        for (SharingStation sd : toUpdate) {
            entityManager.createQuery(query)
                         .setParameter("globalId", sd.getGlobalId())
                         .setParameter("geoPos", sd.getGeoPos())
                         .setParameter("capacity", sd.getCapacity())
                         .setParameter("availableCapacity", sd.getAvailableCapacity())
                         .setParameter("availableVehicles", sd.getAvailableVehicles())
                         .setParameter("name", sd.getName())
                         .setParameter("providerId", sd.getProviderId())
                         .setParameter("placeId", sd.getPlaceId())
                         .executeUpdate();
        }
    }

    @Override
    public void savePTStationList(List<? extends PTStation> stations) {
        for (PTStation station : stations) {
            try {
                save(station);
            } catch (DatabaseException e) {
                log.error(e.getMessage());
            }
        }
    }

    private void doUpdateAvailability(SharingStation station, Integer newCapacity, Integer newAvailability) {
        if (station != null) {
            station.setAvailableCapacity(newCapacity);
            station.setAvailableVehicles(newAvailability);
            entityManager.merge(station);
        }
    }

    @Override
    public void updateSharingStationAvailability(String placeId, String providerId, Integer newCapacity,
                                                 Integer newAvailability) {
        SharingStation station = findSharingStation(placeId, providerId);
        doUpdateAvailability(station, newCapacity, newAvailability);
    }

    @Override
    public List<BusStation> findAllBus() throws DatabaseException {
        return findAll(BusStation.class);
    }

    @Override
    public List<PTStation> findAllPT() throws DatabaseException {
        List<PTStation> results = new ArrayList<>();
        results.addAll(findAllBus());

        return results;
    }

    @Override
    public List<BikeStation> findAllBike() throws DatabaseException {
        return findAll(BikeStation.class);
    }

    @Override
    public List<CarStation> findAllCar() throws DatabaseException {
        return findAll(CarStation.class);
    }

    private SharingStation findSharingStation(String placeId, String providerId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SharingStation> s = cb.createQuery(SharingStation.class);
        Root<SharingStation> station = s.from(SharingStation.class);
        s.where(cb.and(
                cb.equal(station.get(SharingStation_.providerId), providerId),
                cb.equal(station.get(SharingStation_.placeId), placeId))
        );
        TypedQuery<SharingStation> query = entityManager.createQuery(s);

        try {
            return query.getSingleResult();
        } catch (PersistenceException e) {
            log.error("Could not find the station {} {} in database although it should exist! ({})", providerId,
                    placeId, e.getMessage());
            return null;
        }
    }

    @Override
    public CarAndBikeStations findAllStationsInRadius(Double targetX, Double targetY, Double radius) {
        List<CarStation> nearestCs = findCarStationsInRadius(targetX, targetY, radius);
        List<BikeStation> nearestBs = findBikeStationsInRadius(targetX, targetY, radius);
        return new CarAndBikeStations(nearestCs, nearestBs);
    }

    /**
     * Spatial selection of BikeStation entity using circle around target point
     * NOTE: the \\ notation is required for escaping the query
     *
     * @param targetX X coordinate of the target location (longitude)
     * @param targetY Y coordinate of the target location (latitude)
     * @param radius  Radius around target in meters
     * @return List of BikeStation entities in range
     */
    @Override
    public List<BikeStation> findBikeStationsInRadius(Double targetX, Double targetY, Double radius) {
        String sql = "WITH index_sel AS (" +
                "SELECT *, st_distance(st_geomfromtext('POINT(' || ? || ' ' || ? || ')', 4326)" +
                "\\:\\:GEOGRAPHY, s.geopos\\:\\:GEOGRAPHY) AS distance " +
                "FROM bikestation s " +
                "ORDER BY st_geomfromtext('POINT(' || ? || ' ' || ? || ')', 4326) <-> s.geopos) " +
                "SELECT " + getStationFieldsConcat() +
                "FROM index_sel " +
                "WHERE distance < ? " +
                "ORDER BY distance;";

        Query query = entityManager.createNativeQuery(sql, BikeStation.class);
        query.setParameter(1, targetX);
        query.setParameter(2, targetY);
        query.setParameter(3, targetX);
        query.setParameter(4, targetY);
        query.setParameter(5, radius);

        try {
             return query.getResultList();
        } catch (PersistenceException e) {
            // Unable to find closest Sharing Station in Database
            return Lists.newArrayList();
        }
    }

    /**
     * Spatial selection of CarStation entity using circle around target point
     * NOTE: the \\ notation is required for escaping the query
     *
     * @param targetX X coordinate of the target location (longitude)
     * @param targetY Y coordinate of the target location (latitude)
     * @param radius  Radius around target in meters
     * @return List of CarStation entities in range
     */
    @Override
    public List<CarStation> findCarStationsInRadius(Double targetX, Double targetY, Double radius) {
        String sql = "WITH index_sel AS (" +
                "SELECT s.*, st_distance(st_geomfromtext('POINT(' || ? || ' ' || ? || ')', 4326)" +
                "\\:\\:GEOGRAPHY, s.geopos\\:\\:GEOGRAPHY) AS distance " +
                "FROM carstation s " +
                "ORDER BY st_geomfromtext('POINT(' || ? || ' ' || ? || ')', 4326) <-> s.geopos) " +
                "SELECT " + getStationFieldsConcat() +
                "FROM index_sel " +
                "WHERE distance < ? ORDER BY distance;";

        Query query = entityManager.createNativeQuery(sql, CarStation.class);
        query.setParameter(1, targetX);
        query.setParameter(2, targetY);
        query.setParameter(3, targetX);
        query.setParameter(4, targetY);
        query.setParameter(5, radius);

        try {
            return query.getResultList();
        } catch (PersistenceException e) {
            // Unable to find closest Sharing Station in Database
            return Lists.newArrayList();
        }
    }

    private String getStationFieldsConcat() {
        return "id, place_id, available_capacity, available_vehicles, geopos, global_id, provider_id, capacity, name ";
    }

    /**
     * Selection of arbitrary sharing stations at specific location via lat/lon coordinates
     *
     * @param lon Longitude of the target location
     * @param lat Latitude of the target location
     * @return A descendant of SharingStation class if exists at target location
     * @throws DatabaseException if no station could be retrieved
     */
    @Override
    public SharingStation findByCoordinate(Double lon, Double lat, Class<? extends SharingStation> clazz) throws DatabaseException {
        String sql = "SELECT * FROM bikestation WHERE " +
                     "ST_PointFromText('POINT(' || ? || ' ' || ? || ')', 4326) = geopos " +
                     "UNION " +
                     "SELECT * FROM carstation " +
                     "WHERE ST_PointFromText('POINT(' || ? || ' ' || ? || ')', 4326) = geopos;";

        Query q = entityManager.createNativeQuery(sql, clazz);

        q.setParameter(1, lon);
        q.setParameter(2, lat);
        q.setParameter(3, lon);
        q.setParameter(4, lat);
        try {
            return (SharingStation) q.getSingleResult();
        } catch (PersistenceException e) {
            throw new DatabaseException("Unable to find Sharing Station in Database");
        }
    }
}
