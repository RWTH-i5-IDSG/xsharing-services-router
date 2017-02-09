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
package de.rwth.idsg.xsharing.router.persistence.repository;

import de.rwth.idsg.xsharing.router.persistence.DatabaseException;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.BikeLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.CarLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.RouteLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.WalkingLeg;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public interface RouteLegRepository {

    RouteLeg saveRouteLeg(RouteLeg leg) throws DatabaseException;
    WalkingLeg saveWalkingLeg(WalkingLeg leg) throws DatabaseException;
    void saveOrUpdate(RouteLeg leg);

    RouteLeg find(Long id, Class<? extends RouteLeg> clazz) throws DatabaseException;

    RouteLeg findLegByFromTo(GeoCoord from, GeoCoord to, Class<? extends RouteLeg> legClazz) throws DatabaseException;
    BikeLeg findBikeLegByFromTo(GeoCoord from, GeoCoord to) throws DatabaseException;
    CarLeg findCarLegByFromTo(GeoCoord from, GeoCoord to) throws DatabaseException;
}
