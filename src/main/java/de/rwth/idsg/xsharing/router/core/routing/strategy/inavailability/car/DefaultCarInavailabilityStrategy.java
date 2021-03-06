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
package de.rwth.idsg.xsharing.router.core.routing.strategy.inavailability.car;

import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.SharingStation;
import de.rwth.idsg.xsharing.router.core.routing.strategy.RouteLegWrapper;
import de.rwth.idsg.xsharing.router.core.routing.strategy.RouteLegList;
import de.rwth.idsg.xsharing.router.core.routing.strategy.duration.DurationCheckStrategy;
import de.rwth.idsg.xsharing.router.core.routing.strategy.duration.TotalStrategy;
import de.rwth.idsg.xsharing.router.core.routing.strategy.inavailability.AbstractInavailabilityStrategy;
import org.joda.time.Interval;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 26.05.2016
 */
public class DefaultCarInavailabilityStrategy
        extends AbstractInavailabilityStrategy
        implements CarInavailabilityStrategy {

    private final DurationCheckStrategy durationCheckStrategy;

    public DefaultCarInavailabilityStrategy(DurationCheckStrategy durationCheckStrategy) {
        this.durationCheckStrategy = durationCheckStrategy;
    }

    @Override
    public TotalStrategy getTotalStrategy() {
        return durationCheckStrategy;
    }

    @Override
    public boolean overlaps(int stayTime, RouteLegList legs,
                            SharingStation bsStart, RouteLegWrapper carWrapper) {
        // We always set stay time. Depending on DurationCheckStrategy it will be used or not.
        carWrapper.setStayTime(stayTime);

        int duration = durationCheckStrategy.getDurationToCheck(carWrapper);
        Interval interval = legs.getIntervalAfterPossibleLeg(duration);

        return overlaps(bsStart, interval);
    }
}
