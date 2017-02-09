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
import de.rwth.idsg.xsharing.router.persistence.domain.util.GeoCoordDeserializer;
import de.rwth.idsg.xsharing.router.persistence.domain.util.GeoCoordListDeserializer;
import de.rwth.idsg.xsharing.router.persistence.domain.util.GeoCoordListSerializer;
import de.rwth.idsg.xsharing.router.persistence.domain.util.GeoCoordSerializer;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 09.03.2016
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LowerBoundsRequest implements SharingRequest, Serializable {

    @JsonDeserialize(using = GeoCoordDeserializer.class)
    @JsonSerialize(using = GeoCoordSerializer.class)
    @JsonProperty(value = "startGeoCoordinate")
    private GeoCoord startPoint;

    @JsonDeserialize(using = GeoCoordListDeserializer.class)
    @JsonSerialize(using = GeoCoordListSerializer.class)
    @JsonProperty(value = "endGeoCoordinates")
    private List<GeoCoord> endPoint;

    @JsonProperty(value = "modalType")
    private String mode;
}
