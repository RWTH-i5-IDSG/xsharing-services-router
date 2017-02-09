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
package de.rwth.idsg.xsharing.router.persistence.domain.util;

import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.BikeLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.CarLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.LegType;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.RouteLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.SharingLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.WalkingLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.leg.LegDetailsRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.leg.LegMinimalRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.representation.StationRepresentation;
import de.rwth.idsg.xsharing.router.iv.model.EsriFeatureAttribute;
import de.rwth.idsg.xsharing.router.iv.model.EsriPolyLineFeature;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.iv.model.SpatialReference;
import de.rwth.idsg.xsharing.router.utils.JsonMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Slf4j
public class RouteLegFactory {

    public static RouteLeg getLeg(LegType type, EsriPolyLineFeature poly) {
        RouteLeg leg;
        switch (type) {
            case BikeLeg:
                leg = new BikeLeg();
                break;
            case CarLeg:
                leg = new CarLeg();
                break;
            case WalkingLeg:
                leg = new WalkingLeg();
                break;
            default:
                log.error("Could not instantiate new RouteLeg class!");
                return null;
        }

        EsriFeatureAttribute att = poly.getAttributes();

        // early exit to prevent invalid route legs
        if (att.getDistance() == -1 && att.getTime() == -1) {
            return null;
        }

        leg.setType(type);
        leg.setFrom(JsonMapper.deserializeOrNull(att.getStartId(), GeoCoord.class));
        leg.setTo(JsonMapper.deserializeOrNull(att.getEndId(), GeoCoord.class));
        leg.setDistance((double) att.getDistance());
        leg.setDuration(att.getTime());
        leg.setSpatialReference(SpatialReference.builder().build());

        // minimal representation does not contain path!
        List<GeoCoord> path = extractPath(poly);
        if (path != null) {
            leg.setPath(PolylineCoder.encode(path));
        }

        return leg;
    }

    public static LegMinimalRepresentation getMinRep(RouteLeg routeLeg) {
        int transferTime = 0;
        if (routeLeg instanceof SharingLeg) {
            transferTime = ((SharingLeg) routeLeg).getTransferTime();
        }
        boolean isReversed = false;
        if (routeLeg instanceof WalkingLeg) {
            isReversed = ((WalkingLeg) routeLeg).isReversed();
        }
        return new LegMinimalRepresentation(routeLeg.getId(), routeLeg.getType(), routeLeg.getFrom(), routeLeg.getTo(),
                routeLeg.getDistance(), routeLeg.getDuration(), routeLeg.getStayTime(), transferTime, isReversed);
    }

    public static LinkedList<LegMinimalRepresentation> getListOfMinRep(List<RouteLeg> legs) {
        LinkedList<LegMinimalRepresentation> resultList = new LinkedList<>();
        legs.forEach(leg -> resultList.add(getMinRep(leg)));
        return resultList;
    }

    public static LinkedList<LegDetailsRepresentation> getListOfDetailsRep(List<RouteLeg> legs,
                                                                           List<StationRepresentation> stations) {
        ListIterator<StationRepresentation> stationIterator = stations.listIterator();
        LinkedList<LegDetailsRepresentation> resultList = new LinkedList<>();
        try {
            for (RouteLeg leg : legs) {
                if (leg instanceof BikeLeg) {
                    StationRepresentation fromStation = extractStation(stationIterator);
                    StationRepresentation toStation = extractStation(stationIterator);
                    resultList.add(getDetailsRep(leg, fromStation, toStation));

                } else if (leg instanceof CarLeg) {
                    StationRepresentation fromStation = extractStation(stationIterator);
                    resultList.add(getDetailsRep(leg, fromStation, null));

                } else {
                    resultList.add(getDetailsRep(leg, null, null));
                }
            }
            return resultList;
        } catch (Exception e) {
            return null;
        }
    }

    private static LegDetailsRepresentation getDetailsRep(RouteLeg routeLeg, StationRepresentation fromStation,
                                                          StationRepresentation toStation) {
        int transferTime = 0;
        if (routeLeg instanceof SharingLeg) {
            transferTime = ((SharingLeg) routeLeg).getTransferTime();
        }

        boolean isReversed = false;
        if (routeLeg instanceof WalkingLeg) {
            isReversed = ((WalkingLeg) routeLeg).isReversed();
        }

        return new LegDetailsRepresentation(routeLeg.getId(), routeLeg.getType(), routeLeg.getFrom(), routeLeg.getTo(),
                routeLeg.getDistance(), routeLeg.getDuration(), routeLeg.getStayTime(), transferTime, routeLeg.getPath(),
                fromStation, toStation, isReversed);
    }

    private static StationRepresentation extractStation(ListIterator<StationRepresentation> iterator) throws Exception {
        if (iterator.hasNext()) {
            return iterator.next();

        } else {
            throw new Exception();
        }
    }

    private static List<GeoCoord> extractPath(EsriPolyLineFeature poly) {
        return poly != null && poly.getGeometry() != null && poly.getGeometry().getPaths() != null
                ? poly.getGeometry().getPaths().get(0)
                : null;
    }
}
