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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.conversion.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.adminshell.aas.v3.model.Reference;
import java.util.Objects;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;


/**
 * Superclass for all OPC UA provider classes
 *
 * @param <T> type of the asset provider config
 */
public abstract class AbstractOpcUaProvider<T extends AssetProviderConfig> {

    protected final ServiceContext serviceContext;
    protected final OpcUaClient client;
    protected final Reference reference;
    protected final T providerConfig;
    protected final ValueConverter valueConverter;

    protected AbstractOpcUaProvider(ServiceContext serviceContext,
            OpcUaClient client,
            Reference reference,
            T providerConfig,
            ValueConverter valueConverter) {
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        Ensure.requireNonNull(client, "client must be non-null");
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(providerConfig, "providerConfig must be non-null");
        Ensure.requireNonNull(valueConverter, "valueConverter must be non-null");
        this.serviceContext = serviceContext;
        this.reference = reference;
        this.client = client;
        this.providerConfig = providerConfig;
        this.valueConverter = valueConverter;
    }


    @Override
    public int hashCode() {
        return Objects.hash(serviceContext, client, reference, providerConfig, valueConverter);
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
        final AbstractOpcUaProvider<?> that = (AbstractOpcUaProvider<?>) obj;
        return Objects.equals(serviceContext, that.serviceContext)
                && Objects.equals(client, that.client)
                && Objects.equals(reference, that.reference)
                && Objects.equals(providerConfig, that.providerConfig)
                && Objects.equals(valueConverter, that.valueConverter);
    }

}
