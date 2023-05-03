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
import com.prosysopc.ua.UaAddress;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.stack.core.EndpointDescription;
import com.prosysopc.ua.stack.core.UserTokenType;
import com.prosysopc.ua.stack.transport.security.SecurityPolicy;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestConstants;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestService;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestUtils;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EndpointException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test class for OPC UA Endpoint configuration tests.
 */
public class OpcUaEndpointConfigTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaEndpointConfigTest.class);

    @Test
    public void testSecurityPolicyOnlyNone() throws ConfigurationException, Exception {
        Assert.assertTrue(testConfig(List.of(SecurityPolicy.NONE), List.of(UserTokenType.Anonymous)));
    }


    @Test
    public void testSecurityPolicyBasic256Sha256() throws ConfigurationException, Exception {
        Assert.assertTrue(testConfig(List.of(SecurityPolicy.BASIC256SHA256), List.of(UserTokenType.UserName)));
    }


    @Test
    public void testSecurityPolicyBasic128() throws ConfigurationException, Exception {
        Assert.assertTrue(testConfig(List.of(SecurityPolicy.BASIC128RSA15), List.of(UserTokenType.Anonymous, UserTokenType.Certificate)));
    }


    @Test
    public void testSecurityPolicyAllSecure104() throws ConfigurationException, Exception {
        Assert.assertTrue(testConfig(List.of(SecurityPolicy.ALL_SECURE_104.toArray(SecurityPolicy[]::new)),
                List.of(UserTokenType.Anonymous, UserTokenType.UserName, UserTokenType.Certificate)));
    }


    @Test
    public void testSecurityPolicyMultiple1() throws ConfigurationException, Exception {
        Assert.assertTrue(
                testConfig(List.of(SecurityPolicy.BASIC256SHA256, SecurityPolicy.NONE, SecurityPolicy.BASIC256), List.of(UserTokenType.Anonymous, UserTokenType.Certificate)));
    }


    @Test
    public void testSecurityPolicyMultiple2() throws ConfigurationException, Exception {
        Assert.assertTrue(
                testConfig(List.of(SecurityPolicy.BASIC256SHA256, SecurityPolicy.AES128_SHA256_RSAOAEP, SecurityPolicy.AES256_SHA256_RSAPSS, SecurityPolicy.BASIC128RSA15),
                        List.of(UserTokenType.UserName, UserTokenType.Certificate)));
    }


    @Test
    public void testSecurityPolicyMultiple3() throws ConfigurationException, Exception {
        Assert.assertTrue(testConfig(List.of(SecurityPolicy.NONE, SecurityPolicy.BASIC256SHA256, SecurityPolicy.AES128_SHA256_RSAOAEP, SecurityPolicy.AES256_SHA256_RSAPSS,
                SecurityPolicy.BASIC128RSA15), List.of(UserTokenType.Certificate, UserTokenType.Anonymous)));
    }


    private boolean testConfig(List<SecurityPolicy> expectedPolicies, List<UserTokenType> expectedUserTokens)
            throws ConfigurationException, IOException, AssetConnectionException, MessageBusException, EndpointException, SecureIdentityException, ServiceException {
        int port = TestUtils.findFreePort();
        String url = "opc.tcp://localhost:" + port;

        List<String> expectedPolicyUris = new ArrayList<>();
        expectedPolicies.stream().forEach(ep -> {
            expectedPolicyUris.add(ep.getPolicyUri());
        });
        OpcUaEndpointConfig config = new OpcUaEndpointConfig.Builder()
                .tcpPort(port)
                .secondsTillShutdown(0)
                .supportedAuthentication(UserTokenType.Anonymous)
                .serverCertificateBasePath(TestConstants.SERVER_CERT_PATH)
                .userCertificateBasePath(TestConstants.USER_CERT_PATH)
                .discoveryServerUrl(null)
                .supportedSecurityPolicies(expectedPolicies)
                .supportedAuthentications(expectedUserTokens)
                .build();

        TestService service = new TestService(config, null, false);
        service.start();

        UaClient discoveryClient = new UaClient();
        discoveryClient.setAddress(UaAddress.parse(url));
        List<String> currentPolicies = new ArrayList<>();
        List<UserTokenType> currentUserTokens = new ArrayList<>();
        for (EndpointDescription ed: discoveryClient.discoverEndpoints()) {
            if (!currentPolicies.contains(ed.getSecurityPolicyUri())) {
                LOGGER.info("testConfig: found SecurityPolicyUri {}", ed.getSecurityPolicyUri());
                currentPolicies.add(ed.getSecurityPolicyUri());
            }
            if (currentUserTokens.isEmpty()) {
                for (var t: ed.getUserIdentityTokens()) {
                    currentUserTokens.add(t.getTokenType());
                }
            }
        }

        LOGGER.info("testConfig: found {} policyUris and {} userTokens", currentPolicies.size(), currentUserTokens.size());
        Assert.assertEquals(expectedPolicies.size(), currentPolicies.size());
        Assert.assertTrue(
                expectedPolicyUris.size() == currentPolicies.size() && expectedPolicyUris.containsAll(currentPolicies) && currentPolicies.containsAll(expectedPolicyUris));
        Assert.assertTrue(
                expectedUserTokens.size() == currentUserTokens.size() && expectedUserTokens.containsAll(currentUserTokens) && currentUserTokens.containsAll(expectedUserTokens));
        service.stop();
        return true;
    }
}
