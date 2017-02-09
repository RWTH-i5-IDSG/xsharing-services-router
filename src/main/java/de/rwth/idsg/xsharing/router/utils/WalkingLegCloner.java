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
package de.rwth.idsg.xsharing.router.utils;

import com.google.common.base.Strings;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.WalkingLeg;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.iv.model.SpatialReference;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 30.03.2016
 */
@Slf4j
public final class WalkingLegCloner {

    private WalkingLegCloner() { }

    public static WalkingLeg cloneAndReverse(WalkingLeg origin) {
        if (origin == null) {
            return null;
        }

        WalkingLeg clone = new WalkingLeg();

        clone.setId(origin.getId());
        clone.setType(origin.getType());
        clone.setDistance(origin.getDistance());
        clone.setDuration(origin.getDuration());
        clone.setSpatialReference(clone(origin.getSpatialReference()));
        clone.setStayTime(origin.getStayTime());

        // This, unfortunately can be null (DB entries contain no path!)
        // So, we have to check
        //
        String path = origin.getPath();
        if (!Strings.isNullOrEmpty(path)) {
            clone.setPath(path);
        }

        // Copy with reversed values
        //
        clone.setFrom(clone(origin.getTo()));
        clone.setTo(clone(origin.getFrom()));
        clone.setReversed(!origin.isReversed());

        return clone;
    }

    private static GeoCoord clone(GeoCoord origin) {
        if (origin == null) {
            return null;
        }

        GeoCoord clone = new GeoCoord();
        clone.setX(origin.getX());
        clone.setY(origin.getY());
        return clone;
    }

    /**
     * SpatialReference is an Object (reference) type with only one final primitive field (ok not really but Integer
     * does not count). Therefore, we cannot create a new instance and set the wkid field to the value of the original.
     * So, "new SpatialReference()" hopefully should do fine. But in order to guarantee that the wkid field has the
     * same value, we check it and log the unexpected change.
     */
    private static SpatialReference clone(SpatialReference origin) {
        if (origin == null) {
            return null;
        }

        SpatialReference clone = new SpatialReference();

        if (!Objects.equals(origin.getWkid(), clone.getWkid())) {
            log.warn(
                    "wkid field of the original SpatialReference changed. Expected value was {}, but is {}",
                    clone.getWkid(), origin.getWkid()
            );
        }

        return clone;
    }
}
