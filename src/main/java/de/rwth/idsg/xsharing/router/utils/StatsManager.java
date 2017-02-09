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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 * @since 30.01.2016
 */
@Slf4j
public enum StatsManager {

    MINIMAL("MinimalRequestQueue"),
    COMPACT("CompactRequestQueue"),
    DETAILS("DetailsRequestQueue"),
    LOWER_BOUNDS("LowerBoundsRequestQueue");

    @Getter final Stats stats;

    StatsManager(String statsName) {
        this.stats = new Stats(statsName);
    }

    /**
     * To be provided to Web frontend upon request
     */
    public static List<Stats.Report> getAll() {
        return Arrays.stream(StatsManager.values())
                     .map(item -> item.stats.getReport())
                     .collect(Collectors.toList());
    }

    /**
     * To be logged at fixed intervals
     */
    public static void logAll() {
        Arrays.stream(StatsManager.values())
              .forEach(item -> log.info("{} stats: {}", item.stats.name, item.stats.getReportForLogging()));
    }

    public static void reset() {
        Arrays.stream(StatsManager.values())
              .forEach(item -> item.stats.reset());
    }
}
