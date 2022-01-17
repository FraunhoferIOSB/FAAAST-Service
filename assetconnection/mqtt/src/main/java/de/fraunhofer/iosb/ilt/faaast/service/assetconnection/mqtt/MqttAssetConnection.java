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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.*;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.content.ContentParserFactory;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import io.adminshell.aas.v3.model.DataElement;
import io.adminshell.aas.v3.model.Reference;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


/**
 * Implementation of MQTT Asset Connection
 */
public class MqttAssetConnection
        implements AssetConnection<MqttAssetConnectionConfig, MqttValueProviderConfig, MqttOperationProviderConfig, MqttSubscriptionProviderConfig> {

    private MqttAssetConnectionConfig config;
    private final Map<Reference, AssetValueProvider> valueProviders;
    private final Map<Reference, AssetOperationProvider> operationProviders;
    private final Map<Reference, AssetSubscriptionProvider> subscriptionProviders;
    private MqttClient client;
    private ServiceContext context;

    public MqttAssetConnection() {
        this.valueProviders = new HashMap<>();
        this.operationProviders = new HashMap<>();
        this.subscriptionProviders = new HashMap<>();
    }


    @Override
    public void close() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            }
            catch (MqttException ex) {}
            try {
                client.close(true);
            }
            catch (MqttException ex) {}
        }
    }


    @Override
    public void init(CoreConfig coreConfig, MqttAssetConnectionConfig config, ServiceContext context) throws AssetConnectionException {
        this.config = config;
        this.context = context;
        try {
            client = new MqttClient(config.getServerURI(), config.getClientID(), new MemoryPersistence());
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
        config.getValueProviders().forEach((k, v) -> {
            try {
                registerValueProvider(k, v);
            }
            catch (AssetConnectionException ex) {
                // TODO rethrow
            }
        });
        config.getOperationProviders().forEach((k, v) -> {
            try {
                registerOperationProvider(k, v);
            }
            catch (AssetConnectionException ex) {
                // TODO rethrow
            }
        });
        config.getSubscriptionProviders().forEach((k, v) -> {
            try {
                registerSubscriptionProvider(k, v);
            }
            catch (AssetConnectionException ex) {
                // TODO rethrow
            }
        });
    }


    @Override
    public MqttAssetConnectionConfig asConfig() {
        return config;
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
                    //check that provided value is a PropertyValue
                    if(!(value instanceof PropertyValue)) {
                        throw new AssetConnectionException(String.format("unsupported value (%s)", value.getClass().getSimpleName()));
                    }
                    client.publish(valueProviderConfig.getTopic(), new MqttMessage(
                            (((PropertyValue) value).getValue().getBytes())
                    ));
                }
                catch (MqttException ex) {
                    throw new AssetConnectionException("writing value via MQTT asset connection failed", ex);
                }
            }
        });
    }


    @Override
    public void registerOperationProvider(Reference reference, MqttOperationProviderConfig operationProviderConfig) throws AssetConnectionException {
        throw new UnsupportedOperationException("Not supported.");
    }


    @Override
    public void registerSubscriptionProvider(Reference reference, MqttSubscriptionProviderConfig subscriptionProviderConfig) throws AssetConnectionException {
        this.subscriptionProviders.put(reference, new AssetSubscriptionProvider() {
            @Override
            public void addNewDataListener(NewDataListener listener) {
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable throwable) {}


                    @Override
                    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                        String mqttValue = new String(mqttMessage.getPayload());
                        Class elementType = context.getElementType(reference);
                        if (!DataElement.class.isAssignableFrom(elementType)) {
                            throw new AssetConnectionException(String.format("unsupported submodel element type (%s)", elementType.getSimpleName()));
                        }
                        DataElementValue newValue = ContentParserFactory
                                .create(subscriptionProviderConfig.getContentFormat())
                                .parseValue(mqttValue, elementType);
                        listener.newDataReceived(newValue);
                    }


                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
                });
                //subscribe to topic
                try {
                    client.subscribe(subscriptionProviderConfig.getTopic());
                }
                catch (MqttException ex) {
                    // throw runtime exception??
                    ex.printStackTrace();
                }
            }


            @Override
            public void removeNewDataListener(NewDataListener listener) {
                //unsubscribe from topic
                try {
                    client.unsubscribe(subscriptionProviderConfig.getTopic());
                }
                catch (MqttException ex) {
                    // throw runtime exception??
                    ex.printStackTrace();
                }
            }
        });
    }


    @Override
    public void unregisterValueProvider(Reference reference) {
        this.valueProviders.remove(reference);
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
    public Map<Reference, AssetValueProvider> getValueProviders() {
        return this.valueProviders;
    }


    @Override
    public Map<Reference, AssetOperationProvider> getOperationProviders() {
        return this.operationProviders;
    }


    @Override
    public Map<Reference, AssetSubscriptionProvider> getSubscriptionProviders() {
        return this.subscriptionProviders;
    }


    @Override
    public boolean sameAs(AssetConnection other) {
        return false;
    }
}
