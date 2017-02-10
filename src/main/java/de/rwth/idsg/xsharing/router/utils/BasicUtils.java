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

import com.vividsolutions.jts.geom.Point;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;

import javax.annotation.Nullable;
import java.util.Collection;

import static org.apache.commons.math3.util.FastMath.atan2;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;
import static org.apache.commons.math3.util.FastMath.toRadians;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 14.05.2016
 */
public final class BasicUtils {

    private BasicUtils() { }

    // in meters
    private static final double EARTH_RADIUS = 6_371_000;

    /**
     * The benchmark/test: {@see de.rwth.idsg.xsharing.router.util.DistanceCalcBenchmark}
     *
     * http://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java
     */
    public static double distFrom(double lat1, double lng1,
                                  double lat2, double lng2) {

        double dLat = toRadians(lat2 - lat1);
        double dLng = toRadians(lng2 - lng1);

        double dLatSin = sin(dLat / 2);
        double dLngSin = sin(dLng / 2);

        double a = dLatSin * dLatSin + cos(toRadians(lat1)) * cos(toRadians(lat2)) * dLngSin * dLngSin;

        double c = 2 * atan2(sqrt(a), sqrt(1 - a));

        return (EARTH_RADIUS * c);
    }

    public static boolean checkNullOrEmpty(Collection<?> col) {
        return (col == null) || col.isEmpty();
    }

    public static GeoCoord toGeoCoord(Point p) {
        return new GeoCoord(p.getX(), p.getY());
    }

    public static boolean isEqualXY(Point p, GeoCoord gc) {
        return isEqualX(p, gc) && isEqualY(p, gc);
    }

    private static boolean isEqualX(Point p, GeoCoord gc) {
        return p.getX() == gc.getX();
    }

    private static boolean isEqualY(Point p, GeoCoord gc) {
        return p.getY() == gc.getY();
    }

    public static boolean getTypeSafeBoolean(@Nullable Boolean val) {
        if (val == null) {
            return false;
        } else {
            return val;
        }
    }

    public static String getTypeSafeString(@Nullable Object val) {
        if (val == null) {
            return "";
        } else {
            return val.toString();
        }
    }

    public static int getIntegerOrZero(@Nullable Integer val) {
        if (val == null) {
            return 0;
        } else {
            return val;
        }
    }
}
