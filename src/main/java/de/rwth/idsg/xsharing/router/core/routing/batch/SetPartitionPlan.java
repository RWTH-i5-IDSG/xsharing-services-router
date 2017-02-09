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

import de.rwth.idsg.xsharing.router.AppConfiguration;
import de.rwth.idsg.xsharing.router.Constants.BatchConstants;
import lombok.extern.slf4j.Slf4j;

import javax.batch.api.partition.PartitionPlan;
import java.util.Properties;

import static de.rwth.idsg.xsharing.router.AppConfiguration.CONFIG;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Slf4j
public class SetPartitionPlan implements PartitionPlan {

    private Integer size;
    private Integer maxPartitionSize;
    private Integer actualPartitionSize;

    public SetPartitionPlan(Integer size, Integer maxPartitionSize) {
        this.size = size;
        this.maxPartitionSize = maxPartitionSize;
    }

    @Override
    public int getPartitions() {
        actualPartitionSize = (size < maxPartitionSize) ? Math.max(1,(size / getThreads())) : maxPartitionSize;
        return size / actualPartitionSize + 1;
    }

    @Override
    public int getThreads() {
        return CONFIG.getBatchJobThreadCount();
    }

    @Override
    public Properties[] getPartitionProperties() {
        log.info("Performing job partitioning for {} entries at batch size of {}", size, maxPartitionSize);
        Properties[] prop = new Properties[getPartitions()];
        int start = 0;
        int end = Math.min(actualPartitionSize, size);
        for (int i = 0; i < prop.length; i++) {
            prop[i] = new Properties();
            prop[i].setProperty(BatchConstants.START, String.valueOf(start));
            prop[i].setProperty(BatchConstants.END, String.valueOf(end));
            start = Math.min(end + 1, size);
            end = Math.min(start + actualPartitionSize, size);
        }
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
