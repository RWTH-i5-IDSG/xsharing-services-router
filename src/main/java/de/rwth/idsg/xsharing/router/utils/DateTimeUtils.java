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

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 23.05.2016
 */
@Slf4j
public final class DateTimeUtils {

    private DateTimeUtils() { }

    private static final long OFFSET_FOR_CHECK = TimeUnit.MINUTES.toMillis(5);
    private static final long OFFSET_FOR_CHECK_END = TimeUnit.MINUTES.toMillis(15);

    public static boolean contains(List<Interval> intervals, DateTime query) {
        try {
            for (Interval in : intervals) {
                if (in.contains(query)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("Failed to check whether {} is contained within {}", query, intervals);
            return false;
        }
    }

    public static boolean contains(List<Interval> intervals, Interval query) {
        try {
            for (Interval in : intervals) {
                if (in.overlaps(query)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("Failed to check whether {} overlaps with {}", query, intervals);
            return false;
        }
    }

    /**
     * Our "current time" lies a little bit in the past (5 min), to factor out
     * time differences between external partner systems.
     */
    public static long getNowWithOffset() {
        return org.joda.time.DateTimeUtils.currentTimeMillis() - OFFSET_FOR_CHECK;
    }

    /**
     * For some checks, we need a mechanism to express "if the time is _around_ current time".
     * With this, we return a time window with "current time ± offset".
     */
    public static Interval getNowTimeWindow() {
        long now = org.joda.time.DateTimeUtils.currentTimeMillis();
        return new Interval(now - OFFSET_FOR_CHECK, now + OFFSET_FOR_CHECK_END);
    }
}
