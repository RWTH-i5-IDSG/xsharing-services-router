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
package de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.factory;

import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.RouteLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.util.RouteLegFactory;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.leg.LegMinimalRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteCompactRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteDetailsRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteMinimalRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.representation.StationRepresentation;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public class RouteRepresentationFactory {

    public static RouteMinimalRepresentation getMinRep(GeoCoord from, GeoCoord to, DateTime time, boolean isArrival,
                                                       List<RouteLeg> legs) {
        if (checkNullOrEmpty(legs)) {
            return null;
        }

        RouteMinimalRepresentation route = new RouteMinimalRepresentation();
        route.setFrom(from);
        route.setTo(to);

        LinkedList<LegMinimalRepresentation> legList = new LinkedList<>();
        for (RouteLeg l : legs) {
            legList.add(RouteLegFactory.getMinRep(l));
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

    public static RouteCompactRepresentation getCompactRep(RouteMinimalRepresentation input,
                                                           LinkedList<RouteLeg> legs) {
        if (input == null || checkNullOrEmpty(legs)) {
            return null;
        } else {
            return new RouteCompactRepresentation(input, RouteLegFactory.getListOfMinRep(legs));
        }
    }

    public static RouteDetailsRepresentation getDetailsRep(RouteMinimalRepresentation input,
                                                           LinkedList<RouteLeg> legs,
                                                           LinkedList<StationRepresentation> stations) {

        return new RouteDetailsRepresentation(input, RouteLegFactory.getListOfDetailsRep(legs, stations));
    }

    private static boolean checkNullOrEmpty(Collection<?> col) {
        return (col == null) || col.isEmpty();
    }

}
