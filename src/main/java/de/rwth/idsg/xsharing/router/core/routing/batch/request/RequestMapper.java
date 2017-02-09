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
import de.rwth.idsg.xsharing.router.core.routing.batch.SetPartitionPlan;

import javax.batch.api.partition.PartitionMapper;
import javax.batch.api.partition.PartitionPlan;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Named(value = "RequestMapper")
@Dependent
public class RequestMapper implements PartitionMapper {

    @Inject
    private JobContext jobContext;

    @Override
    public PartitionPlan mapPartitions() throws Exception {
        // get count of planned elements
        int size = Integer.parseInt(BatchRuntime.getJobOperator()
                                                .getParameters(jobContext.getExecutionId())
                                                .getProperty(BatchConstants.BATCH_SIZE));

        return new SetPartitionPlan(size, IVRouterConfig.MAX_MATRIX_SIZE);
    }

}
