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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.conversion.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.OpcUaOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.OpcUaSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.OpcUaValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.model.Reference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of
 * {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection}
 * for the OPC UA protocol.
 * <p>
 * All asset connection operations are supported.
 * <p>
 * This implementation currently only supports submodel elements of type
 * {@link io.adminshell.aas.v3.model.Property} resp.
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue}.
 * <p>
 * This class uses a single underlying OPC UA connection.
 */
public class OpcUaAssetConnection implements AssetConnection<OpcUaAssetConnectionConfig, OpcUaValueProviderConfig, OpcUaOperationProviderConfig, OpcUaSubscriptionProviderConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaAssetConnection.class);
    private static final ValueConverter valueConverter = new ValueConverter();

    private OpcUaClient client;
    private OpcUaAssetConnectionConfig config;
    private ManagedSubscription opcUaSubscription;
    private Map<Reference, AssetOperationProvider> operationProviders;
    private ServiceContext serviceContext;
    private Map<Reference, AssetSubscriptionProvider> subscriptionProviders;
    private Map<Reference, AssetValueProvider> valueProviders;

    public OpcUaAssetConnection() {
        this.valueProviders = new HashMap<>();
        this.operationProviders = new HashMap<>();
        this.subscriptionProviders = new HashMap<>();
    }


    /**
     * Consutrctor to conveniently create and init asset connection from code.
     *
     * @param coreConfig core configuration
     * @param config asset connection configuration
     * @param serviceContext service context which this asset connection is
     *            running in
     * @throws ConfigurationInitializationException if initialization fails
     */
    protected OpcUaAssetConnection(CoreConfig coreConfig, OpcUaAssetConnectionConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this();
        init(coreConfig, config, serviceContext);
    }


    @Override
    public OpcUaAssetConnectionConfig asConfig() {
        return config;
    }


    @Override
    public void close() throws AssetConnectionException {
        if (client != null) {
            try {
                subscriptionProviders.values().stream().forEach(
                        LambdaExceptionHelper.rethrowConsumer(x -> ((OpcUaSubscriptionProvider) x).close()));
            }
            catch (AssetConnectionException e) {
                LOGGER.info("unsubscribing from OPC UA asset connection on connection closing failed", e);
            }
            try {
                client.disconnect().get();
            }
            catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new AssetConnectionException("error closing OPC UA asset connection", e);
            }
        }
    }


    private void connect() throws AssetConnectionException {
        IdentityProvider identityProvider = StringUtils.isAllBlank(config.getUsername())
                ? AnonymousProvider.INSTANCE
                : new UsernameProvider(config.getUsername(), config.getPassword());
        try {
            client = OpcUaClient.create(
                    config.getHost(),
                    endpoints -> endpoints.stream()
                            .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getUri()))
                            .findFirst(),
                    configBuilder -> configBuilder
                            .setApplicationName(LocalizedText.english("AAS-Service"))
                            .setApplicationUri("urn:de:fraunhofer:iosb:aas:service")
                            .setIdentityProvider(identityProvider)
                            .setRequestTimeout(uint(1000))
                            .setAcknowledgeTimeout(uint(1000))
                            .build());
            client.connect().get();
            // without sleep bad timeout while waiting for acknowledge appears from time to time
            Thread.sleep(200);
            opcUaSubscription = ManagedSubscription.create(client);
        }
        catch (InterruptedException | ExecutionException | UaException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException(String.format("error opening OPC UA connection (endpoint: %s)", config.getHost()), e);
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


    @Override
    public void init(CoreConfig coreConfig, OpcUaAssetConnectionConfig config, ServiceContext context) throws ConfigurationInitializationException {
        this.serviceContext = context;
        this.config = config;
        try {
            connect();
            for (var provider: config.getValueProviders().entrySet()) {
                registerValueProvider(provider.getKey(), provider.getValue());
            }
            for (var provider: config.getOperationProviders().entrySet()) {
                registerOperationProvider(provider.getKey(), provider.getValue());
            }
            for (var provider: config.getSubscriptionProviders().entrySet()) {
                registerSubscriptionProvider(provider.getKey(), provider.getValue());
            }
        }
        catch (AssetConnectionException e) {
            throw new ConfigurationInitializationException("initializaing OPC UA asset connection failed", e);
        }
    }


    @Override
    public void registerOperationProvider(Reference reference, OpcUaOperationProviderConfig providerConfig) throws AssetConnectionException {
        this.operationProviders.put(reference, new OpcUaOperationProvider(serviceContext, client, reference, providerConfig, valueConverter));
    }


    @Override
    public void registerSubscriptionProvider(Reference reference, OpcUaSubscriptionProviderConfig providerConfig) throws AssetConnectionException {
        this.subscriptionProviders.put(reference, new OpcUaSubscriptionProvider(serviceContext, reference, providerConfig, client, opcUaSubscription, valueConverter));
    }


    @Override
    public void registerValueProvider(Reference reference, OpcUaValueProviderConfig providerConfig) throws AssetConnectionException {
        this.valueProviders.put(reference, new OpcUaValueProvider(serviceContext, client, reference, providerConfig, valueConverter));
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
