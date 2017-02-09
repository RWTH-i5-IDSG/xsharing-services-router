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
import de.rwth.idsg.xsharing.router.persistence.DatabaseException;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.ComputationLog;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.ComputationLog_;
import org.joda.time.DateTime;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public class ComputationLogRepositoryImpl extends AbstractRepository implements ComputationLogRepository {

    @PersistenceContext(unitName = DatabaseConstants.DB_NAME_XSHARING)
    private EntityManager entityManager;

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public void insert(Date timeStamp) throws DatabaseException {
        ComputationLog log = new ComputationLog(timeStamp);
        this.saveLog(log);
    }

    @Override
    public ComputationLog saveLog(ComputationLog log) throws DatabaseException {
        return this.save(log);
    }

    @Override
    public ComputationLog findLog(DateTime timestamp) throws DatabaseException {
        // find log by time stamp
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ComputationLog> c = cb.createQuery(ComputationLog.class);
        Root<ComputationLog> log = c.from(ComputationLog.class);
        c.where(cb.equal(log.get(ComputationLog_.timestamp), timestamp));
        TypedQuery<ComputationLog> query = entityManager.createQuery(c);

        try {
            return query.getSingleResult();
        } catch (RuntimeException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public ComputationLog findAny() throws DatabaseException {
        // optimistic: just return the first log we find!
        List<ComputationLog> logs = findAll(ComputationLog.class);
        return logs.isEmpty() ? null : logs.get(0);
    }
}
