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
package de.rwth.idsg.xsharing.router.core.routing.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.rwth.idsg.xsharing.router.persistence.domain.util.GeoCoordListDeserializer;
import de.rwth.idsg.xsharing.router.persistence.domain.util.GeoCoordListSerializer;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MinimalRequest implements SharingRequest, Serializable {

    // REQUIRED PARAMETERS

    @JsonDeserialize(using = GeoCoordListDeserializer.class)
    @JsonSerialize(using = GeoCoordListSerializer.class)
    @JsonProperty(value = "startGeoCoordinates")
    private List<GeoCoord> startPoint;

    @JsonDeserialize(using = GeoCoordListDeserializer.class)
    @JsonSerialize(using = GeoCoordListSerializer.class)
    @JsonProperty(value = "endGeoCoordinates")
    private List<GeoCoord> endPoint;

    @JsonProperty(value = "departureTime")
    private List<DateTime> time;

    @JsonProperty(value = "isArrivalTime")
    private Boolean isArrivalTime;

    // OPTIONAL PARAMETERS

    @JsonProperty(value = "isLastLeg")
    private Boolean isLastLeg;
    // optional field indicating that the route should include return trip
    @JsonProperty(value = "isWithReturn")
    private Boolean isWithReturn = false;
    // only to be set when isWithReturn is true!
    // duration of the stay at location before returntrip
    @JsonProperty(value = "stayTime")
    private Integer stayTime = 0;
    @JsonProperty(value = "maxWalkDistance")
    private Double maxWalkDistance;
    @JsonProperty(value = "modalTypes")
    private String[] modes;
}
