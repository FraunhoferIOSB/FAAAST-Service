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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.provider.MultiFormatOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.config.MqttOperationProviderConfig;
import java.util.function.UnaryOperator;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;


/**
 * OperationProvider for MQTT.
 */
public class MqttOperationProvider extends MultiFormatOperationProvider<MqttOperationProviderConfig> {

    protected MqttOperationProvider(MqttOperationProviderConfig config) {
        super(config);
    }


    @Override
    protected OperationVariable[] getOutputParameters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    protected byte[] invoke(byte[] input, UnaryOperator<String> variableReplacer) throws AssetConnectionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
