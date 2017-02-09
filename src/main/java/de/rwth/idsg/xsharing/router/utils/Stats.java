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
package de.rwth.idsg.xsharing.router.utils;

import lombok.Builder;
import lombok.Getter;
import org.joda.time.DateTime;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Inspired by {@link java.util.LongSummaryStatistics} and {@link com.google.common.base.Stopwatch}
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 30.01.2016
 */
public final class Stats {
    public final String name;

    // Count of requests/responses
    private final AtomicLong incomingCount = new AtomicLong(0);
    private final AtomicLong outGoingCount = new AtomicLong(0);

    // Processing duration metrics of incoming requests (in millis)
    private final AtomicInteger min = new AtomicInteger(0);
    private final AtomicInteger max = new AtomicInteger(0);
    private final AtomicLong    sum = new AtomicLong(0);

    // Timestamp of the last incoming request (in millis)
    private long lastRequestTimestamp = 0;

    // Timestamp for since when we collect the stats
    private long collectingSince = now();

    public Stats(String uniqueName) {
        this.name = uniqueName;
    }

    public void starting() {
        incomingCount.incrementAndGet();
    }

    public void finished(final long startMillis, final long stopMillis) {
        final int elapsed = (int) (stopMillis - startMillis);

        lastRequestTimestamp = startMillis;
        outGoingCount.incrementAndGet();
        sum.addAndGet(elapsed);

        if (elapsed > max.get()) {
            max.set(elapsed);
        }

        if (elapsed < min.get()) {
            min.set(elapsed);
        }
    }

    private long incomingCount() {
        return incomingCount.get();
    }

    private long outGoingCount() {
        return outGoingCount.get();
    }

    private int min() {
        return min.get();
    }

    private int max() {
        return max.get();
    }

    private long sum() {
        return sum.get();
    }

    private double avg() {
        if (outGoingCount.get() > 0) {
            return (double) sum.get() / outGoingCount.get();
        } else {
            return 0.0d;
        }
    }

    private String lastRequestTimestamp() {
        return toDateTime(lastRequestTimestamp);
    }

    private String collectingSince() {
        return toDateTime(collectingSince);
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    private static String toDateTime(long millis) {
        return new DateTime(millis).toString();
    }

    public void reset() {
        incomingCount.set(0);
        outGoingCount.set(0);
        min.set(0);
        max.set(0);
        sum.set(0);
        lastRequestTimestamp = 0;
        collectingSince = now();
    }

    public String getReportForLogging() {
        return "[incomingCount=" + incomingCount()
                + ", outGoingCount=" + outGoingCount()
                + ", min(in ms)=" + min()
                + ", max(in ms)=" + max()
                + ", avg(in ms)=" + avg()
                + ", sum(in ms)=" + sum()
                + ", collectingSince=" + collectingSince()
                + ", lastRequestTimestamp=" + lastRequestTimestamp() + "]";
    }

    public Stats.Report getReport() {
        return Report.builder()
                     .name(name)
                     .incomingCount(incomingCount())
                     .outGoingCount(outGoingCount())
                     .min(min())
                     .max(max())
                     .sum(sum())
                     .avg(avg())
                     .lastRequestTimestamp(lastRequestTimestamp())
                     .collectingSince(collectingSince())
                     .build();
    }

    @Getter
    @Builder
    public static final class Report {
        private final String name;
        private final long incomingCount;
        private final long outGoingCount;
        private final int min;
        private final int max;
        private final double avg;
        private final long sum;
        private final String lastRequestTimestamp;
        private final String collectingSince;
    }
}
