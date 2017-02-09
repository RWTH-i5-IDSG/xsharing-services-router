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

import de.rwth.idsg.xsharing.router.core.routing.strategy.RouteLegWrapper;
import de.rwth.idsg.xsharing.router.core.routing.strategy.RouteLegList;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import de.rwth.idsg.xsharing.router.iv.model.SpatialReference;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.CarLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.LegType;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.WalkingLeg;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 24.05.2016
 */
public class RouteLegListTest {

    private final SpatialReference sr = SpatialReference.builder().build();
    private final GeoCoord geoCoord = new GeoCoord(4, 5);
    private final int distance = 500;

    @Test
    public void testDeparture() {

        DateTime t1 = new DateTime(2016, 5, 24, 11, 0, DateTimeZone.UTC);

        RouteLegList routeLegList = new RouteLegList(t1, false);

        WalkingLeg walkingLeg1 = new WalkingLeg(LegType.WalkingLeg, geoCoord, geoCoord, distance, 50, sr, "");
        DateTime t2 = t1.plusSeconds(50);

        routeLegList.addAndShift(new RouteLegWrapper(walkingLeg1));
        System.out.println(routeLegList);

        CarLeg carLeg = new CarLeg(LegType.CarLeg, geoCoord, geoCoord, distance, 280, sr, "");
        DateTime t3 = t2.plusSeconds(280);

        Interval interval = routeLegList.getIntervalAfterPossibleLeg(carLeg.getDuration());
        System.out.println(interval);

        Assert.assertEquals(interval, new Interval(t2, t3));
        routeLegList.addAndShift(new RouteLegWrapper(carLeg));
        System.out.println(routeLegList);

        WalkingLeg walkingLeg2 = new WalkingLeg(LegType.WalkingLeg, geoCoord, geoCoord, distance, 90, sr, "");
        DateTime t4 = t3.plusSeconds(90);

        Interval interval2 = routeLegList.getIntervalAfterPossibleLeg(walkingLeg2.getDuration());
        System.out.println(interval2);

        Assert.assertEquals(interval2, new Interval(t3, t4));

        routeLegList.addAndShift(new RouteLegWrapper(walkingLeg2));
        System.out.println(routeLegList);
    }

    @Test
    public void testArrival() {

        DateTime t1 = new DateTime(2016, 5, 24, 11, 0, DateTimeZone.UTC);

        RouteLegList routeLegList = new RouteLegList(t1, true);

        WalkingLeg walkingLeg1 = new WalkingLeg(LegType.WalkingLeg, geoCoord, geoCoord, distance, 50, sr, "");
        DateTime t2 = t1.minusSeconds(50);

        routeLegList.addAndShift(new RouteLegWrapper(walkingLeg1));
        System.out.println(routeLegList);

        CarLeg carLeg = new CarLeg(LegType.CarLeg, geoCoord, geoCoord, distance, 280, sr, "");
        DateTime t3 = t2.minusSeconds(280);

        Interval interval = routeLegList.getIntervalAfterPossibleLeg(carLeg.getDuration());
        System.out.println(interval);

        Assert.assertEquals(interval, new Interval(t3, t2));
        routeLegList.addAndShift(new RouteLegWrapper(carLeg));
        System.out.println(routeLegList);

        WalkingLeg walkingLeg2 = new WalkingLeg(LegType.WalkingLeg, geoCoord, geoCoord, distance, 90, sr, "");
        DateTime t4 = t3.minusSeconds(90);

        Interval interval2 = routeLegList.getIntervalAfterPossibleLeg(walkingLeg2.getDuration());
        System.out.println(interval2);

        Assert.assertEquals(interval2, new Interval(t4, t3));

        routeLegList.addAndShift(new RouteLegWrapper(walkingLeg2));
        System.out.println(routeLegList);
    }
}
