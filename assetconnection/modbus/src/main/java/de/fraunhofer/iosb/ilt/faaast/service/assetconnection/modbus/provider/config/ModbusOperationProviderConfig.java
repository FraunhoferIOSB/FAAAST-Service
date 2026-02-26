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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.ArgumentValidationMode;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProviderConfig;


/**
 * Modbus asset connection operation provider config.
 */
public class ModbusOperationProviderConfig extends AbstractModbusProviderConfig implements AssetOperationProviderConfig {

    @Override
    public ArgumentValidationMode getInputValidationMode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void setInputValidationMode(ArgumentValidationMode mode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public ArgumentValidationMode getInoutputValidationMode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void setInoutputValidationMode(ArgumentValidationMode mode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public ArgumentValidationMode getOutputValidationMode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void setOutputValidationMode(ArgumentValidationMode mode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
