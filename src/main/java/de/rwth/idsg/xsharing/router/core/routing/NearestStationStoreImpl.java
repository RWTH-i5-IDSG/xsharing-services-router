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
package de.rwth.idsg.xsharing.router.core.routing;

import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.BikeStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.CarStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.SharingStation;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public class NearestStationStoreImpl implements NearestStationStore {

    private StationStore<BikeStation> bikeStore = new StationStore<>();
    private StationStore<CarStation> carStore = new StationStore<>();

    @Override
    public void reset() {
        bikeStore.clear();
        carStore.clear();
    }

    @Override
    public void addBikeStations(GeoCoord at, List<BikeStation> stationList) {
        bikeStore.store(at, stationList);
    }

    @Override
    public void addCarStations(GeoCoord at, List<CarStation> stationList) {
        carStore.store(at, stationList);
    }

    @Override
    public List<BikeStation> allNearestBikeStations(GeoCoord at) {
        return bikeStore.getOrEmpty(at);
    }

    @Override
    public List<CarStation> allNearestCarStations(GeoCoord at) {
        return carStore.getOrEmpty(at);
    }

    /**
     * Simple Map wrapper for lookup. Input is List for convenience, we store
     * it as Set to prevent duplicate elements.
     */
    private static class StationStore<T extends SharingStation> {

        private Map<GeoCoord, Set<T>> map = new HashMap<>();

        void store(GeoCoord at, List<T> list) {
            Set<T> set = map.get(at);
            if (set == null) {
                // For this key, no value in map yet
                map.put(at, new HashSet<>(list));
            } else {
                set.addAll(list);
            }
        }

        List<T> getOrEmpty(GeoCoord at) {
            Set<T> set = map.get(at);
            if (set == null) {
                return Collections.emptyList();
            } else {
                return new ArrayList<>(set);
            }
        }

        void clear() {
            map.clear();
        }
    }
}
