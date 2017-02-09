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

import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.iv.model.SpatialReference;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.LegType;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.WalkingLeg;
import de.rwth.idsg.xsharing.router.utils.WalkingLegCloner;
import org.apache.commons.lang3.SerializationUtils;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.ThreadLocalRandom;

/**
 * WARNING:
 *
 * Do not uncomment JMH annotations and check in the code in that state !!
 * Otherwise, wildfly plugin fails !!
 *
 * --------------------------------
 *
 * Result "apacheCloner":
 *   417,154 ±(99.9%) 17,395 ms/op [Average]
 *   (min, avg, max) = (412,074, 417,154, 424,008), stdev = 4,518
 *   CI (99.9%): [399,759, 434,550] (assumes normal distribution)
 *
 *
 * Result "customCloner":
 *   0,481 ±(99.9%) 0,032 ms/op [Average]
 *   (min, avg, max) = (0,469, 0,481, 0,490), stdev = 0,008
 *   CI (99.9%): [0,449, 0,513] (assumes normal distribution)
 *
 *
 * Benchmark               Mode  Cnt    Score    Error  Units
 * CloneTest.apacheCloner  avgt    5  417,154 ± 17,395  ms/op
 * CloneTest.customCloner  avgt    5    0,481 ±  0,032  ms/op
 *
 * --------------------------------
 *
 * Conclusion: "customCloner" is 867 times faster than "apacheCloner"
 *
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 31.03.2016
 */
//@State(Scope.Thread)
//@BenchmarkMode(Mode.AverageTime)
//@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class CloneTest {

    private static final int size = 10_000;

    private WalkingLeg[] input;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CloneTest.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    //@Setup
    public void setup() {
        input = new WalkingLeg[size];
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < size; i++) {
            WalkingLeg leg = new WalkingLeg();

            leg.setId(random.nextLong());
            leg.setType(LegType.BikeLeg);
            leg.setFrom(getCoord(random));
            leg.setTo(getCoord(random));
            leg.setDistance(random.nextDouble());
            leg.setDuration(random.nextInt());
            leg.setSpatialReference(new SpatialReference());
            leg.setPath(String.valueOf(random.nextInt()));

            input[i] = leg;
        }
    }

    //@Benchmark
    public void apacheCloner(Blackhole bh) {
        for (WalkingLeg origin : input) {
            WalkingLeg clone = SerializationUtils.clone(origin);
            clone.reverse();

            bh.consume(clone);
        }
    }

    //@Benchmark
    public void customCloner(Blackhole bh) {
        for (WalkingLeg origin : input) {
            WalkingLeg clone = WalkingLegCloner.cloneAndReverse(origin);
            bh.consume(clone);
        }
    }

    private static GeoCoord getCoord(ThreadLocalRandom random) {
        return new GeoCoord(
                getLat(random),
                getLong(random)
        );
    }

    private static double getLat(ThreadLocalRandom random) {
        return random.nextDouble(-90, 90);
    }

    private static double getLong(ThreadLocalRandom random) {
        return random.nextDouble(-180, 180);
    }

}
