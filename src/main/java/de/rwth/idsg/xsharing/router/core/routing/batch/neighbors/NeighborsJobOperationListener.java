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
package de.rwth.idsg.xsharing.router.core.routing.batch.neighbors;

import de.rwth.idsg.xsharing.router.core.CoreBootstrapper;
import de.rwth.idsg.xsharing.router.core.routing.RouteBuilderService;
import de.rwth.idsg.xsharing.router.core.routing.ServerStatus;
import de.rwth.idsg.xsharing.router.Constants;
import lombok.extern.slf4j.Slf4j;

import javax.batch.api.listener.AbstractJobListener;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.Singleton;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Listener for initiating followup job
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Named(value = "NeighborsJobOperationListener")
@Singleton
@Dependent
@Slf4j
public class NeighborsJobOperationListener extends AbstractJobListener {

    @Inject private RouteBuilderService routeBuilderService;
    @Inject private CoreBootstrapper coreBootstrapper;

    @Override
    public void beforeJob() throws Exception {
        log.info("Starting neighbors job");
    }

    @Override
    public void afterJob() throws Exception {
        List<Long> activeJobs = BatchRuntime.getJobOperator().getRunningExecutions(Constants.BatchConstants.NEIGHBORS_JOB);
        log.debug("finished neighbors job (now at {})", activeJobs);

        if (activeJobs.isEmpty()) {
            log.info("All neighbors finished. Continuing with leg precomputation");

            coreBootstrapper.setServerStatus(ServerStatus.COMPUTING);

            routeBuilderService.prepareAllWalkingLegs();
            routeBuilderService.prepareAllSharingLegs();
        }
     }
}
