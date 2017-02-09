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
package de.rwth.idsg.xsharing.router.persistence.domain.station.sharing;

import com.vividsolutions.jts.geom.Point;
import de.rwth.idsg.xsharing.router.persistence.domain.station.Station;
import de.rwth.idsg.xsharing.router.persistence.domain.station.VehicleStatus;
import de.rwth.idsg.xsharing.router.utils.BasicUtils;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Table(
        indexes = {
                @Index(columnList = "id"),
                @Index(columnList = "place_id")
        }
)
public abstract class SharingStation implements Station, Serializable {

    // ID only for internal database management, actual identification by place_id + provider_id
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(name = "place_id")
    @NonNull private String placeId;

    @Column(name = "global_id")
    private String globalId;

    @Column(name = "geopos", columnDefinition="Geometry")
    @Type(type = "org.hibernate.spatial.GeometryType")
    @NonNull private Point geoPos;

    @Column(name = "provider_id")
    @NonNull private String providerId;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "available_capacity")
    private Integer availableCapacity;

    @Column(name = "available_vehicles")
    private Integer availableVehicles;

    private String name;

    @Transient
    private List<VehicleStatus> vehicleStatusList = Collections.emptyList();

    public boolean isSame(SharingStation s) {
        if (isNullOrEmpty(providerId) && isNullOrEmpty(placeId)) {
            // This case actually should not happen, but xsharing database
            // does not enforce "NOT NULL" constraint on these fields.
            return false;
        } else {
            return providerId.equals(s.getProviderId()) && placeId.equals(s.getPlaceId());
        }
    }

    // -------------------------------------------------------------------------
    // capacity and availableCapacity can be NULL, when the sharing provider
    // does not support Place Availability in IXSI!
    //
    // availableVehicles should never be NULL, because the query in
    // MBDataRepositoryImpl#SELECTION selects into
    // this value with count(). But experience and stack traces in log show
    // that funnily enough it can (sigh). We should investigate why this is.
    //
    // But for the time being, to prevent NPEs, we set them to 0 when NULL.
    // These fields are deprecated and not used by our routing anymore, anyway.
    // -------------------------------------------------------------------------

    public void setCapacity(Integer capacity) {
        this.capacity = BasicUtils.getIntegerOrZero(capacity);
    }

    public void setAvailableCapacity(Integer availableCapacity) {
        this.availableCapacity = BasicUtils.getIntegerOrZero(availableCapacity);
    }

    public void setAvailableVehicles(Integer availableVehicles) {
        this.availableVehicles = BasicUtils.getIntegerOrZero(availableVehicles);
    }
}
