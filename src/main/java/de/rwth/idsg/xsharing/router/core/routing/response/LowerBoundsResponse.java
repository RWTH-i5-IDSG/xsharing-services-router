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
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteMinimalRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.util.GeoCoordDeserializer;
import de.rwth.idsg.xsharing.router.persistence.domain.util.GeoCoordSerializer;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.core.routing.request.SingleMinimalRequest;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 09.03.2016
 */
@Getter
@ToString
@EqualsAndHashCode
public class LowerBoundsResponse implements SharingResponse, Serializable {

    @JsonProperty(value = "startGeoCoordinate")
    @JsonSerialize(using = GeoCoordSerializer.class)
    @JsonDeserialize(using = GeoCoordDeserializer.class)
    private GeoCoord from;

    @JsonProperty(value = "endGeoCoordinate")
    @JsonSerialize(using = GeoCoordSerializer.class)
    @JsonDeserialize(using = GeoCoordDeserializer.class)
    private GeoCoord to;

    private Integer minimalDuration;
    private Double minimalDistance;
    private Integer minimalTransfers;

    public LowerBoundsResponse(RouteMinimalRepresentation rep) {
        this.from = rep.getFrom();
        this.to = rep.getTo();
        this.minimalDuration = rep.getTotalDuration();
        this.minimalDistance = rep.getTotalDistance();
        this.minimalTransfers = rep.getTransfers();
    }

    // -------------------------------------------------------------------------
    // Error constructor
    // -------------------------------------------------------------------------

    private RouterError routerError;

    public LowerBoundsResponse(RouterError error) {
        this.routerError = error;
    }

    public LowerBoundsResponse withFromTo(SingleMinimalRequest req) {
        this.from = req.getStartPoint();
        this.to = req.getEndPoint();
        return this;
    }

}
