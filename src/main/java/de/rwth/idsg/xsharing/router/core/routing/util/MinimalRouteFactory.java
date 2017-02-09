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
package de.rwth.idsg.xsharing.router.core.routing.util;

import de.rwth.idsg.xsharing.router.core.routing.strategy.RouteLegWrapper;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.RouteLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.SharingLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.WalkingLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.leg.LegMinimalRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteMinimalRepresentation;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.core.routing.strategy.RouteContext;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.factory.RouteRepresentationFactory;
import org.joda.time.DateTime;

import java.util.LinkedList;
import java.util.List;

import static de.rwth.idsg.xsharing.router.utils.BasicUtils.checkNullOrEmpty;

/**
 * Duplicate of {@link RouteRepresentationFactory}
 * with the change from RouteLeg to RouteLegWrapper.
 *
 * TODO: Cleanup and unify duplicates later
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 26.05.2016
 */
public final class MinimalRouteFactory {

    private MinimalRouteFactory() { }

    public static RouteMinimalRepresentation getMinRep(GeoCoord from, GeoCoord to,
                                                       DateTime time, boolean isArrival,
                                                       RouteContext sm) {
        List<RouteLegWrapper> legs = sm.getLegs();

        if (checkNullOrEmpty(legs)) {
            return null;
        }

        RouteMinimalRepresentation route = new RouteMinimalRepresentation();
        route.setFrom(from);
        route.setTo(to);
        route.setTotalDurationCached(sm.getTotalDurationInSeconds());
        route.setTotalDistance(sm.getTotalDistance());

        LinkedList<LegMinimalRepresentation> legList = new LinkedList<>();
        for (RouteLegWrapper l : legs) {
            legList.add(getSingleLeg(l));
        }
        route.setLegs(legList);
        route.setArrivalTime(isArrival);

        // internal duration in seconds !!!
        //
        if (isArrival) {
            route.setArrival(time);
            route.setDeparture(time.minusSeconds(route.getTotalDuration()));
        } else {
            route.setDeparture(time);
            route.setArrival(time.plusSeconds(route.getTotalDuration()));
        }
        return route;
    }

    private static LegMinimalRepresentation getSingleLeg(RouteLegWrapper decorator) {
        RouteLeg routeLeg = decorator.getLeg();

        int transferTime = 0;
        if (routeLeg instanceof SharingLeg) {
            transferTime = ((SharingLeg) routeLeg).getTransferTime();
        }

        boolean isReversed = false;
        if (routeLeg instanceof WalkingLeg) {
            isReversed = ((WalkingLeg) routeLeg).isReversed();
        }

        return new LegMinimalRepresentation(routeLeg.getId(),
                                            routeLeg.getType(),
                                            routeLeg.getFrom(),
                                            routeLeg.getTo(),
                                            routeLeg.getDistance(),
                                            routeLeg.getDuration(),
                                            decorator.getStayTime(),
                                            transferTime,
                                            isReversed);
    }
}
