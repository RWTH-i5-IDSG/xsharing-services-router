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
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.ivu.realtime.modules.ura.data.GeoCoordinates;

import java.io.IOException;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public class UraGeoCoordWrapper extends JsonSerializer<GeoCoordinates[]> {

    @Override
    public void serialize(GeoCoordinates[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartArray();
        for (GeoCoordinates c : value) {
            jgen.writeStartArray();
            jgen.writeNumber(c.getLatitude());
            jgen.writeNumber(c.getLongitude());
            jgen.writeEndArray();
        }
        jgen.writeEndArray();
    }
}
