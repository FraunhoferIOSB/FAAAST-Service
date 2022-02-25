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

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.content.ContentDeserializerFactory;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.content.ContentSerializerFactory;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Reference;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of
 * {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection}
 * for the MQTT protocol.
 * <p>
 * Following asset connection operations are supported:
 * <p>
 * <ul>
 * <li>setting values (via MQTT publish)
 * <li>subscribing to values (via MQTT subscribe)
 * </ul>
 * <p>
 * Following asset connection operations are not supported:
 * <p>
 * <ul>
 * <li>reading values
 * <li>executing operations
 * </ul>
 * <p>
 * This implementation currently only supports submodel elements of type
 * {@link io.adminshell.aas.v3.model.Property} resp.
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue}.
 * <p>
 * This class uses a single underlying MQTT connection.
 */
public class MqttAssetConnection
        implements AssetConnection<MqttAssetConnectionConfig, MqttValueProviderConfig, MqttOperationProviderConfig, MqttSubscriptionProviderConfig> {

    private static Logger logger = LoggerFactory.getLogger(MqttAssetConnection.class);
    private MqttClient client;
    private MqttAssetConnectionConfig config;
    private ServiceContext context;
    private final Map<Reference, AssetOperationProvider> operationProviders;
    private final Map<Reference, AssetSubscriptionProvider> subscriptionProviders;
    private MqttClient client;
    private ServiceContext serviceContext;


    public MqttAssetConnection() {
        this.valueProviders = new HashMap<>();
        this.operationProviders = new HashMap<>();
        this.subscriptionProviders = new HashMap<>();
    }


    @Override
    public MqttAssetConnectionConfig asConfig() {
        return config;
    }


    @Override
    public void close() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            }
            catch (MqttException ex) {
                logger.debug("MQTT connection could not be properly closed", ex);
            }
            try {
                client.close(true);
            }
            catch (MqttException ex) {
                logger.debug("MQTT connection could not be properly closed", ex);
            }
        }
    }


    @Override
    public void init(CoreConfig coreConfig, MqttAssetConnectionConfig config, ServiceContext serviceContext) throws AssetConnectionException {
        if (config == null) {
            throw new IllegalArgumentException("config must be non-null");
        }
        if (serviceContext == null) {
            throw new IllegalArgumentException("serviceContext must be non-null");
        }

        this.config = config;
        this.serviceContext = serviceContext;
        try {
            client = new MqttClient(config.getServerUri(), config.getClientId(), new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            //todo: add SSL
            //options.setUserName("");
            //options.setPassword("".toCharArray());
            //SSLSocketFactory ssf = configureSSLSocketFactory();
            //options.setSocketFactory(ssf);
            client.connect(options);

        }
        catch (MqttException ex) {
            throw new AssetConnectionException("initializaing MQTT asset connection failed", ex);
        }
        for (var providerConfig: config.getValueProviders().entrySet()) {
            registerValueProvider(providerConfig.getKey(), providerConfig.getValue());
        }
        for (var providerConfig: config.getOperationProviders().entrySet()) {
            registerOperationProvider(providerConfig.getKey(), providerConfig.getValue());
        }
        for (var providerConfig: config.getSubscriptionProviders().entrySet()) {
            registerSubscriptionProvider(providerConfig.getKey(), providerConfig.getValue());
        }
    }


    /**
     * {@inheritdoc}
     *
     * @throws UnsupportedOperationException as this operation is not supported
     */
    @Override
    public void registerOperationProvider(Reference reference, MqttOperationProviderConfig operationProviderConfig) throws AssetConnectionException {
        throw new UnsupportedOperationException("executing operations via MQTT not supported.");
    }


    @Override
    public void registerSubscriptionProvider(Reference reference, MqttSubscriptionProviderConfig subscriptionProviderConfig) throws AssetConnectionException {
        this.subscriptionProviders.put(reference, new AssetSubscriptionProvider() {
            @Override
            public void addNewDataListener(NewDataListener listener) throws AssetConnectionException {
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable throwable) {
                        // TODO how to forward exception?
                        logger.debug("MQTT asset connection lost (reference: {}, url: {})",
                                AasUtils.asString(reference),
                                config.getServerUri(),
                                throwable);
                    }


                    @Override
                    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                        listener.newDataReceived(ContentDeserializerFactory
                                .create(subscriptionProviderConfig.getContentFormat())
                                .read(new String(mqttMessage.getPayload()),
                                        subscriptionProviderConfig.getQuery(),
                                        serviceContext.getTypeInfo(reference)));
                    }


                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
                });
                try {
                    client.subscribe(subscriptionProviderConfig.getTopic());
                }
                catch (MqttException ex) {
                    // TODO how to forward exception?
                    logger.error("error subscribing to MQTT asset connection (reference: {}, topic: {})",
                            AasUtils.asString(reference),
                            subscriptionProviderConfig.getTopic(),
                            ex);
                }
            }


            @Override
            public void removeNewDataListener(NewDataListener listener) {
                try {
                    client.unsubscribe(subscriptionProviderConfig.getTopic());
                }
                catch (MqttException ex) {
                    // TODO how to forward exception?
                    logger.error("error unsubscribing from MQTT asset connection (reference: {}, topic: {})",
                            AasUtils.asString(reference),
                            subscriptionProviderConfig.getTopic(),
                            ex);
                }
            }
        });
    }


    @Override
    public void registerValueProvider(Reference reference, MqttValueProviderConfig valueProviderConfig) throws AssetConnectionException {
        this.valueProviders.put(reference, new AssetValueProvider() {
            @Override
            public DataElementValue getValue() throws AssetConnectionException {
                throw new UnsupportedOperationException("Not supported.");
            }


            @Override
            public void setValue(DataElementValue value) throws AssetConnectionException {
                try {
                    if (!(value instanceof PropertyValue)) {
                        throw new AssetConnectionException(String.format("unsupported value (%s)", value.getClass().getSimpleName()));
                    }
                    client.publish(
                            valueProviderConfig.getTopic(),
                            new MqttMessage(ContentSerializerFactory
                                    .create(valueProviderConfig.getContentFormat())
                                    .write(value, valueProviderConfig.getQuery())
                                    .getBytes()));
                }
                catch (MqttException ex) {
                    throw new AssetConnectionException("writing value via MQTT asset connection failed", ex);
                }
            }
        });
    }


    @Override
    public boolean sameAs(AssetConnection other) {
        return false;
    }


    @Override
    public void unregisterOperationProvider(Reference reference) {
        this.operationProviders.remove(reference);
    }


    @Override
    public void unregisterSubscriptionProvider(Reference reference) {
        this.subscriptionProviders.remove(reference);
    }


    @Override
    public void unregisterValueProvider(Reference reference) {
        this.valueProviders.remove(reference);
    }

}
