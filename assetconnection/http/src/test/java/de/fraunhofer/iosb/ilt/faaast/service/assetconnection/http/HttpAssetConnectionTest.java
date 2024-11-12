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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.request;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.format.JsonFormat;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.CertificateData;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.CertificateInformation;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.util.KeyStoreHelper;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeExtractor;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.net.ssl.SSLHandshakeException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;


public class HttpAssetConnectionTest {

    @ClassRule
    public static WireMockClassRule server;
    // required, see https://wiremock.org/docs/junit-extensions/#other-rule-configurations
    @Rule
    public WireMockClassRule instanceRule = server;

    private static final long DEFAULT_TIMEOUT = 10000;
    private static final String KEY_PASSWORD = "changeit";
    private static final String KEY_STORE_PASSWORD = "changeit";
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final Reference REFERENCE = ReferenceHelper.parseReference("(Property)[ID_SHORT]Temperature");
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final CertificateInformation SELF_SIGNED_SERVER_CERTIFICATE_INFO = CertificateInformation.builder()
            .applicationUri("urn:de:fraunhofer:iosb:ilt:faaast:service:assetconnection:http:test")
            .commonName("FA³ST Service HTTP Asset Connection Test")
            .countryCode("DE")
            .localityName("Karlsruhe")
            .organization("Fraunhofer IOSB")
            .organizationUnit("ILT")
            .ipAddress("127.0.0.1")
            .dnsName("localhost")
            .build();

    private static URL httpUrl;
    private static URL httpsUrl;
    private static File keyStoreFile;

    @BeforeClass
    public static void init() throws IOException, GeneralSecurityException {
        generateSelfSignedServerCertificate();
        server = new WireMockClassRule(options()
                .dynamicHttpsPort()
                .dynamicPort()
                .httpDisabled(false)
                .keystoreType(KEYSTORE_TYPE)
                .keystorePath(keyStoreFile.getAbsolutePath())
                .keystorePassword(KEY_PASSWORD)
                .keyManagerPassword(KEY_STORE_PASSWORD));
    }


    @Before
    public void initUrls() throws MalformedURLException {
        httpUrl = new URL("http", "localhost", server.port(), "");
        httpsUrl = new URL("https", "localhost", server.httpsPort(), "");
    }


    @After
    public void resetWiremock() {
        WireMock.resetAllRequests();
    }


    @Test
    public void testValueProviderPropertySetValueWithTemplateJSONHttps() throws AssetConnectionException,
            ConfigurationInitializationException,
            ValueFormatException,
            ResourceNotFoundException, PersistenceException {
        String template = "{\"foo\" : \"${value}\", \"bar\": [1, 2, 3]}";
        String value = "5";
        assertValueProviderPropertyWriteJson(
                PropertyValue.of(Datatype.INT, "5"),
                template,
                template.replaceAll("\\$\\{value\\}", value),
                true);
    }


    @Test
    public void testValueProviderWithHeaders()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertValueProviderHeaders(Map.of(), Map.of(), Map.of(), false);
        assertValueProviderHeaders(Map.of("foo", "bar"), Map.of(), Map.of("foo", "bar"), false);
        assertValueProviderHeaders(Map.of("foo", "bar"), Map.of("foo", "bar"), Map.of("foo", "bar"), false);
        assertValueProviderHeaders(Map.of("foo", "bar"), Map.of("foo", "bar2"), Map.of("foo", "bar2"), false);
        assertValueProviderHeaders(Map.of("foo", "bar"), Map.of("bar", "foo"), Map.of("foo", "bar", "bar", "foo"), false);
    }


    @Test
    public void testValueProviderPropertyGetValueJSON()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertValueProviderPropertyReadJson(
                PropertyValue.of(Datatype.INT, "5"),
                "5",
                null,
                false);
    }


    @Test
    public void testHttpsUntrusted() {
        HttpAssetConnectionConfig config = HttpAssetConnectionConfig.builder()
                .baseUrl(httpsUrl)
                .build();
        AssetConnectionException exception = Assert.assertThrows(
                AssetConnectionException.class,
                () -> assertValueProviderPropertyReadJson(
                        PropertyValue.of(Datatype.INT, "5"),
                        "5",
                        null,
                        config));
        Throwable cause = exception.getCause();
        while (Objects.nonNull(cause)) {
            if (SSLHandshakeException.class.isAssignableFrom(cause.getClass())) {
                return;
            }
            cause = cause.getCause();
        }
        Assert.fail("Expected SSLHandshakeException but none found");
    }


    @Test
    public void testValueProviderPropertyGetValueWithQueryJSON()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertValueProviderPropertyReadJson(
                PropertyValue.of(Datatype.INT, "5"),
                "{\"foo\" : [1, 2, 5]}",
                "$.foo[2]",
                false);
    }


    @Test
    public void testValueProviderPropertySetValueJSON()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertValueProviderPropertyWriteJson(
                PropertyValue.of(Datatype.INT, "5"),
                null,
                "5",
                false);
    }


    @Test
    public void testValueProviderPropertySetValueWithTemplateJSON()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        String template = "{\"foo\" : \"${value}\", \"bar\": [1, 2, 3]}";
        String value = "5";
        assertValueProviderPropertyWriteJson(
                PropertyValue.of(Datatype.INT, "5"),
                template,
                template.replaceAll("\\$\\{value\\}", value),
                false);
    }


    @Test
    public void testSubscriptionProviderPropertyJsonGET()
            throws AssetConnectionException, ConfigurationInitializationException, InterruptedException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertSubscriptionProviderPropertyJson(
                Datatype.INT,
                RequestMethod.GET,
                List.of("{ \"value\": 1}",
                        "{ \"value\": 2}"),
                "$.value",
                //null,
                false,
                PropertyValue.of(Datatype.INT, "1"),
                PropertyValue.of(Datatype.INT, "2"));
    }


    @Test
    public void testSubscriptionProviderPropertyJsonGET2()
            throws AssetConnectionException, ConfigurationInitializationException, InterruptedException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertSubscriptionProviderPropertyJson(
                Datatype.INT,
                RequestMethod.GET,
                List.of("{\n"
                        + "	\"data\": [\n"
                        + "		{\n"
                        + "			\"value\": 42\n"
                        + "		}\n"
                        + "	]\n"
                        + "}"),
                "$.data[-1:].value",
                //null,
                false,
                PropertyValue.of(Datatype.INT, "42"));
    }


    @Test
    public void testSubscriptionProviderPropertyJsonPOST()
            throws AssetConnectionException, ConfigurationInitializationException, InterruptedException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertSubscriptionProviderPropertyJson(
                Datatype.INT,
                RequestMethod.GET,
                List.of("{ \"value\": 1}",
                        "{ \"value\": 2}"),
                "$.value",
                //"{ \"input\": \"foo\"}",
                false,
                PropertyValue.of(Datatype.INT, "1"),
                PropertyValue.of(Datatype.INT, "2"));
    }


    @Test
    public void testOperationProviderPropertyJsonPOSTNoParameters() throws AssetConnectionException,
            ConfigurationInitializationException,
            ResourceNotFoundException, PersistenceException {
        assertOperationProviderPropertyJson(
                RequestMethod.POST,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false);
    }


    @Test
    public void testOperationProviderPropertyJsonPOSTInputOnly()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertOperationProviderPropertyJson(
                RequestMethod.POST,
                "{ \"parameters\": { \"in1\": ${in1} }}",
                "{ \"parameters\": { \"in1\": \"foo\" }}",
                null,
                null,
                Map.of("in1", TypedValueFactory.create(Datatype.STRING, "foo")),
                null,
                null,
                null,
                false);
    }


    @Test
    public void testOperationProviderPropertyJsonPOSTOutputOnly()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertOperationProviderPropertyJson(
                RequestMethod.POST,
                null,
                null,
                "{ \"result\": 1.5 }",
                Map.of("out1", "$.result"),
                null,
                null,
                Map.of("out1", TypedValueFactory.create(Datatype.DOUBLE, "1.5")),
                null,
                false);
    }


    @Test
    public void testOperationProviderPropertyJsonPOSTInputOutputOnly()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertOperationProviderPropertyJson(
                RequestMethod.POST,
                "{ \"parameters\": { \"in1\": ${in1}, \"in2\": ${in2} }}",
                "{ \"parameters\": { \"in1\": 42, \"in2\": 17 }}",
                "{ \"result\": 25 }",
                Map.of("out1", "$.result"),
                Map.of("in1", TypedValueFactory.create(Datatype.INTEGER, "42"),
                        "in2", TypedValueFactory.create(Datatype.INTEGER, "17")),
                null,
                Map.of("out1", TypedValueFactory.create(Datatype.INTEGER, "25")),
                null,
                false);
    }


    @Test
    public void testOperationProviderPropertyJsonPOSTInoutputOnly()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertOperationProviderPropertyJson(
                RequestMethod.POST,
                "{ \"parameters\": { \"inout1\": ${inout1}}}",
                "{ \"parameters\": { \"inout1\": 42}}",
                "{ \"result\": { \"inout1\": 17}}",
                Map.of("inout1", "$.result.inout1"),
                null,
                Map.of("inout1", TypedValueFactory.create(Datatype.INTEGER, "42")),
                null,
                Map.of("inout1", TypedValueFactory.create(Datatype.INTEGER, "17")),
                false);
    }


    @Test
    public void testOperationProviderPropertyJsonPOST()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertOperationProviderPropertyJson(
                RequestMethod.POST,
                "{ \"parameters\": { \"in1\": ${in1}, \"inout1\": ${inout1} }}",
                "{ \"parameters\": { \"in1\": 1, \"inout1\": 2 }}",
                "{ \"result\": 3, \"modified\": { \"inout1\": 4 }}",
                Map.of("out1", "$.result",
                        "inout1", "$.modified.inout1"),
                Map.of("in1", TypedValueFactory.create(Datatype.INT, "1")),
                Map.of("inout1", TypedValueFactory.create(Datatype.INT, "2")),
                Map.of("out1", TypedValueFactory.create(Datatype.INT, "3")),
                Map.of("inout1", TypedValueFactory.create(Datatype.INT, "4")),
                false);
    }


    private static void generateSelfSignedServerCertificate() throws IOException, GeneralSecurityException {
        keyStoreFile = Files.createTempFile("faaast-assetconnection-http-cert", ".p12").toFile();
        keyStoreFile.deleteOnExit();
        CertificateData certificateData = KeyStoreHelper.generateSelfSigned(SELF_SIGNED_SERVER_CERTIFICATE_INFO);
        KeyStoreHelper.save(certificateData, keyStoreFile, KEYSTORE_TYPE, null, KEY_PASSWORD, KEY_STORE_PASSWORD);
    }


    private void assertValueProviderHeaders(
                                            Map<String, String> connectionHeaders,
                                            Map<String, String> providerHeaders,
                                            Map<String, String> expectedHeaders,
                                            boolean useHttps)
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        PropertyValue value = PropertyValue.of(Datatype.INT, "5");
        assertValueProviderPropertyJson(
                value.getValue().getDataType(),
                RequestMethod.GET,
                connectionHeaders,
                providerHeaders,
                value.getValue().asString(),
                null,
                null,
                useHttps,
                x -> {
                    RequestPatternBuilder result = x;
                    if (expectedHeaders != null) {
                        for (var expectedHeader: expectedHeaders.entrySet()) {
                            result = result.withHeader(expectedHeader.getKey(), equalTo(expectedHeader.getValue()));
                        }
                    }
                    return result;
                },
                LambdaExceptionHelper.rethrowConsumer(x -> Assert.assertEquals(value, x.getValue())));
    }


    private void assertValueProviderPropertyReadJson(PropertyValue expected, String httpResponseBody, String query, boolean useHttps)
            throws AssetConnectionException, ConfigurationInitializationException, ResourceNotFoundException, PersistenceException {
        assertValueProviderPropertyJson(
                expected.getValue().getDataType(),
                RequestMethod.GET,
                null,
                null,
                httpResponseBody,
                query,
                null,
                useHttps,
                x -> x.withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON)),
                LambdaExceptionHelper.rethrowConsumer(x -> Assert.assertEquals(expected, x.getValue())));
    }


    private void assertValueProviderPropertyReadJson(PropertyValue expected, String httpResponseBody, String query, HttpAssetConnectionConfig config)
            throws AssetConnectionException, ConfigurationInitializationException, ResourceNotFoundException, PersistenceException {
        assertValueProviderPropertyJson(
                expected.getValue().getDataType(),
                RequestMethod.GET,
                null,
                httpResponseBody,
                query,
                null,
                config,
                x -> x.withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON)),
                LambdaExceptionHelper.rethrowConsumer(x -> {
                    try {
                        Assert.assertEquals("Unexpected property value.", expected, x.getValue());
                    }
                    catch (AssertionError e) {
                        throw new AssertionError("Assertion failed in assertValueProviderPropertyReadJson().", e);
                    }
                }));
    }


    private void assertValueProviderPropertyWriteJson(PropertyValue newValue, String template, String expectedResponseBody, boolean useHttps)
            throws AssetConnectionException, ConfigurationInitializationException, ResourceNotFoundException, PersistenceException {
        assertValueProviderPropertyJson(
                newValue.getValue().getDataType(),
                RequestMethod.PUT,
                null,
                null,
                null,
                null,
                template,
                useHttps,
                x -> x.withRequestBody(equalToJson(expectedResponseBody)),
                LambdaExceptionHelper.rethrowConsumer(x -> x.setValue(newValue)));
    }


    private void awaitConnection(AssetConnection connection) {
        await().atMost(60, TimeUnit.SECONDS)
                .with()
                .pollInterval(2, TimeUnit.SECONDS)
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


    private HttpAssetConnectionConfig createAssetConnectionConfig(Map<String, String> connectionHeaders, boolean useHttps) {
        HttpAssetConnectionConfig result = HttpAssetConnectionConfig.builder()
                .headers(connectionHeaders != null ? connectionHeaders : Map.of())
                .baseUrl(httpUrl)
                .build();
        if (useHttps) {
            result.setBaseUrl(httpsUrl);
            result.getTrustedCertificates().setKeyStorePath(keyStoreFile.getAbsolutePath());
            result.getTrustedCertificates().setKeyStorePassword(KEY_PASSWORD);
        }
        return result;
    }


    private void assertValueProviderPropertyJson(Datatype datatype,
                                                 RequestMethod method,
                                                 Map<String, String> connectionHeaders,
                                                 Map<String, String> providerHeaders,
                                                 String httpResponseBody,
                                                 String query,
                                                 String template,
                                                 boolean useHttps,
                                                 Function<RequestPatternBuilder, RequestPatternBuilder> verifierModifier,
                                                 Consumer<AssetValueProvider> customAssert)
            throws AssetConnectionException, ConfigurationInitializationException, ResourceNotFoundException, PersistenceException {
        assertValueProviderPropertyJson(
                datatype,
                method,
                providerHeaders,
                httpResponseBody,
                query,
                template,
                createAssetConnectionConfig(connectionHeaders, useHttps),
                verifierModifier,
                customAssert);
    }


    private void assertValueProviderPropertyJson(Datatype datatype,
                                                 RequestMethod method,
                                                 Map<String, String> providerHeaders,
                                                 String httpResponseBody,
                                                 String query,
                                                 String template,
                                                 HttpAssetConnectionConfig config,
                                                 Function<RequestPatternBuilder, RequestPatternBuilder> verifierModifier,
                                                 Consumer<AssetValueProvider> customAssert)
            throws AssetConnectionException, ConfigurationInitializationException, ResourceNotFoundException, PersistenceException {
        ServiceContext serviceContext = mock(ServiceContext.class);
        doReturn(ElementValueTypeInfo.builder()
                .type(PropertyValue.class)
                .datatype(datatype)
                .build())
                .when(serviceContext)
                .getTypeInfo(REFERENCE);
        String path = String.format("/test/random/%s", UUID.randomUUID());
        stubFor(request(method.getName(), urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(httpResponseBody)));
        config.getValueProviders().put(REFERENCE, HttpValueProviderConfig.builder()
                .path(path)
                .headers(providerHeaders != null ? providerHeaders : Map.of())
                .format(JsonFormat.KEY)
                .query(query)
                .template(template)
                .build());
        HttpAssetConnection connection = new HttpAssetConnection(
                CoreConfig.builder().build(),
                config,
                serviceContext);
        awaitConnection(connection);
        try {
            if (customAssert != null) {
                customAssert.accept(connection.getValueProviders().get(REFERENCE));
            }
            RequestPatternBuilder verifier = new RequestPatternBuilder(method, urlEqualTo(path));
            if (verifierModifier != null) {
                verifier = verifierModifier.apply(verifier);
            }
            verify(exactly(1), verifier);
        }
        finally {
            connection.disconnect();
        }
    }


    private void assertSubscriptionProviderPropertyJson(Datatype datatype,
                                                        RequestMethod method,
                                                        List<String> httpResponseBodies,
                                                        String query,
                                                        boolean useHttps,
                                                        PropertyValue... expected)
            throws AssetConnectionException, ConfigurationInitializationException, InterruptedException, ResourceNotFoundException, PersistenceException {
        ServiceContext serviceContext = mock(ServiceContext.class);
        doReturn(ElementValueTypeInfo.builder()
                .type(PropertyValue.class)
                .datatype(datatype)
                .build())
                .when(serviceContext)
                .getTypeInfo(REFERENCE);
        String id = UUID.randomUUID().toString();
        String path = String.format("/test/random/%s", id);
        if (httpResponseBodies != null && !httpResponseBodies.isEmpty()) {
            stubFor(request(method.getName(), urlEqualTo(path))
                    .inScenario(id)
                    .whenScenarioStateIs(Scenario.STARTED)
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                            .withBody(httpResponseBodies.get(0)))
                    .willSetStateTo("1"));
            for (int i = 1; i < httpResponseBodies.size(); i++) {
                stubFor(request(method.getName(), urlEqualTo(path))
                        .inScenario(id)
                        .whenScenarioStateIs(Objects.toString(i))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                                .withBody(httpResponseBodies.get(i)))
                        .willSetStateTo(Objects.toString(i + 1)));
            }
        }
        HttpAssetConnectionConfig config = createAssetConnectionConfig(null, useHttps);
        config.getSubscriptionProviders().put(REFERENCE, HttpSubscriptionProviderConfig.builder()
                .interval(1000)
                .path(path)
                .format(JsonFormat.KEY)
                .query(query)
                .build());
        HttpAssetConnection connection = new HttpAssetConnection(
                CoreConfig.builder()
                        .build(),
                config,
                serviceContext);
        awaitConnection(connection);
        NewDataListener listener = null;
        try {
            CountDownLatch condition = new CountDownLatch(expected.length);
            final PropertyValue[] actual = new PropertyValue[expected.length];
            final AtomicInteger pointer = new AtomicInteger(0);
            listener = (DataElementValue data) -> {
                if (pointer.get() <= expected.length) {
                    actual[pointer.getAndIncrement()] = ((PropertyValue) data);
                    condition.countDown();
                }
            };
            connection.getSubscriptionProviders().get(REFERENCE).addNewDataListener(listener);
            condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            connection.getSubscriptionProviders().get(REFERENCE).removeNewDataListener(listener);
            Assert.assertArrayEquals(expected, actual);
            RequestPatternBuilder verifier = new RequestPatternBuilder(method, urlEqualTo(path));
            verify(exactly(httpResponseBodies.size()), verifier);
        }
        finally {
            connection.disconnect();
        }
    }


    private void assertOperationProviderPropertyJson(
                                                     RequestMethod method,
                                                     String template,
                                                     String expectedRequestToAsset,
                                                     String assetResponse,
                                                     Map<String, String> queries,
                                                     Map<String, TypedValue> input,
                                                     Map<String, TypedValue> inoutput,
                                                     Map<String, TypedValue> expectedOutput,
                                                     Map<String, TypedValue> expectedInoutput,
                                                     boolean useHttps)
            throws AssetConnectionException, ResourceNotFoundException, ConfigurationInitializationException, PersistenceException {
        ServiceContext serviceContext = mock(ServiceContext.class);
        OperationVariable[] output = toOperationVariables(expectedOutput);
        doReturn(output)
                .when(serviceContext)
                .getOperationOutputVariables(REFERENCE);
        if (output != null) {
            Stream.of(output).forEach(x -> {
                try {
                    doReturn(TypeExtractor.extractTypeInfo(x.getValue()))
                            .when(serviceContext)
                            .getTypeInfo(AasUtils.toReference(REFERENCE, x.getValue()));
                }
                catch (ResourceNotFoundException | PersistenceException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        String path = String.format("/test/random/%s", "foo");
        stubFor(request(method.getName(), urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(assetResponse)));
        HttpAssetConnectionConfig config = createAssetConnectionConfig(null, useHttps);
        config.getOperationProviders().put(REFERENCE, HttpOperationProviderConfig.builder()
                .method(method.toString())
                .path(path)
                .queries(queries)
                .format(JsonFormat.KEY)
                .template(template)
                .build());
        HttpAssetConnection connection = new HttpAssetConnection(
                CoreConfig.builder()
                        .build(),
                config,
                serviceContext);
        awaitConnection(connection);
        try {
            OperationVariable[] actualInput = toOperationVariables(input);
            OperationVariable[] actualInoutput = toOperationVariables(inoutput);
            OperationVariable[] actualOutput = connection.getOperationProviders().get(REFERENCE).invoke(actualInput, actualInoutput);
            // assert output is as correct
            Assert.assertArrayEquals(output, actualOutput);
            // assert inoutput is correct
            Assert.assertArrayEquals(toOperationVariables(expectedInoutput), actualInoutput);
            // assert correct HTTP request to asset has been made
            RequestPatternBuilder verifier = new RequestPatternBuilder(method, urlEqualTo(path));
            if (expectedRequestToAsset != null) {
                verifier = verifier.withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
                        .withRequestBody(equalToJson(expectedRequestToAsset));
            }
            verify(exactly(1), verifier);
        }
        finally {
            connection.disconnect();
        }
    }


    private OperationVariable[] toOperationVariables(Map<String, TypedValue> values) {
        if (values == null) {
            return new OperationVariable[0];
        }
        return values.entrySet().stream()
                .map(x -> new DefaultOperationVariable.Builder()
                        .value(new DefaultProperty.Builder()
                                .idShort(x.getKey())
                                .value(x.getValue().asString())
                                .valueType(x.getValue().getDataType().getAas4jDatatype())
                                .build())
                        .build())
                .toArray(OperationVariable[]::new);
    }
}
