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
package de.rwth.idsg.xsharing.router.core.routing.batch;

import de.rwth.idsg.xsharing.router.persistence.domain.raster.RasterPoint;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.LegType;
import de.rwth.idsg.xsharing.router.iv.model.EsriPointFeature;
import de.rwth.idsg.xsharing.router.iv.request.IVRequestTuple;

import java.util.List;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public interface BatchManager {
    void startMatrixJob(List<IVRequestTuple> points, LegType legType);
    void startCartesianMatrixJob(List<EsriPointFeature> sourcePoints, LegType legType);
    void startIVMatrixJob(IVRequestTuple request, LegType legType);
    void startNeighborsJob(List<RasterPoint> rasterPoints);

    List<?> getJobSourceSublist(Long jobId, Integer from, Integer to);
    List<EsriPointFeature> getCarTargetList(Long jobId, Integer from, Integer to);
    List<IVRequestTuple> getMatrixSublist(Long jobId, Integer from, Integer to);
    List<RasterPoint> getRasterSublist(Long jobId, Integer from, Integer to);

    void doneJob(long id);
}
