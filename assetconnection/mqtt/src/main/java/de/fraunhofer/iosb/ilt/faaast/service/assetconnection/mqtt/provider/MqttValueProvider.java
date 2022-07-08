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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.MultiFormatValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MqttValueProvider extends MultiFormatValueProvider<MqttValueProviderConfig> {

    private final MqttClient client;

    public MqttValueProvider(MqttClient client, MqttValueProviderConfig config) {
        super(config);
        Ensure.requireNonNull(client, "client must be non-null");
        this.client = client;
    }


    @Override
    public DataElementValue getValue() throws AssetConnectionException {
        throw new UnsupportedOperationException("reading values via MQTT not supported.");
    }


    @Override
    protected void setValue(byte[] value) throws AssetConnectionException {
        try {
            client.publish(config.getTopic(), new MqttMessage(value));
        }
        catch (MqttException e) {
            throw new AssetConnectionException("writing value via MQTT asset connection failed", e);
        }
    }
}
