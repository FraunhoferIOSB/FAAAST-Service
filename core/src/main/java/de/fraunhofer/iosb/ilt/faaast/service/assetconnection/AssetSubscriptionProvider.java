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

/**
 * An AssetSubscriptionProvider provides methods to subscribe to an asset and provide events about new data values.
 */
public interface AssetSubscriptionProvider extends AssetProvider {

    /**
     * Add a NewDataListener to be notified when new data is received from the asset.
     *
     * @param listener listener to add
     * @throws AssetConnectionException if adding listener fails
     */
    public void addNewDataListener(NewDataListener listener) throws AssetConnectionException;


    /**
     * Remove NewDataListener if present, otherwise to nothing.
     *
     * @param listener listener to remove
     * @throws AssetConnectionException if removinglistener fails
     */
    public void removeNewDataListener(NewDataListener listener) throws AssetConnectionException;
}
