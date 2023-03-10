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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
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
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
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
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType;
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
    public void testConnectNoSecurityInvalidUsernamePassword() throws Exception {
        final String usernameServer = "username-test";
        final String passwordServer = "password-test";
        final String usernameClient = "foo";
        final String passwordClient = "bar";
        EmbeddedOpcUaServer server = startServer(
                EmbeddedOpcUaServerConfig.builder()
                        .endpointSecurityConfiguration(EndpointSecurityConfiguration.NONE_NONE_TCP)
                        .endpointSecurityConfiguration(EndpointSecurityConfiguration.NONE_NONE_HTTPS)
                        .applicationCertificate(loadServerApplicationCertificate())
                        .allowedCredential(usernameServer, passwordServer)
                        .build());
        try {
            Assert.assertThrows(IllegalArgumentException.class,
                    () -> assertConnectSecureUsernamePassword(server, EndpointSecurityConfiguration.NONE_NONE_TCP, usernameClient, passwordClient));
            Assert.assertThrows(IllegalArgumentException.class,
                    () -> assertConnectSecureUsernamePassword(server, EndpointSecurityConfiguration.NONE_NONE_HTTPS, usernameClient, passwordClient));
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
        try {
            configurations.forEach(
                    LambdaExceptionHelper.rethrowConsumer(
                            x -> assertConnectSecure(
                                    server,
                                    x,
                                    OpcUaAssetConnectionConfig.builder().build(),
                                    null)));
        }
        finally {
            server.shutdown();
        }
    }


    @Test
    public void testConnectAes128Anonymous() throws Exception {
        List<EndpointSecurityConfiguration> configurations = EndpointSecurityConfiguration.POLICY_AES128_SHA256_RSAOAEP;
        EmbeddedOpcUaServer server = startServer(
                EmbeddedOpcUaServerConfig.builder()
                        .endpointSecurityConfigurations(configurations)
                        .applicationCertificate(loadServerApplicationCertificate())
                        .build());
        try {
            configurations.forEach(
                    LambdaExceptionHelper.rethrowConsumer(
                            x -> assertConnectSecure(
                                    server,
                                    x,
                                    OpcUaAssetConnectionConfig.builder().build(),
                                    null)));
        }
        finally {
            server.shutdown();
        }
    }


    @Test
    public void testConnectAes256Anonymous() throws Exception {
        List<EndpointSecurityConfiguration> configurations = EndpointSecurityConfiguration.POLICY_AES256_SHA256_RSAPSS;
        EmbeddedOpcUaServer server = startServer(
                EmbeddedOpcUaServerConfig.builder()
                        .endpointSecurityConfigurations(configurations)
                        .applicationCertificate(loadServerApplicationCertificate())
                        .build());
        try {
            configurations.forEach(
                    LambdaExceptionHelper.rethrowConsumer(
                            x -> assertConnectSecure(
                                    server,
                                    x,
                                    OpcUaAssetConnectionConfig.builder().build(),
                                    null)));
        }
        finally {
            server.shutdown();
        }
    }


    @Test
    public void testConnectCertificate() throws Exception {
        EmbeddedOpcUaServer server = startServer(
                EmbeddedOpcUaServerConfig.builder()
                        .endpointSecurityConfiguration(EndpointSecurityConfiguration.NONE_NONE_TCP)
                        .endpointSecurityConfiguration(EndpointSecurityConfiguration.NONE_NONE_HTTPS)
                        .applicationCertificate(loadServerApplicationCertificate())
                        .build());
        try {
            assertConnectSecureCertificate(server, EndpointSecurityConfiguration.NONE_NONE_TCP);
            assertConnectSecureCertificate(server, EndpointSecurityConfiguration.NONE_NONE_HTTPS);
        }
        finally {
            server.shutdown();
        }
    }


    private void assertConnectSecureUsernamePassword(
                                                     EmbeddedOpcUaServer server,
                                                     EndpointSecurityConfiguration securityConfiguration,
                                                     String username,
                                                     String password)
            throws ValueFormatException, AssetConnectionException, IOException, GeneralSecurityException, ConfigurationException {
        assertConnectSecure(
                server,
                securityConfiguration,
                OpcUaAssetConnectionConfig.builder()
                        .userTokenType(UserTokenType.UserName)
                        .username(username)
                        .password(password)
                        .build(),
                null);
    }


    private void assertConnectSecureCertificate(
                                                EmbeddedOpcUaServer server,
                                                EndpointSecurityConfiguration securityConfiguration)
            throws ValueFormatException, ConfigurationInitializationException, AssetConnectionException, IOException, GeneralSecurityException, ConfigurationException {
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
                        x.setUserTokenType(UserTokenType.Certificate);
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
            throws ValueFormatException, AssetConnectionException, IOException, GeneralSecurityException, ConfigurationException {

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
            throws ValueFormatException, AssetConnectionException, ConfigurationException {
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
        OpcUaAssetConnectionConfig assetConnConfig = OpcUaAssetConnectionConfig.builder()
                .of(config)
                .valueProvider(reference,
                        OpcUaValueProviderConfig.builder()
                                .nodeId(nodeId)
                                .build())
                .host(server.getEndpoint(config.getTransportProfile()))
                .build();
        OpcUaAssetConnection connection = assetConnConfig.newInstance(CoreConfig.DEFAULT, serviceContext);
        awaitConnection(connection);
        connection.getValueProviders().get(reference).setValue(expected);
        DataElementValue actual = connection.getValueProviders().get(reference).getValue();
        connection.disconnect();
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


    private void setOpcUaValue(OpcUaAssetConnectionConfig config, String nodeId, Object value) throws Exception {
        OpcUaClient client = OpcUaHelper.connect(config);
        client.connect().get();
        StatusCode statusCode = OpcUaHelper.writeValue(client, nodeId, value);
        Assert.assertTrue(statusCode.isGood());
        client.disconnect().get();
    }


    @Test
    public void testReconnect() throws Exception {
        EmbeddedOpcUaServer server = startDefaultServer();
        var serverConfig = server.getConfig();
        double initialValue = 1.0;
        double updatedValue = 2.2;
        String nodeId = "ns=2;s=HelloWorld/ScalarTypes/Double";
        PropertyValue expectedInitial = PropertyValue.of(Datatype.DOUBLE, Double.toString(initialValue));
        PropertyValue expectedUpdated = PropertyValue.of(Datatype.DOUBLE, Double.toString(updatedValue));
        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        ServiceContext serviceContext = mock(ServiceContext.class);
        TypeInfo infoExample = ElementValueTypeInfo.builder()
                .type(PropertyValue.class)
                .datatype(Datatype.DOUBLE)
                .build();
        doReturn(infoExample).when(serviceContext).getTypeInfo(reference);
        OpcUaAssetConnectionConfig config = OpcUaAssetConnectionConfig.builder()
                .host(server.getEndpoint(Protocol.TCP))
                .subscriptionProvider(reference, OpcUaSubscriptionProviderConfig.builder()
                        .nodeId(nodeId)
                        .build())
                .build();
        // set asset value to initial value
        setOpcUaValue(config, nodeId, initialValue);
        // start asset connection & wait for initial value
        OpcUaAssetConnection connection = config.newInstance(CoreConfig.DEFAULT, serviceContext);
        connection.connect();
        final AtomicReference<DataElementValue> initialResponse = new AtomicReference<>();
        CountDownLatch conditionOriginalValue = new CountDownLatch(1);
        NewDataListener initialListener = (DataElementValue data) -> {
            initialResponse.set(data);
            conditionOriginalValue.countDown();
        };
        connection.getSubscriptionProviders().get(reference).addNewDataListener(initialListener);
        Assert.assertTrue(String.format("test failed because there was no response within defined time (%d %s)", getWaitTime(), TimeUnit.MILLISECONDS),
                conditionOriginalValue.await(getWaitTime(), TimeUnit.MILLISECONDS));
        Assert.assertEquals(expectedInitial, initialResponse.get());
        connection.getSubscriptionProviders().get(reference).removeNewDataListener(initialListener);
        // stop asset
        server.shutdown();
        await().atMost(10, TimeUnit.SECONDS)
                .until(() -> !connection.isConnected());
        // restart asset
        server = new EmbeddedOpcUaServer(serverConfig);
        server.startup();
        await().atMost(30, TimeUnit.SECONDS)
                .until(() -> connection.isConnected());
        // set value on asset to updated value
        setOpcUaValue(config, nodeId, updatedValue);
        // wait for updated value from asset connection
        final AtomicReference<DataElementValue> updatedResponse = new AtomicReference<>();
        CountDownLatch conditionUpdated = new CountDownLatch(1);
        NewDataListener updatedListener = (DataElementValue data) -> {
            updatedResponse.set(data);
            conditionUpdated.countDown();
        };
        connection.getSubscriptionProviders().get(reference).addNewDataListener(updatedListener);
        Assert.assertTrue(String.format("test failed because there was no response within defined time (%d %s)", getWaitTime(), TimeUnit.MILLISECONDS),
                conditionUpdated.await(getWaitTime(), TimeUnit.MILLISECONDS));
        Assert.assertEquals(expectedUpdated, updatedResponse.get());
        server.shutdown();
    }


    private void awaitConnection(AssetConnection connection) {
        await().atMost(90, TimeUnit.SECONDS)
                .with()
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> {
                    try {
                        connection.connect();
                    }
                    catch (AssetConnectionException e) {
                        // do nothing
                    }
                    return connection.isConnected();
                });
    }


    private void assertSubscribe(EmbeddedOpcUaServer server, String nodeId, PropertyValue expected, String elementIndex)
            throws AssetConnectionException, InterruptedException, ExecutionException, UaException, ConfigurationInitializationException, Exception {
        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        long interval = 1000;
        ServiceContext serviceContext = mock(ServiceContext.class);
        TypeInfo infoExample = ElementValueTypeInfo.builder()
                .type(PropertyValue.class)
                .datatype(expected.getValue().getDataType())
                .build();
        doReturn(infoExample).when(serviceContext).getTypeInfo(reference);

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
        OpcUaAssetConnection connection = config.newInstance(
                CoreConfig.builder()
                        .build(),
                serviceContext);
        awaitConnection(connection);
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
            throws AssetConnectionException, InterruptedException, ConfigurationInitializationException, ConfigurationException {
        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        ServiceContext serviceContext = mock(ServiceContext.class);
        doReturn(ElementValueTypeInfo.builder()
                .type(expected.getClass())
                .datatype(expected.getValue().getDataType())
                .build())
                        .when(serviceContext)
                        .getTypeInfo(reference);
        OpcUaAssetConnectionConfig config = OpcUaAssetConnectionConfig.builder()
                .valueProvider(reference,
                        OpcUaValueProviderConfig.builder()
                                .nodeId(nodeId)
                                .arrayIndex(arrayIndex)
                                .build())
                .host(server.getEndpoint(Protocol.TCP))
                .build();
        OpcUaAssetConnection connection = config.newInstance(CoreConfig.DEFAULT, serviceContext);
        awaitConnection(connection);
        connection.getValueProviders().get(reference).setValue(expected);
        DataElementValue actual = connection.getValueProviders().get(reference).getValue();
        connection.disconnect();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testValueProviderWithScalarValues()
            throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException, ConfigurationException, Exception {
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
    public void testValueProviderWithArrayValues()
            throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException, ConfigurationException, Exception {
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
            throws AssetConnectionException, InterruptedException, ConfigurationInitializationException, ConfigurationException {
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
            throws AssetConnectionException, InterruptedException, ConfigurationInitializationException, ConfigurationException {
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
        OpcUaAssetConnection connection = config.newInstance(CoreConfig.DEFAULT, serviceContext);
        awaitConnection(connection);
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
        connection.disconnect();
        Assert.assertArrayEquals(expectedOut, actual);
        Assert.assertArrayEquals(expectedInOut, inoutputVariables);
    }


    @Test
    public void testOperationProvider()
            throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException, ConfigurationException, Exception {
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
    public void testOperationProviderMapping()
            throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException, ConfigurationException, Exception {
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

        // copy client certificate to server
        Files.createDirectories(SecurityPathHelper.trustedAllowed(server.getConfig().getSecurityBaseDir()));
        Files.write(
                SecurityPathHelper.trustedAllowed(server.getConfig().getSecurityBaseDir()).resolve("client.cer"),
                KeystoreHelper
                        .loadOrDefault(
                                Thread.currentThread().getContextClassLoader().getResourceAsStream(CLIENT_APPLICATION_CERTIFICATE_FILE),
                                CLIENT_APPLICATION_CERTIFICATE_PASSWORD,
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
    @Ignore("Helper method for generating resources")
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
