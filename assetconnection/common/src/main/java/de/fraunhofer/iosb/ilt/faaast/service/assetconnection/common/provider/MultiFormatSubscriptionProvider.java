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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.provider;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.provider.config.MultiFormatSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.util.MultiFormatReadWriteHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract base class for custom implementations of AssetSubscriptionProvider
 * supporting multiple data formats.
 *
 * @param <T> concrete type of matching configuration
 */
public abstract class MultiFormatSubscriptionProvider<T extends MultiFormatSubscriptionProviderConfig> implements AssetSubscriptionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiFormatSubscriptionProvider.class);
    protected final List<NewDataListener> listeners;
    protected T config;

    protected MultiFormatSubscriptionProvider(T config) {
        Ensure.requireNonNull(config, "config must be non-null");
        this.config = config;
        this.listeners = Collections.synchronizedList(new ArrayList<>());
    }


    @Override
    public void addNewDataListener(NewDataListener listener) throws AssetConnectionException {
        if (listeners.isEmpty()) {
            subscribe();
        }
        listeners.add(listener);
    }


    /**
     * Notifies all listeners about new event
     *
     * @param value new data to notify about
     */
    protected void fireNewDataReceived(byte[] value) {
        try {
            DataElementValue newValue = MultiFormatReadWriteHelper.convertForRead(config, value, getTypeInfo());
            synchronized (listeners) {
                listeners.forEach(x -> {
                    try {
                        x.newDataReceived(newValue);
                    }
                    catch (Exception e) {
                        LOGGER.warn("error while calling newDataReceived handler", e);
                    }
                });
            }
        }
        catch (AssetConnectionException e) {
            LOGGER.error("error deserializing message (received message: {})",
                    new String(value),
                    e);
        }
    }


    @Override
    public void removeNewDataListener(NewDataListener listener) throws AssetConnectionException {
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            unsubscribe();
        }
    }


    /**
     * Gets type information about the underlying element
     *
     * @return
     */
    protected abstract TypeInfo getTypeInfo();


    /**
     * Subscribe via underlying protocol
     *
     * @throws AssetConnectionException if subscription fails
     */
    protected abstract void subscribe() throws AssetConnectionException;


    /**
     * Unsubscribe via underlying protocol
     *
     * @throws AssetConnectionException if unsubscribe fails
     */
    protected abstract void unsubscribe() throws AssetConnectionException;

}
