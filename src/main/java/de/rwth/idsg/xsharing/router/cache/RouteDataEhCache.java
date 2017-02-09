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

import de.rwth.idsg.xsharing.router.AppConfiguration;
import de.rwth.idsg.xsharing.router.Constants;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteMinimalRepresentation;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of route data cache
 * Used to perform all mitigation between cache implementation and iv services
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
@Alternative
@ApplicationScoped
@Slf4j
public class RouteDataEhCache implements RouteDataCache {

    @Resource private ManagedScheduledExecutorService scheduler;

    private EhCacheWrapper<String, RouteMinimalRepresentation> cache;

    @PostConstruct
    public void initialize() {
        cache = new EhCacheWrapper<>(Constants.EH_CACHE_NAME);

        int cleanUpInterval = AppConfiguration.CONFIG.getCache().getCleanUpInterval();

        scheduler.scheduleAtFixedRate(
                cache::evictExpired,
                cleanUpInterval,
                cleanUpInterval,
                TimeUnit.MINUTES
        );
    }

    @Override
    @Asynchronous
    public void putRoute(@Nullable RouteMinimalRepresentation route) {
        if (route != null) {
            cache.put(route.getId(), route);
        }
    }

    @Nullable
    @Override
    public RouteMinimalRepresentation getRoute(String id) {
        return cache.get(id);
    }

    /**
     * Short description of the cache configuration for display in admin interface
     *
     * @return String containing all relevant configuration parameters.
     */
    @Override
    public String getCacheStatus() {
        return cache.getStatus();
    }

    /**
     * Obtain all references to stored route data, represented by hash values
     *
     * @return a list of all route hash values contained in the cache
     */
    @Override
    public String getCacheKeys() {
        return cache.getAllKeys().toString();
    }

    @Override
    public void clearCache() {
        log.info("Removing all items from the cache!");
        cache.clearCache();
    }
}
