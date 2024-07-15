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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.assetconnection;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestAssetConnection implements
        AssetConnection<TestAssetConnectionConfig, TestValueProviderConfig, AssetValueProvider, TestOperationProviderConfig, AssetOperationProvider, TestSubscriptionProviderConfig, AssetSubscriptionProvider> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestAssetConnection.class);

    private final Map<Reference, AssetValueProvider> valueProviders;
    private final Map<Reference, AssetOperationProvider> operationProviders;
    private final Map<Reference, AssetSubscriptionProvider> subscriptionProviders;

    public TestAssetConnection() {
        valueProviders = new HashMap<>();
        operationProviders = new HashMap<>();
        subscriptionProviders = new HashMap<>();
    }


    @Override
    public void connect() throws AssetConnectionException {
        // nothing to do here
    }


    @Override
    public String getEndpointInformation() {
        return TestAssetConnection.class.getName();
    }


    @Override
    public void registerValueProvider(Reference reference, TestValueProviderConfig valueProvider) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void registerOperationProvider(Reference reference, TestOperationProviderConfig operationProvider) {
        try {
            operationProviders.put(reference, new AssetOperationProvider<AssetOperationProviderConfig>() {
                @Override
                public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
                    LOGGER.trace("method invoked!");
                    return operationProvider.getOutputArgs().toArray(OperationVariable[]::new);
                }


                @Override
                public void invokeAsync(OperationVariable[] input, OperationVariable[] inoutput, BiConsumer<OperationVariable[], OperationVariable[]> callbackSuccess,
                                        Consumer<Throwable> callbackFailure)
                        throws AssetConnectionException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }


                @Override
                public AssetOperationProviderConfig getConfig() {
                    return operationProvider;
                }
            });
        }
        catch (Exception e) {
            LOGGER.error("registerOperationProvider error", e);
        }
    }


    @Override
    public void registerSubscriptionProvider(Reference reference, TestSubscriptionProviderConfig subscriptionProvider) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public Map<Reference, AssetValueProvider> getValueProviders() {
        return valueProviders;
    }


    @Override
    public void disconnect() throws AssetConnectionException {
        // nothing to do here
    }


    @Override
    public boolean isConnected() {
        return true;
    }


    @Override
    public Map<Reference, AssetOperationProvider> getOperationProviders() {
        return operationProviders;
    }


    @Override
    public Map<Reference, AssetSubscriptionProvider> getSubscriptionProviders() {
        return subscriptionProviders;
    }


    @Override
    public boolean sameAs(AssetConnection other) {
        return false;
    }


    @Override
    public TestAssetConnectionConfig asConfig() {
        return null;
    }


    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void unregisterOperationProvider(Reference reference) throws AssetConnectionException {
        if (ReferenceHelper.containsSameReference(operationProviders, reference)) {
            var operation = ReferenceHelper.getEntryBySameReference(operationProviders, reference);
            operationProviders.remove(operation.getKey());
        }
    }


    @Override
    public void unregisterSubscriptionProvider(Reference reference) throws AssetConnectionException {
        if (ReferenceHelper.containsSameReference(subscriptionProviders, reference)) {
            var subscription = ReferenceHelper.getEntryBySameReference(subscriptionProviders, reference);
            subscriptionProviders.remove(subscription.getKey());
        }
    }


    @Override
    public void unregisterValueProvider(Reference reference) throws AssetConnectionException {
        if (ReferenceHelper.containsSameReference(valueProviders, reference)) {
            var value = ReferenceHelper.getEntryBySameReference(valueProviders, reference);
            valueProviders.remove(value.getKey());
        }
    }


    @Override
    public void init(CoreConfig coreConfig, TestAssetConnectionConfig config, ServiceContext context) {
        LOGGER.trace("init called");
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
}
