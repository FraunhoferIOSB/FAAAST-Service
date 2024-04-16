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
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * MessageBusInternal: Implements the internal MessageBus interface subscribe/unsubscribe and publishes/dispatches
 * EventMessages to subscribers.
 */
public class MessageBusInternal implements MessageBus<MessageBusInternalConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBusInternal.class);
    private final BlockingQueue<EventMessage> messageQueue;

    private final AtomicBoolean running;
    private final Map<SubscriptionId, SubscriptionInfo> subscriptions;
    private final ExecutorService executor;
    private MessageBusInternalConfig config;

    public MessageBusInternal() {
        running = new AtomicBoolean(false);
        subscriptions = new ConcurrentHashMap<>();
        messageQueue = new LinkedBlockingDeque<>();
        executor = Executors.newSingleThreadExecutor();
    }


    @Override
    public MessageBusInternalConfig asConfig() {
        return config;
    }


    @Override
    public void init(CoreConfig coreConfig, MessageBusInternalConfig config, ServiceContext serviceContext) {
        this.config = config;
        running.set(false);
    }


    @Override
    public void publish(EventMessage message) throws MessageBusException {
        if (message != null) {
            try {
                messageQueue.put(message);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MessageBusException("adding message to queue failed", e);
            }
        }
    }


    private void run() {
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
        executor.submit(this::run);
    }


    @Override
    public void stop() {
        running.set(false);
        executor.shutdown();
        try {
            executor.awaitTermination(2, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            LOGGER.error("interrupted while waiting for shutdown.", e);
            Thread.currentThread().interrupt();
        }
    }


    @Override
    public SubscriptionId subscribe(SubscriptionInfo subscriptionInfo) {
        Ensure.requireNonNull(subscriptionInfo, "subscriptionInfo must be non-null");
        SubscriptionId subscriptionId = new SubscriptionId();
        subscriptions.put(subscriptionId, subscriptionInfo);
        return subscriptionId;
    }


    @Override
    public void unsubscribe(SubscriptionId id) {
        subscriptions.remove(id);
    }

}
