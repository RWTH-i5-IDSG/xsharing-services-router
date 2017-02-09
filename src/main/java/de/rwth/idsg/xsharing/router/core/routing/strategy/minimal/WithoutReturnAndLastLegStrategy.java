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
package de.rwth.idsg.xsharing.router.core.routing.strategy.minimal;

import de.rwth.idsg.xsharing.router.core.routing.strategy.StrategyDependencyContext;
import de.rwth.idsg.xsharing.router.core.routing.strategy.duration.DurationCheck;
import de.rwth.idsg.xsharing.router.core.routing.strategy.inavailability.bike.DefaultBikeInavailabilityStrategy;
import de.rwth.idsg.xsharing.router.core.routing.strategy.inavailability.car.DefaultCarInavailabilityStrategy;
import de.rwth.idsg.xsharing.router.core.routing.strategy.mode.BikeModeStrategy;
import de.rwth.idsg.xsharing.router.core.routing.strategy.mode.CarModeStrategy;


/**
 * Supported configuration:
 *
 * withReturn = false
 * lastLeg    = true
 *
 * Routes:
 *
 * 1) START --(walk)-> STATION --(car)-> DESTINATION
 * 2) START --(walk)-> STATION --(bike)-> STATION --(walk)--> DESTINATION
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 24.05.2016
 */
public class WithoutReturnAndLastLegStrategy extends AbstractLastLegRouteStrategy {

    public WithoutReturnAndLastLegStrategy(StrategyDependencyContext ctx) {
        super(
                new BikeModeStrategy(ctx, new DefaultBikeInavailabilityStrategy(DurationCheck.WITHOUT_RETURN)),
                new CarModeStrategy(ctx, new DefaultCarInavailabilityStrategy(DurationCheck.WITHOUT_RETURN))
        );
    }

    public WithoutReturnAndLastLegStrategy(BikeModeStrategy bikeModeStrategy, CarModeStrategy carModeStrategy) {
        super(bikeModeStrategy, carModeStrategy);
    }
}
