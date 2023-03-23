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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AbstractAssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.LambdaOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.LambdaSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.LambdaValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.config.LambdaOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.config.LambdaSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.config.LambdaValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import io.adminshell.aas.v3.model.Reference;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;


/**
 * Asset connection implementation that executes Java code.
 */
public class LambdaAssetConnection extends
        AbstractAssetConnection<LambdaAssetConnection, LambdaAssetConnectionConfig, LambdaValueProviderConfig, LambdaValueProvider, LambdaOperationProviderConfig, LambdaOperationProvider, LambdaSubscriptionProviderConfig, LambdaSubscriptionProvider> {

    public LambdaAssetConnection() {
        super();
    }


    public LambdaAssetConnection(
            CoreConfig coreConfig,
            ServiceContext serviceContext,
            Map<Reference, LambdaValueProvider> valueProviders,
            Map<Reference, LambdaOperationProvider> operationProviders,
            Map<Reference, LambdaSubscriptionProvider> subscriptionProviders) throws ConfigurationInitializationException {
        super();
        LambdaAssetConnectionConfig config = new LambdaAssetConnectionConfig();
        if (valueProviders != null) {
            config.setValueProviders(valueProviders.entrySet().stream()
                    .collect(Collectors.toMap(
                            Entry::getKey,
                            x -> LambdaValueProviderConfig.builder().implementation(x.getValue()).build())));
        }
        if (operationProviders != null) {
            config.setOperationProviders(operationProviders.entrySet().stream()
                    .collect(Collectors.toMap(
                            Entry::getKey,
                            x -> LambdaOperationProviderConfig.builder().implementation(x.getValue()).build())));
        }
        if (subscriptionProviders != null) {
            config.setSubscriptionProviders(subscriptionProviders.entrySet().stream()
                    .collect(Collectors.toMap(
                            Entry::getKey,
                            x -> LambdaSubscriptionProviderConfig.builder().implementation(x.getValue()).build())));
        }
        init(coreConfig, config, serviceContext);
        valueProviders.values().forEach(x -> x.init(serviceContext));
        operationProviders.values().forEach(x -> x.init(serviceContext));
        subscriptionProviders.values().forEach(x -> x.init(serviceContext));
    }


    @Override
    public String getEndpointInformation() {
        return "lambda";
    }


    @Override
    protected LambdaOperationProvider createOperationProvider(Reference reference, LambdaOperationProviderConfig providerConfig) throws AssetConnectionException {
        providerConfig.getImplementation().init(serviceContext);
        return providerConfig.getImplementation();
    }


    @Override
    protected LambdaSubscriptionProvider createSubscriptionProvider(Reference reference, LambdaSubscriptionProviderConfig providerConfig) throws AssetConnectionException {
        providerConfig.getImplementation().init(serviceContext);
        return providerConfig.getImplementation();
    }


    @Override
    protected LambdaValueProvider createValueProvider(Reference reference, LambdaValueProviderConfig providerConfig) throws AssetConnectionException {
        providerConfig.getImplementation().init(serviceContext);
        return providerConfig.getImplementation();
    }


    @Override
    protected void doConnect() throws AssetConnectionException {
        // intentionally left empty
    }


    @Override
    protected void doDisconnect() throws AssetConnectionException {
        // intentionally left empty
    }

}
