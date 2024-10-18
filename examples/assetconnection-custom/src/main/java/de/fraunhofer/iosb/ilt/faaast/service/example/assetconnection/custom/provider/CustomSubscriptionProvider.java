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
package de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.provider;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.provider.config.CustomSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.util.AasHelper;
import de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.util.RandomValueGenerator;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CustomSubscriptionProvider implements AssetSubscriptionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomSubscriptionProvider.class);
    public static final long MINIMUM_INTERVAL = 100;
    private final CustomSubscriptionProviderConfig config;
    private final Reference reference;
    private final Datatype datatype;
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> executorHandler;
    protected final List<NewDataListener> listeners;

    public CustomSubscriptionProvider(Reference reference, CustomSubscriptionProviderConfig config, ServiceContext serviceContext)
            throws ValueMappingException, ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(config, "config must be non-null");
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        AasHelper.ensureType(reference, Property.class, serviceContext);
        this.listeners = Collections.synchronizedList(new ArrayList<>());
        this.config = config;
        this.reference = reference;
        this.datatype = AasHelper.getDatatype(reference, serviceContext);
    }


    @Override
    public void addNewDataListener(NewDataListener listener) throws AssetConnectionException {
        if (listeners.isEmpty()) {
            subscribe();
        }
        listeners.add(listener);
    }


    @Override
    public void removeNewDataListener(NewDataListener listener) throws AssetConnectionException {
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            unsubscribe();
        }
    }


    private void subscribe() throws AssetConnectionException {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newScheduledThreadPool(0);
            executorHandler = executor.scheduleAtFixedRate(() -> {
                try {
                    fireNewDataReceived(PropertyValue.of(datatype, RandomValueGenerator.generateRandomValue(datatype).toString()));
                }
                catch (ValueFormatException e) {
                    LOGGER.debug("error subscribing to asset connection (reference: {})", ReferenceHelper.toString(reference), e);
                }
            }, 0, Math.max(MINIMUM_INTERVAL, config.getInterval()), TimeUnit.MILLISECONDS);
        }
    }


    private void fireNewDataReceived(DataElementValue value) {
        synchronized (listeners) {
            listeners.forEach(x -> {
                try {
                    x.newDataReceived(value);
                }
                catch (Exception e) {
                    LOGGER.warn("error while calling newDataReceived handler", e);
                }
            });
        }
    }


    private void unsubscribe() throws AssetConnectionException {
        if (executorHandler != null) {
            executorHandler.cancel(true);
        }
        if (executor != null) {
            executor.shutdown();
        }
    }

}
