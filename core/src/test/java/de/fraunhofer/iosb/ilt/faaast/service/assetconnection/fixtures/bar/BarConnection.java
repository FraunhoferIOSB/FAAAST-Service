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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.fixtures.bar;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AbstractAssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BarConnection extends
        AbstractAssetConnection<BarConnection, BarConnectionConfig, BarValueProviderConfig, BarValueProvider, BarOperationProviderConfig, BarOperationProvider, BarSubscriptionProviderConfig, BarSubscriptionProvider> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BarConnection.class);

    @Override
    protected void doConnect() throws AssetConnectionException {
        LOGGER.info("bar connection - connect");
    }


    @Override
    protected void doDisconnect() throws AssetConnectionException {
        LOGGER.info("bar connection - disconnect");
    }


    @Override
    protected BarValueProvider createValueProvider(Reference reference, BarValueProviderConfig providerConfig) throws AssetConnectionException {
        return new BarValueProvider(serviceContext, reference, providerConfig);
    }


    @Override
    protected BarOperationProvider createOperationProvider(Reference reference, BarOperationProviderConfig providerConfig) throws AssetConnectionException {
        return new BarOperationProvider(serviceContext, reference, providerConfig);
    }


    @Override
    protected BarSubscriptionProvider createSubscriptionProvider(Reference reference, BarSubscriptionProviderConfig providerConfig) throws AssetConnectionException {
        return new BarSubscriptionProvider(serviceContext, reference, providerConfig);
    }


    @Override
    public String getEndpointInformation() {
        return "bar endpoint";
    }

}
