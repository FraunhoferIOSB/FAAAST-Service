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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AbstractAssetValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.ReadWriteMode;
import java.util.Objects;


/**
 * Modbus asset connection value provider config.
 */
public class ModbusValueProviderConfig extends AbstractModbusProviderConfig implements AssetValueProviderConfig {

    protected ReadWriteMode readWriteMode;

    protected ModbusValueProviderConfig() {
        this.readWriteMode = AbstractAssetValueProviderConfig.DEFAULT_READ_WRITE_MODE;
    }


    @Override
    public ReadWriteMode getReadWriteMode() {
        return readWriteMode;
    }


    public void setReadWriteMode(ReadWriteMode readWriteMode) {
        this.readWriteMode = readWriteMode;
    }


    @Override
    public boolean sameAs(AssetProviderConfig other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ModbusValueProviderConfig that = (ModbusValueProviderConfig) other;
        return super.sameAs(that)
                && Objects.equals(readWriteMode, that.readWriteMode);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModbusValueProviderConfig that = (ModbusValueProviderConfig) o;
        return super.equals(o)
                && Objects.equals(readWriteMode, that.readWriteMode);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), readWriteMode);
    }

    protected abstract static class AbstractBuilder<T extends ModbusValueProviderConfig, B extends AbstractBuilder<T, B>>
            extends AbstractModbusProviderConfig.AbstractBuilder<T, B> {

        public B readWriteMode(ReadWriteMode value) {
            getBuildingInstance().setReadWriteMode(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<ModbusValueProviderConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected ModbusValueProviderConfig newBuildingInstance() {
            return new ModbusValueProviderConfig();
        }
    }
}
