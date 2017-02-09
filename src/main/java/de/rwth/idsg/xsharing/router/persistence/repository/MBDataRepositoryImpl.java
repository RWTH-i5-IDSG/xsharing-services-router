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

import de.rwth.idsg.xsharing.router.Constants.DatabaseConstants;
import de.rwth.idsg.xsharing.router.persistence.domain.mb.MBPlaceEntity;
import de.rwth.idsg.xsharing.router.persistence.domain.mb.MBPlaceInavailability;
import de.rwth.idsg.xsharing.router.utils.BasicUtils;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Interval;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static de.rwth.idsg.xsharing.router.persistence.domain.util.ConverterUtil.getInterval;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
@Slf4j
@Stateless
public class MBDataRepositoryImpl extends AbstractRepository implements MBDataRepository {

    @PersistenceContext(unitName = DatabaseConstants.DB_NAME_MB)
    private EntityManager entityManager;

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    private static final String SELECTION = "SELECT p.place_id, p.global_id, p.capacity, " +
            "p.on_premises_time_in_seconds, p.provider_id, p.available_capacity, pn.value AS name, " +
            "CAST(p.gps_position AS VARCHAR(255)), available.available_vehicles " +
            "FROM ixsi.place p " +
            "LEFT JOIN ixsi.place_attribute pa ON p.place_id = pa.place_id " +
            "LEFT JOIN ixsi.attribute a ON pa.attribute_id = a.attribute_id " +
            "LEFT JOIN ixsi.place_name pn ON p.place_id = pn.place_id " +
            "LEFT JOIN (" +
                "SELECT p2.place_id, p2.provider_id, count(btsp.place_id) as available_vehicles " +
                "FROM ixsi.place p2 " +
                "JOIN ixsi.placegroup_place pgp " +
                    "ON p2.place_id = pgp.place_id " +
                "LEFT OUTER JOIN ixsi.booking_target bt " +
                    "ON pgp.placegroup_id = bt.exclusive_to_placegroup_id " +
                    "AND bt.status = 'ACTIVE' " +
                "LEFT OUTER JOIN ixsi.booking_target_status_place btsp " +
                    "ON bt.booking_target_id = btsp.booking_target_id AND bt.provider_id = btsp.provider_id " +
                    "AND bt.provider_id = btsp.provider_id " +
                    "AND p2.place_id = btsp.place_id " +
                "LEFT OUTER JOIN ixsi.booking_target_status_inavailability btsi " +
                    "ON btsi.booking_target_id = btsp.booking_target_id " +
                "WHERE btsi.booking_target_id ISNULL " +
                "GROUP BY p2.place_id " +
            "UNION " +
                "SELECT p3.place_id, p3.provider_id, count(av) AS available_vehicles " +
                "FROM ixsi.place p3 " +
                "LEFT OUTER JOIN ixsi.booking_target bt2 " +
                "  ON p3.place_id = bt2.exclusive_to_place_id " +
                "  AND bt2.status = 'ACTIVE' " +
                "LEFT JOIN ( " +
                    "SELECT bt2.booking_target_id " +
                    "FROM ixsi.booking_target bt2 " +
                    "LEFT JOIN ixsi.booking_target_status_inavailability btsi2 " +
                    "ON bt2.booking_target_id = btsi2.booking_target_id " +
                    "WHERE btsi2.booking_target_id ISNULL " +
                ") AS av " +
                "ON av.booking_target_id = bt2.booking_target_id " +
                "WHERE bt2.exclusive_to_place_id NOTNULL " +
                "GROUP BY p3.place_id" +
            ") available " +
                "ON available.place_id = p.place_id " +
                "AND available.provider_id = p.provider_id " +
            "WHERE a.class = :clazz";

    private static final String INAVAILABILITY_SELECTION =
            "WITH place_dependent AS ( " +
                    "SELECT " +
                        "bt.provider_id, " +
                        "bt.exclusive_to_place_id AS place_id, " +
                        "bt.booking_target_id, " +
                        "lower(si.inavailability) AS inavail_from, " +
                        "upper(si.inavailability) AS inavail_to " +
                    "FROM ixsi.booking_target bt " +
                    "LEFT JOIN ixsi.booking_target_status_inavailability si " +
                        "ON bt.booking_target_id = si.booking_target_id " +
                        "AND bt.provider_id = si.provider_id " +
                        "AND (si.inavailability IS NOT NULL OR si.inavailability != 'empty') " +
                    "WHERE bt.exclusive_to_place_id IS NOT NULL)," +
                 "place_independent AS ( " +
                    "SELECT " +
                        "p.provider_id, " +
                        "p.place_id, " +
                        "p.booking_target_id, " +
                        "lower(si.inavailability) AS inavail_from, " +
                        "upper(si.inavailability) AS inavail_to " +
                    "FROM ixsi.booking_target_status_place p " +
                    "LEFT JOIN ixsi.booking_target_status_inavailability si " +
                        "ON p.booking_target_id = si.booking_target_id " +
                        "AND p.provider_id = si.provider_id " +
                        "AND (si.inavailability IS NOT NULL OR si.inavailability != 'empty')) " +
                 "SELECT * FROM place_dependent UNION ALL (SELECT * FROM place_independent)";


    private static final String CAR = "car_sharing";
    private static final String BIKE = "bike_sharing";

    @Override
    @Transactional
    public List<MBPlaceEntity> getAllIXSIBikeStations() {
        log.debug("getting IXSI bike sharing stations.");

        Query q = entityManager.createNativeQuery(SELECTION, MBPlaceEntity.class)
                               .setParameter("clazz", BIKE);

        List<MBPlaceEntity> places = q.getResultList();

        return places;
    }

    @Override
    @Transactional
    public List<MBPlaceEntity> getAllIXSICarStations() {
        log.debug("getting IXSI car sharing stations.");

        Query q = entityManager.createNativeQuery(SELECTION, MBPlaceEntity.class)
                               .setParameter("clazz", CAR);

        List<MBPlaceEntity> places = q.getResultList();

        return places;
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public List<MBPlaceInavailability> getInavailabilities() {
        log.debug("getting IXSI station inavailabilities");

        Query query = entityManager.createNativeQuery(INAVAILABILITY_SELECTION);

        List<Object[]> rows = query.getResultList();
        List<MBPlaceInavailability> inavails = new ArrayList<>(rows.size());

        for (Object[] row : rows) {
            MBPlaceInavailability in = rowToInavail(row);
            if (in != null) {
                inavails.add(in);
            }
        }

        return inavails;
    }

    @Nullable
    private MBPlaceInavailability rowToInavail(Object[] row) {
        try {
            Interval interval = getInterval(BasicUtils.getTypeSafeString(row[3]),
                                            BasicUtils.getTypeSafeString(row[4]));

            MBPlaceInavailability in = new MBPlaceInavailability();
            in.setProviderId(BasicUtils.getTypeSafeString(row[0]));
            in.setPlaceId(BasicUtils.getTypeSafeString(row[1]));
            in.setBookingTargetId(BasicUtils.getTypeSafeString(row[2]));
            in.setInavailability(interval);
            return in;
        } catch (Exception e) {
            return null;
        }
    }

}
