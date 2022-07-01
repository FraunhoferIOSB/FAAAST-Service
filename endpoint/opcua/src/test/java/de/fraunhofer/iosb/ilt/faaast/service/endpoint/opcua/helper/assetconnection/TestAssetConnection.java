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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Reference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tino Bischoff
 */
@SuppressWarnings("rawtypes")
public class TestAssetConnection implements AssetConnection<TestAssetConnectionConfig, TestValueProviderConfig, TestOperationProviderConfig, TestSubscriptionProviderConfig> {

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
    public void registerValueProvider(Reference reference, TestValueProviderConfig valueProvider) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public void registerOperationProvider(Reference reference, TestOperationProviderConfig operationProvider) {
        try {
            operationProviders.put(reference, new AssetOperationProvider() {
                @Override
                public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
                    LOGGER.info("method invoked!");
                    return operationProvider.getOutputArgs().toArray(OperationVariable[]::new);
                    //return new OperationVariable[0];
                }


                @Override
                public void invokeAsync(OperationVariable[] input, OperationVariable[] inoutput, BiConsumer<OperationVariable[], OperationVariable[]> callback)
                        throws AssetConnectionException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
        }
        catch (Exception e) {
            LOGGER.error("registerOperationProvider error", e);
        }
    }


    @Override
    public void registerSubscriptionProvider(Reference reference, TestSubscriptionProviderConfig subscriptionProvider) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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


    @Override
    public TestAssetConnectionConfig asConfig() {
        return null;
    }


    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public void unregisterOperationProvider(Reference reference) throws AssetConnectionException {
        this.subscriptionProviders.remove(reference);
    }


    @Override
    public void unregisterSubscriptionProvider(Reference reference) throws AssetConnectionException {
        this.subscriptionProviders.remove(reference);
    }


    @Override
    public void unregisterValueProvider(Reference reference) throws AssetConnectionException {
        this.subscriptionProviders.remove(reference);
    }


    @Override
    public void init(CoreConfig coreConfig, TestAssetConnectionConfig config, ServiceContext context) {
        LOGGER.info("init called");
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
