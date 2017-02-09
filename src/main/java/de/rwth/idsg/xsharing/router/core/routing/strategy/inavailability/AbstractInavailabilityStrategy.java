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
package de.rwth.idsg.xsharing.router.core.routing.strategy.inavailability;

import de.rwth.idsg.xsharing.router.persistence.domain.station.VehicleStatus;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.SharingStation;
import org.joda.time.Interval;

import java.util.List;

import static de.rwth.idsg.xsharing.router.utils.DateTimeUtils.contains;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 02.06.2016
 */
public abstract class AbstractInavailabilityStrategy {

    protected static boolean overlaps(SharingStation station, Interval interval) {
        List<VehicleStatus> vehicleStatusList = station.getVehicleStatusList();
        for (VehicleStatus vs : vehicleStatusList) {
            List<Interval> inavailabilities = vs.getInavailabilities();
            if (!contains(inavailabilities, interval)) {
                // as long as there is a vehicle with non-overlapping inavailabilities,
                // the station can be used during the routing.
                return false;
            }
        }
        // ok, there are two possible reasons for this outcome
        // 1) there is no vehicle at the station
        // 2) we have gone through all the vehicles and there is no available one (all inavailabilities overlap)
        return true;
    }
}
