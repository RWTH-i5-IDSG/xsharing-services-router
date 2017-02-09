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
package de.rwth.idsg.xsharing.router.iv;

import de.rwth.idsg.xsharing.router.Constants.IVRouterConfig;
import de.rwth.idsg.xsharing.router.iv.response.IVRouterResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Produces(MediaType.APPLICATION_FORM_URLENCODED)
@Consumes(MediaType.APPLICATION_JSON)
public interface IVRouterService {

    @POST
    @Path(IVRouterConfig.SHORTEST_PATHS)
    IVRouterResponse getShortestPaths(@FormParam(IVRouterConfig.MODE) String mode,
                                      @FormParam(IVRouterConfig.VEHICLE_ID) String vehicleId,
                                      @FormParam(IVRouterConfig.DISTANCE_TIME_WEIGHTING) Double distanceTimeWeighting,
                                      @FormParam(IVRouterConfig.CREATE_ROUTES) Boolean createRoutes,
                                      @FormParam(IVRouterConfig.POINTS) String point);

    @POST
    @Path(IVRouterConfig.DISTANCE_MATRIX)
    IVRouterResponse getDistanceMatrix(@FormParam(IVRouterConfig.VEHICLE_ID) String vehicleId,
                                       @FormParam(IVRouterConfig.DISTANCE_TIME_WEIGHTING) Double distanceTimeWeighting,
                                       @FormParam(IVRouterConfig.START_POINTS) String startPoints,
                                       @FormParam(IVRouterConfig.END_POINTS) String endPoints);

}
