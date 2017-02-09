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

import de.rwth.idsg.xsharing.router.core.routing.strategy.time.DateTimeCheck;
import de.rwth.idsg.xsharing.router.core.routing.strategy.time.DateTimeCheckStrategy;
import lombok.ToString;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Decorator around the route response "List<RouteLeg>" with additional availability/interval check logic.
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 24.05.2016
 */
@ToString
public final class RouteLegList {

    private final List<RouteLegWrapper> legs = new ArrayList<>();

    private final DateTimeCheckStrategy strategy;
    private DateTime afterLastLeg;
    private RouteLegWrapper last = null;

    /**
     * @param requestTime   default: departure time
     * @param isArrival     when true, requestTime becomes arrival time!
     */
    public RouteLegList(DateTime requestTime, boolean isArrival) {

        // working copy which we move forwards/backwards after adding a leg
        // depending on the strategy
        afterLastLeg = requestTime;

        strategy = DateTimeCheck.get(isArrival);
    }

    /**
     * Before the availability/interval overlap check.
     */
    public boolean addAndShift(@Nonnull RouteLegWrapper decorator) {
        last = decorator;
        afterLastLeg = strategy.adjustDateTime(afterLastLeg, getDurationToShift(decorator));
        return legs.add(decorator);
    }

    /**
     * After the availability/interval overlap check. We do not need to adjust date/time anymore.
     */
    public boolean add(@Nonnull RouteLegWrapper decorator) {
        return legs.add(decorator);
    }

    public List<RouteLegWrapper> getList() {
        return legs;
    }

    public Interval getIntervalAfterPossibleLeg(int durationToShift) {
        return strategy.getCheck(afterLastLeg, durationToShift);
    }

    public DateTime getAfterLastLeg() {
        return afterLastLeg;
    }

    private static int getDurationToShift(RouteLegWrapper decorator) {
        return decorator.getLeg().getDuration();
    }

}
