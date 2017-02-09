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
package de.rwth.idsg.xsharing.router.core.routing.strategy.duration;

import com.google.common.collect.Iterables;
import de.rwth.idsg.xsharing.router.core.routing.strategy.RouteLegWrapper;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.BikeLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.CarLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.RouteLeg;

import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 25.05.2016
 */
public enum DurationCheck implements DurationCheckStrategy {

    ONLY_LEG {
        @Override
        public int getDurationToCheck(RouteLegWrapper decorator) {
            return decorator.getLeg().getDuration();
        }

        @Override
        public int getTotalRouteDurationInSeconds(List<RouteLegWrapper> legs) {
            // "Hinfahrt"
            return getTotalToDestination(legs);
        }

        @Override
        public double getTotalRouteDistance(List<RouteLegWrapper> legs) {
            return getTotalDistance(legs);
        }
    },

    WITHOUT_RETURN {
        @Override
        public int getDurationToCheck(RouteLegWrapper decorator) {
            return decorator.getLeg().getDuration() + decorator.getStayTime();
        }

        @Override
        public int getTotalRouteDurationInSeconds(List<RouteLegWrapper> legs) {
            // "Hinfahrt"
            return getTotalToDestination(legs);
        }

        @Override
        public double getTotalRouteDistance(List<RouteLegWrapper> legs) {
            return getTotalDistance(legs);
        }
    },

    WITH_RETURN {
        @Override
        public int getDurationToCheck(RouteLegWrapper decorator) {
            return 2 * decorator.getLeg().getDuration() + decorator.getStayTime();
        }

        @Override
        public int getTotalRouteDurationInSeconds(List<RouteLegWrapper> legs) {
            // "Hinfahrt" + "Rückfahrt"
            int total = 2 * getTotalToDestination(legs);

            // add the stay time of the last leg
            total += getStayTimeOfLastLeg(legs);

            return total;
        }

        @Override
        public double getTotalRouteDistance(List<RouteLegWrapper> legs) {
            return 2 * getTotalDistance(legs);
        }
    };

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static double getTotalDistance(List<RouteLegWrapper> legs) {
        double result = 0.0;
        for (RouteLegWrapper d : legs) {
            result += d.getLeg().getDistance();
        }
        return result;
    }

    private static int getStayTimeOfLastLeg(List<RouteLegWrapper> legs) {
        RouteLegWrapper last = Iterables.getLast(legs, null);
        if (last == null) {
            return 0;
        } else {
            return last.getStayTime();
        }
    }

    private static int getTotalToDestination(List<RouteLegWrapper> legs) {
        int totalTo = 0;
        for (RouteLegWrapper d : legs) {
            totalTo = totalTo + d.getLeg().getDuration() + getTransferTime(d.getLeg());
        }
        return totalTo;
    }

    private static int getTransferTime(RouteLeg leg) {
        if (leg instanceof BikeLeg) {
            BikeLeg bikeLeg = (BikeLeg) leg;
            // 2 because Station -> Station
            return bikeLeg.getTransferTime() * 2;

        } else if (leg instanceof CarLeg) {
            CarLeg carLeg = (CarLeg) leg;
            // 1 because Station -> Destination
            return carLeg.getTransferTime();

        } else {
            return 0;
        }
    }
}
