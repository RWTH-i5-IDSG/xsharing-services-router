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
package de.rwth.idsg.xsharing.router.iv.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vividsolutions.jts.geom.Point;

import java.io.IOException;

/**
 * This is not a general-purpose serializer for Point! Needed for ControlResource.getStation(),
 * otherwise "Infinite recursion (StackOverflowError)"
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 01.06.2016
 */
public class PointSerializer extends JsonSerializer<Point> {

    @Override
    public void serialize(Point value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (value == null || value.getCoordinate() == null) {
            provider.defaultSerializeNull(jgen);

        } else {
            jgen.writeStartObject();
            jgen.writeObjectField("x", value.getX());
            jgen.writeObjectField("y", value.getY());
            jgen.writeEndObject();
        }
    }
}
