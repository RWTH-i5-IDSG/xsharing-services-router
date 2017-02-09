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
package de.rwth.idsg.xsharing.router.core.messaging.listener;

import de.rwth.idsg.xsharing.router.Constants.JMSConfig;
import de.rwth.idsg.xsharing.router.core.messaging.RequestValidationException;
import de.rwth.idsg.xsharing.router.core.routing.processor.MinimalRequestProcessor;
import de.rwth.idsg.xsharing.router.core.routing.request.MinimalRequest;
import de.rwth.idsg.xsharing.router.core.routing.response.MinimalResponse;
import de.rwth.idsg.xsharing.router.utils.JsonMapper;
import de.rwth.idsg.xsharing.router.utils.StatsManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.io.IOException;

import static de.rwth.idsg.xsharing.router.utils.BasicUtils.checkNullOrEmpty;

/**
 * Concrete listener bean responsible for binding the actual JMS queue from config and setting the actual
 * JMS context for its parent implementation
 * Provides more validation functionality than its siblings due to adjusted cardinality restrictions
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
@MessageDriven(name = "MinimalRequestListener", messageListenerInterface = MessageListener.class, activationConfig =
        {
                @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = JMSConfig.MINIMAL_QUEUE_NAME),
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JMSConfig.DESTINATION_TYPE),
                @ActivationConfigProperty(propertyName = "maxSession", propertyValue = JMSConfig.MAX_CONSUMER_COUNT)
        })
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
@Slf4j
public class MinimalRequestListener extends AbstractSharingListener<MinimalRequest, MinimalResponse> {

    @Inject private JMSContext actualContext;
    @Inject private MinimalRequestProcessor minimalRequestProcessor;

    @Override
    public void onMessage(Message message) {
        long start = System.currentTimeMillis();
        StatsManager.MINIMAL.getStats().starting();
        try {
            super.onMessage(message);
        } finally {
            long stop = System.currentTimeMillis();
            StatsManager.MINIMAL.getStats().finished(start, stop);
        }
    }

    /**
     * Validates the received message according to requirements set in IVU interface description
     * valid combinations are: (in order of start, end, time, isArrival)
     * 1, n, 1, false
     * 1, n, n, true
     * n, 1, 1, true
     * n, 1, n, false
     *
     * @param message the (Text!)Message
     * @return the unpacked and valid request if possible, else throws ex.
     * @throws RequestValidationException
     */
    @Override
    public MinimalRequest validateAndUnwrap(Message message) throws RequestValidationException {
        MinimalRequest request;

        try {
            TextMessage txt = (TextMessage) message;
            String str = txt.getText();

            request = JsonMapper.deserialize(str, MinimalRequest.class);

            log.debug("Request (string): {}", str);
            log.debug("Request (object): {}", request);

        } catch (ClassCastException e) {
            throw new RequestValidationException("Not a TextMessage, aborting!");

        } catch (IOException | JMSException e) {
            throw new RequestValidationException("Unable retrieve/parse TextMessage!");
        }

        if (log.isTraceEnabled()) {
            log.trace("Message cardinalities are: {} {} {}",
                    request.getStartPoint().size(),
                    request.getEndPoint().size(),
                    request.getTime().size());
        }

        if (checkNullOrEmpty(request.getStartPoint())
                || checkNullOrEmpty(request.getEndPoint())
                || checkNullOrEmpty(request.getTime())
                || request.getIsArrivalTime() == null) {

            throw new RequestValidationException("Unable to validate request. Required parameters not set.");
        }


        // try to match the cardinality constraints required for m x n requests
        boolean isArrival = request.getIsArrivalTime();
        int startSize = request.getStartPoint().size();
        int endSize = request.getEndPoint().size();
        int timeSize = request.getTime().size();
        boolean valid;

        if (isArrival) {
            valid = startSize == 1 ? (endSize >= 1 && timeSize == endSize) : (endSize == 1 && timeSize == endSize);
        } else {
            valid = startSize == 1 ? (endSize >= 1 && timeSize == 1) : (endSize == 1 && timeSize == startSize);
        }

        if (!valid) {
            throw new RequestValidationException("Unable to validate request. The cardinalities of the lists " +
                    "do not match!");
        }

        return request;
    }

    // -------------------------------------------------------------------------
    // Getters, to be used by super class
    // -------------------------------------------------------------------------

    @Override
    public JMSContext getContext() {
        return actualContext;
    }

    @Override
    public MinimalRequestProcessor getProcessor() {
        return minimalRequestProcessor;
    }

    @Override
    public Logger getLogger() {
        return log;
    }
}
