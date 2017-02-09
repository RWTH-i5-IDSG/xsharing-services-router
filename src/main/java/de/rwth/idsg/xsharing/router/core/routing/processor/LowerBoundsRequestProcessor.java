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
package de.rwth.idsg.xsharing.router.core.routing.processor;

import de.rwth.idsg.xsharing.router.core.CoreBootstrapper;
import de.rwth.idsg.xsharing.router.core.RoutingComponentsProvider;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteMinimalRepresentation;
import de.rwth.idsg.xsharing.router.core.routing.ServerStatus;
import de.rwth.idsg.xsharing.router.core.routing.request.LowerBoundsRequest;
import de.rwth.idsg.xsharing.router.core.routing.util.SingleLowerBoundsRequestFactory;
import de.rwth.idsg.xsharing.router.core.routing.request.SingleMinimalRequest;
import de.rwth.idsg.xsharing.router.core.routing.response.LowerBoundsResponse;
import de.rwth.idsg.xsharing.router.core.routing.response.RouterError;
import de.rwth.idsg.xsharing.router.core.routing.serving.SharingRouterServiceImpl;
import de.rwth.idsg.xsharing.router.core.routing.util.RouteBuilderException;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 09.03.2016
 */
@Slf4j
public class LowerBoundsRequestProcessor implements RequestProcessor<LowerBoundsRequest, LowerBoundsResponse> {

    @Inject private CoreBootstrapper bootstrapper;
    @Inject private RoutingComponentsProvider provider;

    private static SharingRouterServiceImpl routerService;
    private static Function<LowerBoundsRequest, List<LowerBoundsResponse>> worker;

    @PostConstruct
    public void init() {
        routerService = provider.getLowerBoundsRouterService();
        setWorker(bootstrapper.getServerStatus());
    }

    @Override
    public List<LowerBoundsResponse> process(LowerBoundsRequest request) {
        return worker.apply(request);
    }

    /**
     * We listen to the server status changes, and switch the implementation according to the change
     */
    @Override
    public synchronized void setWorker(@Observes ServerStatus status) {
        switch (status) {
            case SERVING:
                worker = this::processInternal;
                break;

            case BOOTING:
            case COMPUTING:
                worker = (request) -> notReady(status);
                break;
        }
    }

    @Override
    public LowerBoundsResponse buildSingleError(RouterError.ErrorCode err, String msg) {
        return new LowerBoundsResponse(new RouterError(msg, err));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private List<LowerBoundsResponse> processInternal(LowerBoundsRequest request) {
        return SingleLowerBoundsRequestFactory.toSingleList(request)
                                              .stream()
                                              .parallel()
                                              .unordered()
                                              .map(this::processSingle)
                                              .collect(Collectors.toList());
    }

    private LowerBoundsResponse processSingle(SingleMinimalRequest req) {
        try {
            RouteMinimalRepresentation route = routerService.getRoutesForLowerBounds(req);

            if (route == null) {
                return buildNoRouteFound(req);
            } else {
                return new LowerBoundsResponse(route);
            }
        } catch (RouteBuilderException e) {
            log.debug(e.getMessage());
            return buildUnknownError(req, e.getMessage());

        } catch (Exception e) {
            String msg = "Unforeseen error occurred: " + e.getMessage();
            log.error(msg);
            return buildUnknownError(req, msg);
        }
    }

    private LowerBoundsResponse buildUnknownError(SingleMinimalRequest req, String msg) {
        return buildSingleError(RouterError.ErrorCode.UNKNOWN_ERROR, msg).withFromTo(req);
    }

    private LowerBoundsResponse buildNoRouteFound(SingleMinimalRequest req) {
        return noRouteFound().withFromTo(req);
    }

}
