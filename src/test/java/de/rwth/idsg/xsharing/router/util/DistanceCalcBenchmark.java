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

import lombok.RequiredArgsConstructor;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.ThreadLocalRandom;

import static org.apache.commons.math3.util.FastMath.atan2;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;
import static org.apache.commons.math3.util.FastMath.toRadians;

/**
 * WARNING:
 *
 * Do not uncomment JMH annotations and check in the code in that state !!
 * Otherwise, wildfly plugin fails !!
 *
 * ---
 *
 * The implementation with FastMath is approx. 35% faster than the one with Math
 *
 * Inspired by:
 * http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_34_SafeLooping.java
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 30.01.2016
 */
//@State(Scope.Thread)
//@BenchmarkMode(Mode.AverageTime)
//@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class DistanceCalcBenchmark {

    //@Param({"1000", "100000", "1000000"})
    int size;

    GeoCoordTuple[] input;

    /**
     * Main method to run benchmark
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DistanceCalcBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    //@Setup
    public void setup() {
        input = new GeoCoordTuple[size];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < size; i++) {
            input[i] = new GeoCoordTuple(
                    getLat(random),
                    getLong(random),
                    getLat(random),
                    getLong(random)
            );
        }
    }

    //@Benchmark
    public void math(Blackhole bh) {
        for (GeoCoordTuple g : input) {
            double d = distFromWithMath(g.lat1, g.lng1, g.lat2, g.lng2);
            bh.consume(d);
        }
    }

    //@Benchmark
    public void fastMath(Blackhole bh) {
        for (GeoCoordTuple g : input) {
            double d = distFromWithFastMath(g.lat1, g.lng1, g.lat2, g.lng2);
            bh.consume(d);
        }
    }

    private static double getLat(ThreadLocalRandom random) {
        return random.nextDouble(-90, 90);
    }

    private static double getLong(ThreadLocalRandom random) {
        return random.nextDouble(-180, 180);
    }

    // -------------------------------------------------------------------------
    // Calculate distance
    // -------------------------------------------------------------------------

    /**
     * Initial implementation (taken from source code as is)
     */
    // http://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java
    private static double distFromWithMath(double lat1, double lng1, double lat2, double lng2) {
        // in meters
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return (earthRadius * c);
    }

    // in meters
    private static final double EARTH_RADIUS = 6_371_000;

    private static double distFromWithFastMath(double lat1, double lng1, double lat2, double lng2) {
        double dLat = toRadians(lat2 - lat1);
        double dLng = toRadians(lng2 - lng1);

        double dLatSin = sin(dLat / 2);
        double dLngSin = sin(dLng / 2);

        double a = dLatSin * dLatSin + cos(toRadians(lat1)) * cos(toRadians(lat2)) * dLngSin * dLngSin;

        double c = 2 * atan2(sqrt(a), sqrt(1 - a));

        return (EARTH_RADIUS * c);
    }

    // -------------------------------------------------------------------------
    // Data holder class
    // -------------------------------------------------------------------------

    @RequiredArgsConstructor
    private static final class GeoCoordTuple {
        public final double lat1, lng1, lat2, lng2;
    }

}
