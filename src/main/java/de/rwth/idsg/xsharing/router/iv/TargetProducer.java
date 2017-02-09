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
package de.rwth.idsg.xsharing.router.iv;

import de.rwth.idsg.xsharing.router.Constants.IVRouterConfig;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import static de.rwth.idsg.xsharing.router.AppConfiguration.CONFIG;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@ApplicationScoped
public class TargetProducer {

    @Resource private ManagedExecutorService executor;

    @Produces
    @ApplicationScoped
    public IVRouterService produceWebTarget() {
        return new ResteasyClientBuilder()
                .connectionPoolSize(100)
                .maxPooledPerRoute(20)
                .asyncExecutor(executor)
                .build()
                .target(CONFIG.getIvRouterBaseUrl())
                .queryParam(IVRouterConfig.FORMAT, IVRouterConfig.REQUEST_FORMAT)
                .proxy(IVRouterService.class);
    }

}
