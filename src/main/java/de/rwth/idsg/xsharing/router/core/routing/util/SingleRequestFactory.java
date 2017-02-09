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
package de.rwth.idsg.xsharing.router.core.routing.util;

import de.rwth.idsg.xsharing.router.core.routing.SharingStationType;
import de.rwth.idsg.xsharing.router.core.routing.request.MinimalRequest;
import de.rwth.idsg.xsharing.router.core.routing.request.SingleMinimalRequest;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.utils.StationClassUtils;
import de.rwth.idsg.xsharing.router.utils.BasicUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
public final class SingleRequestFactory {

    /**
     * From the list sets in the request generate a list of single requests for handling with iv service.
     * Assume here that the request has been successfully validated!
     *
     * @param request the validated multi-point request
     * @return A list of single requests for input into iv service
     */
    public static List<SingleMinimalRequest> getSingleRequestList(MinimalRequest request) {
        ListIterator<DateTime> times = request.getTime().listIterator();

        boolean isArrivalTime = BasicUtils.getTypeSafeBoolean(request.getIsArrivalTime());
        boolean isLastLeg = BasicUtils.getTypeSafeBoolean(request.getIsLastLeg());
        boolean isWithReturn = BasicUtils.getTypeSafeBoolean(request.getIsWithReturn());

        Integer stayTime = BasicUtils.getIntegerOrZero(request.getStayTime());
        Double maxWalkDistance = request.getMaxWalkDistance();
        String[] modes = request.getModes();

        DateTime time = times.next();
        ListIterator<GeoCoord> theIterator;
        GeoCoord otherPoint;
        boolean manyStartCoords;
        int finalListSize;

        // determine if we iterate start or end points
        // we assume here that the lists are correctly validated and can be used as described in interface
        //
        if (request.getStartPoint().size() > 1) {
            finalListSize = request.getStartPoint().size();
            theIterator = request.getStartPoint().listIterator();
            otherPoint = request.getEndPoint().get(0);
            manyStartCoords = true;

        // the other case must be true
        //
        } else {
            finalListSize = request.getEndPoint().size();
            theIterator = request.getEndPoint().listIterator();
            otherPoint = request.getStartPoint().get(0);
            manyStartCoords = false;
        }

        Optional<List<SharingStationType>> stationTypes = StationClassUtils.getStationTypesList(modes);

        List<SingleMinimalRequest> results = new ArrayList<>(finalListSize);

        while (theIterator.hasNext()) {
            // if we have multiple times iterate, else always use the one we have!
            GeoCoord point = theIterator.next();

            SingleMinimalRequest single
                    = manyStartCoords
                    ? new SingleMinimalRequest(point, otherPoint, time, isArrivalTime, isLastLeg, isWithReturn, stayTime, maxWalkDistance, stationTypes)
                    : new SingleMinimalRequest(otherPoint, point, time, isArrivalTime, isLastLeg, isWithReturn, stayTime, maxWalkDistance, stationTypes);

            results.add(single);

            if (times.hasNext()) {
                time = times.next();
            }
        }

        return results;
    }
}
