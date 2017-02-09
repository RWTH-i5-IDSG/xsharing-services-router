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
package de.rwth.idsg.xsharing.router.utils;

import de.rwth.idsg.xsharing.router.core.routing.SharingStationType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
public class StationClassUtils {

    /**
     * Used for translating a list of sharing mode Strings (from properties or requests)
     * to the corresponding Sharing station classes
     * TODO: make Strings an Enum
     * @param modes Array of vehicles in String form
     * @return List of classes of corresponding SharingStations
     */
    public static Optional<List<SharingStationType>> getStationTypesList(String[] modes) {
        if (modes == null) {
            return Optional.empty();
        }

        List<SharingStationType> stationsList = new ArrayList<>(modes.length);
        for (String modeString : modes) {
            SharingStationType mode = getStationType(modeString);
            if (mode != null) {
                stationsList.add(mode);
            }
        }
        return Optional.of(stationsList);
    }

    /**
     * This will not scale when the list of switch cases increases. In that case, map is better.
     * But for our list with two elements, we can bypass the map lookup overhead.
     */
    @Nullable
    public static SharingStationType getStationType(String mode) {
        switch (mode) {
            case "Bike" :
            case "bike" :
            case "cycle":
            case "Cycle":
                return SharingStationType.Bike;

            case "Car" :
            case "car" :
            case "self-drive-car":
                return SharingStationType.Car;

            default :
                return null;
        }
    }
}
