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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AbstractAssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
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
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Reference;
import java.util.concurrent.ExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.SessionActivityListener;
import org.eclipse.milo.opcua.sdk.client.api.UaSession;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection} for the OPC UA
 * protocol.
 *
 * <p>All asset connection operations are supported.
 *
 * <p>This implementation currently only supports submodel elements of type {@link io.adminshell.aas.v3.model.Property}
 * resp. {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue}.
 *
 * <p>This class uses a single underlying OPC UA connection.
 */
public class OpcUaAssetConnection extends
        AbstractAssetConnection<OpcUaAssetConnection, OpcUaAssetConnectionConfig, OpcUaValueProviderConfig, OpcUaValueProvider, OpcUaOperationProviderConfig, OpcUaOperationProvider, OpcUaSubscriptionProviderConfig, OpcUaSubscriptionProvider> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaAssetConnection.class);
    private static final ValueConverter valueConverter = new ValueConverter();

    private OpcUaClient client;
    private ManagedSubscription opcUaSubscription;

    public OpcUaAssetConnection() {

    }


    protected OpcUaAssetConnection(CoreConfig coreConfig, OpcUaAssetConnectionConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        super(coreConfig, config, serviceContext);
    }


    @Override
    public void close() throws AssetConnectionException {
        if (client != null) {
            try {
                subscriptionProviders.values().stream().forEach(LambdaExceptionHelper.rethrowConsumer(OpcUaSubscriptionProvider::close));
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


    private void createNewSubscription() throws UaException {
        opcUaSubscription = ManagedSubscription.create(client);
        opcUaSubscription.addStatusListener(new ManagedSubscription.StatusListener() {
            @Override
            public void onSubscriptionTransferFailed(ManagedSubscription subscription, StatusCode statusCode) {
                reconnect();
            }
        });
    }


    private void reconnect() {
        try {
            createNewSubscription();
        }
        catch (UaException e) {
            LOGGER.warn("Error re-creating OPC UA subscription after disconnect (endpoint: {})",
                    config.getHost(),
                    e);
        }
        for (var subscriptionProvider: subscriptionProviders.values()) {
            try {
                subscriptionProvider.reconnect(client, opcUaSubscription);
            }
            catch (AssetConnectionException e) {
                LOGGER.warn("Error re-creating OPC UA subscription after disconnect (endpoint: {}, AAS reference: {}, nodeId: {})",
                        config.getHost(),
                        AasUtils.asString(subscriptionProvider.getReference()),
                        subscriptionProvider.getNodeId(),
                        e);
            }
        }
    }


    @Override
    protected OpcUaOperationProvider createOperationProvider(Reference reference, OpcUaOperationProviderConfig providerConfig) throws AssetConnectionException {
        return new OpcUaOperationProvider(serviceContext, client, reference, providerConfig, valueConverter);
    }


    @Override
    protected OpcUaSubscriptionProvider createSubscriptionProvider(Reference reference, OpcUaSubscriptionProviderConfig providerConfig) {
        return new OpcUaSubscriptionProvider(serviceContext, reference, providerConfig, client, opcUaSubscription, valueConverter);
    }


    @Override
    protected OpcUaValueProvider createValueProvider(Reference reference, OpcUaValueProviderConfig providerConfig) throws AssetConnectionException {
        return new OpcUaValueProvider(serviceContext, client, reference, providerConfig, valueConverter);
    }


    @Override
    protected void initConnection(OpcUaAssetConnectionConfig config) throws ConfigurationInitializationException {
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
            client.addSessionActivityListener(new SessionActivityListener() {
                @Override
                public void onSessionActive(UaSession session) {
                    LOGGER.info("OPC UA asset connection established (host: {})", config.getHost());
                }


                @Override
                public void onSessionInactive(UaSession session) {
                    LOGGER.warn("OPC UA asset connection lost (host: {})", config.getHost());
                }
            });
            // without sleep bad timeout while waiting for acknowledge appears from time to time
            Thread.sleep(200);
            createNewSubscription();
        }
        catch (InterruptedException | ExecutionException | UaException e) {
            Thread.currentThread().interrupt();
            throw new ConfigurationInitializationException(String.format("error opening OPC UA connection (endpoint: %s)", config.getHost()), e);
        }
    }

}
