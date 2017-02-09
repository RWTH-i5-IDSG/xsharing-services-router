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

import de.rwth.idsg.xsharing.router.core.messaging.RequestValidationException;

import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * JMS interface extending the default message listener to include validation behavior
 *
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public interface SharingMessageListener<T, A> extends MessageListener {
    void onMessage(Message message);
    T validateAndUnwrap(Message message) throws RequestValidationException;
}
