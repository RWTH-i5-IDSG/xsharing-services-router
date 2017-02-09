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
package de.rwth.idsg.xsharing.router.core.messaging.debug;

import de.rwth.idsg.xsharing.router.Constants.JMSConfig;
import de.rwth.idsg.xsharing.router.core.routing.request.CompactRequest;
import de.rwth.idsg.xsharing.router.utils.JsonMapper;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.DeliveryMode;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;
import java.util.Optional;

/**
 * Caller implementation for sending compact requests from admin interface
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Slf4j
public class CompactCaller implements Caller {
    @Inject private JMSContext context;

    @Resource(mappedName = JMSConfig.COMPACT_QUEUE_NAME)
    private Queue minQ;

    public String sendRequest(Optional<String> routeId) {
        CompactRequest req = new CompactRequest(routeId.orElse("asdf"));
        try {
            TextMessage msg = context.createTextMessage(JsonMapper.serializeOrThrow(req));
            msg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);

            Queue answerQ = context.createTemporaryQueue();
            msg.setJMSReplyTo(answerQ);

            context.createProducer().send(minQ, msg);

            Message response = context.createConsumer(answerQ).receive();
            if (response instanceof TextMessage) {
                return ((TextMessage) response).getText();
            }

            return "";
        } catch (JMSException e) {
            return e.getMessage();
        }
    }

}