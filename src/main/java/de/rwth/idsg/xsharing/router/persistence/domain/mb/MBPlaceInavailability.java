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
package de.rwth.idsg.xsharing.router.persistence.domain.mb;

import lombok.Getter;
import lombok.Setter;
import org.joda.time.Interval;

import javax.annotation.Nullable;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 23.05.2016
 */
@Getter
@Setter
public class MBPlaceInavailability {

    private String providerId;
    private String placeId;
    private String bookingTargetId;
    private Interval inavailability;

    @Nullable
    public Interval getInavailability() {
        return inavailability;
    }

    public void setInavailability(@Nullable Interval inavailability) {
        this.inavailability = inavailability;
    }
}
