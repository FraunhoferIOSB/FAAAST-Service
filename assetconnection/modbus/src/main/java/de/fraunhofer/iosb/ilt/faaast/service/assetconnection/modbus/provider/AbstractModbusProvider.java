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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.BooleanValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.IntegerValue;
import java.math.BigInteger;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


public abstract class AbstractModbusProvider<C extends AbstractModbusProviderConfig> implements AssetProvider {

    protected final ServiceContext serviceContext;
    protected final ModbusClient modbusClient;
    protected final Reference reference;
    protected final C config;
    private final int unitId;

    protected AbstractModbusProvider(ServiceContext serviceContext, ModbusClient modbusClient, Reference reference, int unitId, C config) {
        this.serviceContext = serviceContext;
        this.modbusClient = modbusClient;
        this.reference = reference;
        this.unitId = unitId;
        this.config = config;
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


    protected void doWrite(DataElementValue value) throws AssetConnectionException {
        try {
            write(value);
        }
        catch (ModbusExecutionException | ModbusTimeoutException | ModbusResponseException e) {
            throw new AssetConnectionException(e);
        }
    }


    private void write(DataElementValue value) throws ModbusExecutionException, ModbusTimeoutException, ModbusResponseException, AssetConnectionException {
        ModbusRequestPdu request = createWriteRequest(AasToModbusConversionHelper.convert(value));

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


    protected TypedValue<?> doRead() throws AssetConnectionException {
        try {
            return read();
        }
        catch (ModbusExecutionException | ModbusTimeoutException | ModbusResponseException e) {
            throw new AssetConnectionException(e);
        }
    }


    private TypedValue<?> read() throws ModbusExecutionException, ModbusTimeoutException, ModbusResponseException {
        ModbusRequestPdu request = createReadRequest();

        if (request instanceof ReadCoilsRequest req) {
            byte[] coils = modbusClient.readCoils(unitId, req).coils();
            return readBoolean(coils);
        }
        else if (request instanceof ReadDiscreteInputsRequest req) {
            byte[] discreteInputs = modbusClient.readDiscreteInputs(unitId, req).inputs();
            return readBoolean(discreteInputs);
        }
        else if (request instanceof ReadHoldingRegistersRequest req) {
            byte[] holdingRegisters = modbusClient.readHoldingRegisters(unitId, req).registers();
            return readInt16(holdingRegisters);
        }
        else if (request instanceof ReadInputRegistersRequest req) {
            byte[] inputRegisters = modbusClient.readInputRegisters(unitId, req).registers();
            return readInt16(inputRegisters);
        }
        else {
            throw new UnsupportedOperationException(String.format("Request type unknown: %s", request.getClass()));
        }
    }


    private byte[] validateQuantityRead(byte[] read) {
        if (read.length != config.getQuantity()) {
            throw new IllegalStateException(String.format("Attempted to read or erroneously read an amount of values other than defined in the config!"
                    + " (read: %s; config: %s)", read.length, config.getQuantity()));
        }
        return read;
    }


    private BooleanValue readBoolean(byte[] bytes) {
        return new BooleanValue(validateQuantityRead(bytes)[0] != 0x0);
    }


    private IntegerValue readInt16(byte[] bytes) {
        return new IntegerValue(BigInteger.valueOf(validateQuantityRead(bytes)[0]));
    }


    public ModbusRequestPdu createReadRequest() {
        int address = config.getAddress();
        int quantity = config.getQuantity();
        return switch (config.getDataType()) {
            case COIL -> new ReadCoilsRequest(address, quantity);
            case DISCRETE_INPUT -> new ReadDiscreteInputsRequest(address, quantity);
            case HOLDING_REGISTER -> new ReadHoldingRegistersRequest(address, quantity);
            case INPUT_REGISTER -> new ReadInputRegistersRequest(address, quantity);
        };
    }


    public ModbusRequestPdu createWriteRequest(byte[] value) throws AssetConnectionException {
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
