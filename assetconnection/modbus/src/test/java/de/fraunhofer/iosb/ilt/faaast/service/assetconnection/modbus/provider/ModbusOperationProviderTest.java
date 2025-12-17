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
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;


public class ModbusOperationProviderTest extends AbstractModbusProviderTest {
    @Test
    public void testEquals() throws Exception {
        server.start();
        try {
            ModbusClient client1 = getClient();
            client1.connect();
            ModbusClient client2 = getClient();
            client2.connect();

            EqualsVerifier.simple()
                    .forClass(ModbusSubscriptionProvider.class)
                    .withPrefabValues(ModbusClient.class, client1, client2)
                    .withIgnoredFields("lastValue")
                    .verify();

            client1.disconnect();
            client2.disconnect();
        }
        finally {
            server.stop();
        }
    }
}
