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
package de.rwth.idsg.xsharing.router.persistence.domain.station.sharing;

import com.vividsolutions.jts.geom.Point;
import de.rwth.idsg.xsharing.router.persistence.domain.station.VehicleStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import java.util.List;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Entity
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Getter
@Setter
public class CarStation extends SharingStation {

    public CarStation(String placeId, String globalId, Point geoPos, String providerId,
                      Integer capacity, Integer availableCapacity, Integer availableVehicles,
                      String name, List<VehicleStatus> vehicleStatusList) {

        super(null, placeId, globalId, geoPos, providerId,
              capacity, availableCapacity, availableVehicles,
              name, vehicleStatusList);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
