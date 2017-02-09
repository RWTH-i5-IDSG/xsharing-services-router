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
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.persistence.DatabaseException;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.BikeLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.CarLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.RouteLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.RouteLeg_;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.WalkingLeg;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
@Stateless
@Slf4j
public class RouteLegRepositoryImpl extends AbstractRepository implements RouteLegRepository {

    @PersistenceContext(unitName = DatabaseConstants.DB_NAME_XSHARING)
    private EntityManager entityManager;

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    @Transactional
    public RouteLeg saveRouteLeg(RouteLeg leg) throws DatabaseException {
        save(leg);
        return refreshEntry(leg);
    }

    @Override
    @Transactional
    public WalkingLeg saveWalkingLeg(WalkingLeg leg) throws DatabaseException {
        return save(leg);
    }

    @Override
    @Transactional
    public void saveOrUpdate(RouteLeg leg) {
        RouteLeg dbLeg = entityManager.find(leg.getClass(), leg.getId());
        if (dbLeg != null) {
            dbLeg = leg;
            entityManager.merge(dbLeg);
        } else {
            log.warn("Route with id {} not found in DB, inserting as new...", leg.getId());
            try {
                saveRouteLeg(leg);
            } catch (DatabaseException e) {
                log.error("Fatal: could not save route leg to db: {}", e.getMessage());
            }
        }
    }

    @Override
    public RouteLeg find(Long id, Class<? extends RouteLeg> clazz) throws DatabaseException {
        return super.find("id", id, clazz);
    }

    @Override
    public RouteLeg findLegByFromTo(GeoCoord from, GeoCoord to, Class<? extends RouteLeg> legClazz) throws DatabaseException {
        return findRouteLegByFromToHQL(from, to, legClazz);
    }

    @Override
    public BikeLeg findBikeLegByFromTo(GeoCoord from, GeoCoord to) throws DatabaseException {
        return (BikeLeg) findRouteLegByFromToHQL(from, to, BikeLeg.class);
    }

    @Override
    public CarLeg findCarLegByFromTo(GeoCoord from, GeoCoord to) throws DatabaseException {
        // TODO especially not realistic in cars
        return (CarLeg) findRouteLegByFromToHQL(from, to, CarLeg.class);
    }

    private <T> T extractOne(List<T> resultList ) {
        if (resultList.isEmpty()) {
            return null;

        } else if (resultList.size() > 1) {
            throw new IndexOutOfBoundsException("More than one item found");

        } else {
            return resultList.get(0);
        }
    }

    // -------------------------------------------------------------------------
    // Various 'find' implementations
    // -------------------------------------------------------------------------

    /**
     * Criteria API version of the query
     */
    @SuppressWarnings("unchecked")
    private <T extends RouteLeg> RouteLeg findRouteLegByFromTo(GeoCoord from, GeoCoord to, Class clazz) throws DatabaseException {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> l = cb.createQuery(clazz);
        Root<T> legRoot = l.from(clazz);

        l.where(
                cb.and(
                        cb.equal(legRoot.get(RouteLeg_.from), from),
                        cb.equal(legRoot.get(RouteLeg_.to), to)
                )
        );

        try {
            List<T> resultList = entityManager.createQuery(l).getResultList();
            return extractOne(resultList);
        } catch (IndexOutOfBoundsException e) {
            throw new DatabaseException("Could not find entity " + clazz + " for coordinates " + from + " -> " + to);
        }
    }

    /**
     * HQL version of the query
     */
    private <T extends RouteLeg> RouteLeg findRouteLegByFromToHQL(GeoCoord from, GeoCoord to, Class clazz) throws DatabaseException {
        final String bikeQuery = "SELECT leg from BikeLeg leg WHERE leg.from = :fromGeo AND leg.to = :toGeo";
        final String carQuery = "SELECT leg from CarLeg leg WHERE leg.from = :fromGeo AND leg.to = :toGeo";

        try {
            TypedQuery<? extends RouteLeg> query;
            if (clazz == BikeLeg.class) {
                query = entityManager.createQuery(bikeQuery, BikeLeg.class);
            } else {
                query = entityManager.createQuery(carQuery, CarLeg.class);
            }

            List<? extends RouteLeg> resultList = query.setParameter("fromGeo", from)
                                                       .setParameter("toGeo", to)
                                                       .getResultList();

            return extractOne(resultList);

        } catch (IndexOutOfBoundsException e) {
            throw new DatabaseException("Could not find entity " + clazz + " for coordinates " + from + " -> " + to);
        }
    }

    /**
     * Plain SQL version of the query
     */
    @SuppressWarnings("unchecked")
    private <T extends RouteLeg> RouteLeg findRouteLegByFromToSQL(GeoCoord from, GeoCoord to, Class clazz) throws DatabaseException {
        final String tableName = clazz.getSimpleName();
        final String sql = "SELECT * from " + tableName + " leg WHERE (leg.from_lon, leg.from_lat) = (?, ?) AND (leg.to_lon, leg.to_lat) = (?, ?)";

        try {
            Query query = entityManager.createNativeQuery(sql, clazz);

            // Hibernate's parameter positions start with 1 !!
            // https://docs.jboss.org/hibernate/entitymanager/3.6/reference/en/html/query_native.html
            //
            List<T> resultList = query.setParameter(1, from.getX())
                                      .setParameter(2, from.getY())
                                      .setParameter(3, to.getX())
                                      .setParameter(4, to.getY())
                                      .getResultList();

            return extractOne(resultList);

        } catch (IndexOutOfBoundsException e) {
            throw new DatabaseException("Could not find entity " + clazz + " for coordinates " + from + " -> " + to);
        }
    }
}
