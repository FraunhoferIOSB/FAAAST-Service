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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Abstract default implementation of class
 * {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection}.This class exists to simplify creating
 * asset connections and reduce redundancy. If you are looking to create a custom asset connection, use this class.
 *
 * @param <T> concrete type of asset connection
 * @param <C> corresponding config type
 * @param <VC> type of value provider config
 * @param <V> type of value provider
 * @param <OC> type of operation provider config
 * @param <O> type of operation provider
 * @param <SC> type of subscription config
 * @param <S> type of subscription
 */
public abstract class AbstractAssetConnection<T extends AssetConnection<C, VC, V, OC, O, SC, S>, C extends AssetConnectionConfig<T, VC, OC, SC>, VC extends AssetValueProviderConfig, V extends AssetValueProvider, OC extends AssetOperationProviderConfig, O extends AssetOperationProvider, SC extends AssetSubscriptionProviderConfig, S extends AssetSubscriptionProvider>
        implements AssetConnection<C, VC, V, OC, O, SC, S> {

    protected volatile boolean connected;
    protected static final String ERROR_MSG_REFERENCE_NOT_NULL = "reference must be non-null";
    protected static final String ERROR_MSG_PROVIDER_CONFIG_NOT_NULL = "providerConfig must be non-null";
    protected C config;
    protected final Map<Reference, O> operationProviders;
    protected ServiceContext serviceContext;
    protected final Map<Reference, S> subscriptionProviders;
    protected final Map<Reference, V> valueProviders;

    protected AbstractAssetConnection() {
        connected = false;
        valueProviders = new HashMap<>();
        operationProviders = new HashMap<>();
        subscriptionProviders = new HashMap<>();
    }


    protected AbstractAssetConnection(CoreConfig coreConfig, C config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this();
        init(coreConfig, config, serviceContext);
    }


    @Override
    public boolean isConnected() {
        return connected;
    }


    public void setConnected(boolean connected) {
        this.connected = connected;
    }


    @Override
    public C asConfig() {
        return config;
    }


    @Override
    public Map<Reference, V> getValueProviders() {
        return this.valueProviders;
    }


    @Override
    public Map<Reference, O> getOperationProviders() {
        return this.operationProviders;
    }


    @Override
    public Map<Reference, S> getSubscriptionProviders() {
        return this.subscriptionProviders;
    }


    /**
     * Connects to the asset.
     *
     * @throws AssetConnectionException if connecting fails
     */
    protected abstract void doConnect() throws AssetConnectionException;


    /**
     * Closes the asset connection.
     *
     * @throws AssetConnectionException if closing fails
     */
    protected abstract void doDisconnect() throws AssetConnectionException;


    @Override
    public void connect() throws AssetConnectionException {
        doConnect();
        connected = true;
        registerProviders();
    }


    @Override
    public void disconnect() throws AssetConnectionException {
        doDisconnect();
        unregisterProviders();
        connected = false;
    }


    private void unregisterProviders() {
        for (var providerConfig: config.getValueProviders().entrySet()) {
            unregisterValueProvider(providerConfig.getKey());
        }
        for (var providerConfig: config.getOperationProviders().entrySet()) {
            unregisterOperationProvider(providerConfig.getKey());
        }
        for (var providerConfig: config.getSubscriptionProviders().entrySet()) {
            unregisterSubscriptionProvider(providerConfig.getKey());
        }
    }


    private void registerProviders() throws AssetConnectionException {
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


    @Override
    public void init(CoreConfig coreConfig, C config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        Ensure.requireNonNull(coreConfig, "coreConfig must be non-null");
        Ensure.requireNonNull(config, "config must be non-null");
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        this.config = config;
        this.serviceContext = serviceContext;
    }


    @Override
    public void registerValueProvider(Reference reference, VC providerConfig) throws AssetConnectionException {
        Ensure.requireNonNull(reference, ERROR_MSG_REFERENCE_NOT_NULL);
        Ensure.requireNonNull(providerConfig, ERROR_MSG_PROVIDER_CONFIG_NOT_NULL);
        this.valueProviders.put(reference, createValueProvider(reference, providerConfig));
    }


    @Override
    public void registerOperationProvider(Reference reference, OC providerConfig) throws AssetConnectionException {
        Ensure.requireNonNull(reference, ERROR_MSG_REFERENCE_NOT_NULL);
        Ensure.requireNonNull(providerConfig, ERROR_MSG_PROVIDER_CONFIG_NOT_NULL);
        this.operationProviders.put(reference, createOperationProvider(reference, providerConfig));
    }


    @Override
    public void registerSubscriptionProvider(Reference reference, SC providerConfig) throws AssetConnectionException {
        Ensure.requireNonNull(reference, ERROR_MSG_REFERENCE_NOT_NULL);
        Ensure.requireNonNull(providerConfig, ERROR_MSG_PROVIDER_CONFIG_NOT_NULL);
        this.subscriptionProviders.put(reference, createSubscriptionProvider(reference, providerConfig));
    }


    /**
     * Creates a new value provider instance.
     *
     * @param reference the reference to create the value provider for
     * @param providerConfig the value provider configuration
     * @return then new value provider instance
     * @throws de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException if creation fails
     */
    protected abstract V createValueProvider(Reference reference, VC providerConfig) throws AssetConnectionException;


    /**
     * Creates a new operation provider instance.
     *
     * @param reference the reference to create the operation provider for
     * @param providerConfig the operation provider configuration
     * @return then new operation provider instance
     * @throws de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException if creation fails
     */
    protected abstract O createOperationProvider(Reference reference, OC providerConfig) throws AssetConnectionException;


    /**
     * Creates a new subscription provider instance.
     *
     * @param reference the reference to create the subscription provider for
     * @param providerConfig the subscription provider configuration
     * @return then new subscription provider instance
     * @throws de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException if creation fails
     */
    protected abstract S createSubscriptionProvider(Reference reference, SC providerConfig) throws AssetConnectionException;


    @Override
    public boolean sameAs(AssetConnection other) {
        return Objects.equals(this, other);
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

}
