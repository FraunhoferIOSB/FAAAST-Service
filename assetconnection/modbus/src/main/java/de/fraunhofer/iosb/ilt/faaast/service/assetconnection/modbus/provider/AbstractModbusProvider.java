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
import com.digitalpetri.modbus.exceptions.ModbusExecutionException;
import com.digitalpetri.modbus.exceptions.ModbusResponseException;
import com.digitalpetri.modbus.exceptions.ModbusTimeoutException;
import com.digitalpetri.modbus.pdu.ModbusRequestPdu;
import com.digitalpetri.modbus.pdu.ReadCoilsRequest;
import com.digitalpetri.modbus.pdu.ReadDiscreteInputsRequest;
import com.digitalpetri.modbus.pdu.ReadHoldingRegistersRequest;
import com.digitalpetri.modbus.pdu.ReadInputRegistersRequest;
import com.digitalpetri.modbus.pdu.WriteMultipleCoilsRequest;
import com.digitalpetri.modbus.pdu.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.pdu.WriteSingleCoilRequest;
import com.digitalpetri.modbus.pdu.WriteSingleRegisterRequest;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.AbstractModbusProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.util.AasToModbusConversionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Modbus provider common functionality.
 */
public abstract class AbstractModbusProvider<C extends AbstractModbusProviderConfig> implements AssetProvider {

    protected final ServiceContext serviceContext;
    protected final ModbusClient modbusClient;
    protected final Reference reference;
    protected final C config;
    private final int unitId;
    private Datatype datatype;

    protected AbstractModbusProvider(ServiceContext serviceContext, ModbusClient modbusClient, Reference reference, int unitId, C config) throws AssetConnectionException {
        this.serviceContext = serviceContext;
        this.modbusClient = modbusClient;
        this.reference = reference;
        this.unitId = unitId;
        this.config = config;
        datatype = getDatatype(reference);
    }


    private Datatype getDatatype(Reference reference) throws AssetConnectionException {
        TypeInfo<?> typeInfo;
        try {
            typeInfo = serviceContext.getTypeInfo(reference);
        }
        catch (ResourceNotFoundException | PersistenceException ex) {
            throw new AssetConnectionException(
                    String.format("Could not resolve type information (reference: %s)",
                            ReferenceHelper.toString(reference)));
        }
        if (typeInfo == null) {
            throw new AssetConnectionException(
                    String.format("Could not resolve type information (reference: %s)",
                            ReferenceHelper.toString(reference)));
        }
        if (!(typeInfo instanceof ElementValueTypeInfo valueTypeInfo)) {
            throw new AssetConnectionException(
                    String.format("Reference must point to element with value (reference: %s)",
                            ReferenceHelper.toString(reference)));
        }
        if (!PropertyValue.class.isAssignableFrom(valueTypeInfo.getType())) {
            throw new AssetConnectionException(String.format("Unsupported element type (reference: %s, element type: %s)",
                    ReferenceHelper.toString(reference),
                    valueTypeInfo.getType()));
        }
        datatype = valueTypeInfo.getDatatype();
        if (datatype == null) {
            throw new AssetConnectionException(String.format("Missing datatype (reference: %s)",
                    ReferenceHelper.toString(reference)));
        }
        return datatype;
    }


    @Override
    public AssetProviderConfig<?> asConfig() {
        return config;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), serviceContext, modbusClient, reference, config);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ModbusValueProvider modbusValueProvider)) {
            return false;
        }
        return super.equals(obj)
                && Objects.equals(serviceContext, modbusValueProvider.serviceContext)
                && Objects.equals(modbusClient, modbusValueProvider.modbusClient)
                && Objects.equals(reference, modbusValueProvider.reference)
                && config.sameAs(modbusValueProvider.asConfig());
    }


    /**
     * Write a byte array to a specified modbus server address.
     *
     * @param bytesToWrite The bytes to write.
     * @throws AssetConnectionException If writing to modbus server fails.
     */
    protected void doWrite(byte[] bytesToWrite) throws AssetConnectionException {
        try {
            write(bytesToWrite);
        }
        catch (ModbusExecutionException | ModbusTimeoutException | ModbusResponseException e) {
            throw new AssetConnectionException(e);
        }
    }


    private void write(byte[] bytesToWrite) throws ModbusExecutionException, ModbusTimeoutException, ModbusResponseException, AssetConnectionException {
        // TODO Might be relaxed to >. Throwing this guard away is also possible (but unsafe)
        // TODO another thing: quantity != bytes.length. Registers have 2 bytes each, so qty = bytes.length * 2
//        if (bytesToWrite.length != config.getQuantity()) {
//            throw new AssetConnectionException(
//                    String.format("Bytes to write do not match configured quantity (To write: %d, quantity: %d)", bytesToWrite.length, config.getQuantity()));
//        }
        ModbusRequestPdu request = createWriteRequest(bytesToWrite);

        if (request instanceof WriteMultipleCoilsRequest req) {
            modbusClient.writeMultipleCoils(unitId, req);
        }
        else if (request instanceof WriteSingleCoilRequest req) {
            modbusClient.writeSingleCoil(unitId, req);
        }
        else if (request instanceof WriteMultipleRegistersRequest req) {
            modbusClient.writeMultipleRegisters(unitId, req);
        }
        else if (request instanceof WriteSingleRegisterRequest req) {
            modbusClient.writeSingleRegister(unitId, req);
        }
        else {
            throw new UnsupportedOperationException(String.format("Request type unknown: %s", request.getClass()));
        }
    }


    /**
     * Convert raw bytes read from modbus servers to AAS data.
     *
     * @param rawBytes The bytes to convert.
     * @return AAS TypedValue data
     * @throws AssetConnectionException If conversion of data fails due to type constraints.
     */
    protected TypedValue<?> convert(byte[] rawBytes) throws AssetConnectionException {
        return AasToModbusConversionHelper.convert(rawBytes, datatype);
    }


    /**
     * Convert AAS data to raw bytes read from modbus servers.
     *
     * @param value AAS TypedValue data
     * @return The bytes to write.
     * @throws AssetConnectionException If conversion of data fails due to type constraints.
     */
    protected byte[] convert(DataElementValue value) throws AssetConnectionException {
        return AasToModbusConversionHelper.convert(value);
    }


    /**
     * Read a byte array from a specified modbus server address.
     *
     * @return The read bytes.
     * @throws AssetConnectionException If writing to modbus server fails.
     */
    protected byte[] doRead() throws AssetConnectionException {
        try {
            return read();
        }
        catch (ModbusExecutionException | ModbusTimeoutException | ModbusResponseException e) {
            throw new AssetConnectionException(e);
        }
    }


    private byte[] read() throws ModbusExecutionException, ModbusTimeoutException, ModbusResponseException {
        ModbusRequestPdu request = createReadRequest();

        if (request instanceof ReadCoilsRequest req) {
            return modbusClient.readCoils(unitId, req).coils();
        }
        else if (request instanceof ReadDiscreteInputsRequest req) {
            return modbusClient.readDiscreteInputs(unitId, req).inputs();
        }
        else if (request instanceof ReadHoldingRegistersRequest req) {
            return modbusClient.readHoldingRegisters(unitId, req).registers();
        }
        else if (request instanceof ReadInputRegistersRequest req) {
            return modbusClient.readInputRegisters(unitId, req).registers();
        }
        else {
            throw new UnsupportedOperationException(String.format("Request type unknown: %s", request.getClass()));
        }
    }


    private ModbusRequestPdu createReadRequest() {
        int address = config.getAddress();
        int quantity = config.getQuantity();
        return switch (config.getDataType()) {
            case COIL -> new ReadCoilsRequest(address, quantity);
            case DISCRETE_INPUT -> new ReadDiscreteInputsRequest(address, quantity);
            case HOLDING_REGISTER -> new ReadHoldingRegistersRequest(address, quantity);
            case INPUT_REGISTER -> new ReadInputRegistersRequest(address, quantity);
        };
    }


    private ModbusRequestPdu createWriteRequest(byte[] value) throws AssetConnectionException {
        int quantity = config.getQuantity();
        if (quantity != value.length) {
            throw new AssetConnectionException("Mismatched quantity and actual values to write (quantity: %s, actual values: %s)");
        }
        int address = config.getAddress();
        return switch (config.getDataType()) {
            case COIL -> quantity > 1 ? new WriteMultipleCoilsRequest(address, quantity, value) : new WriteSingleCoilRequest(address, value[0]);
            case HOLDING_REGISTER -> quantity > 1 ? new WriteMultipleRegistersRequest(address, quantity, value) : new WriteSingleRegisterRequest(address, value[0]);
            case DISCRETE_INPUT, INPUT_REGISTER -> throw new AssetConnectionException(String.format("Unsupported operation WRITE on %s", config.getDataType()));
        };
    }
}
