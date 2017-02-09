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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.vividsolutions.jts.geom.Point;
import de.ivu.realtime.modules.ura.data.response.IndividualTrip;
import de.ivu.realtime.modules.ura.data.response.PathData;
import de.ivu.realtime.modules.ura.data.response.Prediction;
import de.rwth.idsg.xsharing.router.iv.util.CustomIntervalSerializer;
import de.rwth.idsg.xsharing.router.iv.util.DurationSecondsSerializer;
import de.rwth.idsg.xsharing.router.iv.util.PointSerializer;
import de.rwth.idsg.xsharing.router.iv.util.UraIndividualTripMixIn;
import de.rwth.idsg.xsharing.router.iv.util.UraPathDataMixIn;
import de.rwth.idsg.xsharing.router.iv.util.UraPredictionMixIn;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Duration;
import org.joda.time.Interval;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
@Slf4j
public final class JsonMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Duration.class, new DurationSecondsSerializer());

        // Needed for ControlResource.getStation(), otherwise "Infinite recursion (StackOverflowError)"
        simpleModule.addSerializer(Point.class, new PointSerializer());

        // Needed for ControlResource.getStation(), because default prints millis which is not human-readable
        simpleModule.addSerializer(Interval.class, new CustomIntervalSerializer());

        MAPPER.registerModule(new JodaModule());
        MAPPER.registerModule(simpleModule);

        MAPPER.addMixInAnnotations(PathData.class, UraPathDataMixIn.class);
        MAPPER.addMixInAnnotations(IndividualTrip.class, UraIndividualTripMixIn.class);
        MAPPER.addMixInAnnotations(Prediction.class, UraPredictionMixIn.class);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String serialize(Object obj) throws JsonProcessingException {
        return MAPPER.writeValueAsString(obj);
    }

    public static String serializeOrThrow(Object obj) {
        try {
            return serialize(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static String serializeOrNull(Object obj) {
        try {
            return serialize(obj);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * @throws      IOException, when it fails to deserialize the json
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(String json, Class clazz) throws IOException {
        // log.debug("Message received: {}", json);

        return (T) MAPPER.readValue(json, clazz);
    }

    /**
     * @return      The deserialized Java object or null when it fails
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T deserializeOrNull(String json, Class clazz) {
        // log.debug("Message received: {}", json);

        try {
            return (T) MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

}
