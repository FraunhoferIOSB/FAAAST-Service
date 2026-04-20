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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents;

import static de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.CloudEventMapperRegistryProvider.defaultRegistry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.CloudEventMapperConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.CloudEventMapperRegistry;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mqtt.client.PahoClient;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mqtt.client.config.MqttClientConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mqtt.client.impl.PasswordBasedPahoClient;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mqtt.client.impl.TokenBasedPahoClient;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternal;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionId;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Uses internal message bus to distribute events within FA³ST Service and MQTT client to send CloudEvents to an MQTT
 * broker.
 */
public class MessageBusCloudEvents implements MessageBus<MessageBusCloudEventsConfig> {

    private static final String PUBLISH_ERROR_MSG = "{} publishing event via CloudEvents MQTT message bus for message type {}";

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBusCloudEvents.class);

    private final MessageBusInternal messageBusInternal;
    private final ExecutorService executor;
    private final ObjectMapper mapper;

    private MessageBusCloudEventsConfig config;
    private PahoClient client;
    private CloudEventMapperRegistry mapperRegistry;

    /**
     * Class constructor.
     */
    public MessageBusCloudEvents() {
        executor = Executors.newSingleThreadExecutor();
        messageBusInternal = new MessageBusInternal();
        mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY)
                .registerModule(JsonFormat.getCloudEventJacksonModule());
    }


    @Override
    public void start() throws MessageBusException {
        messageBusInternal.start();

        client.prepareConnect();
        client.connect();
    }


    @Override
    public void stop() {
        messageBusInternal.stop();
        client.disconnect();
    }


    @Override
    public SubscriptionId subscribe(SubscriptionInfo subscriptionInfo) {
        // Internal subscriptions only. Subscribing to CloudEvents is handled by MQTT broker
        return messageBusInternal.subscribe(subscriptionInfo);
    }


    @Override
    public void unsubscribe(SubscriptionId id) {
        // Internal subscriptions only. Subscribing to CloudEvents is handled by MQTT broker
        messageBusInternal.unsubscribe(id);
    }


    @Override
    public MessageBusCloudEventsConfig asConfig() {
        return config;
    }


    @Override
    public void init(CoreConfig coreConfig, MessageBusCloudEventsConfig config, ServiceContext serviceContext) {
        messageBusInternal.init(coreConfig, MessageBusInternalConfig.builder().build(), serviceContext);

        this.config = config;
        if (config.getIdentityProviderUrl() != null) {
            client = new TokenBasedPahoClient(MqttClientConfig.from(config));
        }
        else {
            client = new PasswordBasedPahoClient(MqttClientConfig.from(config));
        }

        Function<Reference, Referable> referableSupplier = reference -> {
            try {
                return EnvironmentHelper.resolve(reference, serviceContext.getAASEnvironment());
            }
            catch (PersistenceException | ResourceNotFoundException persistenceException) {
                LOGGER.warn("A resource was not found after an event fired.", persistenceException);
                return null;
            }
        };

        mapperRegistry = defaultRegistry(CloudEventMapperConfig.from(config, coreConfig.getCallbackAddress()), referableSupplier);
    }


    @Override
    public void publish(EventMessage message) throws MessageBusException {
        messageBusInternal.publish(message);

        executor.submit(() -> distributeCloudEvent(message));
    }


    private void distributeCloudEvent(EventMessage message) {
        try {
            if (mapperRegistry.canHandle(message)) {
                LOGGER.debug("Publishing {} to {}", message.getClass().getSimpleName(), config.getHost());
                CloudEvent cloudEvent = mapperRegistry.createCloudEvent(message);
                client.publish(config.getTopicPrefix(), mapper.writeValueAsString(cloudEvent));
            }
        }
        catch (JsonProcessingException | MessageBusException publishException) {
            LOGGER.warn(PUBLISH_ERROR_MSG, publishException.getClass().getSimpleName(), message.getClass(),
                    publishException);
        }
    }
}
