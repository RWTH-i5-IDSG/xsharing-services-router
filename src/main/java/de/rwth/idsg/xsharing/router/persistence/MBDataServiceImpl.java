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
package de.rwth.idsg.xsharing.router.persistence;

import de.rwth.idsg.xsharing.router.persistence.domain.mb.MBPlaceEntity;
import de.rwth.idsg.xsharing.router.persistence.domain.mb.MBPlaceInavailability;
import de.rwth.idsg.xsharing.router.persistence.domain.station.VehicleStatus;
import de.rwth.idsg.xsharing.router.persistence.domain.station.sharing.SharingStation;
import de.rwth.idsg.xsharing.router.persistence.domain.util.ConverterUtil;
import de.rwth.idsg.xsharing.router.persistence.repository.MBDataRepository;
import de.rwth.idsg.xsharing.router.persistence.repository.StationRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Interval;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Stateless
@Slf4j
public class MBDataServiceImpl implements MBDataService {

    @Inject private MBDataRepository mbDataRepository;
    @Inject private StationRepository stationRepository;

    // retrieve sharing data from mobility broker DB
    public void importMBData() {
        List<MBPlaceEntity> mbBikeStations = mbDataRepository.getAllIXSIBikeStations();
        log.info("Retrieved bike station list from MobilityBroker");
        log.debug("list: {}", mbBikeStations);
        List<SharingStation> bikeStations = mbBikeStations.stream()
                                                          .map(ConverterUtil::toBikeStation)
                                                          .collect(Collectors.toList());
        log.debug("Successfully converted places to local bike station representation");
        stationRepository.saveSharingStationList(bikeStations);

        List<MBPlaceEntity> mbCarStations = mbDataRepository.getAllIXSICarStations();
        log.info("Retrieved car station list from MobilityBroker");
        log.debug("list: {}", mbCarStations);
        List<SharingStation> carStations = mbCarStations.stream()
                                                        .map(ConverterUtil::toCarStation)
                                                        .collect(Collectors.toList());
        log.debug("Successfully converted places to local car station representation");
        stationRepository.saveSharingStationList(carStations);
    }

    @Override
    public List<SharingStation> updateAvailabilities() {

        List<MBPlaceEntity> mbBikeStations = mbDataRepository.getAllIXSIBikeStations();
        List<MBPlaceEntity> mbCarStations = mbDataRepository.getAllIXSICarStations();

        Map<String, Map<String, List<MBPlaceInavailability>>> map =
                mbDataRepository.getInavailabilities()
                                .stream()
                                .collect(Collectors.groupingBy(MBPlaceInavailability::getProviderId,
                                         Collectors.groupingBy(MBPlaceInavailability::getPlaceId)));

        int size = mbBikeStations.size() + mbCarStations.size();
        List<SharingStation> stations = new ArrayList<>(size);

        for (MBPlaceEntity mbStation : mbBikeStations) {
            performUpdate(mbStation);
            stations.add(ConverterUtil.toBikeStation(mbStation, getInavail(map, mbStation)));
        }

        for (MBPlaceEntity mbStation : mbCarStations) {
            performUpdate(mbStation);
            stations.add(ConverterUtil.toCarStation(mbStation, getInavail(map, mbStation)));
        }

        return stations;
    }

    private List<VehicleStatus> getInavail(Map<String, Map<String, List<MBPlaceInavailability>>> map,
                                           MBPlaceEntity s) {

        // Get all booking target inavailabilities at a station
        Map<String, List<MBPlaceInavailability>> forAPlace =
                map.getOrDefault(s.getProviderId(), Collections.emptyMap())
                   .getOrDefault(s.getPlaceId(), Collections.emptyList())
                   .stream()
                   .collect(Collectors.groupingBy(MBPlaceInavailability::getBookingTargetId));

        List<VehicleStatus> vehicleStatusList = new ArrayList<>(forAPlace.size());
        for (Map.Entry<String, List<MBPlaceInavailability>> entry : forAPlace.entrySet()) {

            String bookingTargetId = entry.getKey();
            List<MBPlaceInavailability> values = entry.getValue();

            List<Interval> intervalList = new ArrayList<>(values.size());
            for (MBPlaceInavailability item : values) {
                Interval interval = item.getInavailability();
                if (interval != null) {
                    intervalList.add(interval);
                }
            }

            vehicleStatusList.add(new VehicleStatus(bookingTargetId, intervalList));
        }

        return vehicleStatusList;
    }

    private void performUpdate(MBPlaceEntity station) {
        stationRepository.updateSharingStationAvailability(
                station.getPlaceId(),
                station.getProviderId(),
                station.getAvailableCapacity(),
                station.getAvailableVehicles()
        );
    }

    @Override
    public void subscribeAvailabilities() {
        // TODO actually use IXSI client instead of direct DB access?
        // maybe register scheduler for periodic updates here for now?
        log.warn("Subscription to IXSI service not implemented! (using direct connection!)");
    }
}
