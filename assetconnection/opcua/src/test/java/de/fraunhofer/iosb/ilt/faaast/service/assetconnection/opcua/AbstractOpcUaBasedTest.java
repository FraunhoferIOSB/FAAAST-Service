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
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import org.eclipse.milo.opcua.sdk.server.identity.AnonymousIdentityValidator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;


public abstract class AbstractOpcUaBasedTest {

    protected static final long WAITTIME_MS = 100000;
    protected final long DEFAULT_TIMEOUT = 1000;
    protected EmbeddedOpcUaServer server;
    protected String serverUrl;
    protected int tcpPort;
    protected int httpsPort;

    @Before
    public void init() throws Exception {
        tcpPort = findFreePort();
        httpsPort = findFreePort();
        server = new EmbeddedOpcUaServer(AnonymousIdentityValidator.INSTANCE, tcpPort, httpsPort);
        server.startup().get();
        serverUrl = "opc.tcp://localhost:" + tcpPort + "/milo";
    }


    @After
    public void cleanup() throws Exception {
        server.shutdown().get();
    }


    protected static int findFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Assert.assertNotNull(serverSocket);
            Assert.assertTrue(serverSocket.getLocalPort() > 0);
            return serverSocket.getLocalPort();
        }
    }


    protected static boolean isDebugging() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
    }


    protected static long getWaitTime() {
        return WAITTIME_MS * (isDebugging() ? 1000 : 1);
    }

}
