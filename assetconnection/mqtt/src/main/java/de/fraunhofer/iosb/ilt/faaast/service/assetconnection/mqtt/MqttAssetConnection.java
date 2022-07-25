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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.MqttSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.MqttValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.config.MqttOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.config.MqttSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.config.MqttValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.adminshell.aas.v3.model.Reference;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.plexus.util.StringUtils;
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
 * <ul>
 * <li>setting values (via MQTT publish)
 * <li>subscribing to values (via MQTT subscribe)
 * </ul>
 * <p>
 * Following asset connection operations are not supported:
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

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttAssetConnection.class);
    private MqttClient client;
    private MqttAssetConnectionConfig config;
    private final Map<Reference, AssetOperationProvider> operationProviders;
    private ServiceContext serviceContext;
    private final Map<Reference, AssetSubscriptionProvider> subscriptionProviders;
    private final Map<Reference, AssetValueProvider> valueProviders;

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


    @Override
    public Map<Reference, AssetOperationProvider> getOperationProviders() {
        return this.operationProviders;
    }


    @Override
    public Map<Reference, AssetSubscriptionProvider> getSubscriptionProviders() {
        return this.subscriptionProviders;
    }


    @Override
    public Map<Reference, AssetValueProvider> getValueProviders() {
        return this.valueProviders;
    }


    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if coreConfig is null
     * @throws IllegalArgumentException if config is null
     * @throws IllegalArgumentException if serviceContext if null
     */
    @Override
    public void init(CoreConfig coreConfig, MqttAssetConnectionConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        Ensure.requireNonNull(coreConfig, "coreConfig must be non-null");
        Ensure.requireNonNull(config, "config must be non-null");
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        this.config = config;
        this.serviceContext = serviceContext;
        try {
            client = new MqttClient(config.getServerUri(), config.getClientId(), new MemoryPersistence());
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    LOGGER.warn("MQTT asset connection lost (url: {}, reason: {})",
                            config.getServerUri(),
                            throwable.getMessage(),
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
            });
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            if (StringUtils.isNotBlank(config.getUsername())) {
                options.setUserName(config.getUsername());
                options.setPassword(config.getPassword() != null
                        ? config.getPassword().toCharArray()
                        : new char[0]);
            }
            client.connect(options);
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
        catch (MqttException | AssetConnectionException e) {
            throw new ConfigurationInitializationException("initializaing MQTT asset connection failed", e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException as this operation is not supported
     */
    @Override
    public void registerOperationProvider(Reference reference, MqttOperationProviderConfig providerConfig) throws AssetConnectionException {
        throw new UnsupportedOperationException("executing operations via MQTT not supported.");
    }


    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if reference is null
     * @throws IllegalArgumentException if providerConfig is null
     */
    @Override
    public void registerSubscriptionProvider(Reference reference, MqttSubscriptionProviderConfig providerConfig) throws AssetConnectionException {
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(providerConfig, "providerConfig must be non-null");
        this.subscriptionProviders.put(reference, new MqttSubscriptionProvider(serviceContext, reference, client, providerConfig));
    }


    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if reference is null
     * @throws IllegalArgumentException if providerConfig is null
     */
    @Override
    public void registerValueProvider(Reference reference, MqttValueProviderConfig providerConfig) throws AssetConnectionException {
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(providerConfig, "providerConfig must be non-null");
        this.valueProviders.put(reference, new MqttValueProvider(serviceContext, reference, client, providerConfig));
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
