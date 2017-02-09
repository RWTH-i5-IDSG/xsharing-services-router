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
package de.rwth.idsg.xsharing.router.persistence.domain.mb;

import lombok.ToString;
import org.hibernate.annotations.Immutable;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@ToString
@Entity
@Immutable
@Cacheable(false)
@Table(name = "place", schema = "ixsi", catalog = "mobility_broker_db")
public class MBPlaceEntity {

    private String placeId;
    private String globalId;
    private Integer capacity;
    private Short onPremisesTimeInSeconds;
    private String providerId;
    private Integer availableCapacity;
    private Integer availableVehicles;
    private String name;
    private String gpsPosition;

    @Id
    @Column(name = "place_id")
    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    @Basic
    @Column(name = "global_id")
    public String getGlobalId() {
        return globalId;
    }

    public void setGlobalId(String globalId) {
        this.globalId = globalId;
    }

    @Basic
    @Column(name = "capacity")
    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    @Basic
    @Column(name = "on_premises_time_in_seconds")
    public Short getOnPremisesTimeInSeconds() {
        return onPremisesTimeInSeconds;
    }

    public void setOnPremisesTimeInSeconds(Short onPremisesTimeInSeconds) {
        this.onPremisesTimeInSeconds = onPremisesTimeInSeconds;
    }

    @Basic
    @Column(name = "provider_id")
    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    @Basic
    @Column(name = "available_capacity")
    public Integer getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(Integer availableCapacity) {
        this.availableCapacity = availableCapacity;
    }

    @Basic
    @Column(name = "available_vehicles")
    public Integer getAvailableVehicles() {
        return availableVehicles;
    }

    public void setAvailableVehicles(Integer availableVehicles) {
        this.availableVehicles = availableVehicles;
    }

    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "gps_position")
    public String getGpsPosition() {
        return gpsPosition;
    }

    public void setGpsPosition(String gpsPosition) {
        this.gpsPosition = gpsPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MBPlaceEntity that = (MBPlaceEntity) o;

        if (placeId != null ? !placeId.equals(that.placeId) : that.placeId != null) return false;
        if (globalId != null ? !globalId.equals(that.globalId) : that.globalId != null) return false;
        if (capacity != null ? !capacity.equals(that.capacity) : that.capacity != null) return false;
        if (onPremisesTimeInSeconds != null ? !onPremisesTimeInSeconds.equals(that.onPremisesTimeInSeconds) : that.onPremisesTimeInSeconds != null)
            return false;
        if (providerId != null ? !providerId.equals(that.providerId) : that.providerId != null) return false;
        if (availableCapacity != null ? !availableCapacity.equals(that.availableCapacity) : that.availableCapacity != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (gpsPosition != null ? !gpsPosition.equals(that.gpsPosition) : that.gpsPosition != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = placeId != null ? placeId.hashCode() : 0;
        result = 31 * result + (globalId != null ? globalId.hashCode() : 0);
        result = 31 * result + (capacity != null ? capacity.hashCode() : 0);
        result = 31 * result + (onPremisesTimeInSeconds != null ? onPremisesTimeInSeconds.hashCode() : 0);
        result = 31 * result + (providerId != null ? providerId.hashCode() : 0);
        result = 31 * result + (availableCapacity != null ? availableCapacity.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (gpsPosition != null ? gpsPosition.hashCode() : 0);
        return result;
    }
}
