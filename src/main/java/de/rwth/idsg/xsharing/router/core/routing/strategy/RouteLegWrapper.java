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

import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.RouteLeg;
import lombok.Getter;
import lombok.Setter;

/**
 * Why: Since we are caching RouteLegs after fetching them from DB, they are not one-shot, throw-away objects
 * they used to be. We should not change the state of a RouteLeg!
 *
 * Therefore, we MUST NOT change the {@link RouteLeg#stayTime} while processing a routing request,
 * which has the stayTime parameter set, since the same RouteLeg might be used at the same time by multiple requests,
 * or later by future requests. This might have unwanted side effects, or produce wrong results.
 *
 * The new {@link RouteLegWrapper#stayTime} is the old {@link RouteLeg#stayTime}.
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 25.05.2016
 */
@Getter
@Setter
public final class RouteLegWrapper {

    private final RouteLeg leg;
    private int stayTime;

    public RouteLegWrapper(RouteLeg leg) {
        this.leg = leg;
        this.stayTime = leg.getStayTime();
    }
}
