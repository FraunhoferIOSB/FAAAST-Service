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
import com.digitalpetri.modbus.pdu.ModbusRequestPdu;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * ValueProvider for Modbus. Supports reading from COILs, DISCRETE_INPUTs, INPUT_REGISTERs and HOLDING_REGISTERs and
 * writing to COILS and HOLDING_REGISTERS.
 */
public class ModbusValueProvider extends AbstractModbusProvider<ModbusValueProviderConfig> implements AssetValueProvider {

    public ModbusValueProvider(ServiceContext serviceContext, Reference reference, ModbusClient modbusClient, ModbusValueProviderConfig config)
            throws AssetConnectionException {
        super(serviceContext, reference, modbusClient, config);
    }


    @Override
    public DataElementValue getValue() throws AssetConnectionException {
        ModbusRequestPdu request = createReadRequest();

        byte[] responseBytes = doRead(request);
        TypedValue<?> responseAas = convert(responseBytes);

        return new PropertyValue(responseAas);
    }


    @Override
    public void setValue(DataElementValue value) throws AssetConnectionException {
        byte[] bytesToWrite = convert(value);
        ModbusRequestPdu request = createWriteRequest(bytesToWrite);
        doWrite(request);
    }
}
