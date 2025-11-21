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

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProviderConfig;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FooOperationProvider implements AssetOperationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(FooOperationProvider.class);
    private final ServiceContext serviceContext;
    private final Reference reference;
    private final FooOperationProviderConfig providerConfig;

    public FooOperationProvider(ServiceContext serviceContext, Reference reference, FooOperationProviderConfig providerConfig) {
        this.serviceContext = serviceContext;
        this.reference = reference;
        this.providerConfig = providerConfig;
    }


    @Override
    public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
        LOGGER.info("foo operation provider invoked");
        return new OperationVariable[0];
    }


    @Override
    public FooOperationProviderConfig asConfig() {
        return providerConfig;
    }


    @Override
    public AssetOperationProviderConfig getConfig() {
        return providerConfig;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), reference, providerConfig);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FooOperationProvider that = (FooOperationProvider) obj;
        return super.equals(that)
                && Objects.equals(reference, that.reference)
                && Objects.equals(providerConfig, that.providerConfig);
    }

}
