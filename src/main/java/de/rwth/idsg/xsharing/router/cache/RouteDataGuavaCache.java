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

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import de.rwth.idsg.xsharing.router.AppConfiguration;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteMinimalRepresentation;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import java.util.concurrent.TimeUnit;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 31.03.2016
 */
@Default
@ApplicationScoped
@Slf4j
public class RouteDataGuavaCache implements RouteDataCache {

    @Resource private ManagedScheduledExecutorService scheduler;

    private Cache<String, RouteMinimalRepresentation> cache;

    @PostConstruct
    public void initialize() {
        AppConfiguration.Cache config = AppConfiguration.CONFIG.getCache();

        cache = CacheBuilder.newBuilder()
                            .maximumSize(config.getMaxSize())
                            .expireAfterWrite(config.getLifeTime(), TimeUnit.MINUTES)
                            .recordStats() // This is costly! But we need it because of getCacheStatus().
                            .build();

        // https://github.com/google/guava/wiki/CachesExplained#when-does-cleanup-happen
        //
        // If we do not clean-up expired objects ourselves, the insertion of objects seems to get slower when
        // the size approaches the limit. This is because small clean-ups happen which block the operation.
        //
        scheduler.scheduleAtFixedRate(
                this::cleanUpCache,
                config.getCleanUpInterval(),
                config.getCleanUpInterval(),
                TimeUnit.MINUTES
        );
    }

    private void cleanUpCache() {
        long oldSize = cache.size();
        cache.cleanUp();
        long newSize = cache.size();
        log.info("Cleaned up the cache. Size before/after: {}/{}", oldSize, newSize);
    }

    @Override
    public void putRoute(@Nullable RouteMinimalRepresentation route) {
        if (route != null) {
            scheduler.execute(() -> cache.put(route.getId(), route));
        }
    }

    @Nullable
    @Override
    public RouteMinimalRepresentation getRoute(String id) {
        try {
            return cache.getIfPresent(id);
        } catch (Exception e) {
            // No-op. Client side should null-check!
            return null;
        }
    }

    /**
     * Short description of the cache configuration for display in admin interface
     * @return String containing all relevant configuration parameters.
     */
    @Override
    public String getCacheStatus() {
        CacheStats stats = cache.stats();

        return Objects.toStringHelper("Guava-CacheStats")
                      .add("size", cache.size())
                      .add("hitCount", stats.hitCount())
                      .add("missCount", stats.missCount())
                      .add("loadSuccessCount", stats.loadSuccessCount())
                      .add("loadExceptionCount", stats.loadExceptionCount())
                      .add("totalLoadTime", stats.totalLoadTime())
                      .add("evictionCount", stats.evictionCount())
                      .toString();
    }

    /**
     * Obtain all references to stored route data, represented by hash values
     * @return a list of all route hash values contained in the cache
     */
    @Override
    public String getCacheKeys() {
        return cache.asMap().keySet().toString();
    }

    @Override
    public void clearCache() {
        log.info("Removing all items from the cache!");
        cache.invalidateAll();
    }
}
