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
import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Interface for asset connections, i.e. connecting assets to the AAS (a.k.a. lower DT interface). An asset connection
 * can support the following operations:
 * <ul>
 * <li>get/set data values from/to the asset (via {@link AssetValueProvider})
 * <li>execute operations on the asset (via {@link AssetOperationProvider})
 * <li>subscribe to new values from the asset (via {@link AssetSubscriptionProvider})
 * </ul>
 *
 * @param <T> corresponding config type
 * @param <VC> type of value provider config
 * @param <V> type of value provider
 * @param <OC> type of operation provider config
 * @param <O> type of operation provider
 * @param <SC> type of subscription config
 * @param <S> type of subscription
 */
public interface AssetConnection<T extends AssetConnectionConfig, VC extends AssetValueProviderConfig, V extends AssetValueProvider, OC extends AssetOperationProviderConfig, O extends AssetOperationProvider, SC extends AssetSubscriptionProviderConfig, S extends AssetSubscriptionProvider>
        extends Configurable<T> {

    /**
     * Closes the asset connection.
     *
     * @throws AssetConnectionException if closing fails
     */
    public void disconnect() throws AssetConnectionException;


    /**
     * Connects to the asset.
     *
     * @throws AssetConnectionException if connecting fails
     */
    public void connect() throws AssetConnectionException;


    public boolean isConnected();


    public Map<Reference, O> getOperationProviders();


    public Map<Reference, S> getSubscriptionProviders();


    public Map<Reference, V> getValueProviders();


    /**
     * Gets information about the endpoint of the connection used for proper error reporting.
     *
     * @return information about the endpoint of the connection
     */
    public String getEndpointInformation();


    /**
     * Registers an operation provider for this asset connection.
     *
     * @param reference Reference to the AAS element that this provider will map to
     * @param providerConfig AssetOperationProvider instance to use
     * @throws AssetConnectionException if registering provider fails
     */
    public void registerOperationProvider(Reference reference, OC providerConfig) throws AssetConnectionException;


    /**
     * Registers a subscription provider for this asset connection.
     *
     * @param reference Reference to the AAS element that this provider will map to
     * @param providerConfig AssetSubscriptionProvider instance to use
     * @throws AssetConnectionException if registering provider fails
     */
    public void registerSubscriptionProvider(Reference reference, SC providerConfig) throws AssetConnectionException;


    /**
     * Registers a value provider for this asset connection.
     *
     * @param reference Reference to the AAS element that this provider will map to
     * @param providerConfig AssetValueProvider instance to use
     * @throws AssetConnectionException if registering provider fails
     */
    public void registerValueProvider(Reference reference, VC providerConfig) throws AssetConnectionException;


    /**
     * Compares two instances of AssetConnection if they are referencing the same asset connection.
     *
     * @param other other AssetConnection to compare to this.
     * @return true if other is the same as this.
     */
    public boolean sameAs(AssetConnection other);


    /**
     * Unregisters an operation provider for this asset connection.
     *
     * @param reference Reference to the AAS element that this provider should be unregistered for
     * @throws AssetConnectionException if unregistering provider fails
     */
    public void unregisterOperationProvider(Reference reference) throws AssetConnectionException;


    /**
     * Unregisters a subscription provider for this asset connection.
     *
     * @param reference Reference to the AAS element that this provider should be unregistered for
     * @throws AssetConnectionException if unregistering provider fails
     */
    public void unregisterSubscriptionProvider(Reference reference) throws AssetConnectionException;


    /**
     * Unregisters a value provider for this asset connection.
     *
     * @param reference Reference to the AAS element that this provider should be unregistered for
     * @throws AssetConnectionException if unregistering provider fails
     */
    public void unregisterValueProvider(Reference reference) throws AssetConnectionException;

}
