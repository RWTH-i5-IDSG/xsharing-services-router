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

import de.rwth.idsg.xsharing.router.core.routing.ServerStatus;
import de.rwth.idsg.xsharing.router.core.routing.request.SharingRequest;
import de.rwth.idsg.xsharing.router.core.routing.response.RouterError;
import de.rwth.idsg.xsharing.router.core.routing.response.SharingResponse;

import java.util.Collections;
import java.util.List;

/**
 * General SharingRequest processor interface
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public interface RequestProcessor<A extends SharingRequest, B extends SharingResponse> {

    List<B> process(A request);

    B buildSingleError(RouterError.ErrorCode err, String msg);

    void setWorker(ServerStatus status);

    // -------------------------------------------------------------------------
    // Default implementations (for convenience)
    // -------------------------------------------------------------------------

    default List<B> buildError(RouterError.ErrorCode err, String msg) {
        return Collections.singletonList(buildSingleError(err, msg));
    }

    default List<B> noRouteFoundList() {
        return Collections.singletonList(noRouteFound());
    }

    default B noRouteFound() {
        return buildSingleError(RouterError.ErrorCode.NO_ROUTE_FOUND, "Route construction impossible!");
    }

    default List<B> notReady(ServerStatus status) {
        return buildError(RouterError.ErrorCode.NOT_READY, "Server not in serving state. Currently " + status);
    }
}
