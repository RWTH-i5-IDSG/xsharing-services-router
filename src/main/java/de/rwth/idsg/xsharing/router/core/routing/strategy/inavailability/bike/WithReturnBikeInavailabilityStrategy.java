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
package de.rwth.idsg.xsharing.router.core.routing.strategy.inavailability.bike;

import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.SharingStation;
import de.rwth.idsg.xsharing.router.core.routing.strategy.RouteLegWrapper;
import de.rwth.idsg.xsharing.router.core.routing.strategy.RouteLegList;
import de.rwth.idsg.xsharing.router.core.routing.strategy.duration.DurationCheck;
import de.rwth.idsg.xsharing.router.core.routing.strategy.duration.TotalStrategy;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import static de.rwth.idsg.xsharing.router.utils.DateTimeUtils.getNowTimeWindow;

/**
 * Strategy when:
 *
 * withReturn = true
 * lastLeg    = true
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 26.05.2016
 */
public class WithReturnBikeInavailabilityStrategy extends DefaultBikeInavailabilityStrategy {

    public WithReturnBikeInavailabilityStrategy() {
        super(DurationCheck.WITH_RETURN);
    }

    @Override
    public TotalStrategy getTotalStrategy() {
        return durationCheckStrategy;
    }

    /**
     *  start   | end    | overlaps?
     * ---------+--------+--------------------------------------------------
     *  now     | now    | check inavailability at both stations
     *  now     | future | check inavailability at start station, be optimistic about end station
     *  future  | now    | false. cannot happen, chronologically not possible
     *  future  | future | false. both in future => be optimistic, no check
     */
    @Override
    public boolean overlaps(int stayTime, RouteLegList legs,
                            SharingStation startStation, RouteLegWrapper bikeWrapper,
                            SharingStation endStation, RouteLegWrapper walkWrapper) {

        // We always set stay time. Depending on DurationCheckStrategy it will be used or not.
        walkWrapper.setStayTime(stayTime);

        // -------------------------------------------------------------------------
        // 1) If in future, be optimistic and assume always available
        // -------------------------------------------------------------------------

        Interval nowTimeWindow = getNowTimeWindow();

        DateTime timeAtStartStation = legs.getAfterLastLeg();
        boolean startIsNow = nowTimeWindow.contains(timeAtStartStation);

        if (!startIsNow) {
            return false;
        }

        // -------------------------------------------------------------------------
        // 2) Check actual intervals for availability
        // -------------------------------------------------------------------------

        int bikeLegDuration = bikeWrapper.getLeg().getDuration();
        int durationAfterStartStation = bikeLegDuration + durationCheckStrategy.getDurationToCheck(walkWrapper);

        DateTime timeAtReturnStation = timeAtStartStation.plusSeconds(durationAfterStartStation);
        boolean endIsNow = nowTimeWindow.contains(timeAtReturnStation);

        if (endIsNow) {
            // Check bike availability for "HinFahrt" at start station and "RückFahrt" at end station
            return overlapsAtStation(legs, startStation, bikeLegDuration)
                    || overlapsAtStation(legs, endStation, durationAfterStartStation);
        } else {
            // Check bike availability for "HinFahrt" at start station
            return overlapsAtStation(legs, startStation, bikeLegDuration);
        }
    }

    private static boolean overlapsAtStation(RouteLegList legs, SharingStation station, int durationToShift) {
        Interval interval = legs.getIntervalAfterPossibleLeg(durationToShift);
        return overlaps(station, interval);
    }
}
