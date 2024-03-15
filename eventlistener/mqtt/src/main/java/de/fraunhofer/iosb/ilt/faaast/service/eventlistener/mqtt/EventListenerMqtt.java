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
package de.fraunhofer.iosb.ilt.faaast.service.eventlistener.mqtt;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonEventDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.eventlistener.EventListener;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EventListenerException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionId;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Implementation of {@link de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage} for in memory storage.
 */
public class EventListenerMqtt implements EventListener<EventListenerMqttConfig> {

    private EventListenerMqttConfig config;
    private final Map<SubscriptionId, SubscriptionInfo> subscriptions;
    private final JsonEventDeserializer deserializer;
    private PahoClient client;
    private SubscriptionId subscriptionId;
    private Rule rule;
    private RuleHandler ruleHandler;

    public EventListenerMqtt() {
        subscriptions = new ConcurrentHashMap<>();
        deserializer = new JsonEventDeserializer();
    }


    @Override
    public void init(CoreConfig coreConfig, EventListenerMqttConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
        Ensure.requireNonNull(config.getRule(), "rule must be non-null");
        this.rule = new Rule(config.getRule());
        this.ruleHandler = new RuleHandler(config.getHost());
        client = new PahoClient(config);
    }


    @Override
    public EventListenerMqttConfig asConfig() {
        return config;
    }


    @Override
    public void start() throws EventListenerException {
        client.start();
        subscriptionId = subscribe(SubscriptionInfo.create(ValueChangeEventMessage.class,
                LambdaExceptionHelper.wrap(x -> {
                    this.ruleHandler.handle(this.rule, x.getNewValue(), x.getElement());
                })));
    }


    @Override
    public void stop() {
        unsubscribe(subscriptionId);
        client.stop();
    }


    /**
     * Subscribes to the events.
     *
     * @param subscriptionInfo
     * @return id
     */
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


    /**
     * Unsubscribes the events.
     *
     * @param id
     */
    public void unsubscribe(SubscriptionId id) {
        SubscriptionInfo info = subscriptions.get(id);
        Ensure.requireNonNull(info.getSubscribedEvents(), "subscriptionInfo must be non-null");
        subscriptions.get(id).getSubscribedEvents().stream().forEach(a -> //find all events for given abstract or event
        determineEvents((Class<? extends EventMessage>) a).stream().forEach(e -> //unsubscribe from all events
        client.unsubscribe(config.getTopicPrefix() + e.getSimpleName())));
        subscriptions.remove(id);
    }
}
