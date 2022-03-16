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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionId;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * MessageBusInternal: Implements the internal MessageBus interface
 * subscribe/unsubscribe and publishes/dispatches EventMessages to subscribers
 */
public class MessageBusInternal implements MessageBus<MessageBusInternalConfig>, Runnable {

    private final BlockingQueue<EventMessage> messageQueue;

    private final AtomicBoolean running;
    private final Map<SubscriptionId, SubscriptionInfo> subscriptions;

    public MessageBusInternal() {
        running = new AtomicBoolean(false);
        subscriptions = new ConcurrentHashMap<>();
        messageQueue = new LinkedBlockingDeque<>();
    }


    @Override
    public MessageBusInternalConfig asConfig() {
        return null;
    }


    @Override
    public void init(CoreConfig coreConfig, MessageBusInternalConfig config, ServiceContext context) {
        running.set(false);
    }


    @Override
    public void publish(EventMessage message) throws MessageBusException {
        if (message != null) {
            try {
                messageQueue.put(message);
            }
            catch (InterruptedException e) {
                throw new MessageBusException("adding message to queue failed", e);
            }
        }
    }


    /**
     * Take an EventMessage from the queue. Iterate over all subscribers and
     * check which filter applies and call the subscription handler
     */
    @Override
    public void run() {
        running.set(true);
        try {
            while (running.get()) {
                EventMessage message = messageQueue.take();
                Class<? extends EventMessage> messageType = message.getClass();
                for (SubscriptionInfo subscription: subscriptions.values()) {
                    if (subscription.getSubscribedEvents().stream().anyMatch(x -> x.isAssignableFrom(messageType))
                            && subscription.getFilter().test(message.getElement())) {
                        subscription.getHandler().accept(message);
                    }
                }
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    @Override
    public void start() {
        new Thread(this).start();
    }


    @Override
    public void stop() {
        running.set(false);
    }


    @Override
    public SubscriptionId subscribe(SubscriptionInfo subscriptionInfo) {
        if (subscriptionInfo == null) {
            throw new IllegalArgumentException("subscription must be non-null");
        }
        SubscriptionId subscriptionId = new SubscriptionId();
        subscriptions.put(subscriptionId, subscriptionInfo);
        return subscriptionId;
    }


    @Override
    public void unsubscribe(SubscriptionId id) {
        subscriptions.remove(id);
    }

}
