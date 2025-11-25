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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FooSubscriptionProvider implements AssetSubscriptionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(FooSubscriptionProvider.class);
    private final Reference reference;
    private final FooSubscriptionProviderConfig providerConfig;

    public FooSubscriptionProvider(Reference reference, FooSubscriptionProviderConfig providerConfig) {
        this.reference = reference;
        this.providerConfig = providerConfig;
    }


    @Override
    public void addNewDataListener(NewDataListener listener) throws AssetConnectionException {
        LOGGER.info("listener added for foo subscription provider");
    }


    @Override
    public void removeNewDataListener(NewDataListener listener) throws AssetConnectionException {
        LOGGER.info("listener removed for foo subscription provider");
    }


    @Override
    public void unsubscribe() throws AssetConnectionException {
        LOGGER.info("foo subscription provider unsubscribed");
    }


    @Override
    public FooSubscriptionProviderConfig asConfig() {
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
        final FooSubscriptionProvider that = (FooSubscriptionProvider) obj;
        return super.equals(that)
                && Objects.equals(reference, that.reference)
                && Objects.equals(providerConfig, that.providerConfig);
    }

}
