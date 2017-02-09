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
package de.rwth.idsg.xsharing.router.core.routing.strategy.time;

import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 25.05.2016
 */
public enum DateTimeCheck implements DateTimeCheckStrategy {

    DEPARTURE {
        @Override
        public DateTime adjustDateTime(DateTime queryTime, int durationToShift) {
            // returns a copy!
            return queryTime.plusSeconds(durationToShift);
        }

        @Override
        public Interval getCheck(DateTime afterLastAddedLeg, int durationToShift) {
            DateTime afterPossibleLeg = this.adjustDateTime(afterLastAddedLeg, durationToShift);
            return new Interval(afterLastAddedLeg, afterPossibleLeg);
        }
    },

    ARRIVAL {
        @Override
        public DateTime adjustDateTime(DateTime queryTime, int durationToShift) {
            // returns a copy!
            return queryTime.minusSeconds(durationToShift);
        }

        @Override
        public Interval getCheck(DateTime afterLastAddedLeg, int durationToShift) {
            DateTime afterPossibleLeg = this.adjustDateTime(afterLastAddedLeg, durationToShift);
            return new Interval(afterPossibleLeg, afterLastAddedLeg);
        }
    };

    public static DateTimeCheck get(boolean isArrival) {
        if (isArrival) {
            return DateTimeCheck.ARRIVAL;
        } else {
            return DateTimeCheck.DEPARTURE;
        }
    }
}
