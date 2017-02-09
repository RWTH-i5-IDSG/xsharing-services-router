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

import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.LegType;
import de.rwth.idsg.xsharing.router.persistence.domain.station.representation.StationRepresentation;
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
@EqualsAndHashCode(callSuper = true)
@ToString
public class LegDetailsRepresentation extends AbstractLegRepresentation {

    private String path;
    private StationRepresentation startStation;
    private StationRepresentation endStation;

    public LegDetailsRepresentation(Long id, LegType type, GeoCoord from, GeoCoord to, double distance,
                                    int duration, int stayTime, int transferTime,
                                    boolean isReversed) {

        super(id, type, from, to, distance, duration, stayTime, transferTime, isReversed);
    }

    public LegDetailsRepresentation(Long id, LegType type, GeoCoord from, GeoCoord to, double distance,
                                    int duration, int stayTime, int transferTime, String path,
                                    StationRepresentation startStation, StationRepresentation endStation,
                                    boolean isReversed) {

        super(id, type, from, to, distance, duration, stayTime, transferTime, isReversed);
        this.path = path;
        this.startStation = startStation;
        this.endStation = endStation;
    }
}
