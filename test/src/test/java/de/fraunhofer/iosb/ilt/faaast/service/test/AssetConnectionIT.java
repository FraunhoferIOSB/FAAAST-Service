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
package de.fraunhofer.iosb.ilt.faaast.service.test;

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper.toHttpStatusCode;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.OpcUaAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.OpcUaEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.ApiPaths;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.PortHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.ModelingKind;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShell;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;


public class AssetConnectionIT {

    private static final String HOST = "http://localhost";
    private static AssetAdministrationShellEnvironment environment;
    private static Service service;

    private static final int SOURCE_VALUE = 42;
    private static final int TARGET_VALUE = 0;
    private static final String NODE_ID_SOURCE = "ns=3;s=1.Value";
    private static Submodel submodel;
    private static Property source;
    private static Property target;

    @BeforeClass
    public static void initClass() throws IOException {
        source = new DefaultProperty.Builder()
                .idShort("source")
                .value(Integer.toString(SOURCE_VALUE))
                .valueType("integer")
                .build();
        target = new DefaultProperty.Builder()
                .idShort("target")
                .value(Integer.toString(TARGET_VALUE))
                .valueType("integer")
                .build();
        submodel = new DefaultSubmodel.Builder()
                .idShort("Submodel1")
                .identification(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.IRI)
                        .identifier("http://example.org/submodel/1")
                        .build())
                .kind(ModelingKind.INSTANCE)
                .submodelElement(source)
                .submodelElement(target)
                .build();
        environment = new DefaultAssetAdministrationShellEnvironment.Builder()
                .assetAdministrationShells(new DefaultAssetAdministrationShell.Builder()
                        .idShort("AAS1")
                        .identification(new DefaultIdentifier.Builder()
                                .idType(IdentifierType.IRI)
                                .identifier("https://example.org/aas/1")
                                .build())
                        .submodel(ReferenceHelper.toReference(submodel.getIdentification(), Submodel.class))
                        .build())
                .submodels(submodel)
                .build();
    }


    private static ServiceConfig serviceConfig(int portHttp, int portOpcUa) {
        return ServiceConfig.builder()
                .core(CoreConfig.DEFAULT)
                .persistence(PersistenceInMemoryConfig.builder()
                        .initialModel(DeepCopyHelper.deepCopy(environment))
                        .build())
                .endpoint(OpcUaEndpointConfig.builder()
                        .tcpPort(portOpcUa)
                        .allowAnonymous(true)
                        .build())
                .endpoint(HttpEndpointConfig.builder()
                        .port(portHttp)
                        .build())
                .messageBus(MessageBusInternalConfig.builder()
                        .build())
                .build();
    }


    private static ServiceConfig withAssetConnection(ServiceConfig config, String nodeIdSource, int port) {
        config.getAssetConnections().add(OpcUaAssetConnectionConfig.builder()
                .host("opc.tcp://" + "localhost:" + port)
                .valueProvider(AasUtils.toReference(AasUtils.toReference(submodel), target),
                        OpcUaValueProviderConfig.builder()
                                .nodeId(nodeIdSource)
                                .build())
                .build());
        return config;
    }


    @Test
    public void testServiceStartInvalidAssetConnection() throws Exception {
        int http = PortHelper.findFreePort();
        int opcua = PortHelper.findFreePort();
        service = new Service(
                withAssetConnection(
                        serviceConfig(http, opcua),
                        "invalid",
                        opcua));
        service.start();
        assertAvailability(http);
    }


    @Test
    public void testServiceStartValidAssetConnection() throws Exception {
        int http = PortHelper.findFreePort();
        int opcua = PortHelper.findFreePort();
        service = new Service(
                withAssetConnection(
                        serviceConfig(http, opcua),
                        NODE_ID_SOURCE,
                        opcua));
        service.start();
        awaitAssetConnected(service);
        assertAvailability(http);
        assertTargetValue(http, SOURCE_VALUE);
    }


    @Test
    public void testServiceStartValidAssetConnectionOffset() throws Exception {
        int http = PortHelper.findFreePort();
        int opcua = PortHelper.findFreePort();
        int http2 = PortHelper.findFreePort();
        int opcua2 = PortHelper.findFreePort();
        ServiceConfig config = withAssetConnection(serviceConfig(http, opcua),
                NODE_ID_SOURCE,
                opcua2);
        service = new Service(config);
        service.start();
        assertAvailability(http);
        assertTargetValue(http, TARGET_VALUE);
        Service service2 = new Service(serviceConfig(http2, opcua2));
        service2.start();
        awaitAssetConnected(service);
        assertTargetValue(http, SOURCE_VALUE);
        service2.stop();
    }


    @After
    public void shutdown() {
        service.stop();
    }


    public void assertAvailability(int port) throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        Object expected = environment.getAssetAdministrationShells();
        assertExecuteMultiple(
                HttpMethod.GET,
                new ApiPaths(HOST, port).aasRepository().assetAdministrationShells(),
                StatusCode.SUCCESS,
                null,
                expected,
                AssetAdministrationShell.class);
    }


    private void awaitAssetConnected(Service service) {
        await().atMost(30, TimeUnit.SECONDS)
                .with()
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> service.getAssetConnectionManager().getConnections().get(0).isConnected());
    }


    private void assertExecuteMultiple(HttpMethod method, String url, StatusCode statusCode, Object input, Object expected, Class<?> type)
            throws IOException, InterruptedException, URISyntaxException, SerializationException, DeserializationException {
        HttpResponse response = HttpHelper.execute(method, url, input);
        assertEquals(toHttpStatusCode(statusCode), response.statusCode());
        if (expected != null) {
            Object actual = HttpHelper.readResponseList(response, type);
            assertEquals(expected, actual);
        }
    }


    public void assertTargetValue(int port, int expectedValue)
            throws IOException, InterruptedException, URISyntaxException, JSONException {
        HttpResponse<String> response = HttpHelper.get(
                new ApiPaths(HOST, port)
                        .submodelRepository()
                        .submodelInterface(submodel)
                        .submodelElement(target, Content.VALUE));
        assertEquals(toHttpStatusCode(StatusCode.SUCCESS), response.statusCode());
        String expected = String.format("{\"target\": %d}", expectedValue);
        JSONAssert.assertEquals(expected, response.body(), false);
    }

}
