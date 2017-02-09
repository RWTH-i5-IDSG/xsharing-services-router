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
package de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.Route;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.leg.AbstractLegRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.util.GeoCoordDeserializer;
import de.rwth.idsg.xsharing.router.persistence.domain.util.GeoCoordSerializer;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.joda.time.DateTime;

import java.util.LinkedList;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Getter
@Setter
@EqualsAndHashCode(exclude = {"legs"})
@ToString
public abstract class AbstractRouteRepresentation<T extends AbstractLegRepresentation> implements Route {

    @JsonProperty(value = "tripId")
    private final String id;

    @JsonSerialize(using = GeoCoordSerializer.class)
    @JsonDeserialize(using = GeoCoordDeserializer.class)
    @JsonProperty(value = "start")
    private GeoCoord from;

    @JsonSerialize(using = GeoCoordSerializer.class)
    @JsonDeserialize(using = GeoCoordDeserializer.class)
    @JsonProperty(value = "end")
    private GeoCoord to;

    protected DateTime departure;
    protected DateTime arrival;

    private boolean isArrivalTime;

    @Setter
    protected LinkedList<T> legs;

    /**
     * Only package local access!
     */
    AbstractRouteRepresentation(String id) {
        this.id = id;
    }

    /**
     * Only package local access!
     * Is used to build details/compact representations from minimal
     */
    AbstractRouteRepresentation(RouteMinimalRepresentation minimal, LinkedList<T> legs) {
        this.id = minimal.getId();
        this.from = minimal.getFrom();
        this.to = minimal.getTo();
        this.departure = minimal.getDeparture();
        this.arrival = minimal.getArrival();
        this.legs = legs;
        this.setTotalDurationCached(minimal.getTotalDurationCached());
        this.setTotalDistance(minimal.getTotalDistance());
    }

    @JsonIgnore
    private double totalDistance = 0.0;

    public Double getTotalDistance() {
        return totalDistance;
    }

    // We cache the value since getTotalDuration() is called twice during processing.
    @JsonIgnore
    private int totalDurationCached = 0;

    /**
     * TODO: refactor commented code out
     */
    @Override
    public Integer getTotalDuration() {
//        if (totalDurationCached == 0) {
//            int temp = 0;
//            for (T leg : legs) {
//                // add both at once
//                temp += leg.getDuration() + leg.getStayTime();
//                // should always be 0 except for return trip
//                if (LegType.CarLeg.equals(leg.getType())) {
//                    temp += leg.getTransferTime();
//                } else if (LegType.BikeLeg.equals(leg.getType())) {
//                    temp += (leg.getTransferTime() * 2);
//                }
//            }
//            totalDurationCached = temp;
//        }
        return totalDurationCached;
    }

    public Integer getTransfers() {
        if (legs == null) {
            return 0;
        }

        return legs.size() > 0 ? legs.size() - 1 : 0;
    }
}
