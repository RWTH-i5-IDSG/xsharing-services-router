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

import de.rwth.idsg.xsharing.router.iv.request.DistanceMatrixRequest;
import de.rwth.idsg.xsharing.router.iv.request.ShortestPathsRequest;
import de.rwth.idsg.xsharing.router.iv.response.DistanceMatrixResult;
import de.rwth.idsg.xsharing.router.iv.response.IVRouterResponse;
import de.rwth.idsg.xsharing.router.iv.response.IVRouterResult;
import de.rwth.idsg.xsharing.router.iv.response.ShortestPathsResult;
import de.rwth.idsg.xsharing.router.utils.JsonMapper;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.ServerErrorException;
import java.util.List;

import static de.rwth.idsg.xsharing.router.AppConfiguration.CONFIG;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
@Slf4j
public class IVRouterClientImpl implements IVRouterClient {

    private final IVRouterService routerService;

    public IVRouterClientImpl(IVRouterService routerService) {
        this.routerService = routerService;
    }

    @Override
    public ShortestPathsResult getShortestPaths(ShortestPathsRequest request) throws ServerErrorException {
        log.debug("Got request for shortest paths.");

        IVRouterResponse response = getShortestPathsInternal(request);

        List<IVRouterResult> results = response.getResults();
        // we can expect the response to contain exactly one element
        if (results != null && results.size() == 1) {
            return (ShortestPathsResult) results.get(0);
        }

        return null;
    }

    @Override
    public DistanceMatrixResult getDistanceMatrix(DistanceMatrixRequest request) {
        log.debug("Got request for distance matrix.");

        IVRouterResponse response = getDistanceMatrixInternal(request);

        List<IVRouterResult> results = response.getResults();
        if (results != null && results.size() == 1) {
            return (DistanceMatrixResult) results.get(0);
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // Retry logic
    // -------------------------------------------------------------------------

    private IVRouterResponse getShortestPathsInternal(ShortestPathsRequest request) {

        // dirty, mapping the points object to json so it can be used in urlencoded request for IVU REST...
        String points = JsonMapper.serializeOrThrow(request.getPoints());

        int counter = 0;

        for (;;) {
            try {
                counter++;
                return routerService.getShortestPaths(request.getMode(),
                                                      request.getVehicleId(),
                                                      request.getDistanceTimeWeighting(),
                                                      request.getCreateRoutes(),
                                                      points);
            } catch (Exception e) {
                checkRetry(counter, e);
            }
        }
    }

    private IVRouterResponse getDistanceMatrixInternal(DistanceMatrixRequest request) {

        String startPoints = JsonMapper.serializeOrThrow(request.getStartPoints());
        String endPoints = JsonMapper.serializeOrThrow(request.getEndPoints());

        int counter = 0;

        for (;;) {
            try {
                counter++;
                return routerService.getDistanceMatrix(request.getVehicleId(),
                                                       request.getDistanceTimeWeighting(),
                                                       startPoints,
                                                       endPoints);

            } catch (Exception e) {
                checkRetry(counter, e);
            }
        }
    }

    private void checkRetry(int counter, Exception e) {
        if (counter >= CONFIG.getIvMaxRetryCount()) {
            log.warn("Maximum tries ({}) reached for client http pool. Giving up!", CONFIG.getIvMaxRetryCount());
            throw new RuntimeException(e);
        }

        log.warn("Exception '{}: {}' during communication with server on {} call. Will retry...",
                e.getClass().getSimpleName(),
                e.getMessage(), counter
        );
    }

}
