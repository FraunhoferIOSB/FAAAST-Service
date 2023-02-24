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

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.ArgumentMapping;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.security.CertificateData;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.security.CertificateInformation;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.server.EmbeddedOpcUaServer;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.server.EmbeddedOpcUaServerConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.server.EndpointSecurityConfiguration;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.server.Protocol;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.util.KeystoreHelper;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.util.OpcUaConstants;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.util.OpcUaHelper;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.util.SecurityPathHelper;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DateTimeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


public class OpcUaAssetConnectionTest {

    private static final long WAITTIME_MS = 100000;
    private final long DEFAULT_TIMEOUT = 1000;
    public static final String SERVER_APPLICATION_CERTIFICATE_FILE = "server-application.p12";
    public static final String SERVER_APPLICATION_CERTIFICATE_PASSWORD = "";

    public static final String CLIENT_APPLICATION_CERTIFICATE_FILE = "client-application.p12";
    public static final String CLIENT_APPLICATION_CERTIFICATE_PASSWORD = "";
    public static final String CLIENT_AUTHENTICATION_CERTIFICATE_FILE = "client-authentication.p12";
    public static final String CLIENT_AUTHENTICATION_CERTIFICATE_PASSWORD = "";

    private static boolean isDebugging() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
    }


    private static long getWaitTime() {
        return WAITTIME_MS * (isDebugging() ? 1000 : 1);
    }


    @Test
    public void testConnectNoSecurity() throws Exception {
        EmbeddedOpcUaServer server = startServer(
                EmbeddedOpcUaServerConfig.builder()
                        .endpointSecurityConfiguration(EndpointSecurityConfiguration.NONE_NONE_TCP)
                        .endpointSecurityConfiguration(EndpointSecurityConfiguration.NONE_NONE_HTTPS)
                        .build());
        try {
            assertConnect(server,
                    OpcUaAssetConnectionConfig.builder()
                            .transportProfile(TransportProfile.TCP_UASC_UABINARY)
                            .build());
            assertConnect(server,
                    OpcUaAssetConnectionConfig.builder()
                            .transportProfile(TransportProfile.HTTPS_UABINARY)
                            .build());
        }
        finally {
            server.shutdown();
        }
    }


    @Test
    public void testConnectNoSecurityUsernamePassword() throws Exception {
        final String username = "username-test";
        final String password = "password-test";
        EmbeddedOpcUaServer server = startServer(
                EmbeddedOpcUaServerConfig.builder()
                        .endpointSecurityConfiguration(EndpointSecurityConfiguration.NONE_NONE_TCP)
                        .endpointSecurityConfiguration(EndpointSecurityConfiguration.NONE_NONE_HTTPS)
                        .applicationCertificate(loadServerApplicationCertificate())
                        .allowedCredential(username, password)
                        .build());
        try {
            assertConnectSecureUsernamePassword(server, EndpointSecurityConfiguration.NONE_NONE_TCP, username, password);
            assertConnectSecureUsernamePassword(server, EndpointSecurityConfiguration.NONE_NONE_HTTPS, username, password);
        }
        finally {
            server.shutdown();
        }
    }


    @Test
    public void testConnectBasic256Sha256Anonymous() throws Exception {
        List<EndpointSecurityConfiguration> configurations = EndpointSecurityConfiguration.POLICY_BASIC256SHA256;
        EmbeddedOpcUaServer server = startServer(
                EmbeddedOpcUaServerConfig.builder()
                        .endpointSecurityConfigurations(configurations)
                        .applicationCertificate(loadServerApplicationCertificate())
                        .build());
        Path clientSecurityBaseDir = Files.createTempDirectory("client");
        Files.createDirectories(clientSecurityBaseDir);
        Files.copy(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(CLIENT_APPLICATION_CERTIFICATE_FILE),
                clientSecurityBaseDir.resolve(CLIENT_APPLICATION_CERTIFICATE_FILE),
                StandardCopyOption.REPLACE_EXISTING);
        Files.copy(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(CLIENT_AUTHENTICATION_CERTIFICATE_FILE),
                clientSecurityBaseDir.resolve(CLIENT_AUTHENTICATION_CERTIFICATE_FILE),
                StandardCopyOption.REPLACE_EXISTING);
        exchangeCertificates(server, clientSecurityBaseDir);
        try {
            configurations.forEach(
                    LambdaExceptionHelper.rethrowConsumer(
                            x -> assertConnect(
                                    server,
                                    OpcUaAssetConnectionConfig.builder()
                                            .securityBaseDir(clientSecurityBaseDir)
                                            .securityMode(x.getSecurityMode())
                                            .securityPolicy(x.getPolicy())
                                            .host(server.getEndpoint(x.getProtocol()))
                                            .transportProfile(EmbeddedOpcUaServer.getTransportProfile(x.getProtocol()))
                                            .applicationCertificateFile(clientSecurityBaseDir.resolve(CLIENT_APPLICATION_CERTIFICATE_FILE).toFile())
                                            .applicationCertificatePassword(CLIENT_APPLICATION_CERTIFICATE_PASSWORD)
                                            .build())));
        }
        finally {
            server.shutdown();
        }
    }


    //    @Test
    //    public void testConnectBasic256() throws ValueFormatException, ConfigurationInitializationException, AssetConnectionException, IOException, GeneralSecurityException {
    //        assertConnectSecure(EndpointSecurityConfiguration.BASIC256_SIGN_TCP);
    //        assertConnectSecure(EndpointSecurityConfiguration.BASIC256_SIGN_HTTPS);
    //        assertConnectSecure(EndpointSecurityConfiguration.BASIC256_SIGN_ENCRYPT_TCP);
    //        assertConnectSecure(EndpointSecurityConfiguration.BASIC256_SIGN_ENCRYPT_HTTPS);
    //    }
    //    @Test
    //    public void testConnectBasic256UsernamePassword()
    //            throws ValueFormatException, ConfigurationInitializationException, AssetConnectionException, IOException, GeneralSecurityException {
    //        assertConnectSecureUsernamePassword(EndpointSecurityConfiguration.BASIC256_SIGN_TCP, USERNAME, PASSWORD);
    //        assertConnectSecureUsernamePassword(EndpointSecurityConfiguration.BASIC256_SIGN_HTTPS, USERNAME, PASSWORD);
    //        assertConnectSecureUsernamePassword(EndpointSecurityConfiguration.BASIC256_SIGN_ENCRYPT_TCP, USERNAME, PASSWORD);
    //        assertConnectSecureUsernamePassword(EndpointSecurityConfiguration.BASIC256_SIGN_ENCRYPT_HTTPS, USERNAME, PASSWORD);
    //    }
    //
    //    @Test
    //    public void testConnectBasic256Certificate()
    //            throws ValueFormatException, ConfigurationInitializationException, AssetConnectionException, IOException, GeneralSecurityException {
    //        assertConnectSecureCertificate(EndpointSecurityConfiguration.BASIC256_SIGN_TCP);
    //        assertConnectSecureCertificate(EndpointSecurityConfiguration.BASIC256_SIGN_HTTPS);
    //        assertConnectSecureCertificate(EndpointSecurityConfiguration.BASIC256_SIGN_ENCRYPT_TCP);
    //        assertConnectSecureCertificate(EndpointSecurityConfiguration.BASIC256_SIGN_ENCRYPT_HTTPS);
    //    }
    //    @Test
    //    public void testConnectBasic256InvalidUsernamePassword()
    //            throws ValueFormatException, ConfigurationInitializationException, AssetConnectionException, IOException, GeneralSecurityException {
    //        String username = "foo";
    //        String password = "bar";
    //
    //        Assert.assertThrows(ConfigurationInitializationException.class,
    //                () -> assertConnectSecureUsernamePassword(EndpointSecurityConfiguration.BASIC256_SIGN_TCP, username, password));
    //
    //        Assert.assertThrows(ConfigurationInitializationException.class,
    //                () -> assertConnectSecureUsernamePassword(EndpointSecurityConfiguration.BASIC256_SIGN_HTTPS, username, password));
    //
    //        Assert.assertThrows(ConfigurationInitializationException.class,
    //                () -> assertConnectSecureUsernamePassword(EndpointSecurityConfiguration.BASIC256_SIGN_ENCRYPT_TCP, username, password));
    //
    //        Assert.assertThrows(ConfigurationInitializationException.class,
    //                () -> assertConnectSecureUsernamePassword(EndpointSecurityConfiguration.BASIC256_SIGN_ENCRYPT_HTTPS, username, password));
    //    }
    private void assertConnectSecureUsernamePassword(
                                                     EmbeddedOpcUaServer server,
                                                     EndpointSecurityConfiguration securityConfiguration,
                                                     String username,
                                                     String password)
            throws ValueFormatException, ConfigurationInitializationException, AssetConnectionException, IOException, GeneralSecurityException {
        assertConnectSecure(
                server,
                securityConfiguration,
                OpcUaAssetConnectionConfig.builder()
                        .username(username)
                        .password(password)
                        .build(),
                null);
    }


    private void assertConnectUsernamePassword(EmbeddedOpcUaServer server, String username, String password)
            throws ValueFormatException, ConfigurationInitializationException, AssetConnectionException, IOException, GeneralSecurityException {
        assertConnect(
                server,
                OpcUaAssetConnectionConfig.builder()
                        .username(username)
                        .password(password)
                        .build());
    }


    private void assertConnectSecureCertificate(
                                                EmbeddedOpcUaServer server,
                                                EndpointSecurityConfiguration securityConfiguration)
            throws ValueFormatException, ConfigurationInitializationException, AssetConnectionException, IOException, GeneralSecurityException {
        List<X509Certificate> clientCertificate = new ArrayList<>();
        try {
            assertConnectSecure(server,
                    securityConfiguration,
                    OpcUaAssetConnectionConfig.builder().build(),
                    LambdaExceptionHelper.rethrowConsumer(x -> {
                        X509Certificate certificate = OpcUaHelper.loadAuthenticationCertificate(
                                x.getSecurityBaseDir(),
                                new File(CLIENT_AUTHENTICATION_CERTIFICATE_FILE),
                                CLIENT_AUTHENTICATION_CERTIFICATE_PASSWORD)
                                .getCertificate();
                        server.allowClient(certificate);
                        clientCertificate.add(certificate);
                        x.setAuthenticationCertificateFile(new File(CLIENT_AUTHENTICATION_CERTIFICATE_FILE));
                        x.setAuthenticationCertificatePassword(CLIENT_AUTHENTICATION_CERTIFICATE_PASSWORD);
                    }));
        }
        finally {
            if (!clientCertificate.isEmpty()) {
                server.disallowClient(clientCertificate.get(0));
            }
        }
    }


    private void assertConnectSecure(
                                     EmbeddedOpcUaServer server,
                                     EndpointSecurityConfiguration securityConfiguration,
                                     OpcUaAssetConnectionConfig config,
                                     Consumer<OpcUaAssetConnectionConfig> beforeConnect)
            throws ValueFormatException, ConfigurationInitializationException, AssetConnectionException, IOException, GeneralSecurityException {

        Path clientBaseSecurityDir = Paths.get(Files.createTempDirectory("client").toString(), "client");
        try {
            Files.createDirectories(clientBaseSecurityDir);
            Files.copy(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(CLIENT_APPLICATION_CERTIFICATE_FILE),
                    clientBaseSecurityDir.resolve(CLIENT_APPLICATION_CERTIFICATE_FILE),
                    StandardCopyOption.REPLACE_EXISTING);
            Files.copy(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(CLIENT_AUTHENTICATION_CERTIFICATE_FILE),
                    clientBaseSecurityDir.resolve(CLIENT_AUTHENTICATION_CERTIFICATE_FILE),
                    StandardCopyOption.REPLACE_EXISTING);
            exchangeCertificates(server, clientBaseSecurityDir);
            OpcUaAssetConnectionConfig actualConfig = OpcUaAssetConnectionConfig.builder()
                    .of(config)
                    .securityBaseDir(clientBaseSecurityDir)
                    .applicationCertificateFile(clientBaseSecurityDir.resolve(CLIENT_APPLICATION_CERTIFICATE_FILE).toFile())
                    .applicationCertificatePassword(CLIENT_APPLICATION_CERTIFICATE_PASSWORD)
                    .securityMode(securityConfiguration.getSecurityMode())
                    .securityPolicy(securityConfiguration.getPolicy())
                    .transportProfile(EmbeddedOpcUaServer.getTransportProfile(securityConfiguration.getProtocol()))
                    .host(server.getEndpoint(securityConfiguration.getProtocol()))
                    .build();
            if (Objects.nonNull(beforeConnect)) {
                beforeConnect.accept(actualConfig);
            }
            assertConnect(server, actualConfig);
        }
        finally {
            removeDir(clientBaseSecurityDir);
        }
    }


    private static void removeDir(Path dir) {
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        catch (IOException ex) {
            // ignore
        }
    }


    private void assertConnect(EmbeddedOpcUaServer server, OpcUaAssetConnectionConfig config)
            throws ValueFormatException, ConfigurationInitializationException, AssetConnectionException {
        String nodeId = "ns=2;s=HelloWorld/ScalarTypes/Double";
        PropertyValue expected = PropertyValue.of(Datatype.DOUBLE, "3.3");
        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        ServiceContext serviceContext = mock(ServiceContext.class);
        doReturn(ElementValueTypeInfo.builder()
                .type(expected.getClass())
                .datatype(expected.getValue().getDataType())
                .build())
                        .when(serviceContext)
                        .getTypeInfo(reference);
        OpcUaAssetConnection connection = new OpcUaAssetConnection(
                CoreConfig.builder().build(),
                OpcUaAssetConnectionConfig.builder()
                        .of(config)
                        .valueProvider(reference,
                                OpcUaValueProviderConfig.builder()
                                        .nodeId(nodeId)
                                        .build())
                        // TODO remove
                        .requestTimeout(config.getRequestTimeout())
                        .acknowledgeTimeout(config.getAcknowledgeTimeout())
                        .host(server.getEndpoint(config.getTransportProfile()))
                        .build(),
                serviceContext);
        connection.getValueProviders().get(reference).setValue(expected);
        DataElementValue actual = connection.getValueProviders().get(reference).getValue();
        connection.close();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSubscriptionProviderWithScalarValues()
            throws AssetConnectionException, InterruptedException, ValueFormatException, ExecutionException, UaException, ConfigurationInitializationException, Exception {
        EmbeddedOpcUaServer server = startDefaultServer();
        assertSubscribe(server, "ns=2;s=HelloWorld/ScalarTypes/Double", PropertyValue.of(Datatype.DOUBLE, "0.1"), null);
        server.shutdown();
    }


    private static EmbeddedOpcUaServer startDefaultServer() throws Exception {
        return startServer(EmbeddedOpcUaServerConfig.builder()
                .endpointSecurityConfiguration(EndpointSecurityConfiguration.NONE_NONE_TCP)
                .build());
    }


    private static EmbeddedOpcUaServer startServer(EmbeddedOpcUaServerConfig config) throws Exception {
        EmbeddedOpcUaServer result = new EmbeddedOpcUaServer(config);
        result.startup();
        return result;
    }


    private static CertificateData loadServerApplicationCertificate() throws IOException, GeneralSecurityException {
        return KeystoreHelper.load(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(SERVER_APPLICATION_CERTIFICATE_FILE),
                SERVER_APPLICATION_CERTIFICATE_PASSWORD);
    }


    @Test
    public void testSubscriptionProviderWithArrayValues()
            throws AssetConnectionException, InterruptedException, ValueFormatException, ExecutionException, UaException, ConfigurationInitializationException, Exception {
        EmbeddedOpcUaServer server = startDefaultServer();
        assertSubscribe(server, "ns=2;s=HelloWorld/MatrixTypes/DoubleArray", PropertyValue.of(Datatype.DOUBLE, "5.3"), "[3][2]");
        server.shutdown();
    }


    @Test
    public void testReconnect()
            throws AssetConnectionException, InterruptedException, ValueFormatException, ExecutionException, UaException, ConfigurationInitializationException, Exception {
        EmbeddedOpcUaServer server = startDefaultServer();
        var serverConfig = server.getConfig();
        Thread.sleep(5000);
        String nodeId = "ns=2;s=HelloWorld/ScalarTypes/Double";
        PropertyValue expected = PropertyValue.of(Datatype.DOUBLE, "0.1");
        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        long interval = 1000;
        ServiceContext serviceContext = mock(ServiceContext.class);
        TypeInfo infoExample = ElementValueTypeInfo.builder()
                .type(PropertyValue.class)
                .datatype(expected.getValue().getDataType())
                .build();
        doReturn(infoExample).when(serviceContext).getTypeInfo(reference);
        OpcUaAssetConnection connection = new OpcUaAssetConnection();
        OpcUaAssetConnectionConfig config = OpcUaAssetConnectionConfig.builder()
                .host(server.getEndpoint(Protocol.TCP))
                .subscriptionProvider(reference, OpcUaSubscriptionProviderConfig.builder()
                        .nodeId(nodeId)
                        .interval(interval)
                        .build())
                .valueProvider(reference,
                        OpcUaValueProviderConfig.builder()
                                .nodeId(nodeId)
                                .build())
                .build();
        connection.init(
                CoreConfig.builder()
                        .build(),
                config,
                serviceContext);
        // first value should always be the current value
        OpcUaClient client = OpcUaHelper.connect(config);
        client.connect().get();
        DataValue originalValue = OpcUaHelper.readValue(client, nodeId);
        client.disconnect().get();
        final AtomicReference<DataElementValue> originalValueResponse = new AtomicReference<>();
        CountDownLatch conditionOriginalValue = new CountDownLatch(1);
        connection.getSubscriptionProviders().get(reference).addNewDataListener((DataElementValue data) -> {
            originalValueResponse.set(data);
            conditionOriginalValue.countDown();
        });
        Assert.assertTrue(String.format("test failed because there was no response within defined time (%d %s)", getWaitTime(), TimeUnit.MILLISECONDS),
                conditionOriginalValue.await(getWaitTime(), TimeUnit.MILLISECONDS));
        Assert.assertEquals(
                PropertyValue.of(expected.getValue().getDataType(), originalValue.getValue().getValue().toString()),
                originalValueResponse.get());
        // second value should be new value
        final AtomicReference<DataElementValue> newValueResponse = new AtomicReference<>();
        CountDownLatch conditionNewValue = new CountDownLatch(1);
        connection.getSubscriptionProviders().get(reference).addNewDataListener((DataElementValue data) -> {
            newValueResponse.set(data);
            conditionNewValue.countDown();
        });
        server.shutdown();
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> !connection.isConnected());
        server = new EmbeddedOpcUaServer(serverConfig);
        server.startup();
        connection.getValueProviders().get(reference).setValue(expected);
        Assert.assertTrue(String.format("test failed because there was no response within defined time (%d %s)", getWaitTime(), TimeUnit.MILLISECONDS),
                conditionNewValue.await(getWaitTime(), TimeUnit.MILLISECONDS));
        Assert.assertEquals(expected, newValueResponse.get());
        server.shutdown();
    }


    private void assertSubscribe(
                                 EmbeddedOpcUaServer server,
                                 String nodeId,
                                 PropertyValue expected,
                                 String elementIndex)
            throws AssetConnectionException, InterruptedException, ExecutionException, UaException, ConfigurationInitializationException, Exception {
        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        long interval = 1000;
        ServiceContext serviceContext = mock(ServiceContext.class);
        TypeInfo infoExample = ElementValueTypeInfo.builder()
                .type(PropertyValue.class)
                .datatype(expected.getValue().getDataType())
                .build();
        doReturn(infoExample).when(serviceContext).getTypeInfo(reference);
        OpcUaAssetConnection connection = new OpcUaAssetConnection();
        OpcUaAssetConnectionConfig config = OpcUaAssetConnectionConfig.builder()
                .host(server.getEndpoint(Protocol.TCP))
                .subscriptionProvider(reference, OpcUaSubscriptionProviderConfig.builder()
                        .nodeId(nodeId)
                        .interval(interval)
                        .arrayIndex(elementIndex)
                        .build())
                .valueProvider(reference,
                        OpcUaValueProviderConfig.builder()
                                .nodeId(nodeId)
                                .arrayIndex(elementIndex)
                                .build())
                .build();
        connection.init(
                CoreConfig.builder()
                        .build(),
                config,
                serviceContext);
        // first value should always be the current value
        OpcUaClient client = OpcUaHelper.connect(config);
        DataValue originalValue = OpcUaHelper.readValue(client, nodeId);
        client.disconnect().get();
        final AtomicReference<DataElementValue> originalValueResponse = new AtomicReference<>();
        CountDownLatch conditionOriginalValue = new CountDownLatch(1);
        connection.getSubscriptionProviders().get(reference).addNewDataListener((DataElementValue data) -> {
            originalValueResponse.set(data);
            conditionOriginalValue.countDown();
        });
        Assert.assertTrue(String.format("test failed because there was no response within defined time (%d %s)", getWaitTime(), TimeUnit.MILLISECONDS),
                conditionOriginalValue.await(getWaitTime(), TimeUnit.MILLISECONDS));
        if ((elementIndex == null) || elementIndex.equals("")) {
            Assert.assertEquals(
                    PropertyValue.of(expected.getValue().getDataType(), originalValue.getValue().getValue().toString()),
                    originalValueResponse.get());
        }
        // second value should be new value
        final AtomicReference<DataElementValue> newValueResponse = new AtomicReference<>();
        CountDownLatch conditionNewValue = new CountDownLatch(1);
        connection.getSubscriptionProviders().get(reference).addNewDataListener((DataElementValue data) -> {
            newValueResponse.set(data);
            conditionNewValue.countDown();
        });
        connection.getValueProviders().get(reference).setValue(expected);
        Assert.assertTrue(String.format("test failed because there was no response within defined time (%d %s)", getWaitTime(), TimeUnit.MILLISECONDS),
                conditionNewValue.await(getWaitTime(), TimeUnit.MILLISECONDS));
        Assert.assertEquals(expected, newValueResponse.get());
    }


    private void assertWriteReadValue(
                                      EmbeddedOpcUaServer server,
                                      String nodeId,
                                      PropertyValue expected,
                                      String arrayIndex)
            throws AssetConnectionException, InterruptedException, ConfigurationInitializationException {
        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        ServiceContext serviceContext = mock(ServiceContext.class);
        doReturn(ElementValueTypeInfo.builder()
                .type(expected.getClass())
                .datatype(expected.getValue().getDataType())
                .build())
                        .when(serviceContext)
                        .getTypeInfo(reference);
        OpcUaAssetConnection connection = new OpcUaAssetConnection(
                CoreConfig.builder()
                        .build(),
                OpcUaAssetConnectionConfig.builder()
                        .valueProvider(reference,
                                OpcUaValueProviderConfig.builder()
                                        .nodeId(nodeId)
                                        .arrayIndex(arrayIndex)
                                        .build())
                        .host(server.getEndpoint(Protocol.TCP))
                        .build(),
                serviceContext);
        connection.getValueProviders().get(reference).setValue(expected);
        DataElementValue actual = connection.getValueProviders().get(reference).getValue();
        connection.close();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testValueProviderWithScalarValues() throws Exception {
        EmbeddedOpcUaServer server = startDefaultServer();
        assertWriteReadValue(server, "ns=2;s=HelloWorld/ScalarTypes/Double", PropertyValue.of(Datatype.DOUBLE, "3.3"), null);
        assertWriteReadValue(server, "ns=2;s=HelloWorld/ScalarTypes/String", PropertyValue.of(Datatype.STRING, "hello world!"), null);
        assertWriteReadValue(server, "ns=2;s=HelloWorld/ScalarTypes/Integer", PropertyValue.of(Datatype.INTEGER, "42"), null);
        assertWriteReadValue(server, "ns=2;s=HelloWorld/ScalarTypes/Boolean", PropertyValue.of(Datatype.BOOLEAN, "true"), null);
        assertWriteReadValue(server, "ns=2;s=HelloWorld/ScalarTypes/DateTime",
                PropertyValue.of(Datatype.DATE_TIME, ZonedDateTime.of(2022, 11, 28, 14, 12, 35, 0, ZoneId.of(DateTimeValue.DEFAULT_TIMEZONE)).toString()), null);
        server.shutdown();
    }


    @Test
    public void testValueProviderWithArrayValues() throws Exception {
        EmbeddedOpcUaServer server = startDefaultServer();
        assertWriteReadValue(server, "ns=2;s=HelloWorld/ArrayTypes/Int32Array", PropertyValue.of(Datatype.INT, "78"), "[2]");
        assertWriteReadValue(server, "ns=2;s=HelloWorld/ArrayTypes/FloatArray", PropertyValue.of(Datatype.FLOAT, "24.5"), "[1]");
        assertWriteReadValue(server, "ns=2;s=HelloWorld/ArrayTypes/StringArray", PropertyValue.of(Datatype.STRING, "new test value"), "[3]");
        assertWriteReadValue(server, "ns=2;s=HelloWorld/MatrixTypes/DoubleArray", PropertyValue.of(Datatype.DOUBLE, "789.5"), "[2][4]");
        assertWriteReadValue(server, "ns=2;s=HelloWorld/MatrixTypes/BooleanArray", PropertyValue.of(Datatype.BOOLEAN, "true"), "[1][0]");
        server.shutdown();
    }


    private void assertInvokeOperation(
                                       EmbeddedOpcUaServer server,
                                       String nodeId,
                                       boolean sync,
                                       Map<String, PropertyValue> input,
                                       Map<String, PropertyValue> inoutput,
                                       Map<String, PropertyValue> expectedInoutput,
                                       Map<String, PropertyValue> expectedOutput)
            throws AssetConnectionException, InterruptedException, ConfigurationInitializationException {
        assertInvokeOperation(server, nodeId, sync, input, inoutput, expectedInoutput, expectedOutput, null, null);
    }


    private void assertInvokeOperation(
                                       EmbeddedOpcUaServer server,
                                       String nodeId,
                                       boolean sync,
                                       Map<String, PropertyValue> input,
                                       Map<String, PropertyValue> inoutput,
                                       Map<String, PropertyValue> expectedInoutput,
                                       Map<String, PropertyValue> expectedOutput,
                                       List<ArgumentMapping> inputMapping,
                                       List<ArgumentMapping> outputMapping)
            throws AssetConnectionException, InterruptedException, ConfigurationInitializationException {
        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        OpcUaAssetConnectionConfig config = OpcUaAssetConnectionConfig.builder()
                .host(server.getEndpoint(Protocol.TCP))
                .operationProvider(reference,
                        OpcUaOperationProviderConfig.builder()
                                .nodeId(nodeId)
                                .inputArgumentMappings(inputMapping)
                                .outputArgumentMappings(outputMapping)
                                .build())
                .build();
        OperationVariable[] inputVariables = input == null
                ? new OperationVariable[0]
                : input.entrySet().stream().map(x -> {
                    Property property = new DefaultProperty.Builder()
                            .idShort(x.getKey())
                            .build();
                    ElementValueMapper.setValue(property, x.getValue());
                    return new DefaultOperationVariable.Builder()
                            .value(property)
                            .build();
                }).toArray(OperationVariable[]::new);
        OperationVariable[] inoutputVariables = inoutput == null
                ? new OperationVariable[0]
                : inoutput.entrySet().stream().map(x -> {
                    Property property = new DefaultProperty.Builder()
                            .idShort(x.getKey())
                            .build();
                    ElementValueMapper.setValue(property, x.getValue());
                    return new DefaultOperationVariable.Builder()
                            .value(property)
                            .build();
                }).toArray(OperationVariable[]::new);
        OperationVariable[] expectedInOut = expectedInoutput == null
                ? new OperationVariable[0]
                : expectedInoutput.entrySet().stream().map(x -> {
                    Property property = new DefaultProperty.Builder()
                            .idShort(x.getKey())
                            .build();
                    ElementValueMapper.setValue(property, x.getValue());
                    return new DefaultOperationVariable.Builder()
                            .value(property)
                            .build();
                }).toArray(OperationVariable[]::new);
        OperationVariable[] expectedOut = expectedOutput == null
                ? new OperationVariable[0]
                : expectedOutput.entrySet().stream().map(x -> {
                    Property property = new DefaultProperty.Builder()
                            .idShort(x.getKey())
                            .build();
                    ElementValueMapper.setValue(property, x.getValue());
                    return new DefaultOperationVariable.Builder()
                            .value(property)
                            .build();
                }).toArray(OperationVariable[]::new);
        ServiceContext serviceContext = mock(ServiceContext.class);
        doReturn(expectedOut)
                .when(serviceContext)
                .getOperationOutputVariables(reference);
        OpcUaAssetConnection connection = new OpcUaAssetConnection(CoreConfig.builder().build(), config, serviceContext);
        OperationVariable[] actual;
        if (sync) {
            actual = connection.getOperationProviders().get(reference).invoke(inputVariables, inoutputVariables);
        }
        else {
            final AtomicReference<OperationVariable[]> operationResult = new AtomicReference<>();
            final AtomicReference<OperationVariable[]> operationInout = new AtomicReference<>();
            CountDownLatch condition = new CountDownLatch(1);
            connection.getOperationProviders().get(reference).invokeAsync(inputVariables, inoutputVariables, (res, inout) -> {
                operationResult.set(res);
                operationInout.set(inout);
                condition.countDown();
            });
            condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            actual = operationResult.get();
            inoutputVariables = operationInout.get();
        }
        connection.close();
        Assert.assertArrayEquals(expectedOut, actual);
        Assert.assertArrayEquals(expectedInOut, inoutputVariables);
    }


    @Test
    public void testOperationProvider() throws Exception {
        EmbeddedOpcUaServer server = startDefaultServer();
        String nodeIdSqrt = "ns=2;s=HelloWorld/sqrt(x)";
        assertInvokeOperation(server,
                nodeIdSqrt,
                true,
                Map.of("x", PropertyValue.of(Datatype.DOUBLE, "9.0")),
                null,
                null,
                Map.of("x_sqrt", PropertyValue.of(Datatype.DOUBLE, "3.0")));
        assertInvokeOperation(server,
                nodeIdSqrt,
                false,
                Map.of("x", PropertyValue.of(Datatype.DOUBLE, "9.0")),
                null,
                null,
                Map.of("x_sqrt", PropertyValue.of(Datatype.DOUBLE, "3.0")));
        assertInvokeOperation(server,
                nodeIdSqrt,
                true,
                null,
                Map.of("x", PropertyValue.of(Datatype.DOUBLE, "4.0"),
                        "x_sqrt", PropertyValue.of(Datatype.DOUBLE, "4.0")),
                Map.of("x", PropertyValue.of(Datatype.DOUBLE, "4.0"),
                        "x_sqrt", PropertyValue.of(Datatype.DOUBLE, "2.0")),
                Map.of("x_sqrt", PropertyValue.of(Datatype.DOUBLE, "2.0")));
        assertInvokeOperation(server,
                nodeIdSqrt,
                false,
                null,
                Map.of("x", PropertyValue.of(Datatype.DOUBLE, "4.0"),
                        "x_sqrt", PropertyValue.of(Datatype.DOUBLE, "4.0")),
                Map.of("x", PropertyValue.of(Datatype.DOUBLE, "4.0"),
                        "x_sqrt", PropertyValue.of(Datatype.DOUBLE, "2.0")),
                Map.of("x_sqrt", PropertyValue.of(Datatype.DOUBLE, "2.0")));
        server.shutdown();
    }


    @Test
    public void testOperationProviderMapping() throws Exception {
        EmbeddedOpcUaServer server = startDefaultServer();
        String nodeIdSqrt = "ns=2;s=HelloWorld/sqrt(x)";
        assertInvokeOperation(server,
                nodeIdSqrt,
                true,
                Map.of("x_aas", PropertyValue.of(Datatype.DOUBLE, "4.0")),
                null,
                null,
                Map.of("x_sqrt", PropertyValue.of(Datatype.DOUBLE, "2.0")),
                List.of(ArgumentMapping.builder()
                        .idShort("x_aas")
                        .argumentName("x")
                        .build()),
                null);
        assertInvokeOperation(server,
                nodeIdSqrt,
                false,
                Map.of("x", PropertyValue.of(Datatype.DOUBLE, "4.0")),
                null,
                null,
                Map.of("x_sqrt_aas", PropertyValue.of(Datatype.DOUBLE, "2.0")),
                null,
                List.of(ArgumentMapping.builder()
                        .idShort("x_sqrt_aas")
                        .argumentName("x_sqrt")
                        .build()));
        assertInvokeOperation(server,
                nodeIdSqrt,
                true,
                null,
                Map.of("x_aas", PropertyValue.of(Datatype.DOUBLE, "4.0"),
                        "x_sqrt_aas", PropertyValue.of(Datatype.DOUBLE, "4.0")),
                Map.of("x_aas", PropertyValue.of(Datatype.DOUBLE, "4.0"),
                        "x_sqrt_aas", PropertyValue.of(Datatype.DOUBLE, "2.0")),
                Map.of("x_sqrt_aas", PropertyValue.of(Datatype.DOUBLE, "2.0")),
                List.of(ArgumentMapping.builder()
                        .idShort("x_aas")
                        .argumentName("x")
                        .build()),
                List.of(ArgumentMapping.builder()
                        .idShort("x_sqrt_aas")
                        .argumentName("x_sqrt")
                        .build()));
        assertInvokeOperation(server,
                nodeIdSqrt,
                false,
                null,
                Map.of("x_aas", PropertyValue.of(Datatype.DOUBLE, "4.0"),
                        "x_sqrt_aas", PropertyValue.of(Datatype.DOUBLE, "4.0")),
                Map.of("x_aas", PropertyValue.of(Datatype.DOUBLE, "4.0"),
                        "x_sqrt_aas", PropertyValue.of(Datatype.DOUBLE, "2.0")),
                Map.of("x_sqrt_aas", PropertyValue.of(Datatype.DOUBLE, "2.0")),
                List.of(ArgumentMapping.builder()
                        .idShort("x_aas")
                        .argumentName("x")
                        .build()),
                List.of(ArgumentMapping.builder()
                        .idShort("x_sqrt_aas")
                        .argumentName("x_sqrt")
                        .build()));
        server.shutdown();
    }


    private void exchangeCertificates(EmbeddedOpcUaServer server, Path clientSecurityBaseDir) throws IOException, GeneralSecurityException {
        // copy server certificate to client
        Files.createDirectories(SecurityPathHelper.trustedAllowed(clientSecurityBaseDir));
        Files.write(
                SecurityPathHelper.trustedAllowed(clientSecurityBaseDir).resolve("server.cer"),
                server.getConfig().getApplicationCertificate()
                        .getCertificate()
                        .getEncoded());
        // load directly into server
        //        server.getServer().getConfig().getTrustListManager().addTrustedCertificate(KeystoreHelper
        //                .loadOrDefault(
        //                        Thread.currentThread().getContextClassLoader().getResourceAsStream(CLIENT_APPLICATION_CERTIFICATE_FILE),
        //                        CLIENT_APPLICATION_CERTIFICATE_PASSWORD,
        //                        OpcUaConstants.DEFAULT_APPLICATION_CERTIFICATE_INFO)
        //                .getCertificate());

        // copy client certificate to server
        Files.createDirectories(SecurityPathHelper.trustedAllowed(server.getConfig().getSecurityBaseDir()));
        Files.write(
                SecurityPathHelper.trustedAllowed(server.getConfig().getSecurityBaseDir()).resolve("client.cer"),
                KeystoreHelper
                        .loadOrDefault(
                                Thread.currentThread().getContextClassLoader().getResourceAsStream(CLIENT_APPLICATION_CERTIFICATE_FILE),
                                "",
                                CertificateInformation.builder().build())
                        .getCertificate()
                        .getEncoded());
    }


    @Test
    @Ignore("Helper method for generating resources")
    public void generateServerCertificateStoreForTesting() throws IOException, GeneralSecurityException, URISyntaxException {
        generateCertificateStoreForTesting(EmbeddedOpcUaServer.DEFAULT_APPLICATION_CERTIFICATE_FILE,
                OpcUaConstants.DEFAULT_APPLICATION_CERTIFICATE_INFO,
                EmbeddedOpcUaServer.DEFAULT_APPLICATION_CERTIFICATE_PASSWORD);
    }


    @Test
    @Ignore("Helper method for generating resources")
    public void generateClientApplicationCertificateStoreForTesting() throws IOException, GeneralSecurityException, URISyntaxException {
        generateCertificateStoreForTesting(
                CLIENT_APPLICATION_CERTIFICATE_FILE,
                OpcUaConstants.DEFAULT_APPLICATION_CERTIFICATE_INFO,
                CLIENT_APPLICATION_CERTIFICATE_PASSWORD);
    }


    @Test
    //@Ignore("Helper method for generating resources")
    public void generateClientAuthenticationCertificateStoreForTesting() throws IOException, GeneralSecurityException, URISyntaxException {
        generateCertificateStoreForTesting(
                CLIENT_AUTHENTICATION_CERTIFICATE_FILE,
                OpcUaConstants.DEFAULT_APPLICATION_CERTIFICATE_INFO,
                CLIENT_AUTHENTICATION_CERTIFICATE_PASSWORD);
    }


    private void generateCertificateStoreForTesting(String filename, CertificateInformation certificateInformation, String password)
            throws IOException, GeneralSecurityException, URISyntaxException {
        File file = Path.of(Thread.currentThread().getContextClassLoader().getResource("").toURI())
                .resolve("../../src/test/resources/")
                .resolve(filename)
                .toFile();
        KeystoreHelper.save(
                file,
                KeystoreHelper.generateSelfSigned(certificateInformation),
                password);
        Assert.assertTrue(file.exists());
    }

}
