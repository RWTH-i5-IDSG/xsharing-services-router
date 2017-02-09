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
package de.rwth.idsg.xsharing.router;

import de.rwth.idsg.xsharing.router.utils.PropertiesFileLoader;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static de.rwth.idsg.xsharing.router.Constants.PROPS_FILE;

/**
 * Represents application.properties
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 29.01.2016
 */
@Getter
public final class AppConfiguration {
    public static final AppConfiguration CONFIG = new AppConfiguration();

    private final double maxWalkingDistance;
    private final String[] sharingTypes;
    private final long stationInfoInterval;

    private final String uraBaseUrl;
    private final String ivRouterBaseUrl;
    private final int ivMaxRetryCount;
    private final int batchJobThreadCount;

    private final Raster raster;
    private final Cache cache;

    private AppConfiguration() {
        PropertiesFileLoader pl = new PropertiesFileLoader(PROPS_FILE);

        maxWalkingDistance = pl.getDouble("maxWalkingDistance");
        sharingTypes = pl.getStringArray("sharingTypes");
        stationInfoInterval = pl.getLong("stationInfoInterval");

        uraBaseUrl = pl.getString("uraBaseUrl");
        ivRouterBaseUrl = pl.getString("ivRouterBaseUrl");
        ivMaxRetryCount = pl.getInt("ivMaxRetryCount");
        batchJobThreadCount = pl.getInt("batchJobThreadCount");

        raster = Raster.builder()
                       .granularity(pl.getDouble("rasterGranularity"))
                       .boundingBoxMinLon(pl.getDouble("boundingBoxMinLon"))
                       .boundingBoxMinLat(pl.getDouble("boundingBoxMinLat"))
                       .boundingBoxMaxLon(pl.getDouble("boundingBoxMaxLon"))
                       .boundingBoxMaxLat(pl.getDouble("boundingBoxMaxLat"))
                       .build();

        cache = Cache.builder()
                     .lifeTime(pl.getInt("cacheLifeTime"))
                     .cleanUpInterval(pl.getInt("cacheCleanUpInterval"))
                     .maxSize(pl.getInt("cacheMaxSize"))
                     .build();
    }

    @Builder @Getter
    public static class Raster {

        // its value can be changed from the web ui. therefore not final, and setter
        @Setter
        private double granularity;

        private final double boundingBoxMinLon;
        private final double boundingBoxMinLat;
        private final double boundingBoxMaxLon;
        private final double boundingBoxMaxLat;
    }

    @Builder @Getter
    public static class Cache {
        private final int lifeTime;
        private final int cleanUpInterval;
        private final int maxSize;
    }
}
