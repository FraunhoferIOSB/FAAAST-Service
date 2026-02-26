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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider;

import com.digitalpetri.modbus.client.ModbusClient;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Subscription provider for modbus servers. Modbus does not support subscriptions out of the box, so values are polled
 * and listeners get notified when a value changes, like in the
 * HTTP AssetConnection.
 */
public class ModbusSubscriptionProvider extends AbstractModbusProvider<ModbusSubscriptionProviderConfig> implements AssetSubscriptionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusSubscriptionProvider.class);

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> executorHandler;
    // Don't rely on equals implementation of TypedValue inheritors
    private byte[] lastValue;

    protected final List<NewDataListener> listeners;

    public ModbusSubscriptionProvider(ServiceContext serviceContext, Reference reference, ModbusClient modbusClient, ModbusSubscriptionProviderConfig config)
            throws AssetConnectionException {
        super(serviceContext, reference, modbusClient, config);
        this.listeners = Collections.synchronizedList(new ArrayList<>());
        // fail fast if address not available
        lastValue = doRead(createReadRequest());
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
        ModbusSubscriptionProvider that = (ModbusSubscriptionProvider) obj;
        return super.equals(obj) &&
                Objects.equals(executor, that.executor) &&
                Objects.equals(executorHandler, that.executorHandler) &&
                Objects.equals(listeners, that.listeners);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                executor,
                executorHandler,
                listeners);
    }


    @Override
    public void addNewDataListener(NewDataListener listener) {
        if (listeners.isEmpty()) {
            subscribe();
        }
        listeners.add(listener);
    }


    @Override
    public void removeNewDataListener(NewDataListener listener) {
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            unsubscribe();
        }
    }


    @Override
    public void unsubscribe() {
        if (executorHandler != null) {
            executorHandler.cancel(true);
        }
        if (executor != null) {
            executor.shutdown();
        }
    }


    private void subscribe() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newScheduledThreadPool(0);
            executorHandler = executor.scheduleAtFixedRate(() -> {
                try {
                    notifyOnChangedData(doRead(createReadRequest()));
                }
                catch (AssetConnectionException e) {
                    LOGGER.debug("error subscribing to asset connection (reference: {})", ReferenceHelper.toString(reference), e);
                }
            }, 0, asConfig().getPollingRate(), TimeUnit.MILLISECONDS);
        }
    }


    private void notifyOnChangedData(byte[] newValue) throws AssetConnectionException {
        if (lastValue != null && Arrays.equals(lastValue, newValue)) {
            return;
        }
        lastValue = newValue;

        for (NewDataListener listener: listeners) {
            listener.newDataReceived(new PropertyValue(convert(newValue)));
        }
    }

}
