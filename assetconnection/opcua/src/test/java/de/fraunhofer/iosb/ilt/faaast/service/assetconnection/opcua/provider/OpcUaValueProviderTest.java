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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.server.EmbeddedOpcUaServer;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.server.EmbeddedOpcUaServerConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.server.EndpointSecurityConfiguration;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.server.Protocol;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.junit.Test;


public class OpcUaValueProviderTest {

    @Test
    public void testEquals() throws Exception {
        EmbeddedOpcUaServer server = new EmbeddedOpcUaServer(
                EmbeddedOpcUaServerConfig.builder().endpointSecurityConfiguration(EndpointSecurityConfiguration.NO_SECURITY_ANONYMOUS).build());
        server.startup();
        try {
            OpcUaClient client1 = OpcUaClient.create(server.getEndpoint(Protocol.TCP));
            OpcUaClient client2 = OpcUaClient.create(server.getEndpoint(Protocol.TCP));
            EqualsVerifier.simple().forClass(OpcUaValueProvider.class)
                    .withPrefabValues(OpcUaClient.class, client1, client2)
                    .verify();
            client1.disconnect();
            client2.disconnect();
        }
        finally {
            server.shutdown();
        }
    }

}
