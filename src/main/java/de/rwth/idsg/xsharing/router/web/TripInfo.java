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
package de.rwth.idsg.xsharing.router.web;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.rwth.idsg.xsharing.router.core.routing.response.CompactResponse;
import de.rwth.idsg.xsharing.router.core.routing.response.DetailsResponse;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.representation.route.RouteMinimalRepresentation;
import lombok.Builder;
import lombok.Getter;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 06.06.2016
 */
@JsonPropertyOrder({"route", "compact", "details"})
@Getter
public class TripInfo {

    private final Wrapper<RouteMinimalRepresentation> route;
    private final Wrapper<CompactResponse> compact;
    private final Wrapper<DetailsResponse> details;

    @Builder
    public TripInfo(RouteMinimalRepresentation route,
                    CompactResponse compact,
                    DetailsResponse details) {

        this.route = new Wrapper<>(route);
        this.compact = new Wrapper<>(compact);
        this.details = new Wrapper<>(details);
    }

    @Getter
    private static class Wrapper<T> {
        private final T item;
        private final String itemString;

        public Wrapper(T item) {
            this.item = item;
            this.itemString = item.toString();
        }
    }
}
