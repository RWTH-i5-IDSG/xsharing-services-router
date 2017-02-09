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

import javax.batch.api.AbstractBatchlet;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchStatus;

/**
 * Help: https://struberg.wordpress.com/2015/09/30/being-unstoppable-a-batchlets-tale/
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 15.05.2016
 */
public abstract class CustomBatchlet extends AbstractBatchlet {

    protected enum StatusForLog {
        Starting,
        Stopping,
        Finished
    }

    protected volatile boolean shouldStop = false;

    public abstract BatchStatus processBatch() throws Exception;

    /**
     * Called by {@link JobOperator}. We delegate it to {@link #processBatch()} just to be type safe
     * with the return value, because the actual API method declares to return String, which can
     * be anything. Batchlet implementations should therefore implement just {@link #processBatch()}.
     */
    @Override
    public String process() throws Exception {
        return processBatch().toString();
    }

    @Override
    public void stop() throws Exception {
        shouldStop = true;
    }

    protected void logBatchStart() {
        logBatch(StatusForLog.Starting);
    }

    protected void logBatchStop() {
        logBatch(StatusForLog.Stopping);
    }

    protected void logBatchFinish() {
        logBatch(StatusForLog.Finished);
    }

    protected void logBatch(StatusForLog status) {
        // Default: No-op.
        // If the subclass wishes to log start/stop/finish, it should only then
        // implement this method to provide the actual logging.
    }
}
