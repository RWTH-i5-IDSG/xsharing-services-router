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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
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
public class ListGeoCoordListDeserializer extends JsonDeserializer<List<List<GeoCoord>>> {

    @Override
    public List<List<GeoCoord>> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectCodec codec = jp.getCodec();
        JsonNode node = codec.readTree(jp);

        if (JsonNodeType.ARRAY != node.getNodeType()) {
            throw new IOException("Unable to instantiate new GeoCoord Path Lists from JSON!");
        }

        int nodeSize = node.size();
        List<List<GeoCoord>> result = new ArrayList<>(nodeSize);

        for (int i = 0; i < nodeSize; i++) {

            JsonNode points = node.get(i);
            int pointsSize = points.size();
            List<GeoCoord> path = new ArrayList<>(pointsSize);

            for (int j = 0; j < pointsSize; j++) {
                JsonNode p = points.get(j);
                double x = p.get(0).asDouble();
                double y = p.get(1).asDouble();
                path.add(new GeoCoord(x, y));
            }
            result.add(path);

        }

        return result;
    }
}
