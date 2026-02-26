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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProviderConfig;
import java.util.Objects;


/**
 * Modbus asset connection subscription provider config.
 */
public class ModbusSubscriptionProviderConfig extends AbstractModbusProviderConfig implements AssetSubscriptionProviderConfig {

    public static final long DEFAULT_POLLING_RATE = 1000;

    private long pollingRate;

    public ModbusSubscriptionProviderConfig() {
        this.pollingRate = DEFAULT_POLLING_RATE;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        ModbusSubscriptionProviderConfig that = (ModbusSubscriptionProviderConfig) o;
        return super.equals(o) &&
                Objects.equals(pollingRate, that.pollingRate);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), Objects.hashCode(pollingRate));
    }


    public long getPollingRate() {
        return pollingRate;
    }


    public void setPollingRate(long pollingRate) {
        this.pollingRate = pollingRate;
    }


    public static Builder builder() {
        return new Builder();
    }

    protected abstract static class AbstractBuilder<T extends ModbusSubscriptionProviderConfig, B extends AbstractBuilder<T, B>>
            extends AbstractModbusProviderConfig.AbstractBuilder<T, B> {

        public B pollingRate(long pollingRate) {
            getBuildingInstance().setPollingRate(pollingRate);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<ModbusSubscriptionProviderConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected ModbusSubscriptionProviderConfig newBuildingInstance() {
            return new ModbusSubscriptionProviderConfig();
        }
    }
}
