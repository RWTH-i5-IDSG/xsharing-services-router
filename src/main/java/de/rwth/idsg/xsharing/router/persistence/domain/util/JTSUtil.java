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
package de.rwth.idsg.xsharing.router.persistence.domain.util;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import de.rwth.idsg.xsharing.router.persistence.domain.mb.MBPlaceEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
@Slf4j
public class JTSUtil {

    private static final String POINT = "POINT";
    private static final WKTReader reader = new WKTReader(new GeometryFactory(new PrecisionModel(), 4326));

    public static Point getPoint(MBPlaceEntity mbPlaceEntity) {
        // Postgres format: (X,Y)
        String input = mbPlaceEntity.getGpsPosition();
        // (X Y)
        String noComma = input.replace(',', ' ');
        // POINT(X Y)
        String prefixed = POINT + noComma;

        return toPoint(prefixed);
    }

    public static Point getPoint(Double longitude, Double latitude) {
        // POINT(X Y)
        String prefixed = POINT + "(" + longitude + " " + latitude + ")";

        return toPoint(prefixed);
    }

    /**
     * Input format: POINT(X Y)
     */
    private static Point toPoint(String input) {
        try {
            return (Point) reader.read(input);
        } catch (ParseException e) {
            log.error("Could not convert Coordinates {}, got exception {}", input, e.getMessage());
            return null;
        }
    }
}
