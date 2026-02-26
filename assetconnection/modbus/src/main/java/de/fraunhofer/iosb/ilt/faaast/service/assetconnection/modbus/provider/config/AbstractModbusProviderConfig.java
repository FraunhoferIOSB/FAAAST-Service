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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.model.ModbusDatatype;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Modbus asset connection common provider configuration.
 */
public abstract class AbstractModbusProviderConfig implements AssetProviderConfig {

    public static final int DEFAULT_QUANTITY = 1;
    public static final int DEFAULT_UNIT_ID = 1;

    private ModbusDatatype dataType;
    private Integer address;
    private int quantity;
    private int unitId;

    protected AbstractModbusProviderConfig() {
        this.quantity = DEFAULT_QUANTITY;
        this.unitId = DEFAULT_UNIT_ID;
    }


    @Override
    public boolean sameAs(AssetProviderConfig other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        AbstractModbusProviderConfig that = (AbstractModbusProviderConfig) other;
        return Objects.equals(dataType, that.dataType) &&
                Objects.equals(address, that.address) &&
                Objects.equals(unitId, that.unitId) &&
                Objects.equals(quantity, that.quantity);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        AbstractModbusProviderConfig that = (AbstractModbusProviderConfig) o;
        return Objects.equals(dataType, that.getDataType()) &&
                Objects.equals(address, that.getAddress()) &&
                Objects.equals(unitId, that.getUnitId()) &&
                Objects.equals(quantity, that.getQuantity());
    }


    @Override
    public int hashCode() {
        return Objects.hash(Objects.hashCode(dataType),
                Objects.hashCode(address),
                Objects.hashCode(unitId),
                Objects.hashCode(quantity));
    }


    public void setDataType(ModbusDatatype dataType) {
        this.dataType = dataType;
    }


    public ModbusDatatype getDataType() {
        return dataType;
    }


    public Integer getAddress() {
        return address;
    }


    public void setAddress(int address) {
        this.address = address;
    }


    public int getQuantity() {
        return quantity;
    }


    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public int getUnitId() {
        return unitId;
    }


    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    protected abstract static class AbstractBuilder<T extends AbstractModbusProviderConfig, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B dataType(ModbusDatatype dataType) {
            getBuildingInstance().setDataType(dataType);
            return getSelf();
        }


        public B address(int address) {
            getBuildingInstance().setAddress(address);
            return getSelf();
        }


        public B quantity(int quantity) {
            getBuildingInstance().setQuantity(quantity);
            return getSelf();
        }


        public B unitId(int unitId) {
            getBuildingInstance().setUnitId(unitId);
            return getSelf();
        }
    }
}
