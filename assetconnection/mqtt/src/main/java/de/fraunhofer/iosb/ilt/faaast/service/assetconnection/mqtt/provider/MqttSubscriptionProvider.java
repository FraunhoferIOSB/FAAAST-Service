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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.content.ContentDeserializerFactory;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Reference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MqttSubscriptionProvider implements AssetSubscriptionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttSubscriptionProvider.class);
    private final MqttClient client;
    private final MqttSubscriptionProviderConfig providerConfig;
    private final Reference reference;
    private final ServiceContext serviceContext;
    private List<NewDataListener> listeners;

    public MqttSubscriptionProvider(ServiceContext serviceContext, MqttClient client, Reference reference, MqttSubscriptionProviderConfig providerConfig) {
        this.listeners = new CopyOnWriteArrayList<>();
        this.serviceContext = serviceContext;
        this.client = client;
        this.reference = reference;
        this.providerConfig = providerConfig;
    }


    private void subscribe() throws AssetConnectionException {
        try {
            client.subscribe(providerConfig.getTopic(), (topic, message) -> {
                listeners.forEach(x -> {
                    try {
                        x.newDataReceived(ContentDeserializerFactory
                                .create(providerConfig.getContentFormat())
                                .read(new String(message.getPayload()),
                                        providerConfig.getQuery(),
                                        serviceContext.getTypeInfo(reference)));
                    }
                    catch (AssetConnectionException e) {
                        LOGGER.error("error deserializing MQTT message (reference: {}, topic: {}, received message: {}",
                                AasUtils.asString(reference),
                                topic,
                                new String(message.getPayload()),
                                e);
                    }
                });
            });
        }
        catch (MqttException e) {
            throw new AssetConnectionException(
                    String.format("error subscribing to MQTT asset connection (reference: %s, topic: %s)",
                            AasUtils.asString(reference),
                            providerConfig.getTopic()),
                    e);
        }

    }


    private void unsubscribe() throws AssetConnectionException {
        try {
            client.unsubscribe(providerConfig.getTopic());
        }
        catch (MqttException e) {
            throw new AssetConnectionException(
                    String.format("error unsubscribing from MQTT asset connection (reference: %s, topic: %s)",
                            AasUtils.asString(reference),
                            providerConfig.getTopic()),
                    e);
        }
    }


    @Override
    public void addNewDataListener(NewDataListener listener) throws AssetConnectionException {
        if (listeners.isEmpty()) {
            subscribe();
        }
        listeners.add(listener);
    }


    @Override
    public void removeNewDataListener(NewDataListener listener) throws AssetConnectionException {
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            unsubscribe();
        }
    }
}
