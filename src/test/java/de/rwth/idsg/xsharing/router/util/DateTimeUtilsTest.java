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
package de.rwth.idsg.xsharing.router.util;

import de.rwth.idsg.xsharing.router.utils.DateTimeUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 23.05.2016
 */
//@State(Scope.Thread)
//@BenchmarkMode(Mode.AverageTime)
//@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DateTimeUtilsTest {

    private List<Interval> inavailabilities;

    private List<Interval> intervalQueries;
    private List<DateTime> dateTimeQueries;

    /**
     * Main method to run benchmark
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DateTimeUtilsTest.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .threads(1)
                .build();

        new Runner(opt).run();
    }

//    @Setup
    public void setup() {
        inavailabilities = generateIntervalValues(40_000);

        intervalQueries = generateIntervalValues(5_000);
        dateTimeQueries = generateDateTimeValues(5_000);
    }

    private List<Interval> generateIntervalValues(int size) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long now = System.currentTimeMillis();
        List<Interval> temp = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            long t1 = random.nextLong(now);
            long t2 = random.nextLong(now);

            Interval interval;
            if (t1 < t2) {
                interval = new Interval(new DateTime(t1), new DateTime(t2));
            } else {
                interval = new Interval(new DateTime(t2), new DateTime(t1));
            }
            temp.add(interval);
        }

        return temp;
    }

    private List<DateTime> generateDateTimeValues(int size) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long now = System.currentTimeMillis();
        List<DateTime> temp = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            long t1 = random.nextLong(now);
            temp.add(new DateTime(t1));
        }

        return temp;
    }

//    @Benchmark
    public void dateTimeQueries(Blackhole bh) {
        for (DateTime query : dateTimeQueries) {
            boolean b = DateTimeUtils.contains(inavailabilities, query);
            bh.consume(b);
        }
    }

//    @Benchmark
    public void intervalQueries(Blackhole bh) {
        for (Interval query : intervalQueries) {
            boolean b = DateTimeUtils.contains(inavailabilities, query);
            bh.consume(b);
        }
    }
}
