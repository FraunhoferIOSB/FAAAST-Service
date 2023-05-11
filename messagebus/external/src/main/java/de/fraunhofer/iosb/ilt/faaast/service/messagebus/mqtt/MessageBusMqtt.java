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
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionId;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * MessageBusMqtt: Implements the external MessageBus interface subscribe/unsubscribe and publishes/dispatches
 * EventMessages.
 */
public class MessageBusMqtt implements MessageBus<MessageBusMqttConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBusMqtt.class);
    private final Map<SubscriptionId, SubscriptionInfo> subscriptions;
    private MessageBusMqttConfig config;
    private MoquetteServer server;
    private PahoClient client;
    private String eventsPrefix = "events/";

    public MessageBusMqtt() {
        subscriptions = new ConcurrentHashMap<>();
    }


    @Override
    public MessageBusMqttConfig asConfig() {
        return config;
    }


    @Override
    public void init(CoreConfig coreConfig, MessageBusMqttConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
        if (config.isInternalBroker()) {
            server = new MoquetteServer(config);
        }
        client = new PahoClient(config);
    }


    @Override
    public void publish(EventMessage message) {
        try {
            Class<? extends EventMessage> messageType = message.getClass();
            JsonApiSerializer serializer = new JsonApiSerializer();
            client.publish(eventsPrefix + messageType.getSimpleName(), serializer.write(message));
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }


    @Override
    public void start() {
        if (config.isInternalBroker()) {
            server.start();
        }
        client.start();
    }


    @Override
    public void stop() {
        if (config.isInternalBroker()) {
            server.stop();
        }
        client.stop();
    }


    @Override
    public SubscriptionId subscribe(SubscriptionInfo subscriptionInfo) {
        Ensure.requireNonNull(subscriptionInfo, "subscriptionInfo must be non-null");
        subscriptionInfo.getSubscribedEvents().forEach((a) -> {
            //get all events corresponding to abstract events
            determineEvents((Class<? extends EventMessage>) a).stream().forEach((e) -> {
                //subscribe to each event
                client.subscribe(eventsPrefix + e.getSimpleName(), (t, message) -> {
                    // deserialize
                    EventMessage event = new JsonApiDeserializer().read(message.toString(), e);
                    // filter
                    if (subscriptionInfo.getFilter().test(event.getElement())) {
                        subscriptionInfo.getHandler().accept(event);
                    }
                });
            });
        });

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
        subscriptions.get(id).getSubscribedEvents().stream().forEach(a ->
        //find all events for given abstract or event
        determineEvents((Class<? extends EventMessage>) a).stream().forEach(e ->
        //unsubscribe from all events
        client.unsubscribe(eventsPrefix + e.getSimpleName())));
        subscriptions.remove(id);
    }
}
