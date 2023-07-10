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

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.junit.Before;


public class HttpAssetConnectionTest extends AssetConnectionBaseTest {

    @Before
    public void init() throws MalformedURLException {
        baseUrl = new URL("http", "localhost", wireMockRule.port(), "");
    }


    @Override
    protected WireMockRule createWireMockRule() {
        return new WireMockRule(options().dynamicPort());
    }


    @Override
    protected String getKeyStorePath() {
        return null;
    }


    @Override
    public void testValueProviderWithHeaders() throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException, InterruptedException {
        assertValueProviderHeaders(Map.of(), Map.of(), Map.of());
        assertValueProviderHeaders(Map.of("foo", "bar"), Map.of(), Map.of("foo", "bar"));
        assertValueProviderHeaders(Map.of("foo", "bar"), Map.of("foo", "bar"), Map.of("foo", "bar"));
        assertValueProviderHeaders(Map.of("foo", "bar"), Map.of("foo", "bar2"), Map.of("foo", "bar2"));
        assertValueProviderHeaders(Map.of("foo", "bar"), Map.of("bar", "foo"), Map.of("foo", "bar", "bar", "foo"));
    }


    @Override
    public void testValueProviderPropertyGetValueJSON() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException {
        assertValueProviderPropertyReadJson(
                PropertyValue.of(Datatype.INT, "5"),
                "5",
                null);
    }


    @Override
    public void testValueProviderPropertyGetValueWithQueryJSON() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException {
        assertValueProviderPropertyReadJson(
                PropertyValue.of(Datatype.INT, "5"),
                "{\"foo\" : [1, 2, 5]}",
                "$.foo[2]");
    }


    @Override
    public void testValueProviderPropertySetValueJSON() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException {
        assertValueProviderPropertyWriteJson(
                PropertyValue.of(Datatype.INT, "5"),
                null,
                "5");
    }


    @Override
    public void testValueProviderPropertySetValueWithTemplateJSON() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException {
        String template = "{\"foo\" : \"${value}\", \"bar\": [1, 2, 3]}";
        String value = "5";
        assertValueProviderPropertyWriteJson(
                PropertyValue.of(Datatype.INT, "5"),
                template,
                template.replaceAll("\\$\\{value\\}", value));
    }


    @Override
    public void testSubscriptionProviderPropertyJsonGET() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException {
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


    @Override
    public void testSubscriptionProviderPropertyJsonGET2() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException {
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
                null,
                PropertyValue.of(Datatype.INT, "42"));
    }


    @Override
    public void testSubscriptionProviderPropertyJsonPOST() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException {
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


    @Override
    public void testOperationProviderPropertyJsonPOSTNoParameters() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException {
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


    @Override
    public void testOperationProviderPropertyJsonPOSTInputOnly() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException {
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


    @Override
    public void testOperationProviderPropertyJsonPOSTOutputOnly() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException {
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


    @Override
    public void testOperationProviderPropertyJsonPOSTInputOutputOnly() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException {
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


    @Override
    public void testOperationProviderPropertyJsonPOSTInoutputOnly() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException {
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


    @Override
    public void testOperationProviderPropertyJsonPOST() throws AssetConnectionException, ValueFormatException,
            ConfigurationInitializationException, InterruptedException {
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
}
