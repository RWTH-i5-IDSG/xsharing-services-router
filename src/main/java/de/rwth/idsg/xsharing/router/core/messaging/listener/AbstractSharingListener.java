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

import com.fasterxml.jackson.core.JsonProcessingException;
import de.rwth.idsg.xsharing.router.core.messaging.RequestValidationException;
import de.rwth.idsg.xsharing.router.utils.JsonMapper;
import de.rwth.idsg.xsharing.router.core.routing.processor.RequestProcessor;
import de.rwth.idsg.xsharing.router.core.routing.request.SharingRequest;
import de.rwth.idsg.xsharing.router.core.routing.response.RouterError;
import de.rwth.idsg.xsharing.router.core.routing.response.SharingResponse;
import org.slf4j.Logger;

import javax.ejb.EJBTransactionRolledbackException;
import javax.jms.DeliveryMode;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.List;

/**
 * Abstract shared implementation of listener behavior Central component for handling incoming messages
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 */
public abstract class AbstractSharingListener<A extends SharingRequest, B extends SharingResponse>
        implements SharingMessageListener<A, B> {

    /**
     * Called when a JMS message is received. Extracts control information (Corr.Id..) and attempts to unwrap request
     * from textmessage
     *
     * @param message received JMS message
     */
    public void onMessage(Message message) {
        if (isValidRequestResponse(message)) {
            try {
                process(message);
            } catch (JMSException | JMSRuntimeException | EJBTransactionRolledbackException jms) {
                getLogger().error("FATAL: Could not send response! {}", jms.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Process
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void process(Message message) throws JMSException {
        A request;
        try {
            request = validateAndUnwrap(message);

        } catch (RequestValidationException ve) {
            getLogger().error("Could not verify request! {}", ve.getMessage());
            List<B> response = getProcessor().buildError(RouterError.ErrorCode.INVALID_REQUEST, ve.getMessage());
            wrapAndSendResponse(message, response);
            return;
        }

        List<B> response = getProcessor().process(request);
        wrapAndSendResponse(message, response);
    }

    /**
     * Since we use a request/response communication style with the client,
     * we must ensure that tha appropriate fields are set.
     */
    private boolean isValidRequestResponse(Message incoming) {
        try {
            if (incoming.getJMSCorrelationID() == null) {
                getLogger().warn("JMSCorrelationID is not set! Will not process request");
                return false;
            }

            if (incoming.getJMSReplyTo() == null) {
                getLogger().warn("JMSReplyTo is not set! Will not process request");
                return false;
            }
        } catch (JMSException e) {
            getLogger().warn(
                    "Failed to read JMSCorrelationID/JMSReplyTo. " +
                    "Will not process request. Exception message = {}", e.getMessage());
            return false;
        }

        return true;
    }

    // -------------------------------------------------------------------------
    // Wrap and send response
    // -------------------------------------------------------------------------

    /**
     * Wrap response in request specific manner
     */
    protected void wrapAndSendResponse(Message incoming, List<B> response) {
        try {
            wrapAndSendResponseInternal(incoming, response);
        } catch (JMSException e) {
            getLogger().error("FATAL: Could not send response! {}", e.getMessage());
        }
    }

    private void wrapAndSendResponseInternal(Message incoming, List<B> response) throws JMSException {
        String text;
        try {
            text = JsonMapper.serialize(response);
        } catch (JsonProcessingException e) {
            getLogger().warn("Failed to convert response to text. Will not send response");
            return;
        }

        getLogger().debug("Response (object): {}", response);
        getLogger().debug("Response (string): {}", text);

        TextMessage msg = getContext().createTextMessage(text);
        msg.setJMSCorrelationID(incoming.getJMSCorrelationID());

        getContext().createProducer()
                    .setDisableMessageID(true)
                    .setDisableMessageTimestamp(true)
                    .setDeliveryMode(DeliveryMode.NON_PERSISTENT)
                    .send(incoming.getJMSReplyTo(), msg);
    }

    // -------------------------------------------------------------------------
    // To be implemented in extending classes to get actual instances
    // -------------------------------------------------------------------------

    public abstract JMSContext getContext();
    public abstract RequestProcessor<A, B> getProcessor();
    public abstract Logger getLogger();
}
