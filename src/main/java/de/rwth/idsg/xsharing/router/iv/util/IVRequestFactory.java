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

import de.rwth.idsg.xsharing.router.iv.model.EsriPoint;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.iv.request.DistanceMatrixRequest;
import de.rwth.idsg.xsharing.router.iv.request.ShortestPathsRequest;
import de.rwth.idsg.xsharing.router.Constants;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public class IVRequestFactory {

    public static ShortestPathsRequest getSPRequest(String mode, EsriPoint point) {
        return ShortestPathsRequest.builder()
                                   .createRoutes(true)
                                   .mode(Constants.IVRouterConfig.MODE_N_X_ONE_TO_ONE)
                                   .vehicleId(mode)
                                   .distanceTimeWeighting(Constants.IVRouterConfig.DISTANCE_TIME_WEIGHTING_VALUE)
                                   .points(point)
                                   .build();
    }

    public static ShortestPathsRequest getSingleSP(String mode, GeoCoord from, GeoCoord to) {
        EsriPoint point = EsriFactory.getSingleRequestPair(from, to);
        return ShortestPathsRequest.builder()
                                   .createRoutes(true)
                                   .mode(Constants.IVRouterConfig.MODE_ONE_TO_ONE)
                                   .vehicleId(mode)
                                   .distanceTimeWeighting(Constants.IVRouterConfig.DISTANCE_TIME_WEIGHTING_VALUE)
                                   .points(point)
                                   .build();
    }

    public static DistanceMatrixRequest getDMXRequest(String mode, EsriPoint startPoints, EsriPoint endPoints) {
        return DistanceMatrixRequest.builder()
                                    .distanceTimeWeighting(Constants.IVRouterConfig.DISTANCE_TIME_WEIGHTING_VALUE)
                                    .vehicleId(mode) // FIXME: mode != vehicleId. Why?
                                    .startPoints(startPoints)
                                    .endPoints(endPoints)
                                    .build();
    }
}
