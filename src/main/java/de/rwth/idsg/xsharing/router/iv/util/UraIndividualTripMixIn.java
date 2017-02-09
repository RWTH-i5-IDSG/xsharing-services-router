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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.ivu.realtime.modules.ura.data.ModalType;
import org.joda.time.Duration;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public abstract class UraIndividualTripMixIn {

    @JsonSerialize(using = UraModalTypeWrapper.class)
    abstract ModalType getModalType();

    @JsonProperty("durationInS")
    @JsonSerialize(using = DurationSecondsSerializer.class)
    abstract Duration getDuration();

}
