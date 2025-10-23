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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.internalmqttforward;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonEventSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionId;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * MessageBusInternal: Implements the internal MessageBus interface
 * subscribe/unsubscribe and publishes/dispatches EventMessages to subscribers.
 */
public class MessageBusInternalMqttForward implements MessageBus<MessageBusInternalMqttForwardConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBusInternalMqttForward.class);
    private final BlockingQueue<EventMessage> messageQueue;

    private final AtomicBoolean running;
    private final Map<SubscriptionId, SubscriptionInfo> subscriptions;
    private final ExecutorService executor;
    private MessageBusInternalMqttForwardConfig config;
    private final JsonEventSerializer serializer;
    private PahoClient client;
    private List<Class<EventMessage>> eventsToForward;
    private Map<String, Class<EventMessage>> eventClasses = Map.of();

    public MessageBusInternalMqttForward() {
        running = new AtomicBoolean(false);
        subscriptions = new ConcurrentHashMap<>();
        messageQueue = new LinkedBlockingDeque<>();
        executor = Executors.newSingleThreadExecutor();
        serializer = new JsonEventSerializer();
        eventsToForward = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().enableClassInfo().scan()) {
            eventClasses = scanResult.getSubclasses(EventMessage.class).stream()
                    .collect(Collectors.toMap(
                            x -> x.getSimpleName(),
                            x -> (Class<EventMessage>) x.loadClass()));
        }
        eventClasses.put(EventMessage.class.getSimpleName(), EventMessage.class);
    }


    @Override
    public MessageBusInternalMqttForwardConfig asConfig() {
        return config;
    }


    @Override
    public void init(CoreConfig coreConfig, MessageBusInternalMqttForwardConfig config, ServiceContext serviceContext) {
        this.config = config;
        running.set(false);
        client = new PahoClient(config);
        eventsToForward = config.getEventsToForward().stream()
                .map(x -> {
                    if (eventClasses.containsKey(x)) {
                        return eventClasses.get(x);
                    }
                    else {
                        LOGGER.warn("invalid event class in MessageBus InternalMqttFoward (value: {})", x);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
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


    private void publishInteral(EventMessage message) {
        Class<? extends EventMessage> messageType = message.getClass();
        for (SubscriptionInfo subscription: subscriptions.values()) {
            if (subscription.getSubscribedEvents().stream().anyMatch(x -> x.isAssignableFrom(messageType))
                    && subscription.getFilter().test(message.getElement())) {
                subscription.getHandler().accept(message);
            }
        }
    }


    private void publishMqtt(EventMessage message) {
        try {
            Class<? extends EventMessage> messageType = message.getClass();
            if (eventsToForward.stream().anyMatch(x -> x.isAssignableFrom(messageType))) {
                client.publish(config.getTopicPrefix() + messageType.getSimpleName(), serializer.write(message));
            }
        }
        catch (Exception e) {
            LOGGER.debug("failed to forward event to MQTT (reason: {})", e.getMessage(), e);
        }
    }


    private void run() {
        running.set(true);
        try {
            while (running.get()) {
                EventMessage message = messageQueue.take();
                publishInteral(message);
                publishMqtt(message);
            }
        }
        catch (InterruptedException e) {
            LOGGER.warn("MessageBus InternalMqttForward main loop failed (reason: {})", e.getMessage(), e);
        }
    }


    @Override
    public void start() {
        executor.submit(this::run);
        try {
            client.start();
        }
        catch (MessageBusException e) {
            LOGGER.error("failed to gracefully shutdown MQTT connection", e);
        }
    }


    @Override
    public void stop() {
        running.set(false);
        client.stop();
        executor.shutdown();
        try {
            executor.awaitTermination(2, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            LOGGER.error("interrupted while waiting for shutdown.", e);
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
