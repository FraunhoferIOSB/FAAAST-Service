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
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Modbus asset connection operation provider
 *
 * <p>
 * My idea of what an operation in modbus-land <i><b>could be</b></i>:
 * <ol>
 * <li>Set the value of a writable modbus server address to given input/inoutput parameter values</li>
 * <li>Wait for {@code timeout} seconds</li>
 * <li>Read inoutput parameter address and optionally output parameter addresses.</li>
 * </ol>
 */
public class ModbusOperationProvider extends AbstractModbusProvider<ModbusOperationProviderConfig> implements AssetOperationProvider<ModbusOperationProviderConfig> {

    public ModbusOperationProvider(ServiceContext serviceContext, Reference reference, ModbusClient modbusClient, int unitId, ModbusOperationProviderConfig config)
            throws AssetConnectionException {
        super(serviceContext, reference, modbusClient, unitId, config);
        throw new AssetConnectionException("Operations are not supported for modbus asset connections");
    }


    @Override
    public ModbusOperationProviderConfig getConfig() {
        return config;
    }


    @Override
    public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) {
        return null;
    }
}
