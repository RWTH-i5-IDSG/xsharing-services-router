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
package de.rwth.idsg.xsharing.router.core.routing.strategy;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Route legs alone are not enough. We need to compute total duration based on the duration check strategy.
 * So it must be done within the ModeStrategy and not later when shipping the response.
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 30.05.2016
 */
@Getter
public final class RouteContext {
    private static final RouteContext SINGLETON = new RouteContext();

    private final List<RouteLegWrapper> legs;
    private final int totalDurationInSeconds;
    private final double totalDistance;

    /**
     * Constructor for no result
     */
    private RouteContext() {
        this.legs = Collections.emptyList();
        this.totalDurationInSeconds = 0;
        this.totalDistance = 0.0;
    }

    /**
     * Constructor with actual results
     */
    public RouteContext(List<RouteLegWrapper> legs,
                        int totalDurationInSeconds,
                        double totalDistance) {
        this.legs = legs;
        this.totalDurationInSeconds = totalDurationInSeconds;
        this.totalDistance = totalDistance;
    }

    public static RouteContext getEmptySingleton() {
        return SINGLETON;
    }
}
