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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.content.ContentSerializerFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MqttValueProvider implements AssetValueProvider {

    private final MqttClient client;
    private final MqttValueProviderConfig providerConfig;

    /**
     * Creates new instance.
     *
     * @param client MQTT client to use, must be non-null
     * @param providerConfig configuration, must be non-null
     * @throws IllegalArgumentException if client is null
     * @throws IllegalArgumentException if providerConfig is null
     */
    public MqttValueProvider(MqttClient client, MqttValueProviderConfig providerConfig) {
        if (client == null) {
            throw new IllegalArgumentException("client must be non-null");
        }
        if (providerConfig == null) {
            throw new IllegalArgumentException("providerConfig must be non-null");
        }
        this.client = client;
        this.providerConfig = providerConfig;
    }


    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException as this operation is not supported
     */
    @Override
    public DataElementValue getValue() throws AssetConnectionException {
        throw new UnsupportedOperationException("reading values via MQTT not supported.");
    }


    @Override
    public void setValue(DataElementValue value) throws AssetConnectionException {
        try {
            if (!(value instanceof PropertyValue)) {
                throw new AssetConnectionException(String.format("unsupported value (%s)", value.getClass().getSimpleName()));
            }
            client.publish(
                    providerConfig.getTopic(),
                    new MqttMessage(ContentSerializerFactory
                            .create(providerConfig.getContentFormat())
                            .write(value, providerConfig.getQuery())
                            .getBytes()));
        }
        catch (MqttException e) {
            throw new AssetConnectionException("writing value via MQTT asset connection failed", e);
        }
    }
}
