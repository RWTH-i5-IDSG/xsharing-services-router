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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import de.ivu.realtime.modules.ura.data.ModalType;
import de.ivu.realtime.modules.ura.data.response.IndividualTrip;
import org.junit.Test;

import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class UraModalTypeWrapperTest {

    @Test
    public void testModalTypeSerialization() {
        String expected = "{\"type\":\"IndividualTrip\",\"uuid\":null,\"start\":null,\"end\":null,\"status\":null," +
                "\"lengthInM\":0.0,\"messageList\":null,\"modalType\":\"walk\",\"isAccessible\":null," +
                "\"ticketMatches\":null,\"pathDataSource\":null,\"pathData\":null,\"durationInS\":null}";
        IndividualTrip trip = new IndividualTrip.Builder().withModalType(ModalType.walk).build();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.addMixInAnnotations(IndividualTrip.class, UraIndividualTripMixIn.class);
        Writer stringWriter = new StringWriter();
        try {
            mapper.writeValue(stringWriter, trip);
            String json = stringWriter.toString();
            assertNotNull(json);
            assertEquals(json, expected);
        } catch (Exception e) {
            fail();
        }
    }

}