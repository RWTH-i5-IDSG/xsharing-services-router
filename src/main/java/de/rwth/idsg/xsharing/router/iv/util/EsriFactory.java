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
package de.rwth.idsg.xsharing.router.iv.util;

import com.vividsolutions.jts.geom.Point;
import de.rwth.idsg.xsharing.router.iv.model.EsriPoint;
import de.rwth.idsg.xsharing.router.iv.model.EsriPointFeature;
import de.rwth.idsg.xsharing.router.iv.model.EsriPointFeatureAttribute;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.iv.model.SpatialReference;
import de.rwth.idsg.xsharing.router.iv.request.IVRequestTuple;
import de.rwth.idsg.xsharing.router.persistence.domain.station.Station;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.SharingStation;
import de.rwth.idsg.xsharing.router.persistence.domain.station.transit.PTStation;
import de.rwth.idsg.xsharing.router.utils.JsonMapper;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toSet;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public class EsriFactory {

    public static EsriPoint getSingleRequestPair(GeoCoord start, GeoCoord end) {
        return new EsriPoint(new SpatialReference(), Arrays.asList(getPointFeature(start), getPointFeature(end)));
    }

    public static EsriPointFeature getPointFeature(GeoCoord point) {
        return new EsriPointFeature(point, getPointFeatureAttribute(point));
    }

    public static EsriPointFeatureAttribute getPointFeatureAttribute(GeoCoord point) {
        return new EsriPointFeatureAttribute(JsonMapper.serializeOrThrow(point));
    }

    public static EsriPointFeature toEsriPointFeature(Station station) {
        return getPointFeature(new GeoCoord(station.getGeoPos().getX(), station.getGeoPos().getY()));
    }

    @Nullable
    public static GeoCoord toGeoCoord(PTStation station) {
        if (station.getGeoPos() == null) {
            return null;
        } else {
            return new GeoCoord(station.getGeoPos().getX(), station.getGeoPos().getY());
        }
    }

    public static <T extends SharingStation> IVRequestTuple getIvRequestTuple(EsriPointFeature sourcePoint,
                                                                              List<T> sharingStations) {
        return new IVRequestTuple(sourcePoint, sharingStations.stream()
                                                              .map(s -> getPointFeature(toGeoCoord(s.getGeoPos())))
                                                              .collect(toSet()));
    }

    public static IVRequestTuple getCarRequestTuple(EsriPointFeature sourcePoint, List<GeoCoord> gcList) {
        return new IVRequestTuple(sourcePoint, gcList.stream()
                                                     .map(EsriFactory::getPointFeature)
                                                     .collect(toSet()));
    }

    private static GeoCoord toGeoCoord(Point geoPos) {
        return new GeoCoord(geoPos.getX(), geoPos.getY());
    }

}
