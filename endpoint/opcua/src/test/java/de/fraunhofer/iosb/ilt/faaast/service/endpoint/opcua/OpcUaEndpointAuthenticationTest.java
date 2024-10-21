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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua;

import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.SessionActivationException;
import com.prosysopc.ua.UserIdentity;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.stack.core.UserTokenType;
import com.prosysopc.ua.stack.transport.security.SecurityMode;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestConstants;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestService;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestUtils;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EndpointException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test class for tests of the OPC UA Endpoint with different authentication settings.
 * Only authentication with username and password is possible, anonymous access is not allowed.
 */
public class OpcUaEndpointAuthenticationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaEndpointAuthenticationTest.class);

    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpassword";
    private static int opcTcpPort;
    private static String endpointUrl;

    private static TestService service;

    @BeforeClass
    public static void startTest() throws ConfigurationException, IOException, AssetConnectionException, MessageBusException, EndpointException, PersistenceException {
        LOGGER.trace("startTest");
        opcTcpPort = PortHelper.findFreePort();
        endpointUrl = "opc.tcp://localhost:" + opcTcpPort;
        Map<String, String> users = new HashMap<>();
        users.put(USERNAME, PASSWORD);
        OpcUaEndpointConfig config = new OpcUaEndpointConfig.Builder()
                .tcpPort(opcTcpPort)
                .secondsTillShutdown(0)
                .supportedAuthentications(Set.of(UserTokenType.UserName))
                .serverCertificateBasePath(TestConstants.SERVER_CERT_PATH)
                .userCertificateBasePath(TestConstants.USER_CERT_PATH)
                .discoveryServerUrl(null)
                .userMap(users)
                .build();
        Path certPath = Paths.get(TestConstants.SERVER_CERT_PATH);
        if (Files.exists(certPath)) {
            Files.walk(certPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        certPath = Paths.get(TestConstants.USER_CERT_PATH);
        if (Files.exists(certPath)) {
            Files.walk(certPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        service = new TestService(config, null, false);
        service.start();
    }


    @AfterClass
    public static void stopTest() {
        LOGGER.trace("stopTest");
        if (service != null) {
            service.stop();
        }
    }


    @Test
    public void testSuccessfulLogin() throws SecureIdentityException, IOException, ServiceException {
        UaClient client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        client.setUserIdentity(new UserIdentity(USERNAME, PASSWORD));
        TestUtils.initialize(client);
        client.connect();
        Assert.assertTrue(client.isConnected());
    }


    @Test(expected = SessionActivationException.class)
    public void testPreventAnonymousAccess() throws SecureIdentityException, IOException, ServiceException {
        UaClient client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        // The call to connect is expected to throw an exception as anonymous access is not allowed
        client.connect();
    }
}
