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
package de.rwth.idsg.xsharing.router.persistence.domain.station.representation;

import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.SharingStation;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public class StationRepresentationFactory {

    /**
     * Retrieve station (as in DB) and construct presentable version
     * @param station A descendant of the SharingStation class; any type of vehicle sharing station
     * @return The station representation compatible with URA3
     */
    public static StationRepresentation getRepresentation(SharingStation station) {
        StationRepresentation rep = new StationRepresentation();
        rep.setStationId(station.getPlaceId());
        rep.setType(station.getClass().getSimpleName());
        rep.setAvailCapacity(station.getAvailableCapacity());
        rep.setCapacity(station.getCapacity());
        rep.setProvider(station.getProviderId());
        rep.setName(station.getName());
        return rep;
    }
}
