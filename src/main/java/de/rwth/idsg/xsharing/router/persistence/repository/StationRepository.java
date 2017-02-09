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

import de.rwth.idsg.xsharing.router.persistence.CarAndBikeStations;
import de.rwth.idsg.xsharing.router.persistence.DatabaseException;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.BikeStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.CarStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.SharingStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.transit.BusStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.transit.PTStation;

import java.util.List;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public interface StationRepository {
    //
    // persistence methods
    //
    void saveSharingStationList(List<? extends SharingStation> stations);
    void savePTStationList(List<? extends PTStation> stations);

    void updateSharingStationAvailability(String placeId, String providerId, Integer newCapacity,
                                          Integer newAvailability);

    //
    // retrieval
    //
    List<BusStation> findAllBus() throws DatabaseException;
    List<PTStation> findAllPT() throws DatabaseException;

    List<BikeStation> findAllBike() throws DatabaseException;
    List<CarStation> findAllCar() throws DatabaseException;

    SharingStation findByCoordinate(Double lon, Double lat, Class<? extends SharingStation> clazz) throws DatabaseException;

    //
    // spatial selections
    //
    CarAndBikeStations findAllStationsInRadius(Double targetX, Double targetY, Double radius);
    List<BikeStation> findBikeStationsInRadius(Double targetX, Double targetY, Double radius);
    List<CarStation> findCarStationsInRadius(Double targetXx, Double targetY, Double radius);
}
