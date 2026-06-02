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
import com.digitalpetri.modbus.server.ModbusTcpServer;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.testutil.ModbusTestHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;


public abstract class AbstractModbusProviderTest<T extends AbstractModbusProvider> {

    private final ModbusTcpServer server;
    private final int serverPort = PortHelper.findFreePort();

    protected AbstractModbusProviderTest() {
        try {
            server = ModbusTestHelper.getServer(serverPort, false);
        }
        catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void testEqualsHashCode() throws ExecutionException, InterruptedException, ModbusExecutionException {
        server.start();
        try {
            ModbusClient client1 = ModbusTestHelper.getClient(serverPort);
            client1.connect();
            ModbusClient client2 = ModbusTestHelper.getClient(serverPort);
            client2.connect();

            EqualsVerifier.simple()
                    .forClass(getImplementation())
                    .withPrefabValues(ModbusClient.class, client1, client2)
                    .withIgnoredFields(getIgnoredFields())
                    .verify();

            client1.disconnect();
            client2.disconnect();
        }
        finally {
            server.stop();
        }
    }


    protected abstract Class<T> getImplementation();


    protected String[] getIgnoredFields() {
        return new String[] {};
    }
}
