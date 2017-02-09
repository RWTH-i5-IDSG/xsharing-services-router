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
package de.rwth.idsg.xsharing.router.core.routing.strategy.route;

import de.rwth.idsg.xsharing.router.core.routing.request.SingleMinimalRequest;
import de.rwth.idsg.xsharing.router.core.routing.util.RouteBuilderException;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteCompactRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteDetailsRepresentation;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteMinimalRepresentation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public interface RouteBuilderStrategy {
    @Nullable RouteMinimalRepresentation createRoute(SingleMinimalRequest request) throws RouteBuilderException;
    @Nullable RouteCompactRepresentation getCompact(@Nonnull RouteMinimalRepresentation min) throws RouteBuilderException;
    @Nullable RouteDetailsRepresentation getDetails(@Nonnull RouteMinimalRepresentation min) throws RouteBuilderException;
}
