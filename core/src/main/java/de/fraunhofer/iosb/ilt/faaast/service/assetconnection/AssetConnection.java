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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection;

import de.fraunhofer.iosb.ilt.faaast.service.config.Configurable;
import io.adminshell.aas.v3.model.Reference;
import java.util.Map;


/**
 * Interface for asset connections, i.e. connecting assets to the AAS (a.k.a. lower DT interface).
 * An AssetConnection can support the following operations
 * - get/set data values from/to the asset (via AssetValueProvider)
 * - execute operations on the asset (via AssetOperationProvider)
 * - get values from the asset by subscribe to it (via AssetSubscriptionProvider)
 * 
 * @param <T> corresponding config type
 * @param <V> type of value provider for this asset connection
 * @param <O> type of operation provider for this asset connection
 * @param <S> type of subscription for this asset connection
 */
public interface AssetConnection<T extends AssetConnectionConfig, V extends AssetValueProviderConfig, O extends AssetOperationProviderConfig, S extends AssetSubscriptionProviderConfig>
        extends Configurable<T> {

    /**
     * Registers a value provider for this asset connection.
     * 
     * @param reference Reference to the AAS element that this provider will map to
     * @param valueProvider AssetValueProvider instance to use
     */
    public void registerValueProvider(Reference reference, V valueProvider);


    /**
     * Registers an operation provider for this asset connection.
     * 
     * @param reference Reference to the AAS element that this provider will map to
     * @param operationProvider AssetOperationProvider instance to use
     */
    public void registerOperationProvider(Reference reference, O operationProvider);


    /**
     * Registers a subscription provider for this asset connection.
     * 
     * @param reference Reference to the AAS element that this provider will map to
     * @param subscriptionProvider AssetSubscriptionProvider instance to use
     */
    public void registerSubscriptionProvider(Reference reference, S subscriptionProvider);


    /**
     * Unregisters a value provider for this asset connection.
     * 
     * @param reference Reference to the AAS element that this provider should be unregistered for
     * @param valueProvider AssetValueProvider instance to unregister
     */
    public void unregisterValueProvider(Reference reference, V valueProvider);


    /**
     * Unregisters an operation provider for this asset connection.
     * 
     * @param reference Reference to the AAS element that this provider should be unregistered for
     * @param operationProvider AssetOperationProvider instance to unregister
     */
    public void unregisterOperationProvider(Reference reference, O operationProvider);


    /**
     * Unregisters a subscription provider for this asset connection.
     * 
     * @param reference Reference to the AAS element that this provider should be unregistered for
     * @param subscriptionProvider AssetSubscriptionProvider instance to unregister
     */
    public void unregisterSubscriptionProvider(Reference reference, S subscriptionProvider);


    public Map<Reference, AssetValueProvider> getValueProviders();


    public Map<Reference, AssetOperationProvider> getOperationProviders();


    public Map<Reference, AssetSubscriptionProvider> getSubscriptionProviders();


    /**
     * Compares two instances of AssetConnection if they are referencing the same asset connection.
     * 
     * @param other other AssetConnection to compare to this.
     * @return true if other is the same as this.
     */
    public boolean sameAs(AssetConnection other);
}
