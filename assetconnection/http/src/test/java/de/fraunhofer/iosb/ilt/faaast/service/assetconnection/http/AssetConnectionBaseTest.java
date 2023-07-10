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
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeExtractor;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import java.io.File;
import java.io.IOException;
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
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AssetConnectionBaseTest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AssetConnectionBaseTest.class);
    protected static final long DEFAULT_TIMEOUT = 10000;
    protected static final String KEYSTORE_PASSWORD = "changeit";
    protected static final String KEYMANAGER_PASSWORD = "changeit";
    protected static final String KEYSTORE_TYPE_SERVER = "PKCS12";
    protected static File keyStoreFile = null;
    protected static final Reference REFERENCE = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
    protected static final String CONTENT_TYPE = "Content-Type";
    protected static final String APPLICATION_JSON = "application/json";
    protected URL baseUrl;
    static final CertificateInformation SELF_SIGNED_SERVER_CERTIFICATE_INFO = CertificateInformation.builder()
            .applicationUri("urn:de:fraunhofer:iosb:ilt:faaast:service:assetconnection:http:test")
            .commonName("FA³ST Service HTTP Asset Connection Test")
            .countryCode("DE")
            .localityName("Karlsruhe")
            .organization("Fraunhofer IOSB")
            .organizationUnit("ILT")
            .ipAddress("127.0.0.1")
            .dnsName("localhost")
            .build();

    @Rule
    public WireMockRule wireMockRule = createWireMockRule();


    protected abstract WireMockRule createWireMockRule();


    protected abstract String getKeyStorePath();


    @Test
    public abstract void testValueProviderWithHeaders() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException;


    @Test
    public abstract void testValueProviderPropertyGetValueJSON() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException;


    @Test
    public abstract void testValueProviderPropertyGetValueWithQueryJSON() throws AssetConnectionException,
            ValueFormatException, ConfigurationInitializationException, InterruptedException;


    @Test
    public abstract void testValueProviderPropertySetValueJSON() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException;


    @Test
    public abstract void testValueProviderPropertySetValueWithTemplateJSON()
            throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException, InterruptedException;


    @Test
    public abstract void testSubscriptionProviderPropertyJsonGET() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException;


    @Test
    public abstract void testSubscriptionProviderPropertyJsonGET2() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException;


    @Test
    public abstract void testSubscriptionProviderPropertyJsonPOST() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException;


    @Test
    public abstract void testOperationProviderPropertyJsonPOSTNoParameters()
            throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException, InterruptedException;


    @Test
    public abstract void testOperationProviderPropertyJsonPOSTInputOnly() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException;


    @Test
    public abstract void testOperationProviderPropertyJsonPOSTOutputOnly()
            throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException, InterruptedException;


    @Test
    public abstract void testOperationProviderPropertyJsonPOSTInputOutputOnly()
            throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException, InterruptedException;


    @Test
    public abstract void testOperationProviderPropertyJsonPOSTInoutputOnly()
            throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException, InterruptedException;


    @Test
    public abstract void testOperationProviderPropertyJsonPOST() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException;

    void generateSelfSignedServerCertificate() {
        try {
            keyStoreFile = Files.createTempFile("faaast-assetconnection-http-cert", ".p12").toFile();
            CertificateData certificateData = KeyStoreHelper.generateSelfSigned(SELF_SIGNED_SERVER_CERTIFICATE_INFO);
            KeyStoreHelper.save(KEYSTORE_TYPE_SERVER, keyStoreFile, certificateData, KEYSTORE_PASSWORD, KEYMANAGER_PASSWORD);
            LOGGER.info("Self-signed cert generated & stored successfully in the server keystore" + SELF_SIGNED_SERVER_CERTIFICATE_INFO);
            KeyStoreHelper.save(keyStoreFile, certificateData, KEYSTORE_PASSWORD);
            LOGGER.info("Self-signed cert stored successfully in the client keystore" + SELF_SIGNED_SERVER_CERTIFICATE_INFO);

        }
        catch (GeneralSecurityException | IOException exception) {
            LOGGER.error("Creating keystore or generating certs not succeeded!", exception);
        }
        finally {
            if (keyStoreFile != null) {
                keyStoreFile.deleteOnExit();
            }
        }
    }


    void assertValueProviderHeaders(Map<String, String> connectionHeaders, Map<String, String> providerHeaders, Map<String, String> expectedHeaders)
            throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException {
        PropertyValue value = PropertyValue.of(Datatype.INT, "5");
        assertValueProviderPropertyJson(
                value.getValue().getDataType(),
                RequestMethod.GET,
                connectionHeaders,
                providerHeaders,
                value.getValue().asString(),
                null,
                null,
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


    void assertValueProviderPropertyReadJson(PropertyValue expected, String httpResponseBody, String query)
            throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException, InterruptedException {
        assertValueProviderPropertyJson(
                expected.getValue().getDataType(),
                RequestMethod.GET,
                null,
                null,
                httpResponseBody,
                query,
                null,
                x -> x.withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON)),
                LambdaExceptionHelper.rethrowConsumer(x -> Assert.assertEquals(expected, x.getValue())));
    }


    void assertValueProviderPropertyWriteJson(PropertyValue newValue, String template, String expectedResponseBody)
            throws AssetConnectionException, ConfigurationInitializationException {
        assertValueProviderPropertyJson(
                newValue.getValue().getDataType(),
                RequestMethod.PUT,
                null,
                null,
                null,
                null,
                template,
                x -> x.withRequestBody(equalToJson(expectedResponseBody)),
                LambdaExceptionHelper.rethrowConsumer(x -> x.setValue(newValue)));
    }


    void awaitConnection(AssetConnection connection) {
        await().atMost(30, TimeUnit.SECONDS)
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


    void assertValueProviderPropertyJson(Datatype datatype,
                                         RequestMethod method,
                                         Map<String, String> connectionHeaders,
                                         Map<String, String> providerHeaders,
                                         String httpResponseBody,
                                         String query,
                                         String template,
                                         Function<RequestPatternBuilder, RequestPatternBuilder> verifierModifier,
                                         Consumer<AssetValueProvider> customAssert)
            throws AssetConnectionException, ConfigurationInitializationException {
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
        HttpAssetConnection connection = new HttpAssetConnection(
                CoreConfig.builder()
                        .build(),
                HttpAssetConnectionConfig.builder()
                        .headers(connectionHeaders != null ? connectionHeaders : Map.of())
                        .valueProvider(REFERENCE,
                                HttpValueProviderConfig.builder()
                                        .path(path)
                                        .headers(providerHeaders != null ? providerHeaders : Map.of())
                                        .format(JsonFormat.KEY)
                                        .query(query)
                                        .template(template)
                                        .build())
                        .baseUrl(baseUrl)
                        .keyStorePath(getKeyStorePath())
                        .keyStorePassword(KEYSTORE_PASSWORD)
                        .build(),
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


    void assertSubscriptionProviderPropertyJson(Datatype datatype,
                                                RequestMethod method,
                                                List<String> httpResponseBodies,
                                                String query,
                                                String payload,
                                                PropertyValue... expected)
            throws AssetConnectionException, ConfigurationInitializationException, InterruptedException {
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
        HttpAssetConnection connection = new HttpAssetConnection(
                CoreConfig.builder()
                        .build(),
                HttpAssetConnectionConfig.builder()
                        .subscriptionProvider(REFERENCE,
                                HttpSubscriptionProviderConfig.builder()
                                        .interval(1000)
                                        .path(path)
                                        .format(JsonFormat.KEY)
                                        .query(query)
                                        .payload(payload)
                                        .build())
                        .baseUrl(baseUrl)
                        .build(),
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
            if (!StringUtils.isBlank(payload) && expected != null) {
                verifier = verifier.withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
                        .withRequestBody(equalToJson(payload));
            }
            verify(exactly(httpResponseBodies.size()), verifier);
        }
        finally {
            connection.disconnect();
        }
    }


    void assertOperationProviderPropertyJson(
                                             RequestMethod method,
                                             String template,
                                             String expectedRequestToAsset,
                                             String assetResponse,
                                             Map<String, String> queries,
                                             Map<String, TypedValue> input,
                                             Map<String, TypedValue> inoutput,
                                             Map<String, TypedValue> expectedOutput,
                                             Map<String, TypedValue> expectedInoutput)
            throws AssetConnectionException, ConfigurationInitializationException {
        ServiceContext serviceContext = mock(ServiceContext.class);
        OperationVariable[] output = toOperationVariables(expectedOutput);
        doReturn(output)
                .when(serviceContext)
                .getOperationOutputVariables(REFERENCE);
        if (output != null) {
            Stream.of(output).forEach(x -> doReturn(TypeExtractor.extractTypeInfo(x.getValue()))
                    .when(serviceContext)
                    .getTypeInfo(AasUtils.toReference(REFERENCE, x.getValue())));
        }

        String path = String.format("/test/random/%s", "foo");
        stubFor(request(method.getName(), urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(assetResponse)));
        HttpAssetConnection connection = new HttpAssetConnection(
                CoreConfig.builder()
                        .build(),
                HttpAssetConnectionConfig.builder()
                        .operationProvider(REFERENCE,
                                HttpOperationProviderConfig.builder()
                                        .method(method.toString())
                                        .path(path)
                                        .queries(queries)
                                        .format(JsonFormat.KEY)
                                        .template(template)
                                        .build())
                        .baseUrl(baseUrl)
                        .build(),
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
                                .valueType(x.getValue().getDataType().getName())
                                .build())
                        .build())
                .toArray(OperationVariable[]::new);
    }
}
