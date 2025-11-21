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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.fixtures.foo;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AbstractAssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FooConnection extends
        AbstractAssetConnection<FooConnection, FooConnectionConfig, FooValueProviderConfig, FooValueProvider, FooOperationProviderConfig, FooOperationProvider, FooSubscriptionProviderConfig, FooSubscriptionProvider> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FooConnection.class);

    @Override
    protected void doConnect() throws AssetConnectionException {
        LOGGER.info("foo connection - connect");
    }


    @Override
    protected void doDisconnect() throws AssetConnectionException {
        LOGGER.info("foo connection - disconnect");
    }


    @Override
    protected FooValueProvider createValueProvider(Reference reference, FooValueProviderConfig providerConfig) throws AssetConnectionException {
        return new FooValueProvider(serviceContext, reference, providerConfig);
    }


    @Override
    protected FooOperationProvider createOperationProvider(Reference reference, FooOperationProviderConfig providerConfig) throws AssetConnectionException {
        return new FooOperationProvider(serviceContext, reference, providerConfig);
    }


    @Override
    protected FooSubscriptionProvider createSubscriptionProvider(Reference reference, FooSubscriptionProviderConfig providerConfig) throws AssetConnectionException {
        return new FooSubscriptionProvider(serviceContext, reference, providerConfig);
    }


    @Override
    public String getEndpointInformation() {
        return "foo endpoint";
    }

}
