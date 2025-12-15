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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.ModbusAssetConnectionConfig;


public class ModbusSubscriptionProviderConfig extends AbstractModbusProviderConfig implements AssetSubscriptionProviderConfig {

    private static final long DEFAULT_POLLING_RATE = ModbusAssetConnectionConfig.DEFAULT_SUBSCRIPTION_POLLING_RATE.toMillis();

    private long pollingRate;

    public ModbusSubscriptionProviderConfig() {
        this.pollingRate = DEFAULT_POLLING_RATE;
    }


    public long getPollingRate() {
        return pollingRate;
    }


    public void setPollingRate(long pollingRate) {
        this.pollingRate = pollingRate;
    }
}
