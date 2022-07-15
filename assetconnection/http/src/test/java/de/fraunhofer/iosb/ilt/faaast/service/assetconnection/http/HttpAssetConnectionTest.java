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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.format.JsonFormat;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeExtractor;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class HttpAssetConnectionTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    private static final long DEFAULT_TIMEOUT = 10000;
    private static final Reference REFERENCE = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private URL baseUrl;

    @Before
    public void init() throws MalformedURLException {
        baseUrl = new URL("http", "localhost", wireMockRule.port(), "");
    }


    @Test
    public void testValueProviderPropertyGetValueJSON() throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException {
        assertValueProviderPropertyReadJson(
                PropertyValue.of(Datatype.INT, "5"),
                "5",
                null);
    }


    @Test
    public void testValueProviderPropertyGetValueWithQueryJSON() throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException {
        assertValueProviderPropertyReadJson(
                PropertyValue.of(Datatype.INT, "5"),
                "{\"foo\" : [1, 2, 5]}",
                "$.foo[2]");
    }


    @Test
    public void testValueProviderPropertySetValueJSON() throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException {
        assertValueProviderPropertyWriteJson(
                PropertyValue.of(Datatype.INT, "5"),
                null,
                "5");
    }


    @Test
    public void testValueProviderPropertySetValueWithTemplateJSON() throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException {
        String template = "{\"foo\" : \"${value}\", \"bar\": [1, 2, 3]}";
        String value = "5";
        assertValueProviderPropertyWriteJson(
                PropertyValue.of(Datatype.INT, "5"),
                template,
                template.replaceAll("\\$\\{value\\}", value));
    }


    private void assertValueProviderPropertyReadJson(PropertyValue expected, String httpResponseBody, String query)
            throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException {
        assertValueProviderPropertyJson(
                expected.getValue().getDataType(),
                RequestMethod.GET, httpResponseBody,
                query,
                null,
                x -> x.withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON)),
                LambdaExceptionHelper.rethrowConsumer(x -> Assert.assertEquals(expected, x.getValue())));
    }


    private void assertValueProviderPropertyWriteJson(PropertyValue newValue, String template, String expectedResponseBody)
            throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException {
        assertValueProviderPropertyJson(
                newValue.getValue().getDataType(),
                RequestMethod.PUT,
                null,
                null,
                template,
                x -> x.withRequestBody(equalToJson(expectedResponseBody)),
                LambdaExceptionHelper.rethrowConsumer(x -> x.setValue(newValue)));
    }


    private void assertValueProviderPropertyJson(Datatype datatype,
                                                 RequestMethod method,
                                                 String httpResponseBody,
                                                 String query,
                                                 String template,
                                                 Consumer<RequestPatternBuilder> verifierModifier,
                                                 Consumer<AssetValueProvider> customAssert)
            throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException {
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
                        .valueProvider(REFERENCE,
                                HttpValueProviderConfig.builder()
                                        .path(path)
                                        .format(JsonFormat.KEY)
                                        .query(query)
                                        .template(template)
                                        .build())
                        .baseUrl(baseUrl)
                        .build(),
                serviceContext);
        try {
            if (customAssert != null) {
                customAssert.accept(connection.getValueProviders().get(REFERENCE));
            }
            RequestPatternBuilder verifier = new RequestPatternBuilder(method, urlEqualTo(path));
            if (verifierModifier != null) {
                verifierModifier.accept(verifier);
            }
            verify(exactly(1), verifier);
        }
        finally {
            connection.close();
        }
    }


    @Test
    public void testSubscriptionProviderPropertyJsonGET() throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException, InterruptedException {
        assertSubscriptionProviderPropertyJson(
                Datatype.INT,
                RequestMethod.GET,
                List.of("{ \"value\": 1}",
                        "{ \"value\": 2}"),
                "$.value",
                null,
                PropertyValue.of(Datatype.INT, "1"),
                PropertyValue.of(Datatype.INT, "2"));
    }


    @Test
    public void testSubscriptionProviderPropertyJsonPOST() throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException, InterruptedException {
        assertSubscriptionProviderPropertyJson(
                Datatype.INT,
                RequestMethod.GET,
                List.of("{ \"value\": 1}",
                        "{ \"value\": 2}"),
                "$.value",
                "{ \"input\": \"foo\"}",
                PropertyValue.of(Datatype.INT, "1"),
                PropertyValue.of(Datatype.INT, "2"));
    }


    private void assertSubscriptionProviderPropertyJson(Datatype datatype,
                                                        RequestMethod method,
                                                        List<String> httpResponseBodies,
                                                        String query,
                                                        String payload,
                                                        PropertyValue... expected)
            throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException, InterruptedException {
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
            connection.close();
        }
    }


    @Test
    public void testOperationProviderPropertyJsonPOSTNoParameters() throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException {
        assertOperationProviderPropertyJson(
                RequestMethod.POST,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }


    @Test
    public void testOperationProviderPropertyJsonPOSTInputOnly() throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException {
        assertOperationProviderPropertyJson(
                RequestMethod.POST,
                "{ \"parameters\": { \"in1\": ${in1} }}",
                "{ \"parameters\": { \"in1\": \"foo\" }}",
                null,
                null,
                Map.of("in1", TypedValueFactory.create(Datatype.STRING, "foo")),
                null,
                null,
                null);
    }


    @Test
    public void testOperationProviderPropertyJsonPOSTOutputOnly() throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException {
        assertOperationProviderPropertyJson(
                RequestMethod.POST,
                null,
                null,
                "{ \"result\": 1.5 }",
                Map.of("out1", "$.result"),
                null,
                null,
                Map.of("out1", TypedValueFactory.create(Datatype.DOUBLE, "1.5")),
                null);
    }


    @Test
    public void testOperationProviderPropertyJsonPOSTInputOutputOnly() throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException {
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
                null);
    }


    @Test
    public void testOperationProviderPropertyJsonPOSTInoutputOnly() throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException {
        assertOperationProviderPropertyJson(
                RequestMethod.POST,
                "{ \"parameters\": { \"inout1\": ${inout1}}}",
                "{ \"parameters\": { \"inout1\": 42}}",
                "{ \"result\": { \"inout1\": 17}}",
                Map.of("inout1", "$.result.inout1"),
                null,
                Map.of("inout1", TypedValueFactory.create(Datatype.INTEGER, "42")),
                null,
                Map.of("inout1", TypedValueFactory.create(Datatype.INTEGER, "17")));
    }


    @Test
    public void testOperationProviderPropertyJsonPOST() throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException {
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
                Map.of("inout1", TypedValueFactory.create(Datatype.INT, "4")));
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
                                                     Map<String, TypedValue> expectedInoutput)
            throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException {
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
            connection.close();
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
