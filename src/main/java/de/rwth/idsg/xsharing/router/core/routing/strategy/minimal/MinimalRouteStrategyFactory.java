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

import de.rwth.idsg.xsharing.router.core.routing.request.SingleMinimalRequest;
import de.rwth.idsg.xsharing.router.core.routing.strategy.StrategyDependencyContext;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 26.05.2016
 */
public class MinimalRouteStrategyFactory {

    private final MinimalRouteStrategy withReturnAndLastLegStrategy;
    private final MinimalRouteStrategy withReturnAndNoLastLegStrategy;

    private final MinimalRouteStrategy withoutReturnAndLastLegStrategy;
    private final MinimalRouteStrategy withoutReturnAndNoLastLegStrategy;

    public MinimalRouteStrategyFactory(StrategyDependencyContext ctx) {
        if (ctx.isUseLiveStatus()) {
            withReturnAndLastLegStrategy = new WithReturnAndLastLegStrategy(ctx);
            withReturnAndNoLastLegStrategy = new WithReturnAndNoLastLegStrategy();
            withoutReturnAndLastLegStrategy = new WithoutReturnAndLastLegStrategy(ctx);
            withoutReturnAndNoLastLegStrategy = new WithoutReturnAndNoLastLegStrategy(ctx);

        } else {
            // This is the only kind of request we support for lower bounds
            withoutReturnAndLastLegStrategy = new LowerBoundsRouteStrategy(ctx);

            // And all the others are deactivated
            withReturnAndLastLegStrategy
                    = withReturnAndNoLastLegStrategy
                    = withoutReturnAndNoLastLegStrategy
                    = new DisabledMinimalRouteStrategy();
        }
    }

    public MinimalRouteStrategy decideStrategy(SingleMinimalRequest request) {
        boolean withReturn = request.isWithReturn();
        boolean isLastLeg = request.isLastLeg();

        if (withReturn) {
            if (isLastLeg) {
                return withReturnAndLastLegStrategy;
            } else {
                return withReturnAndNoLastLegStrategy;
            }
        } else {
            if (isLastLeg) {
                return withoutReturnAndLastLegStrategy;
            } else {
                return withoutReturnAndNoLastLegStrategy;
            }
        }
    }
}
