/*
 * Copyright 2008, 2013 Google Inc.
 * Copyright (C) 2015-2017 RWTH Aachen University - Information Systems - Intelligent Distributed Systems Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.rwth.idsg.xsharing.router.persistence.domain.util;

import de.ivu.realtime.modules.ura.data.GeoCoordinates;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Google Maps API polyline transcoding algorithm implementations.
 * Used for space saving representation of leg paths.
 *
 * https://developers.google.com/maps/documentation/utilities/polylinealgorithm
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
public class PolylineCoder {

    private PolylineCoder() { }

    public static GeoCoordinates[] decode(final String encodedPath, boolean isReversed) {
        List<GeoCoordinates> path = decode(encodedPath);
        if (isReversed) {
            Collections.reverse(path);
        }
        return path.toArray(new GeoCoordinates[path.size()]);
    }

    /**
     * Copyright 2008, 2013 Google Inc.
     *
     * Decodes an encoded path string into a sequence of GeoCoordinates.
     *
     * Taken from:
     * https://github.com/googlemaps/android-maps-utils/blob/master/library/src/com/google/maps/android/PolyUtil.java
     *
     * Changes:
     * - Use data type GeoCoordinates instead of LatLng
     * - See the call path.add(...)
     */
    private static List<GeoCoordinates> decode(final String encodedPath) {
        int len = encodedPath.length();

        // For speed we preallocate to an upper bound on the final length, then
        // truncate the array before returning.
        final List<GeoCoordinates> path = new ArrayList<>();
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;
            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            result = 1;
            shift = 0;
            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            // changed according to
            // http://wptrafficanalyzer.in/blog/route-between-two-locations-with-waypoints-in-google-map-android-api-v2/
            path.add(new GeoCoordinates((double) lat / 1E5, (double) lng / 1E5));
        }

        return path;
    }

    /**
     * Copyright 2008, 2013 Google Inc.
     *
     * Encodes a sequence of GeoCoordinates into an encoded path string.
     *
     * Taken from:
     * https://github.com/googlemaps/android-maps-utils/blob/master/library/src/com/google/maps/android/PolyUtil.java
     *
     * Changes:
     * - Use data type GeoCoord instead of LatLng
     * - Replace StringBuffer by StringBuilder
     */
    public static String encode(final List<GeoCoord> path) {
        long lastLat = 0;
        long lastLng = 0;

        final StringBuilder result = new StringBuilder();

        for (final GeoCoord point : path) {
            long lat = Math.round(point.getY() * 1e5);
            long lng = Math.round(point.getX() * 1e5);

            long dLat = lat - lastLat;
            long dLng = lng - lastLng;

            encode(dLat, result);
            encode(dLng, result);

            lastLat = lat;
            lastLng = lng;
        }
        return result.toString();
    }

    /**
     * Copyright 2008, 2013 Google Inc.
     *
     * Taken from:
     * https://github.com/googlemaps/android-maps-utils/blob/master/library/src/com/google/maps/android/PolyUtil.java
     *
     * Changes:
     * - Replace StringBuffer by StringBuilder
     */
    private static void encode(long v, StringBuilder result) {
        v = v < 0 ? ~(v << 1) : v << 1;
        while (v >= 0x20) {
            result.append(Character.toChars((int) ((0x20 | (v & 0x1f)) + 63)));
            v >>= 5;
        }
        result.append(Character.toChars((int) (v + 63)));
    }
}
