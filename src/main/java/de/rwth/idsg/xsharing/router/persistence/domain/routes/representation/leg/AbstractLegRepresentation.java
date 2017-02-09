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
package de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.leg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.BasicLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.LegType;
import de.rwth.idsg.xsharing.router.persistence.domain.util.GeoCoordDeserializer;
import de.rwth.idsg.xsharing.router.persistence.domain.util.GeoCoordSerializer;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class AbstractLegRepresentation implements BasicLeg {

    @JsonProperty(value = "id")
    private Long id;
    @JsonProperty(value = "legType")
    private LegType type;

    @JsonSerialize(using = GeoCoordSerializer.class)
    @JsonDeserialize(using = GeoCoordDeserializer.class)
    @JsonProperty(value = "start")
    private GeoCoord from;

    @JsonSerialize(using = GeoCoordSerializer.class)
    @JsonDeserialize(using = GeoCoordDeserializer.class)
    @JsonProperty(value = "end")
    private GeoCoord to;

    private double distance;
    private int duration; // in seconds!
    private int stayTime;
    private int transferTime;

    // this is only important for bidirectional-capable legs (walking)
    @JsonIgnore
    private boolean isReversed = false;
}
