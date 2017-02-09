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

import de.rwth.idsg.xsharing.router.Constants.DatabaseConstants;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Singleton
@Slf4j
public class MasterRepositoryImpl implements MasterRepository {

    @PersistenceContext(unitName = DatabaseConstants.DB_NAME_XSHARING)
    private EntityManager entityManager;

    /**
     * WARNING: This kills the database
     */
    @Override
    public void truncateDatabase() {
        log.info("TRUNCATING DATABASE NOW!");
        String rasterNearestBike = "DELETE FROM rasterpoint_nearestbikestations";
        String rasterNearestCar = "DELETE FROM rasterpoint_nearestcarstations";

        String raster = "DELETE FROM RasterPoint ";
        String bike = "DELETE FROM BikeLeg ";
        String bikeStation = "DELETE FROM BikeStation ";
        String busStation = "DELETE FROM BusStation ";
        String car = "DELETE FROM CarLeg ";
        String carStation = "DELETE FROM CarStation ";
        String log = "DELETE FROM ComputationLog ";
        String walk = "DELETE FROM WalkingLeg ";

        // dereference foreign key stuff
        entityManager.createNativeQuery(rasterNearestBike).executeUpdate();
        entityManager.createNativeQuery(rasterNearestCar).executeUpdate();
        entityManager.flush();
        entityManager.createQuery(raster).executeUpdate();
        entityManager.createQuery(bike).executeUpdate();
        entityManager.createQuery(bikeStation).executeUpdate();
        entityManager.createQuery(busStation).executeUpdate();
        entityManager.createQuery(car).executeUpdate();
        entityManager.createQuery(carStation).executeUpdate();
        entityManager.createQuery(log).executeUpdate();
        entityManager.createQuery(walk).executeUpdate();
        entityManager.flush();
    }
}
