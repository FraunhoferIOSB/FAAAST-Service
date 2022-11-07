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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.config.LambdaOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.config.LambdaSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.config.LambdaValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import io.adminshell.aas.v3.model.Reference;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Asset connection implementation that executes Java code.
 */
public class LambdaAssetConnection extends
        AbstractAssetConnection<LambdaAssetConnection, LambdaAssetConnectionConfig, LambdaValueProviderConfig, AssetValueProvider, LambdaOperationProviderConfig, AssetOperationProvider, LambdaSubscriptionProviderConfig, AssetSubscriptionProvider> {

    public LambdaAssetConnection() {
        super();
    }


    public LambdaAssetConnection(
            CoreConfig coreConfig,
            ServiceContext serviceContext,
            Map<Reference, AssetValueProvider> valueProviders,
            Map<Reference, AssetOperationProvider> operationProviders,
            Map<Reference, AssetSubscriptionProvider> subscriptionProviders) throws ConfigurationInitializationException {
        super();
        LambdaAssetConnectionConfig config = new LambdaAssetConnectionConfig();
        if (valueProviders != null) {
            config.setValueProviders(valueProviders.entrySet().stream()
                    .collect(Collectors.toMap(
                            x -> x.getKey(),
                            x -> LambdaValueProviderConfig.builder().implementation(x.getValue()).build())));
        }
        if (operationProviders != null) {
            config.setOperationProviders(operationProviders.entrySet().stream()
                    .collect(Collectors.toMap(
                            x -> x.getKey(),
                            x -> LambdaOperationProviderConfig.builder().implementation(x.getValue()).build())));
        }
        if (subscriptionProviders != null) {
            config.setSubscriptionProviders(subscriptionProviders.entrySet().stream()
                    .collect(Collectors.toMap(
                            x -> x.getKey(),
                            x -> LambdaSubscriptionProviderConfig.builder().implementation(x.getValue()).build())));
        }
        init(coreConfig, config, serviceContext);
    }


    @Override
    public void close() throws AssetConnectionException {
        // intentionally left empty
    }


    @Override
    protected AssetOperationProvider createOperationProvider(Reference reference, LambdaOperationProviderConfig providerConfig) throws AssetConnectionException {
        return providerConfig.getImplementation();
    }


    @Override
    protected AssetSubscriptionProvider createSubscriptionProvider(Reference reference, LambdaSubscriptionProviderConfig providerConfig) throws AssetConnectionException {
        return providerConfig.getImplementation();
    }


    @Override
    protected AssetValueProvider createValueProvider(Reference reference, LambdaValueProviderConfig providerConfig) throws AssetConnectionException {
        return providerConfig.getImplementation();
    }


    @Override
    protected void initConnection(LambdaAssetConnectionConfig config) throws ConfigurationInitializationException {
        // intentionally left empty
    }

}
