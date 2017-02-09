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

import com.google.common.collect.Lists;
import de.ivu.realtime.modules.ura.data.GeoCoordinates;
import de.rwth.idsg.xsharing.router.iv.model.GeoCoord;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Slf4j
public class PolylineCoderTest {
    @Test
    public void testCoder() {
        List<GeoCoord> coords = Lists.newArrayList(
                new GeoCoord(-87.64, 41.87288),
                new GeoCoord(-87.63794, 41.83401),
                new GeoCoord(-87.62112, 41.79461),
                new GeoCoord(-87.60155, 41.75851)
        );
        String encoded = PolylineCoder.encode(coords);
        log.debug(encoded);
        assertEquals(encoded, "ohq~F~d|uO|qF{KfuFchBr`FiyB");
        GeoCoordinates[] decodeCoords = PolylineCoder.decode("ohq~F~d|uO|qF{KfuFchBr`FiyB", false);
        log.debug("{}", Arrays.toString(decodeCoords));
        for (int i = 0; i < coords.size(); i++) {
            assertEquals(coords.get(i).getX(), decodeCoords[i].getLongitude(), 0D);
            assertEquals(coords.get(i).getY(), decodeCoords[i].getLatitude(), 0D);
        }

    }

    @Test
    public void testDecode() {
        GeoCoordinates[] test = PolylineCoder.decode("c~{tH}~~c@ZfBLt@BLKDBRq@\\oCxAy@Ow@M]G_AMSCc@Ii@KQIeAc@", false);
        log.debug("{}", test.length);
    }
}