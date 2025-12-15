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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config;

import com.digitalpetri.modbus.pdu.ModbusRequestPdu;
import com.digitalpetri.modbus.pdu.ReadCoilsRequest;
import com.digitalpetri.modbus.pdu.ReadDiscreteInputsRequest;
import com.digitalpetri.modbus.pdu.ReadHoldingRegistersRequest;
import com.digitalpetri.modbus.pdu.ReadInputRegistersRequest;
import com.digitalpetri.modbus.pdu.WriteMultipleCoilsRequest;
import com.digitalpetri.modbus.pdu.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.pdu.WriteSingleCoilRequest;
import com.digitalpetri.modbus.pdu.WriteSingleRegisterRequest;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.model.DataTypes;
import java.util.Objects;


public abstract class AbstractModbusProviderConfig implements AssetProviderConfig {

    private DataTypes dataType;
    private int address;
    private final int quantity = 1;

    @Override
    public boolean sameAs(AssetProviderConfig other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        AbstractModbusProviderConfig that = (AbstractModbusProviderConfig) other;
        return Objects.equals(dataType, that.dataType);
    }


    public void setDataType(DataTypes dataType) {
        this.dataType = dataType;
    }


    public DataTypes getDataType() {
        return dataType;
    }


    public ModbusRequestPdu toReadRequest() {
        return switch (dataType) {
            case COIL -> new ReadCoilsRequest(address, quantity);
            case DISCRETE_INPUT -> new ReadDiscreteInputsRequest(address, quantity);
            case HOLDING_REGISTER -> new ReadHoldingRegistersRequest(address, quantity);
            case INPUT_REGISTER -> new ReadInputRegistersRequest(address, quantity);
        };
    }


    public ModbusRequestPdu toWriteRequest(byte[] value) throws AssetConnectionException {
        if (quantity != value.length) {
            throw new AssetConnectionException("Mismatched quantity and actual values to write (quantity: %s, actual values: %s)");
        }
        return switch (dataType) {
            case COIL -> quantity > 1 ? new WriteMultipleCoilsRequest(address, quantity, value) : new WriteSingleCoilRequest(address, value[0]);
            case HOLDING_REGISTER -> quantity > 1 ? new WriteMultipleRegistersRequest(address, quantity, value) : new WriteSingleRegisterRequest(address, value[0]);
            case DISCRETE_INPUT, INPUT_REGISTER -> throw new AssetConnectionException(String.format("Unsupported operation WRITE on %s", dataType));
        };
    }


    public int getAddress() {
        return address;
    }


    public void setAddress(int address) {
        this.address = address;
    }


    public int getQuantity() {
        return quantity;
    }
}
