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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusOperationProviderConfig;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * OperationProvider for Modbus.
 */
public class ModbusOperationProvider extends AbstractModbusProvider<ModbusOperationProviderConfig> implements AssetOperationProvider<ModbusOperationProviderConfig> {

    public ModbusOperationProvider(ServiceContext serviceContext, Reference reference, ModbusClient modbusClient, ModbusOperationProviderConfig config)
            throws AssetConnectionException {
        super(serviceContext, reference, modbusClient, config);
    }


    @Override
    public ModbusOperationProviderConfig getConfig() {
        return asConfig();
    }


    @Override
    public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void invokeAsync(OperationVariable[] input, OperationVariable[] inoutput, BiConsumer<OperationVariable[], OperationVariable[]> callbackSuccess,
                            Consumer<Throwable> callbackFailure) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
