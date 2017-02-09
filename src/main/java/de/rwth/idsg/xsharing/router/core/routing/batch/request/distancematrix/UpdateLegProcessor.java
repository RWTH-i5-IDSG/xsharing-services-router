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

import de.rwth.idsg.xsharing.router.core.aggregation.raster.RasterManager;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.WalkingLeg;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 10.05.2016
 */
@Slf4j
public class UpdateLegProcessor {

    private final RasterManager rasterManager;
    private final Lock flushLock = new ReentrantLock();
    private final LinkedBlockingQueue<WalkingLeg> queue = new LinkedBlockingQueue<>();

    public UpdateLegProcessor(RasterManager rasterManager) {
        this.rasterManager = rasterManager;
    }

    /**
     * TODO:
     * This code is not optimal, because the thread which adds an element to the queue, can also start
     * consuming the queue and blocks until the queue is empty!
     *
     * Idea: There is the possibility of "@Resource private ManagedThreadFactory threadFactory".
     * Import the factory, spawn a thread that consumes the queue, sleeps when empty, wakes up after
     * inserting a new item. We should also signal the thread that we are completely done with pre-processing,
     * and therefore it can die. Otherwise it will sleep the whole time, after that.
     */
    public void process(WalkingLeg leg) {
        produce(leg);
        consume();
    }

    private void produce(WalkingLeg leg) {
        queue.add(leg);
    }

    private void consume() {
        do {
            boolean success = tryConsume();
            if (!success) {
                log.debug("Another flush in progress (queue size: {})", queue.size());
                break;
            }
        } while (!queue.isEmpty());
    }

    private boolean tryConsume() {
        if (flushLock.tryLock()) {
            try {
                actualConsume();
            } finally {
                flushLock.unlock();
                log.debug("Finished flushing.");
            }
            return true;
        }
        return false;
    }

    private void actualConsume() {
        while (true) {
            WalkingLeg toProcess = queue.poll();
            if (toProcess == null) {
                // Exit only when the queue is empty
                break;
            }

            // Actual logic that we want to shield
            rasterManager.updateLegForRasterPoint(toProcess);
        }
    }
}
