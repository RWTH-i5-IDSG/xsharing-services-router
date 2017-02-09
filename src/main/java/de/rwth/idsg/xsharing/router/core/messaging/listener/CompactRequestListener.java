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
import de.rwth.idsg.xsharing.router.core.routing.processor.CompactRequestProcessor;
import de.rwth.idsg.xsharing.router.core.routing.request.CompactRequest;
import de.rwth.idsg.xsharing.router.core.routing.response.CompactResponse;
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

/**
 * Concrete listener bean responsible for binding the actual JMS queue from config and setting the actual
 * JMS context for its parent implementation
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
@MessageDriven(name = "CompactRequestListener", messageListenerInterface = MessageListener.class, activationConfig =
        {
                @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = JMSConfig.COMPACT_QUEUE_NAME),
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JMSConfig.DESTINATION_TYPE),
        })
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
@Slf4j
public class CompactRequestListener extends AbstractSharingListener<CompactRequest, CompactResponse> {

    @Inject private JMSContext actualContext;
    @Inject private CompactRequestProcessor compactRequestProcessor;

    @Override
    public void onMessage(Message message) {
        long start = System.currentTimeMillis();
        StatsManager.COMPACT.getStats().starting();
        try {
            super.onMessage(message);
        } finally {
            long stop = System.currentTimeMillis();
            StatsManager.COMPACT.getStats().finished(start, stop);
        }
    }

    @Override
    public CompactRequest validateAndUnwrap(Message message) throws RequestValidationException {
        try {
            TextMessage txt = (TextMessage) message;
            return JsonMapper.deserialize(txt.getText(), CompactRequest.class);

        } catch (ClassCastException e) {
            throw new RequestValidationException("Not a TextMessage, aborting!");

        } catch (IOException | JMSException e) {
            throw new RequestValidationException("Unable retrieve/parse TextMessage!");
        }
    }

    // -------------------------------------------------------------------------
    // Getters, to be used by super class
    // -------------------------------------------------------------------------

    @Override
    public JMSContext getContext() {
        return actualContext;
    }

    @Override
    public CompactRequestProcessor getProcessor() {
        return compactRequestProcessor;
    }

    @Override
    public Logger getLogger() {
        return log;
    }
}
