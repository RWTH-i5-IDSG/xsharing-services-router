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
package de.rwth.idsg.xsharing.router.core.routing.util;

import de.rwth.idsg.xsharing.router.core.routing.SharingStationType;
import de.rwth.idsg.xsharing.router.core.routing.request.LowerBoundsRequest;
import de.rwth.idsg.xsharing.router.core.routing.request.SingleMinimalRequest;
import de.rwth.idsg.xsharing.router.core.routing.strategy.inavailability.bike.DisabledBikeInavailabilityStrategy;
import de.rwth.idsg.xsharing.router.core.routing.strategy.inavailability.car.DisabledCarInavailabilityStrategy;
import de.rwth.idsg.xsharing.router.core.routing.strategy.minimal.LowerBoundsRouteStrategy;
import de.rwth.idsg.xsharing.router.core.routing.strategy.mode.AbstractModeStrategy;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.utils.StationClassUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * We use LowerBoundsRequest type for incoming requests, but use SingleMinimalRequest internally,
 * since the logic is similar and SingleMinimalRequest is a superset of LowerBoundsRequest feature-wise.
 * Only, we need to fill some fields of SingleMinimalRequest with some dummy values, which should
 * have no effect on correctness.
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 09.03.2016
 */
public final class SingleLowerBoundsRequestFactory {

    public static List<SingleMinimalRequest> toSingleList(LowerBoundsRequest request) {
        GeoCoord start = request.getStartPoint();

        SharingStationType type = StationClassUtils.getStationType(request.getMode());
        if (type == null) {
            throw new RuntimeException("Mode not found");
        }

        List<SharingStationType> types = Collections.singletonList(type);
        Optional<List<SharingStationType>> optional = Optional.of(types);

        List<SingleMinimalRequest> results = new ArrayList<>(request.getEndPoint().size());
        for (GeoCoord end : request.getEndPoint()) {
            results.add(map(start, end, optional));
        }
        return results;
    }

    private static SingleMinimalRequest map(GeoCoord start, GeoCoord end, Optional<List<SharingStationType>> optional) {
        return new SingleMinimalRequest(
                start,
                end,
                DummyConstants.DATETIME,
                DummyConstants.IS_ARRIVAL_TIME,
                DummyConstants.IS_LASTLEG,
                DummyConstants.IS_WITH_RETURN,
                DummyConstants.STAY_TIME,
                DummyConstants.MAX_WALK_DISTANCE,
                optional
        );
    }

    /**
     * We use {@link LowerBoundsRouteStrategy} to process these requests, which is a special case of
     * WithoutReturnAndLastLegStrategy. So, we imply the following:
     *
     * withReturn = false
     * lastLeg    = true
     */
    private static class DummyConstants {

        /**
         * See above
         */
        static final boolean IS_WITH_RETURN = false;

        /**
         * See above
         */
        static final boolean IS_LASTLEG = true;

        /**
         * {@link LowerBoundsRouteStrategy} uses {@link DisabledBikeInavailabilityStrategy} and
         * {@link DisabledCarInavailabilityStrategy} process duration, which use DurationCheck.ONLY_LEG. So,
         * this value actually has no effect with the strategies.
         */
        static final int STAY_TIME = 0;

        /**
         * Used in RouteRepresentationFactory.getMinRep() as boolean type "isArrival"
         * Since both values are examined with an if-else, actual value has no effect
         */
        static final boolean IS_ARRIVAL_TIME = false;

        /**
         * Used in RouteRepresentationFactory.getMinRep() within if-else branches of "isArrival"
         * Is set either to arrival or departure depending on "isArrival", but outgoing response
         * does not use this value! So, it has no effect. But still we cannot set it to null,
         * since the value is read.
         */
        static final DateTime DATETIME = new DateTime(0); // Let's use unix epoch

        /**
         * Used in {@link AbstractModeStrategy#checkMaxWalkingDistance(GeoCoord, GeoCoord, Double)}.
         * We should leave it as null, so that system default value is used.
         *
         */
        static final Double MAX_WALK_DISTANCE = null;
    }
}
