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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.HttpOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.HttpSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.HttpValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.adminshell.aas.v3.model.Reference;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.plexus.util.StringUtils;


/**
 * Implementation of
 * {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection}
 * for the HTTP protocol.
 * <p>
 * Following asset connection operations are supported:
 * <ul>
 * <li>setting values (via HTTP PUT)
 * <li>reading values (via HTTP GET)
 * </ul>
 * <p>
 * Following asset connection operations are not supported:
 * <ul>
 * <li>subscribing to values
 * <li>executing operations
 * </ul>
 * <p>
 * This implementation currently only supports submodel elements of type
 * {@link io.adminshell.aas.v3.model.Property} resp.
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue}.
 * <p>
 * This class uses a single underlying HTTP connection.
 */
public class HttpAssetConnection
        implements AssetConnection<HttpAssetConnectionConfig, HttpValueProviderConfig, HttpOperationProviderConfig, HttpSubscriptionProviderConfig> {

    private HttpClient client;
    private HttpAssetConnectionConfig config;
    private final Map<Reference, AssetOperationProvider> operationProviders;
    private ServiceContext serviceContext;
    private final Map<Reference, AssetSubscriptionProvider> subscriptionProviders;
    private final Map<Reference, AssetValueProvider> valueProviders;

    public HttpAssetConnection() {
        valueProviders = new HashMap<>();
        operationProviders = new HashMap<>();
        subscriptionProviders = new HashMap<>();
    }


    protected HttpAssetConnection(CoreConfig coreConfig, HttpAssetConnectionConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this();
        init(coreConfig, config, serviceContext);
    }


    @Override
    public HttpAssetConnectionConfig asConfig() {
        return config;
    }


    @Override
    public void close() {
        // no need to close a HTTP connection
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
    public void init(CoreConfig coreConfig, HttpAssetConnectionConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        Ensure.requireNonNull(coreConfig, "coreConfig must be non-null");
        Ensure.requireNonNull(config, "config must be non-null");
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        this.config = config;
        this.serviceContext = serviceContext;
        HttpClient.Builder builder = HttpClient.newBuilder();
        if (StringUtils.isNotBlank(config.getUsername())) {
            builder = builder.authenticator(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            config.getUsername(),
                            config.getPassword() != null
                                    ? config.getPassword().toCharArray()
                                    : new char[0]);
                }
            });
        }
        client = builder.build();
        try {
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
        catch (AssetConnectionException e) {
            throw new ConfigurationInitializationException("initializing HTTP asset connection failed", e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException as this operation is not supported
     */
    @Override
    public void registerOperationProvider(Reference reference, HttpOperationProviderConfig providerConfig) throws AssetConnectionException {
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(providerConfig, "providerConfig must be non-null");
        this.operationProviders.put(reference, new HttpOperationProvider(serviceContext, reference, client, config.getBaseUrl(), providerConfig));
    }


    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if reference is null
     * @throws IllegalArgumentException if providerConfig is null
     */
    @Override
    public void registerSubscriptionProvider(Reference reference, HttpSubscriptionProviderConfig providerConfig) throws AssetConnectionException {
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(providerConfig, "providerConfig must be non-null");
        this.subscriptionProviders.put(reference, new HttpSubscriptionProvider(serviceContext, reference, client, config.getBaseUrl(), providerConfig));
    }


    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if reference is null
     * @throws IllegalArgumentException if providerConfig is null
     */
    @Override
    public void registerValueProvider(Reference reference, HttpValueProviderConfig providerConfig) throws AssetConnectionException {
        if (reference == null) {
            throw new IllegalArgumentException("reference must be non-null");
        }
        if (providerConfig == null) {
            throw new IllegalArgumentException("providerConfig must be non-null");
        }
        this.valueProviders.put(reference, new HttpValueProvider(serviceContext, reference, client, config.getBaseUrl(), providerConfig));
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
