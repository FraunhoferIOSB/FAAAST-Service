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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AbstractAssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.MqttOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.MqttSubscriptionMultiplexer;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.MqttSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.MqttValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.config.MqttOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.config.MqttSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.config.MqttValueProviderConfig;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection} for the MQTT
 * protocol.
 *
 * <p>Following asset connection operations are supported:
 * <ul>
 * <li>setting values (via MQTT publish)
 * <li>subscribing to values (via MQTT subscribe)
 * </ul>
 *
 * <p>Following asset connection operations are not supported:
 * <ul>
 * <li>reading values
 * <li>executing operations
 * </ul>
 *
 * <p>This implementation currently only supports submodel elements of type
 * {@link org.eclipse.digitaltwin.aas4j.v3.model.Property}
 * resp. {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue}.
 *
 * <p>This class uses a single underlying MQTT connection.
 */
public class MqttAssetConnection extends
        AbstractAssetConnection<MqttAssetConnection, MqttAssetConnectionConfig, MqttValueProviderConfig, MqttValueProvider, MqttOperationProviderConfig, MqttOperationProvider, MqttSubscriptionProviderConfig, MqttSubscriptionProvider> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttAssetConnection.class);
    private MqttClient client;
    private MqttSubscriptionMultiplexer multiplexer;

    @Override
    public String getEndpointInformation() {
        return config.getServerUri();
    }


    @Override
    protected MqttOperationProvider createOperationProvider(Reference reference, MqttOperationProviderConfig providerConfig) {
        throw new UnsupportedOperationException("executing operations via MQTT currently not supported.");
    }


    @Override
    protected MqttSubscriptionProvider createSubscriptionProvider(Reference reference, MqttSubscriptionProviderConfig providerConfig) {
        return new MqttSubscriptionProvider(serviceContext, reference, providerConfig, multiplexer);
    }


    @Override
    protected MqttValueProvider createValueProvider(Reference reference, MqttValueProviderConfig providerConfig) {
        return new MqttValueProvider(serviceContext, reference, client, providerConfig);
    }


    @Override
    protected void doConnect() throws AssetConnectionException {
        try {
            client = new MqttClient(config.getServerUri(), config.getClientId(), new MemoryPersistence());
            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectionLost(Throwable throwable) {
                    connected = false;
                    LOGGER.warn("MQTT asset connection lost (host: {})",
                            config.getServerUri(),
                            throwable);
                }


                @Override
                public void deliveryComplete(IMqttDeliveryToken imdt) {
                    // intentionally left empty
                }


                @Override
                public void messageArrived(String string, MqttMessage mm) throws Exception {
                    // intentionally left empty

                }


                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    if (reconnect) {
                        connected = true;
                        try {
                            // restore lost subscriptions
                            multiplexer.reconnect(client);
                            LOGGER.info("MQTT asset connection reconnected (endpoint: {})", getEndpointInformation());
                        }
                        catch (AssetConnectionException e) {
                            LOGGER.warn("error restoring MQTT subscriptions after connection loss", e);
                        }
                    }
                }

            });
            MqttConnectOptions options = new MqttConnectOptions();
            if (StringUtils.isNotBlank(config.getUsername())) {
                options.setUserName(config.getUsername());
                options.setPassword(config.getPassword() != null
                        ? config.getPassword().toCharArray()
                        : new char[0]);
            }
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            client.connect(options);
            multiplexer = new MqttSubscriptionMultiplexer(serviceContext, client);
        }
        catch (Exception e) {
            throw new AssetConnectionException("initializaing MQTT asset connection failed", e);
        }
    }


    @Override
    protected void doDisconnect() throws AssetConnectionException {
        if (client != null) {
            if (client.isConnected()) {
                try {
                    client.disconnect();
                }
                catch (MqttException e) {
                    LOGGER.debug("MQTT connection could not be properly closed", e);
                }
            }
            try {
                client.close(true);
            }
            catch (MqttException e) {
                LOGGER.debug("MQTT connection could not be properly closed", e);
            }
        }
    }

}
