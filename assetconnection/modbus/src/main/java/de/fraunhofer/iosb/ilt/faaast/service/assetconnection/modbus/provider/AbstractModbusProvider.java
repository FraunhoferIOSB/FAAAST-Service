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

import static de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.model.ModbusDatatype.COIL;
import static de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.model.ModbusDatatype.DISCRETE_INPUT;
import static de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.model.ModbusDatatype.HOLDING_REGISTER;

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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.AbstractModbusProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.model.ModbusDatatype;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.util.AasToModbusConversionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.util.ByteArrayHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.math.BigInteger;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Modbus provider common functionality.
 */
public abstract class AbstractModbusProvider<C extends AbstractModbusProviderConfig> implements AssetProvider {

    private static final String WRITE_OVERFLOW_TEMPLATE = "Attempting to write more %1$s than defined quantity (quantity: %2$d, %1$s to write: %3$d)";

    protected final Reference reference;
    private final C config;
    private final ServiceContext serviceContext;
    private final ModbusClient modbusClient;
    private final Datatype datatype;

    protected AbstractModbusProvider(ServiceContext serviceContext, Reference reference, ModbusClient modbusClient, C config) throws AssetConnectionException {
        this.serviceContext = serviceContext;
        this.reference = reference;
        this.modbusClient = modbusClient;
        this.config = config;
        this.datatype = getDatatype(reference);
    }


    @Override
    public C asConfig() {
        return config;
    }


    @Override
    public int hashCode() {
        return Objects.hash(serviceContext, modbusClient, datatype, reference, config);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractModbusProvider<?> abstractModbusProvider)) {
            return false;
        }
        return Objects.equals(serviceContext, abstractModbusProvider.serviceContext) &&
                Objects.equals(modbusClient, abstractModbusProvider.modbusClient) &&
                Objects.equals(datatype, abstractModbusProvider.datatype) &&
                Objects.equals(reference, abstractModbusProvider.reference) &&
                Objects.equals(config, abstractModbusProvider.config);
    }


    /**
     * Convert raw bytes that were read from the modbus server to AAS data.
     *
     * @param rawBytes The bytes to convert.
     * @return AAS TypedValue data
     * @throws AssetConnectionException If conversion of data fails due to type constraints.
     */
    protected TypedValue<?> convert(byte[] rawBytes) throws AssetConnectionException {
        return AasToModbusConversionHelper.convert(rawBytes, datatype);
    }


    /**
     * Read a byte array from a specified modbus server address and data type defined by {@code address} and
     * {@code datatype}. The amount of bytes depends on the {@code quantity}
     * defined in the provider config.
     *
     * @param readRequest The read request containing address and quantity of values to read.
     * @return Bytes read from the modbus server.
     * @throws AssetConnectionException If writing to modbus server fails.
     */
    protected byte[] doRead(ModbusRequestPdu readRequest) throws AssetConnectionException {
        try {
            return read(readRequest);
        }
        catch (ModbusExecutionException | ModbusTimeoutException | ModbusResponseException e) {
            throw new AssetConnectionException(e);
        }
    }


    /**
     * Convert AAS data to raw bytes readable by the modbus server.
     *
     * @param value AAS TypedValue data
     * @return The bytes to write.
     * @throws AssetConnectionException If conversion of data fails due to type constraints.
     */
    protected byte[] convert(DataElementValue value) throws AssetConnectionException {
        ModbusDatatype dtype = config.getDataType();
        if (dtype == COIL || dtype == DISCRETE_INPUT) {
            return AasToModbusConversionHelper.convert(value, 1);
        }
        else {
            return AasToModbusConversionHelper.convert(value, 2);
        }
    }


    /**
     * Write a byte array to a specified modbus server address.
     *
     * @param writeRequest The write request containing address, quantity and bytes to write.
     */
    protected void doWrite(ModbusRequestPdu writeRequest) {
        write(writeRequest);
    }


    private byte[] read(ModbusRequestPdu request) throws ModbusExecutionException, ModbusTimeoutException, ModbusResponseException {
        int unitId = config.getUnitId();

        if (request instanceof ReadCoilsRequest coilsRequest) {
            return modbusClient.readCoils(unitId, coilsRequest).coils();
        }
        else if (request instanceof ReadDiscreteInputsRequest discreteInputsRequest) {
            return modbusClient.readDiscreteInputs(unitId, discreteInputsRequest).inputs();
        }
        else if (request instanceof ReadHoldingRegistersRequest holdingRegistersRequest) {
            return modbusClient.readHoldingRegisters(unitId, holdingRegistersRequest).registers();
        }
        else if (request instanceof ReadInputRegistersRequest inputRegistersRequest) {
            return modbusClient.readInputRegisters(unitId, inputRegistersRequest).registers();
        }
        else {
            throw new UnsupportedOperationException(String.format("Request type unknown: %s", request.getClass()));
        }
    }


    private void write(ModbusRequestPdu request) {
        int unitId = config.getUnitId();

        // Java 24 offers an enhanced switch for this case
        // We use async operations as we do not use the results.
        if (request instanceof WriteMultipleCoilsRequest req) {
            modbusClient.writeMultipleCoilsAsync(unitId, req);
        }
        else if (request instanceof WriteSingleCoilRequest req) {
            modbusClient.writeSingleCoilAsync(unitId, req);
        }
        else if (request instanceof WriteMultipleRegistersRequest req) {
            modbusClient.writeMultipleRegistersAsync(unitId, req);
        }
        else if (request instanceof WriteSingleRegisterRequest req) {
            modbusClient.writeSingleRegisterAsync(unitId, req);
        }
        else {
            throw new UnsupportedOperationException(String.format("Request type unknown: %s", request.getClass()));
        }
    }


    /**
     * Creates an implementation-specific read modbus write request given the configuration variables.
     *
     * @return The implementation-specific modbus read request
     */
    protected ModbusRequestPdu createReadRequest() {
        int address = config.getAddress();
        int quantity = config.getQuantity();

        return switch (config.getDataType()) {
            case COIL -> new ReadCoilsRequest(address, quantity);
            case DISCRETE_INPUT -> new ReadDiscreteInputsRequest(address, quantity);
            case HOLDING_REGISTER -> new ReadHoldingRegistersRequest(address, quantity);
            case INPUT_REGISTER -> new ReadInputRegistersRequest(address, quantity);
        };
    }


    /**
     * Creates an implementation-specific write modbus write request given the bytes to write in conjunction with the
     * configuration variables.
     *
     * @param rawBytesToWrite bytes to write to modbus server.
     * @return The implementation-specific modbus write request
     * @throws AssetConnectionException If the provider does not support write-operations.
     */
    protected ModbusRequestPdu createWriteRequest(byte[] rawBytesToWrite) throws AssetConnectionException {
        int address = config.getAddress();
        int quantity = config.getQuantity();

        rawBytesToWrite = ByteArrayHelper.removePadding(rawBytesToWrite);

        return switch (config.getDataType()) {
            case COIL -> {
                validateCoilQuantity(rawBytesToWrite);
                yield (quantity > 1) ? new WriteMultipleCoilsRequest(address, rawBytesToWrite.length, rawBytesToWrite) : new WriteSingleCoilRequest(address, rawBytesToWrite[0]);
            }
            case HOLDING_REGISTER -> {
                // Use the configured addresses in full
                if (quantity * 2 > rawBytesToWrite.length) {
                    rawBytesToWrite = ByteArrayHelper.pad(rawBytesToWrite, quantity * 2 - rawBytesToWrite.length);
                }

                validateHoldingRegisterQuantity(rawBytesToWrite);

                if (quantity > 1) {
                    yield new WriteMultipleRegistersRequest(address, quantity, rawBytesToWrite);
                }
                else {
                    yield new WriteSingleRegisterRequest(address, new BigInteger(rawBytesToWrite).intValue());
                }
            }
            case DISCRETE_INPUT, INPUT_REGISTER -> throw new AssetConnectionException(String.format("Unsupported operation WRITE on %s", config.getDataType()));
        };
    }


    private void validateCoilQuantity(byte[] bytesToWrite) throws AssetConnectionException {
        int msbIndex = ByteArrayHelper.mostSignificantBitIndex(bytesToWrite);
        int coilsToWrite = (msbIndex < 0) ? 0 : msbIndex + 1;
        if (config.getQuantity() < coilsToWrite) {
            throw new AssetConnectionException(String.format(WRITE_OVERFLOW_TEMPLATE, COIL, config.getQuantity(), coilsToWrite));
        }
    }


    private void validateHoldingRegisterQuantity(byte[] bytesToWrite) throws AssetConnectionException {
        // If zero-padded, get real bytes to write
        int realNumberOfBytesToWrite = bytesToWrite.length;

        // 2 bytes per register
        realNumberOfBytesToWrite = (int) Math.ceil(realNumberOfBytesToWrite * 0.5);
        if (config.getQuantity() < realNumberOfBytesToWrite) {
            throw new AssetConnectionException(String.format(WRITE_OVERFLOW_TEMPLATE, HOLDING_REGISTER, config.getQuantity(), realNumberOfBytesToWrite));
        }
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
        Datatype type = valueTypeInfo.getDatatype();

        if (type == null) {
            throw new AssetConnectionException(String.format("Missing datatype (reference: %s)",
                    ReferenceHelper.toString(reference)));
        }
        return type;
    }

}
