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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.*;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import io.adminshell.aas.v3.model.Reference;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


/**
 * Implementation of MQTT Asset Connection
 */
public class MqttAssetConnection
        implements AssetConnection<MqttAssetConnectionConfig, MqttValueProviderConfig, MqttOperationProviderConfig, MqttSubscriptionProviderConfig> {

    private Map<Reference, AssetValueProvider> valueProviders;
    private Map<Reference, AssetOperationProvider> operationProviders;
    private Map<Reference, AssetSubscriptionProvider> subscriptionProviders;
    private MqttClient client;

    @Override
    public void init(CoreConfig coreConfig, MqttAssetConnectionConfig config) {
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
            ex.printStackTrace();
        }
    }


    @Override
    public MqttAssetConnectionConfig asConfig() {
        return null;
    }


    @Override
    public void registerValueProvider(Reference reference, MqttValueProviderConfig valueProviderConfig) {
        this.valueProviders.put(reference, new AssetValueProvider() {
            @Override
            public DataElementValue getValue() {
                throw new UnsupportedOperationException("Not supported.");
            }


            @Override
            public void setValue(DataElementValue value) {
                try {
                    client.publish(valueProviderConfig.getTopic(), new MqttMessage(value.toString().getBytes()));
                }
                catch (MqttException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }


    @Override
    public void registerOperationProvider(Reference reference, MqttOperationProviderConfig operationProviderConfig) {
        throw new UnsupportedOperationException("Not supported.");
    }


    @Override
    public void registerSubscriptionProvider(Reference reference, MqttSubscriptionProviderConfig subscriptionProviderConfig) {
        this.subscriptionProviders.put(reference, new AssetSubscriptionProvider() {
            @Override
            public void addNewDataListener(NewDataListener listener) {
                //define MqttCallback
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable throwable) {}


                    @Override
                    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                        PropertyValue value = new PropertyValue();
                        switch (subscriptionProviderConfig.getParser()) {
                            case PLAIN:
                                value.setValue(s);
                                listener.newDataReceived(value);
                                break;
                            case XML:
                                value.setValue(parseXML(s));
                                listener.newDataReceived(value);
                                break;
                            case JSON:
                                value.setValue(parseJSON(s));
                                listener.newDataReceived(value);
                                break;
                        }
                    }


                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
                });
                //subscribe to topic
                try {
                    client.subscribe(subscriptionProviderConfig.getTopic());
                }
                catch (MqttException ex) {
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
                    ex.printStackTrace();
                }
            }
        });
    }


    @Override
    public void unregisterValueProvider(Reference reference, MqttValueProviderConfig valueProvider) {
        this.subscriptionProviders.remove(reference, valueProvider);
    }


    @Override
    public void unregisterOperationProvider(Reference reference, MqttOperationProviderConfig operationProvider) {
        this.subscriptionProviders.remove(reference, operationProvider);
    }


    @Override
    public void unregisterSubscriptionProvider(Reference reference, MqttSubscriptionProviderConfig subscriptionProvider) {
        this.subscriptionProviders.remove(reference, subscriptionProvider);
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


    private String parseXML(String xml) {
        //todo: handle XML
        return xml;
    }


    private String parseJSON(String json) {
        //todo: handle JSON
        return json;
    }
}
