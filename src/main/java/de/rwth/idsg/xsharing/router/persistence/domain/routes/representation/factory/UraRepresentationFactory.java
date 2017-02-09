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

import de.ivu.realtime.modules.ura.data.GeoCoordinates;
import de.ivu.realtime.modules.ura.data.ModalType;
import de.ivu.realtime.modules.ura.data.response.IndividualTrip;
import de.ivu.realtime.modules.ura.data.response.Location;
import de.ivu.realtime.modules.ura.data.response.PathData;
import de.ivu.realtime.modules.ura.data.response.Position;
import de.ivu.realtime.modules.ura.data.response.Prediction;
import de.ivu.realtime.modules.ura.data.response.Station;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.LegType;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.leg.AbstractLegRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.leg.LegDetailsRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteCompactRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteDetailsRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.representation.StationRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.util.PolylineCoder;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public class UraRepresentationFactory {

    private static ModalType toModalType(LegType legType) {
        switch (legType) {
            case BikeLeg:       return ModalType.cycle;
            case CarLeg:        return ModalType.selfDriveCar;
            case WalkingLeg:    return ModalType.walk;
            default:            throw new RuntimeException("Unexpected leg type");
        }
    }

    public static List<IndividualTrip> getUraRepresentation(RouteCompactRepresentation rep) {
        DateTime startTime = rep.getDeparture();
        return getListOfTrips(rep.getLegs(), startTime);
    }

    public static List<IndividualTrip> getUraRepresentation(RouteDetailsRepresentation rep) {
        DateTime startTime = rep.getDeparture();
        return getListOfTrips(rep.getLegs(), startTime);
    }

    private static <T extends AbstractLegRepresentation> List<IndividualTrip> getListOfTrips(List<T> reps,
                                                                                             DateTime startTime) {
        List<IndividualTrip> resultList = new ArrayList<>(reps.size());
        DateTime arrival;
        DateTime departure = startTime;

        // convert legs one by one
        for (T leg : reps) {
            arrival = departure;
            Location startLocation;
            Location endLocation;
            Integer startDelay = 0;
            Integer endDelay = 0;

            switch (leg.getType()) {

                // Station -> Station
                //
                case BikeLeg: {
                    // incorporate delays by check-out etc.
                    startDelay += leg.getTransferTime();
                    endDelay += leg.getTransferTime();

                    StationRepresentation startStation = null;
                    StationRepresentation endStation = null;
                    if (leg instanceof LegDetailsRepresentation) {
                        startStation = ((LegDetailsRepresentation) leg).getStartStation();
                        endStation = ((LegDetailsRepresentation) leg).getEndStation();
                    }
                    startLocation = getUraStation(leg.getFrom(), startStation);
                    endLocation = getUraStation(leg.getTo(), endStation);
                }
                break;

                // Station -> Position
                //
                case CarLeg: {
                    // incorporate delays by check-out etc.
                    startDelay += leg.getTransferTime();

                    StationRepresentation startStation = null;
                    if (leg instanceof LegDetailsRepresentation) {
                        startStation = ((LegDetailsRepresentation) leg).getStartStation();
                    }
                    startLocation = getUraStation(leg.getFrom(), startStation);
                    endLocation = getUraPosition(leg.getTo());
                }
                break;

                // Position -> Position
                //
                case WalkingLeg: {
                    startLocation = getUraPosition(leg.getFrom());
                    endLocation = getUraPosition(leg.getTo());
                }
                break;

                default:
                    throw new RuntimeException("Unexpected leg type");
            }

            departure = arrival.plus(Duration.standardSeconds(startDelay));

            // predictions need to include delay by transfers
            Prediction start = new Prediction.Builder()
                    .withScheduledArrivalTime(arrival)
                    .withScheduledDepartureTime(departure)
                    .withLocation(startLocation)
                    .build();

            arrival = departure.plus(Duration.standardSeconds(leg.getDuration()));
            departure = arrival.plus(Duration.standardSeconds(endDelay));

            Prediction end = new Prediction.Builder()
                    .withScheduledArrivalTime(arrival)
                    .withScheduledDepartureTime(departure)
                    .withLocation(endLocation)
                    .build();

            PathData path = null;
            if (leg instanceof LegDetailsRepresentation) {
                path = new PathData.Builder()
                        .withRouteGeometry(convertRouteGeometry((LegDetailsRepresentation) leg))
                        .build();
            }

            IndividualTrip trip = new IndividualTrip.Builder()
                    .withDuration(Duration.standardSeconds((long) leg.getDuration()))
                    .withStart(start)
                    .withEnd(end)
                    .withLengthInM(leg.getDistance())
                    .withUuid(String.valueOf(leg.getId()))
                    .withModalType(toModalType(leg.getType()))
                    .withPathDataSource("xsharing-iv")
                    .withPathData(path)
                    .build();

            resultList.add(trip);
        }
        return resultList;
    }

    private static GeoCoordinates[] convertRouteGeometry(LegDetailsRepresentation leg) {
        return PolylineCoder.decode(leg.getPath(), leg.isReversed());
    }

    private static Position getUraPosition(GeoCoord at) {
        return new Position.Builder()
                .withLongitude(at.getX())
                .withLatitude(at.getY())
                .build();
    }

    private static Station getUraStation(GeoCoord at, StationRepresentation station) {
        if (station == null) {
            return new Station.Builder()
                    .withLongitude(at.getX())
                    .withLatitude(at.getY())
                    .build();
        }
        return new Station.Builder()
                .withLongitude(at.getX())
                .withLatitude(at.getY())
                .withAvailSpaces(station.getAvailCapacity())
                .withTotalSpaces(station.getCapacity())
                .withProvider(station.getProvider())
                .withStationId(station.getStationId())
                .withStationSubtype(station.getType())
                .withName(station.getName())
                .build();
    }
}
