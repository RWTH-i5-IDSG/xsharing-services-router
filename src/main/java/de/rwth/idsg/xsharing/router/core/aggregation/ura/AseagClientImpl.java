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
package de.rwth.idsg.xsharing.router.core.aggregation.ura;

import de.ivu.realtime.modules.ura.client.ApacheHttpUraConnection;
import de.ivu.realtime.modules.ura.client.UraClient;
import de.ivu.realtime.modules.ura.client.UraResponse;
import de.ivu.realtime.modules.ura.data.request.Ura2Request;
import de.ivu.realtime.modules.ura.data.request.UraRequestParseException;
import de.ivu.realtime.modules.ura.data.response.StopPoint;
import de.ivu.realtime.modules.ura.data.response.UraEntity;
import de.rwth.idsg.xsharing.router.Constants.UraConfig;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static de.rwth.idsg.xsharing.router.AppConfiguration.CONFIG;

/**
 * Implementation of the ASEAG bus stop retrieval utilizing URA2 library from IVU
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
@Slf4j
@Stateless
public class AseagClientImpl implements AseagClient {

    private UraClient client;

    @PostConstruct
    public void init() {
        client = new UraClient(new ApacheHttpUraConnection(CONFIG.getUraBaseUrl()));
    }

    @Override
    public List<StopPoint> getAseagStops() throws UraRequestParseException {
        String params = UraConfig.getBaseParams();
        Ura2Request request = new Ura2Request.Builder().withQueryParameters(params).build();

        UraResponse res = getResponse(client.request(request));
        UraEntity entity = getEntity(res);

        if (entity == null) {
            return new ArrayList<>();
        }

        de.ivu.realtime.modules.ura.data.response.UraResponse response =
                (de.ivu.realtime.modules.ura.data.response.UraResponse) entity;

        List<StopPoint> stops = response.getStops();
        log.info("Got URA V2 response: {}. There are {} ASEAG stops.", response.hashCode(), stops.size());
        return stops;
    }

    @Nullable
    private UraResponse getResponse(Future<UraResponse> rep) {
        try {
            return rep.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error on fetching URA stations: Could not perform request! Reason: {}", e.getMessage());
            return null;
        }
    }

    @Nullable
    private UraEntity getEntity(UraResponse res) {
        if (res == null) {
            return null;

        } else if (res instanceof UraResponse.Success) {
            return ((UraResponse.Success) res).getEntity();

        } else if (res instanceof UraResponse.Failure) {
            log.error("Failure while fetching Bus-Stations with URAClient", ((UraResponse.Failure) res).getReason());
            return null;

        } else {
            return null;
        }
    }
}
