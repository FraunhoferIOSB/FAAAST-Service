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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.mqtt;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonEventDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonEventSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionId;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * MessageBusMqtt: Implements the external MessageBus interface subscribe/unsubscribe and publishes/dispatches
 * EventMessages.
 */
public class MessageBusMqtt implements MessageBus<MessageBusMqttConfig> {

    private final Map<SubscriptionId, SubscriptionInfo> subscriptions;
    private final JsonEventSerializer serializer;
    private final JsonEventDeserializer deserializer;
    private MessageBusMqttConfig config;
    private MoquetteServer server;
    private PahoClient client;

    public MessageBusMqtt() {
        subscriptions = new ConcurrentHashMap<>();
        serializer = new JsonEventSerializer();
        deserializer = new JsonEventDeserializer();
    }


    @Override
    public MessageBusMqttConfig asConfig() {
        return config;
    }


    @Override
    public void init(CoreConfig coreConfig, MessageBusMqttConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
        if (config.getUseInternalServer()) {
            server = new MoquetteServer(config);
        }
        client = new PahoClient(config);
    }


    @Override
    public void publish(EventMessage message) throws MessageBusException {
        try {
            Class<? extends EventMessage> messageType = message.getClass();
            client.publish(config.getTopicPrefix() + messageType.getSimpleName(), serializer.write(message));
        }
        catch (Exception e) {
            throw new MessageBusException("Error publishing event via MQTT message bus", e);
        }
    }


    @Override
    public void start() throws MessageBusException {
        if (config.getUseInternalServer()) {
            try {
                server.start();
            }
            catch (IOException e) {
                throw new MessageBusException("Error starting MQTT server for message bus", e);
            }
        }
        client.start();
    }


    @Override
    public void stop() {
        client.stop();
        if (config.getUseInternalServer()) {
            server.stop();
        }
    }


    @Override
    public SubscriptionId subscribe(SubscriptionInfo subscriptionInfo) {
        Ensure.requireNonNull(subscriptionInfo, "subscriptionInfo must be non-null");
        subscriptionInfo.getSubscribedEvents()
                .forEach(x -> determineEvents((Class<? extends EventMessage>) x).stream()
                        .forEach(e -> client.subscribe(config.getTopicPrefix() + e.getSimpleName(), (t, message) -> {
                            EventMessage event = deserializer.read(message.toString(), e);
                            if (subscriptionInfo.getFilter().test(event.getElement())) {
                                subscriptionInfo.getHandler().accept(event);
                            }
                        })));

        SubscriptionId subscriptionId = new SubscriptionId();
        subscriptions.put(subscriptionId, subscriptionInfo);
        return subscriptionId;
    }


    private List<Class<EventMessage>> determineEvents(Class<? extends EventMessage> messageType) {
        try (ScanResult scanResult = new ClassGraph().acceptPackages("de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event")
                .enableClassInfo().scan()) {
            if (Modifier.isAbstract(messageType.getModifiers())) {
                return scanResult
                        .getSubclasses(messageType.getName())
                        .filter(x -> !x.isAbstract())
                        .loadClasses(EventMessage.class);
            }
            else {
                List<Class<EventMessage>> list = new ArrayList<>();
                list.add((Class<EventMessage>) messageType);
                return list;
            }
        }
    }


    @Override
    public void unsubscribe(SubscriptionId id) {
        SubscriptionInfo info = subscriptions.get(id);
        Ensure.requireNonNull(info.getSubscribedEvents(), "subscriptionInfo must be non-null");
        subscriptions.get(id).getSubscribedEvents().stream().forEach(a -> //find all events for given abstract or event
        determineEvents((Class<? extends EventMessage>) a).stream().forEach(e -> //unsubscribe from all events
        client.unsubscribe(config.getTopicPrefix() + e.getSimpleName())));
        subscriptions.remove(id);
    }
}
