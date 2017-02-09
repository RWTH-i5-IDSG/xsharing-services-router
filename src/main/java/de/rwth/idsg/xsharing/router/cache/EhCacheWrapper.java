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
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Results;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Actual wrapper for Ehcache manager container and management of arbitrary key/value types
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
@Slf4j
public class EhCacheWrapper<KEY, VALUE> {

    // Create a singleton CacheManager using defaults
    private static final CacheManager manager = CacheManager.create();

    private final Cache cache;

    /**
     * http://www.ehcache.org/documentation/2.8/code-samples.html#creating-caches-programmatically
     */
    EhCacheWrapper(String cacheName) {
        AppConfiguration.Cache config = AppConfiguration.CONFIG.getCache();

        // Create a Cache specifying its configuration
        cache = new Cache(
                new CacheConfiguration(cacheName, config.getMaxSize())
                        .timeToLiveSeconds(TimeUnit.MINUTES.toSeconds(config.getLifeTime()))
                        .eternal(false)
                        .persistence(new PersistenceConfiguration().strategy(Strategy.NONE))
                        .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.FIFO)
        );

        // The cache is not usable until it has been added
        manager.addCache(cache);
    }

    public void put(KEY key, VALUE value) {
        cache.put(new Element(key, value));
    }

    @SuppressWarnings("unchecked")
    public VALUE get(KEY key) {
        Element e = cache.get(key);

        if (e == null) {
            return null;
        }
        return (VALUE) e.getObjectValue();
    }

    /**
     * Use the Ehcache provided search API to perform criteria queries on defined search attributes.
     * NOTE: Requires search attributes to be previously defined in Ehcache config!
     * DEPRECATED because at observed large cache quantities the search noticeably slows down the system
     * @param params the search parameters as key/value pairs
     * @return the first found cache hit
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public VALUE lookup(Map<String, String> params) {
        Query q = cache.createQuery();

        for (Map.Entry<String, String> param : params.entrySet()) {
            Attribute<String> theAttribute = ((Ehcache) cache).getSearchAttribute(param.getKey());
            q.addCriteria(theAttribute.eq(param.getValue()));
        }

        q.includeKeys().includeValues();
        Results results = q.execute();

        if (results == null || results.size() == 0) {
            return null;
        }

        if (results.size() > 1) {
            log.warn("There are multiple entries registered for params: {}", params.toString());
        }

        return (VALUE) results.all().get(0).getValue();
    }

    @SuppressWarnings("unchecked")
    public List<KEY> getAllKeys() {
        return cache.getKeys();
    }

    public String getStatus() {
        return "Cache size is: " + cache.getSize() + ", " + cache.toString();
    }

    /**
     * Used to free up expired cache elements. Use in conjunction with periodic runners or manual trigger.
     */
    public void evictExpired() {
        cache.evictExpiredElements();
    }

    /**
     * Completely clean the cache, removing ALL contained elements!
     */
    public void clearCache() {
        cache.removeAll();
    }
}
