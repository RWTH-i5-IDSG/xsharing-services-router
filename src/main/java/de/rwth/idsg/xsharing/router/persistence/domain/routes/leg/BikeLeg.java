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
package de.rwth.idsg.xsharing.router.persistence.domain.routes.leg;

import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.iv.model.SpatialReference;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;

import static de.rwth.idsg.xsharing.router.Constants.DatabaseConstants.BIKE_LEG_TRANSFER_TIME;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
@Getter
@Setter
@Cacheable
@Table(
        indexes = {
                @Index(columnList = "id"),
                @Index(columnList = "from_lon, from_lat, to_lon, to_lat")
        }
)
public class BikeLeg extends RouteLeg implements SharingLeg, Serializable {

    public BikeLeg(LegType type, GeoCoord from, GeoCoord to, double distance, int time,
                   SpatialReference spatialReference, String path) {

        super(type, from, to, distance, time, spatialReference, path);
    }

    @Override
    public int getTransferTime() {
        return BIKE_LEG_TRANSFER_TIME;
    }
}

