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
package de.rwth.idsg.xsharing.router.persistence.domain.station.transit;

import com.vividsolutions.jts.geom.Point;
import de.rwth.idsg.xsharing.router.persistence.domain.station.Station;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.*;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Table(indexes = {
        @Index(columnList = "id"),
        @Index(columnList = "station_id")
}
)
public abstract class PTStation implements Station {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "station_id", unique = true)
    @NonNull private String stationId;

    @Column(name = "station_name")
    @NonNull private String stationName;

    @Column(name = "geopos", columnDefinition="Geometry")
    @Type(type = "org.hibernate.spatial.GeometryType")
    @NonNull private Point geoPos;
}
