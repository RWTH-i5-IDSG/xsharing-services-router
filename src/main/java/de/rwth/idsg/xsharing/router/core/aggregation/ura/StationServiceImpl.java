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
package de.rwth.idsg.xsharing.router.core.aggregation.ura;

import com.vividsolutions.jts.geom.Point;
import de.ivu.realtime.modules.ura.data.request.UraRequestParseException;
import de.ivu.realtime.modules.ura.data.response.StopPoint;
import de.rwth.idsg.xsharing.router.persistence.domain.station.transit.BusStation;
import de.rwth.idsg.xsharing.router.persistence.domain.util.JTSUtil;
import de.rwth.idsg.xsharing.router.persistence.repository.StationRepository;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Realization of public transit stations import using URA2 interface
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Stateless
@Slf4j
public class StationServiceImpl implements StationService {

    @Inject private AseagClient aseagClient;
    @Inject private StationRepository stationRepository;

    @Override
    public void importStopPoints() {
        try {
            List<StopPoint> stops = aseagClient.getAseagStops();

            List<BusStation> stations = stops.stream()
                                             .map(StationServiceImpl::toBusStation)
                                             .collect(Collectors.toList());

            stationRepository.savePTStationList(stations);

        } catch (UraRequestParseException e) {
            log.error("Can not fetch stations from ASEAG using URA2 client. Aborting! {}", e.getLocalizedMessage());
        }
    }

    /**
     * Convenience function, converts URA stop point to pt station
     */
    private static BusStation toBusStation(StopPoint stopPoint) {
        Point geoPos = JTSUtil.getPoint(stopPoint.getLongitude(), stopPoint.getLatitude());
        return new BusStation(stopPoint.getStopPointId(), stopPoint.getStopPointName(), geoPos);
    }
}
