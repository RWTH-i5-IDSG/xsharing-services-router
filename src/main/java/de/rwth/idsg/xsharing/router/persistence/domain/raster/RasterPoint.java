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
package de.rwth.idsg.xsharing.router.persistence.domain.raster;

import de.rwth.idsg.xsharing.router.persistence.domain.raster.station.BikeStationTuple;
import de.rwth.idsg.xsharing.router.persistence.domain.raster.station.CarStationTuple;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.BikeStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.CarStation;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Entity
@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Table(indexes = {
        @Index(columnList = "point_id")
})
public class RasterPoint {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    @Column(name = "point_id")
    private Long pointId;

    private @NonNull GeoCoord coord;

    @ElementCollection
    @OrderColumn
    @LazyCollection(LazyCollectionOption.FALSE)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(
            joinColumns = { @JoinColumn(name = "raster_point_id") },
            indexes = @Index(columnList = "raster_point_id"))
    @Embedded
    @Cascade(CascadeType.REMOVE)
    private List<BikeStationTuple> nearestBikeStations;

    @ElementCollection
    @OrderColumn
    @LazyCollection(LazyCollectionOption.FALSE)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(
            joinColumns = { @JoinColumn(name = "raster_point_id") },
            indexes = @Index(columnList = "raster_point_id"))
    @Embedded
    @Cascade(CascadeType.REMOVE)
    private List<CarStationTuple> nearestCarStations;

    // convenience methods for coordinate extraction
    public double getX() {
        return coord.getX();
    }

    public double getY() {
        return coord.getY();
    }

    public List<BikeStation> getBikeStationsList() {
        if (getNearestBikeStations() == null) {
            return Collections.emptyList();
        }

        return getNearestBikeStations().stream()
                                       .map(BikeStationTuple::getStation)
                                       .collect(Collectors.toList());
    }

    public List<CarStation> getCarStationsList() {
        if (getNearestCarStations() == null) {
            return Collections.emptyList();
        }

        return getNearestCarStations().stream()
                                      .map(CarStationTuple::getStation)
                                      .collect(Collectors.toList());
    }
}
