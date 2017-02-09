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
package de.rwth.idsg.xsharing.router.persistence.domain.util;

import com.vividsolutions.jts.geom.Point;
import de.rwth.idsg.xsharing.router.persistence.domain.mb.MBPlaceEntity;
import de.rwth.idsg.xsharing.router.persistence.domain.station.VehicleStatus;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.BikeStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.CarStation;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Converter functions to enable correct data migration between MB and xsharing
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public class ConverterUtil {

    private ConverterUtil() { }

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS")
                                                                     .withZoneUTC();

    public static BikeStation toBikeStation(MBPlaceEntity mbPlaceEntity) {
        return toBikeStation(mbPlaceEntity, Collections.emptyList());
    }

    public static CarStation toCarStation(MBPlaceEntity mbPlaceEntity) {
        return toCarStation(mbPlaceEntity, Collections.emptyList());
    }

    public static BikeStation toBikeStation(MBPlaceEntity mbPlaceEntity, List<VehicleStatus> vehicleStatusList) {
        if (mbPlaceEntity.getGpsPosition() == null) {
            return null;
        }

        Point geoPos = JTSUtil.getPoint(mbPlaceEntity);

        return new BikeStation(
                mbPlaceEntity.getPlaceId(),
                mbPlaceEntity.getGlobalId(),
                geoPos,
                mbPlaceEntity.getProviderId(),
                mbPlaceEntity.getCapacity(),
                mbPlaceEntity.getAvailableCapacity(),
                mbPlaceEntity.getAvailableVehicles(),
                mbPlaceEntity.getName(),
                vehicleStatusList
        );
    }

    public static CarStation toCarStation(MBPlaceEntity mbPlaceEntity, List<VehicleStatus> vehicleStatusList) {
        if (mbPlaceEntity.getGpsPosition() == null) {
            return null;
        }

        Point geoPos = JTSUtil.getPoint(mbPlaceEntity);

        return new CarStation(
                mbPlaceEntity.getPlaceId(),
                mbPlaceEntity.getGlobalId(),
                geoPos,
                mbPlaceEntity.getProviderId(),
                mbPlaceEntity.getCapacity(),
                mbPlaceEntity.getAvailableCapacity(),
                mbPlaceEntity.getAvailableVehicles(),
                mbPlaceEntity.getName(),
                vehicleStatusList
        );
    }

    @Nullable
    public static Interval getInterval(@Nullable String from,
                                       @Nullable String to) {
        if (isNullOrEmpty(from) || isNullOrEmpty(to)) {
            return null;
        }
        DateTime fromLdt = FORMATTER.parseDateTime(from);
        DateTime toLdt = FORMATTER.parseDateTime(to);
        return new Interval(fromLdt, toLdt);
    }

}
