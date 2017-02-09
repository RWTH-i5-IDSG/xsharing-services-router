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
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteDetailsRepresentation;
import de.rwth.idsg.xsharing.router.core.routing.ServerStatus;
import de.rwth.idsg.xsharing.router.core.routing.request.DetailsRequest;
import de.rwth.idsg.xsharing.router.core.routing.response.DetailsResponse;
import de.rwth.idsg.xsharing.router.core.routing.response.RouterError;
import de.rwth.idsg.xsharing.router.core.routing.serving.SharingRouterServiceImpl;
import de.rwth.idsg.xsharing.router.core.routing.util.RouteBuilderException;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
@Slf4j
public class DetailsRequestProcessor implements RequestProcessor<DetailsRequest, DetailsResponse> {

    @Inject private CoreBootstrapper bootstrapper;
    @Inject private RoutingComponentsProvider provider;

    private static SharingRouterServiceImpl routerService;
    private static Function<DetailsRequest, List<DetailsResponse>> worker;

    @PostConstruct
    public void init() {
        routerService = provider.getRouterService();
        setWorker(bootstrapper.getServerStatus());
    }

    @Override
    public List<DetailsResponse> process(DetailsRequest request) {
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
    public DetailsResponse buildSingleError(RouterError.ErrorCode err, String msg) {
        return new DetailsResponse(new RouterError(msg, err));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private List<DetailsResponse> processInternal(DetailsRequest request) {
        try {
            RouteDetailsRepresentation route = routerService.getRoutes(request);

            if (route == null) {
                return noRouteFoundList();
            } else {
                return Collections.singletonList(new DetailsResponse(route));
            }
        } catch (RouteBuilderException e) {
            String msg = "Route construction impossible! " + e.getMessage();
            log.error(msg, e);
            return buildError(RouterError.ErrorCode.NO_ROUTE_FOUND, msg);

        } catch (Exception e) {
            String msg = "Unforeseen error occurred: " + e.getMessage();
            log.error(msg, e);
            return buildError(RouterError.ErrorCode.UNKNOWN_ERROR, msg);
        }
    }
}
