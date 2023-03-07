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
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.ApiPaths;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.SocketHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.List;
import org.json.JSONException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;


public class AssetConnectionIT {

    private static final String HOST = "http://localhost";
    private static int PORT1;
    private static int PORT2;
    private static int PORT3;
    private static int PORT4;
    private static ApiPaths API_PATHS;
    private static AssetAdministrationShellEnvironment environment;
    private static Service service;

    private static Submodel submodel;
    private static SubmodelElement source;
    private static SubmodelElement target;

    @BeforeClass
    public static void initClass() throws IOException {
        PORT1 = SocketHelper.findFreePort();
        while (PORT2 == 0 || PORT2 == PORT1) {
            PORT2 = SocketHelper.findFreePort();
        }
        while (PORT3 == 0 || PORT3 == PORT2 || PORT3 == PORT1) {
            PORT3 = SocketHelper.findFreePort();
        }
        while (PORT4 == 0 || PORT4 == PORT1 || PORT4 == PORT2 || PORT4 == PORT3) {
            PORT4 = SocketHelper.findFreePort();
        }
        API_PATHS = new ApiPaths(HOST, PORT1);
        environment = AASFull.createEnvironment();
        submodel = environment.getSubmodels().get(0);
        source = submodel.getSubmodelElements().get(1);
        target = submodel.getSubmodelElements().get(0);
    }


    private static ServiceConfig serviceConfig(String nodeId, int opcuaEndpointPort, int opcuaAssetConnectionPort, int httpEndpointPort) {
        return ServiceConfig.builder()
                .core(CoreConfig.builder()
                        .requestHandlerThreadPoolSize(2)
                        .build())
                .persistence(PersistenceInMemoryConfig.builder()
                        .initialModel(DeepCopyHelper.deepCopy(environment))
                        .build())
                .endpoints(List.of(OpcUaEndpointConfig.builder()
                        .tcpPort(opcuaEndpointPort)
                        .allowAnonymous(true)
                        .build(),
                        HttpEndpointConfig.builder()
                                .port(httpEndpointPort)
                                .build()))
                .messageBus(MessageBusInternalConfig.builder()
                        .build())
                .assetConnection(OpcUaAssetConnectionConfig.builder()
                        .host("opc.tcp://" + "localhost:" + opcuaAssetConnectionPort)
                        .initializationInverval(1000)
                        .valueProvider(AasUtils.toReference(AasUtils.toReference(submodel), target),
                                OpcUaValueProviderConfig.builder()
                                        .nodeId(nodeId)
                                        .build())
                        .build())
                .build();
    }


    @Test
    public void test_serviceStart_InvalidAssetConnection() throws Exception {
        service = new Service(serviceConfig("invalid", PORT2, PORT2, PORT1));
        service.start();
        Thread.sleep(5000);
        assertAvailability();
    }


    @Test
    public void test_serviceStart_ValidAssetConnection() throws Exception {
        service = new Service(serviceConfig("ns=3;s=3.Value", PORT2, PORT2, PORT1));
        service.start();
        Thread.sleep(5000);
        assertAvailability();
        assertValue(submodel, target, "{\"ManufacturerName\": \"" + ((DefaultProperty) source).getValue() + "\"}");
    }


    @Test
    public void test_serviceStart_ValidAssetConnectionOffset() throws Exception {
        service = new Service(serviceConfig("ns=3;s=3.Value", PORT2, PORT3, PORT1));
        service.start();
        Thread.sleep(5000);
        assertAvailability();
        assertValue(submodel, target, "{\"ManufacturerName\": \"" + ((DefaultProperty) target).getValue() + "\"}");
        Service targetService = new Service(serviceConfig("noMatter", PORT3, 0, PORT4));
        targetService.start();
        Thread.sleep(5000);
        assertValue(submodel, target, "{\"ManufacturerName\": \"" + ((DefaultProperty) source).getValue() + "\"}");
    }


    @After
    public void shutdown() {
        service.stop();
    }


    public void assertAvailability() throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        Object expected = environment.getAssetAdministrationShells();
        assertExecuteMultiple(
                HttpMethod.GET,
                API_PATHS.aasRepository().assetAdministrationShells(),
                StatusCode.SUCCESS,
                null,
                expected,
                AssetAdministrationShell.class);
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


    public void assertValue(Submodel parent, SubmodelElement submodelElement, String expectedValue)
            throws IOException, InterruptedException, URISyntaxException, JSONException {
        HttpResponse<String> response = HttpHelper.get(API_PATHS.submodelRepository().submodelInterface(parent).submodelElement(submodelElement) + "?content=value");
        assertEquals(toHttpStatusCode(StatusCode.SUCCESS), response.statusCode());
        JSONAssert.assertEquals(expectedValue, response.body(), false);
    }

}
