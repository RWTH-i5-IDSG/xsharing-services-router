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
package de.rwth.idsg.xsharing.router.core.routing.batch.request.distancematrix;

import de.rwth.idsg.xsharing.router.Constants.BatchConstants;
import de.rwth.idsg.xsharing.router.core.CoreBootstrapper;
import de.rwth.idsg.xsharing.router.persistence.DatabaseException;
import de.rwth.idsg.xsharing.router.persistence.repository.ComputationLogRepository;
import lombok.extern.slf4j.Slf4j;

import javax.batch.api.listener.AbstractJobListener;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.Singleton;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;

/**
 * Listener for determining completion of leg precomputation
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Named(value = "MatrixJobOperationListener")
@Singleton
@Dependent
@Slf4j
public class MatrixJobOperationListener extends AbstractJobListener {

    @Inject private ComputationLogRepository logRepository;
    @Inject private CoreBootstrapper coreBootstrapper;

    @Override
    public void beforeJob() throws Exception {
        log.info("Starting matrix job");
    }

    @Override
    public void afterJob() throws Exception {
        List<Long> activeJobs = BatchRuntime.getJobOperator().getRunningExecutions(BatchConstants.MATRIX_JOB);
        log.info("Finished matrix job. Remaining: {}", activeJobs);

        if (activeJobs.size() == 0) {
            // final job execution: save current time stamp of creation to db
            try {
                logRepository.insert(new Date());
                coreBootstrapper.computationFinished();

                log.info("Finished all matrix computations. All done!");
            } catch (DatabaseException e) {
                log.error("FATAL: Error inserting new computation log! {}", e.getMessage());
            }
        }
    }
}
