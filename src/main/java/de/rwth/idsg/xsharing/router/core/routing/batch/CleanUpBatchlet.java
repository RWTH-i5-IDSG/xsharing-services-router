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
package de.rwth.idsg.xsharing.router.core.routing.batch;

import lombok.extern.slf4j.Slf4j;

import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Slf4j
@Named("CleanUpBatchlet")
public class CleanUpBatchlet extends CustomBatchlet {

    @Inject private JobContext jobContext;
    @Inject private BatchManager batchManager;

    @Override
    public BatchStatus processBatch() throws Exception {
        long id = jobContext.getExecutionId();
        String jobName = jobContext.getJobName();
        log.debug("Cleaning up resources for job {} with id {}", jobName, id);
        batchManager.doneJob(id);

        return BatchStatus.COMPLETED;
    }
}
