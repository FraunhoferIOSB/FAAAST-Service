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

import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
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
 * MessageBusInternal: Implements the internal MessageBus Implements
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


    /**
     * Initialize MessageBus
     *
     * @param coreConfig coreConfig
     * @param config an instance of the corresponding message bus configuration
     *            class
     */
    @Override
    public void init(CoreConfig coreConfig, MessageBusInternalConfig config) {
        running.set(false);
    }


    /**
     * Publish a new EventMessage to the message bus
     *
     * @param message which should be published
     * @throws java.lang.InterruptedException
     */
    @Override
    public void publish(EventMessage message) throws InterruptedException {
        if (message != null) {
            try {
                messageQueue.put(message);
            }
            catch (InterruptedException e) {
                throw e;
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


    /**
     * Start MessageBus-Thread
     */
    @Override
    public void start() {
        new Thread(this).start();
    }


    /**
     * Stop MessageBus-Thread
     */
    @Override
    public void stop() {
        running.set(false);
    }


    /**
     * Subscribe to event messages published in the message bus. The
     * Subscription Info determines which event messages are considered in
     * detail
     *
     * @param subscriptionInfo to determine which event messages should be
     *            considered
     * @return the id of the created subscription in the message bus The id can
     *         be used to update/unsubscribe this subscription.
     */
    @Override
    public SubscriptionId subscribe(SubscriptionInfo subscriptionInfo) {
        if (subscriptionInfo == null) {
            throw new IllegalArgumentException("subscription must be non-null");
        }
        SubscriptionId subscriptionId = new SubscriptionId();
        subscriptions.put(subscriptionId, subscriptionInfo);
        return subscriptionId;
    }


    /**
     * Unsubscribe from a specific subscription by id
     *
     * @param id of the subscription which should be deleted
     */
    @Override
    public void unsubscribe(SubscriptionId id) {
        subscriptions.remove(id);
    }

}
