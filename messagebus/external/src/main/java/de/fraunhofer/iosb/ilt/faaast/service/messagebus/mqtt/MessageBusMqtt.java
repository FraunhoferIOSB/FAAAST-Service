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
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationFinishEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationInvokeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ValueReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.error.ErrorEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
            client.publish("events/" + message.getClass().getSimpleName(), serializer.write(message));
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
        subscriptionInfo.getSubscribedEvents().stream().forEach((a) -> {
            //get all events corresponding to abstract events
            determineEvents((Class<? extends EventMessage>) a).stream().forEach((e) -> {
                //subscribe to each event
                client.subscribe("events/" + e.getSimpleName(), (t, message) -> {
                    // deserialize
                    EventMessage event = new JsonApiDeserializer().read(message.toString(), EventMessage.class);
                    subscriptionInfo.getHandler().accept(event);
                    // filter
                    if (subscriptionInfo.getFilter().test(event.getElement())) {

                    }
                });
            });
        });

        SubscriptionId subscriptionId = new SubscriptionId();
        subscriptions.put(subscriptionId, subscriptionInfo);
        return subscriptionId;
    }


    private Set<Class<? extends EventMessage>> determineEvents(Class<? extends EventMessage> messageType) {
        Set<Class<? extends EventMessage>> set = new HashSet<>();
        String test = messageType.getSimpleName();
        switch (messageType.getSimpleName()) {
            //abstracts access
            case ("ReadEventMessage"):
                set.add(ElementReadEventMessage.class);
                set.add(ValueReadEventMessage.class);
                break;
            case ("AccessEventMessage"):
                set.add(OperationFinishEventMessage.class);
                set.add(OperationInvokeEventMessage.class);
                set.add(ElementReadEventMessage.class);
                set.add(ValueReadEventMessage.class);
                break;
            case ("ExecuteEventMessage"):
                set.add(OperationFinishEventMessage.class);
                set.add(OperationInvokeEventMessage.class);
                break;
            //abstracts change
            case ("ChangeEventMessage"):
                set.add(ElementReadEventMessage.class);
                set.add(ElementChangeEventMessage.class);
                set.add(ElementDeleteEventMessage.class);
                set.add(ElementCreateEventMessage.class);
                set.add(ValueChangeEventMessage.class);
                break;
            case ("ElementChangeEventMessage"):
                set.add(ElementReadEventMessage.class);
                set.add(ElementChangeEventMessage.class);
                set.add(ElementDeleteEventMessage.class);
                break;
            case ("EventMessage"):
                set.add(OperationFinishEventMessage.class);
                set.add(OperationInvokeEventMessage.class);
                set.add(ElementReadEventMessage.class);
                set.add(ElementChangeEventMessage.class);
                set.add(ElementCreateEventMessage.class);
                set.add(ElementDeleteEventMessage.class);
                set.add(ValueReadEventMessage.class);
                set.add(ValueChangeEventMessage.class);
                set.add(ErrorEventMessage.class);
                break;
            //all other events
            default:
                set.add(messageType);
                break;
        }
        return set;
    }


    @Override
    public void unsubscribe(SubscriptionId id) {
        SubscriptionInfo info = subscriptions.get(id);
        Ensure.requireNonNull(info.getSubscribedEvents(), "subscriptionInfo must be non-null");
        subscriptions.get(id).getSubscribedEvents().stream().forEach((a) -> {
            //find all events for given abstract or event
            determineEvents((Class<? extends EventMessage>) a).stream().forEach((e) -> {
                //unsubscribe from all events
                client.unsubscribe("events/" + e.getSimpleName());
            });
        });
        subscriptions.remove(id);
    }
}