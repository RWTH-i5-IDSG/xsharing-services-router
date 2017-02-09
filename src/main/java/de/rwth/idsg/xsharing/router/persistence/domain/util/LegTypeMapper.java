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
package de.rwth.idsg.xsharing.router.persistence.domain.util;

import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.BikeLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.CarLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.LegType;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.RouteLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.WalkingLeg;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;

import static de.rwth.idsg.xsharing.router.Constants.IVRouterConfig.BIKE;
import static de.rwth.idsg.xsharing.router.Constants.IVRouterConfig.PEDESTRIAN_NORMAL;
import static de.rwth.idsg.xsharing.router.Constants.IVRouterConfig.PKW;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public enum LegTypeMapper {
    SINGLETON;

    private final HashMap<Enum, Pair<String, Class<? extends RouteLeg>>> lookUp = new HashMap<>(3);

    LegTypeMapper() {
        lookUp.put(LegType.BikeLeg, Pair.of(BIKE, BikeLeg.class));
        lookUp.put(LegType.CarLeg, Pair.of(PKW, CarLeg.class));
        lookUp.put(LegType.WalkingLeg, Pair.of(PEDESTRIAN_NORMAL, WalkingLeg.class));
    }

    public Pair<String, Class<? extends RouteLeg>> getInfo(LegType type) {
        return lookUp.get(type);
    }

}
