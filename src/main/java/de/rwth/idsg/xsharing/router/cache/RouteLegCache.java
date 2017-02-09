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
package de.rwth.idsg.xsharing.router.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.rwth.idsg.xsharing.router.AppConfiguration;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.RouteLeg;
import de.rwth.idsg.xsharing.router.utils.GeoCoordTuple;
import lombok.Builder;
import lombok.Getter;

import javax.annotation.Nullable;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 02.02.2016
 */
public enum RouteLegCache {
    SINGLETON;

    private final Cache<GeoCoordTuple, RouteLeg> cache;

    RouteLegCache() {
        cache = CacheBuilder.newBuilder()
                            .maximumSize(AppConfiguration.CONFIG.getCache().getMaxSize())
                            // Disabled because it imposes a performance penalty. See Javadoc.
                            //.recordStats()
                            .build();
    }

    @Nullable
    public RouteLeg getIfPresent(GeoCoordTuple key) {
        try {
            return cache.getIfPresent(key);
        } catch (Exception e) {
            // No-op. Client side should null-check!
            return null;
        }
    }

    public void put(GeoCoordTuple key, RouteLeg value) {
        cache.put(key, value);
    }

    public void clearCache() {
        cache.invalidateAll();
    }

    public RouteLegCache.Report getReport() {
        // Normally, stats are not activated.
        // We must add recordStats() to CacheBuilder.newBuilder() to activate
        //
        // CacheStats stats = cache.stats();

        return RouteLegCache.Report.builder()
                                   .cacheSize(cache.size())
                                   .build();
    }

    @Getter
    @Builder
    public static final class Report {
        private final long cacheSize;
    }
}
