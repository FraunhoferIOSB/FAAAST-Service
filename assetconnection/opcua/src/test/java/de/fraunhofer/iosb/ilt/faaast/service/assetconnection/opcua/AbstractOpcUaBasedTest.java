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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua;

import java.io.IOException;
import java.net.ServerSocket;
import org.eclipse.milo.opcua.sdk.server.identity.AnonymousIdentityValidator;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;


public abstract class AbstractOpcUaBasedTest {

    protected static final long DEFAULT_TIMEOUT = 1000;
    protected static EmbeddedOpcUaServer server;
    protected static String serverUrl;
    protected static int tcpPort;
    protected static int httpsPort;

    @BeforeClass
    public static void init() throws Exception {
        tcpPort = findFreePort();
        httpsPort = findFreePort();
        server = new EmbeddedOpcUaServer(AnonymousIdentityValidator.INSTANCE, tcpPort, httpsPort);
        server.startup().get();
        serverUrl = "opc.tcp://localhost:" + tcpPort + "/milo";
    }


    @AfterClass
    public static void cleanup() throws Exception {
        server.shutdown().get();
    }


    protected static int findFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Assert.assertNotNull(serverSocket);
            Assert.assertTrue(serverSocket.getLocalPort() > 0);
            return serverSocket.getLocalPort();
        }
    }

}
