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
package de.rwth.idsg.xsharing.router.core.routing.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.Route;
import de.rwth.idsg.xsharing.router.persistence.domain.util.GeoCoordDeserializer;
import de.rwth.idsg.xsharing.router.persistence.domain.util.GeoCoordSerializer;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class BaseResponse implements SharingResponse, Serializable {

    @JsonProperty(value = "tripId")
    private String id;

    @JsonProperty(value = "startGeoCoordinates")
    @JsonSerialize(using = GeoCoordSerializer.class)
    @JsonDeserialize(using = GeoCoordDeserializer.class)
    private GeoCoord from;

    @JsonProperty(value = "endGeoCoordinates")
    @JsonSerialize(using = GeoCoordSerializer.class)
    @JsonDeserialize(using = GeoCoordDeserializer.class)
    private GeoCoord to;

    private Integer totalDuration;
    private Double totalDistance;
    private Integer transfers;

    private RouterError routerError;

    /**
     * Error constructor
     */
    BaseResponse(RouterError error) {
        this.routerError = error;
    }

    /**
     * Normal constructor
     */
    BaseResponse(Route route) {
        this.id = route.getId();
        this.from = route.getFrom();
        this.to = route.getTo();
        this.totalDuration = route.getTotalDuration();
        this.totalDistance = route.getTotalDistance();
        this.transfers = route.getTransfers();
    }
}
