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
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

import static de.rwth.idsg.xsharing.router.Constants.DatabaseConstants.BATCH_SIZE;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
@Slf4j
public abstract class AbstractRepository {

    public abstract EntityManager getEntityManager();

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    protected <T> T save(T entity) throws DatabaseException {
        try {
            getEntityManager().persist(entity);
            return entity;
        } catch (RuntimeException re) {
            throw new DatabaseException("Saving of entity " + entity + " failed! " + re.getMessage());
        }

    }

    protected <T> T refreshEntry(T entity) throws DatabaseException{
        try {
            getEntityManager().flush();
            getEntityManager().refresh(entity);
            return entity;
        } catch (EJBTransactionRolledbackException | HibernateException | PersistenceException e) {
            throw new DatabaseException("Refresh of entity " + entity.toString() + " failed. " + e.getMessage());
        }
    }

    protected <T> void saveBatch(List<T> entities) throws DatabaseException {
        Session session = null;
        Transaction tx = null;

        try {
            session = getEntityManager().unwrap(Session.class);
            // session.setCacheMode(CacheMode.IGNORE);
            tx = session.beginTransaction();

            int counter = 0;
            for (T p : entities) {
                session.persist(p);
                if (++counter % BATCH_SIZE == 0) {
                    // flush a batch of inserts and release memory
                    session.flush();
                    session.clear();
                }
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (RuntimeException rbe) {
                    log.error("Couldn't roll back transaction", rbe);
                }
            }
            throw new DatabaseException("Batch saving of entity list failed: " + e.getMessage());

        } finally {
            if (session != null && session.isOpen()) {
                try {
                    session.close();
                } catch (HibernateException e) {
                    log.error("Error while trying to close the session", e);
                }
            }
        }
    }

    protected <T> List<T> saveList(List<T> entities) throws DatabaseException {
        try {
            entities.forEach(t -> {
                try {
                    t = save(t);
                } catch (DatabaseException dbe) {
                    log.error(dbe.getMessage());
                }
            });
            return entities;
        } catch (Exception e) {
            throw new DatabaseException("Saving of entity list failed:" + e.getMessage());
        }
    }

    protected <T> T find(String idField, Long id, Class clazz) throws DatabaseException {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<T> c = cb.createQuery(clazz);
            Root<T> root = c.from(clazz);
            CriteriaQuery<T> one = c.select(root).where(cb.equal(root.get(idField), id));
            TypedQuery<T> oneQuery = getEntityManager().createQuery(one);

            return oneQuery.getSingleResult();
        } catch (EJBTransactionRolledbackException | PersistenceException e) {
            throw new DatabaseException("Could not find entity of class " + clazz.toString() + ", " + e.getMessage());
        }
    }

    protected  <T> List<T> findAll(Class clazz) throws DatabaseException {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<T> c = cb.createQuery(clazz);
            Root<T> root = c.from(clazz);
            CriteriaQuery<T> all = c.select(root);
            TypedQuery<T> allQuery = getEntityManager().createQuery(all);

            return allQuery.getResultList();
        } catch (EJBTransactionRolledbackException | PersistenceException e) {
            throw new DatabaseException("Could not find entity of class " + clazz.toString() + ", " + e.getMessage());
        }
    }

}
