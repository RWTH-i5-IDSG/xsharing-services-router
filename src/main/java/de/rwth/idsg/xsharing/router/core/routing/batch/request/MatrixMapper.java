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
package de.rwth.idsg.xsharing.router.core.routing.batch.request;

import de.rwth.idsg.xsharing.router.Constants.BatchConstants;
import de.rwth.idsg.xsharing.router.Constants.IVRouterConfig;
import de.rwth.idsg.xsharing.router.core.routing.batch.NoPartitioningPlan;
import de.rwth.idsg.xsharing.router.core.routing.batch.SetPartitionPlan;

import javax.batch.api.partition.PartitionMapper;
import javax.batch.api.partition.PartitionPlan;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Properties;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Named(value = "MatrixMapper")
@Dependent
public class MatrixMapper implements PartitionMapper {

    @Inject
    private JobContext jobContext;

    /**
     * Create partition depending on intended use case (whether cartesian product or not)
     * NOTE: this could be altered since IV Router supports partial requests
     */
    @Override
    public PartitionPlan mapPartitions() throws Exception {
        // get count of planned elements
        Properties params = BatchRuntime.getJobOperator().getParameters(jobContext.getExecutionId());
        int size = Integer.parseInt(params.getProperty(BatchConstants.BATCH_SIZE));
        boolean shouldPartition = Boolean.valueOf(params.getProperty(BatchConstants.PARTITIONING));

        if (shouldPartition) {
            return new SetPartitionPlan(size, IVRouterConfig.MAX_MATRIX_SIZE);
        } else {
            return new NoPartitioningPlan(size);
        }
    }
}