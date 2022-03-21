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
 * Interface for asset connections, i.e. connecting assets to the AAS (a.k.a.
 * lower DT interface). An asset connection can support the following
 * operations:
 * <ul>
 * <li>get/set data values from/to the asset (via {@link AssetValueProvider})
 * <li>execute operations on the asset (via {@link AssetOperationProvider})
 * <li>subscribe to new values from the asset (via
 * {@link AssetSubscriptionProvider})
 * </ul>
 *
 * @param <T> corresponding config type
 * @param <V> type of value provider config
 * @param <O> type of operation provider config
 * @param <S> type of subscription config
 */
public interface AssetConnection<T extends AssetConnectionConfig, V extends AssetValueProviderConfig, O extends AssetOperationProviderConfig, S extends AssetSubscriptionProviderConfig>
        extends Configurable<T> {

    /**
     * Gracefully closes the asset connection.
     *
     * @throws AssetConnectionException if closing fails
     */
    public void close() throws AssetConnectionException;


    public Map<Reference, AssetOperationProvider> getOperationProviders();


    public Map<Reference, AssetSubscriptionProvider> getSubscriptionProviders();


    public Map<Reference, AssetValueProvider> getValueProviders();


    /**
     * Registers an operation provider for this asset connection.
     *
     * @param reference Reference to the AAS element that this provider will map
     *            to
     * @param providerConfig AssetOperationProvider instance to use
     * @throws AssetConnectionException if registering provider fails
     */
    public void registerOperationProvider(Reference reference, O providerConfig) throws AssetConnectionException;


    /**
     * Registers a subscription provider for this asset connection.
     *
     * @param reference Reference to the AAS element that this provider will map
     *            to
     * @param providerConfig AssetSubscriptionProvider instance to use
     * @throws AssetConnectionException if registering provider fails
     */
    public void registerSubscriptionProvider(Reference reference, S providerConfig) throws AssetConnectionException;


    /**
     * Registers a value provider for this asset connection.
     *
     * @param reference Reference to the AAS element that this provider will map
     *            to
     * @param providerConfig AssetValueProvider instance to use
     * @throws AssetConnectionException if registering provider fails
     */
    public void registerValueProvider(Reference reference, V providerConfig) throws AssetConnectionException;


    /**
     * Compares two instances of AssetConnection if they are referencing the
     * same asset connection.
     *
     * @param other other AssetConnection to compare to this.
     * @return true if other is the same as this.
     */
    public boolean sameAs(AssetConnection other);


    /**
     * Unregisters an operation provider for this asset connection.
     *
     * @param reference Reference to the AAS element that this provider should
     *            be unregistered for
     * @throws AssetConnectionException if unregistering provider fails
     */
    public void unregisterOperationProvider(Reference reference) throws AssetConnectionException;


    /**
     * Unregisters a subscription provider for this asset connection.
     *
     * @param reference Reference to the AAS element that this provider should
     *            be unregistered for
     * @throws AssetConnectionException if unregistering provider fails
     */
    public void unregisterSubscriptionProvider(Reference reference) throws AssetConnectionException;


    /**
     * Unregisters a value provider for this asset connection.
     *
     * @param reference Reference to the AAS element that this provider should
     *            be unregistered for
     * @throws AssetConnectionException if unregistering provider fails
     */
    public void unregisterValueProvider(Reference reference) throws AssetConnectionException;

}
