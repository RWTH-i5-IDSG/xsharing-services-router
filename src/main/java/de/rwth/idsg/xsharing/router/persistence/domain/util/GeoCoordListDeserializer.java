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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public class GeoCoordListDeserializer extends JsonDeserializer<List<GeoCoord>> {

    @Override
    public List<GeoCoord> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        if (JsonNodeType.ARRAY != node.getNodeType()) {
            throw new IOException("Unable to instantiate new GeoCoord Path Lists from JSON!");
        }

        List<GeoCoord> result = new ArrayList<>(node.size());
        for (int i = 0; i < node.size(); i++) {
            JsonNode p = node.get(i);
            double lon = p.get("longitude").asDouble();
            double lat = p.get("latitude").asDouble();
            result.add(new GeoCoord(lon, lat));
        }

        return result;
    }
}
