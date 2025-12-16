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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.model.DataTypes;
import java.util.Objects;


/**
 * Modbus asset connection common provider configuration.
 */
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
