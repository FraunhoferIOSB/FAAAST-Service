/*
 * Copyright (c) 2021 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.dummy;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionId;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MessageBusDummy implements MessageBus<MessageBusDummyConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBusDummy.class);
    private MessageBusDummyConfig config;

    public MessageBusDummy() {

    }


    @Override
    public MessageBusDummyConfig asConfig() {
        return config;
    }


    @Override
    public void init(CoreConfig coreConfig, MessageBusDummyConfig config, ServiceContext serviceContext) {
        this.config = config;
    }


    @Override
    public void publish(EventMessage message) throws MessageBusException {

    }


    private void run() {

    }


    @Override
    public void start() {}


    @Override
    public void stop() {

    }


    @Override
    public SubscriptionId subscribe(SubscriptionInfo subscriptionInfo) {
        Ensure.requireNonNull(subscriptionInfo, "subscriptionInfo must be non-null");
        SubscriptionId subscriptionId = new SubscriptionId();
        return subscriptionId;
    }


    @Override
    public void unsubscribe(SubscriptionId id) {}

}
