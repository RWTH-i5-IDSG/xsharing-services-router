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
import de.rwth.idsg.xsharing.router.persistence.domain.raster.RasterPoint;
import de.rwth.idsg.xsharing.router.Constants;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;
import java.util.Collections;
import java.util.List;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Stateless
@Slf4j
@TransactionManagement(TransactionManagementType.BEAN)
public class RasterRepositoryImpl extends AbstractRepository implements RasterRepository {

    @PersistenceContext(unitName = Constants.DatabaseConstants.DB_NAME_XSHARING)
    private EntityManager entityManager;

    @Resource private UserTransaction ut;

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    @Transactional
    public void savePoint(RasterPoint point) throws DatabaseException {
        this.save(point);
    }

    @Override
    @Transactional
    public void savePointsList(List<RasterPoint> points) throws DatabaseException {
//        this.saveList(points);
        saveBatch(points);
    }

    @Override
    public List<RasterPoint> getAllRasterPoints() {
        // set up potentially long running transaction!
        try {
            // TODO refactor out
            ut.setTransactionTimeout(2000);
            ut.begin();

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<RasterPoint> c = cb.createQuery(RasterPoint.class);
            Root<RasterPoint> root = c.from(RasterPoint.class);
            CriteriaQuery<RasterPoint> all = c.select(root);
            TypedQuery<RasterPoint> allQuery = entityManager.createQuery(all);
            List<RasterPoint> resultList = allQuery.getResultList();

            ut.commit();
            return resultList;

        } catch (Exception e) {
            //throw new DatabaseException("Could not load raster from database! Reason: " + e.getMessage());
            log.error("Could not load raster from database! Reason: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public RasterPoint saveOrUpdate(RasterPoint point) {
        return entityManager.merge(point);
    }
}
