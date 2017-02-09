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

import de.rwth.idsg.xsharing.router.Constants;
import lombok.extern.slf4j.Slf4j;

import javax.batch.api.partition.PartitionPlan;
import java.util.Properties;

import static de.rwth.idsg.xsharing.router.AppConfiguration.CONFIG;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Slf4j
public class NoPartitioningPlan implements PartitionPlan {
    private Integer size;

    public NoPartitioningPlan(Integer size) {
        this.size = size;
    }

    @Override
    public int getPartitions() {
        return 1;
    }

    @Override
    public int getThreads() {
        return CONFIG.getBatchJobThreadCount();
    }

    @Override
    public Properties[] getPartitionProperties() {
        log.info("Performing job partitioning for cartesian product of {} entries", size);
        Properties[] prop = new Properties[getPartitions()];
        int start = 0;
        int end = size;
        prop[0] = new Properties();
        prop[0].setProperty(Constants.BatchConstants.START, String.valueOf(start));
        prop[0].setProperty(Constants.BatchConstants.END, String.valueOf(end));
        return prop;
    }

    @Override
    public boolean getPartitionsOverride() {
        return false;
    }

    // unused methods

    @Override
    public void setPartitions(int count) {

    }

    @Override
    public void setPartitionsOverride(boolean override) {

    }

    @Override
    public void setThreads(int count) {

    }

    @Override
    public void setPartitionProperties(Properties[] props) {

    }
}
