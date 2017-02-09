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
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.SharingStation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

/**
 * A leg leading to a {@link SharingStation}!
 *
 * Reasoning: RasterManagerImpl#updateWalkingLegForStation(List, WalkingLeg)
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@MappedSuperclass
@EqualsAndHashCode
@ToString
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "from.x", column = @Column(name = "from_lon")),
        @AttributeOverride(name = "from.y", column = @Column(name = "from_lat")),
        @AttributeOverride(name = "to.x", column = @Column(name = "to_lon")),
        @AttributeOverride(name = "to.y", column = @Column(name = "to_lat"))
})
public abstract class RouteLeg implements BasicLeg, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @NonNull private LegType type;

    @NonNull private GeoCoord from;
    @NonNull private GeoCoord to;
    @NonNull private double distance;
    @NonNull private int duration; // in seconds!

    @Embedded
    @NonNull private SpatialReference spatialReference;

    // for return trips introduce stay time in seconds
    // default value is 0 for non-return trips
    @Column(name = "stay_time")
    @NonNull private int stayTime = 0;

    @Column(columnDefinition="TEXT")
    @NonNull private String path;

    /**
     * See RouteLegWrapper
     */
    @Deprecated
    @Override
    public int getStayTime() {
        return stayTime;
    }

    /**
     * See RouteLegWrapper
     */
    @Deprecated
    public void setStayTime(int stayTime) {
        this.stayTime = stayTime;
    }
}
