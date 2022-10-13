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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.AbstractOpcUaBasedTest;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.junit.Test;


public class OpcUaOperationProviderTest extends AbstractOpcUaBasedTest {

    @Test
    public void testEquals() throws UaException {
        OpcUaClient client1 = OpcUaClient.create(serverUrl);
        OpcUaClient client2 = OpcUaClient.create(serverUrl);
        EqualsVerifier.simple().forClass(OpcUaOperationProvider.class)
                .withPrefabValues(OpcUaClient.class, client1, client2)
                .withIgnoredFields("inputArgumentMappingList", "outputArgumentMappingList")
                .verify();
        client1.disconnect();
        client2.disconnect();
    }

}
