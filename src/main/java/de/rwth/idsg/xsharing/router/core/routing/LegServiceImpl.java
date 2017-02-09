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
package de.rwth.idsg.xsharing.router.core.routing;

import de.rwth.idsg.xsharing.router.persistence.DatabaseException;
import de.rwth.idsg.xsharing.router.core.RoutingComponentsProvider;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.LegType;
import de.rwth.idsg.xsharing.router.persistence.domain.routes.leg.RouteLeg;
import de.rwth.idsg.xsharing.router.persistence.domain.util.RouteLegFactory;
import de.rwth.idsg.xsharing.router.persistence.domain.util.LegTypeMapper;
import de.rwth.idsg.xsharing.router.persistence.repository.RouteLegRepository;
import de.rwth.idsg.xsharing.router.iv.IVRouterClient;
import de.rwth.idsg.xsharing.router.iv.util.IVRequestFactory;
import de.rwth.idsg.xsharing.router.iv.model.EsriPolyLineFeature;
import de.rwth.idsg.xsharing.router.iv.request.ShortestPathsRequest;
import de.rwth.idsg.xsharing.router.iv.response.ShortestPathsResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Stateless
@Slf4j
public class LegServiceImpl implements LegService {

    @Inject private RouteLegRepository repository;
    @Inject private RoutingComponentsProvider componentsProvider;

    private IVRouterClient ivRouterClient;

    @PostConstruct
    private void init() {
        ivRouterClient = componentsProvider.getIVRouterClient();
    }

    @Override
    public RouteLeg fetchLegForCompact(Long id, LegType type) throws DatabaseException {
        Pair<String, Class<? extends RouteLeg>> info = LegTypeMapper.SINGLETON.getInfo(type);
        return repository.find(id, info.getRight());
    }

    @Override
    public RouteLeg fetchLegForDetails(Long id, LegType type) throws DatabaseException {
        Pair<String, Class<? extends RouteLeg>> info = LegTypeMapper.SINGLETON.getInfo(type);
        RouteLeg minimal = repository.find(id, info.getRight());

        // if we already have data for this leg, skip the IVRequest
        //
        if (minimal.getPath() == null) {
            return getFromIvRouter(id, type, minimal, info.getLeft());
        } else {
            return minimal;
        }
    }

    private RouteLeg getFromIvRouter(Long id, LegType type, RouteLeg minimal, String mode) {
        ShortestPathsRequest request = IVRequestFactory.getSingleSP(mode, minimal.getFrom(), minimal.getTo());

        ShortestPathsResult result = ivRouterClient.getShortestPaths(request);
        if (result == null) {
            log.error("Could not obtain directions from IV Router for {} with id {}", type.toString(), id);
            return null;
        }

        List<EsriPolyLineFeature> features = result.getValue().getFeatures();
        if (features.size() > 1) {
            log.error("this cant be good!");
        } else if (features.isEmpty()) {
            log.error("no actual leg found. WTF");
            return null;
        }

        RouteLeg leg = RouteLegFactory.getLeg(type, features.get(0));
        if (leg != null) {
            leg.setId(minimal.getId());
            saveOrUpdateAsync(leg);
        }
        return leg;
    }

    @Asynchronous
    private void saveOrUpdateAsync(RouteLeg leg) {
        repository.saveOrUpdate(leg);
    }
}
