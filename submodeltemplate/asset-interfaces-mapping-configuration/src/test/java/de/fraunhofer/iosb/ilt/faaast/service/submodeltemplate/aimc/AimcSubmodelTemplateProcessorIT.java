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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.request;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.awaitility.Awaitility.await;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.memory.FileStorageInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.mqtt.MessageBusMqttConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.mqtt.MoquetteServer;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.mqtt.PahoClient;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config.AimcSubmodelTemplateProcessorConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.model.HttpModel;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.model.MqttModel;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.model.OpcUaModel;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.server.EmbeddedOpcUaServer;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.server.EmbeddedOpcUaServerConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.server.EndpointSecurityConfiguration;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.server.Protocol;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AimcSubmodelTemplateProcessorIT {

    public static final String DEFAULT_KEY_STORE_TYPE = "PKCS12";
    public static final String SERVER_APPLICATION_CERTIFICATE_FILE = "server-application.p12";
    public static final String SERVER_APPLICATION_CERTIFICATE_PASSWORD = "";

    private static final Logger LOGGER = LoggerFactory.getLogger(AimcSubmodelTemplateProcessorIT.class);

    @ClassRule
    public static final WireMockClassRule wireMockRule = new WireMockClassRule(options().port(PortHelper.findFreePort()));

    @Rule
    public WireMockClassRule instanceRule = wireMockRule;

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);
    private static final Duration MAX_TIMEOUT = Duration.ofSeconds(60);

    private static MoquetteServer mqttServer;
    private static PahoClient mqttClient;
    private static int httpServerPort;
    private static int mqttPort;
    private Service service;
    private EmbeddedOpcUaServer opcuaServer;
    private Path securityTempDir;

    @BeforeClass
    public static void initClass() throws IOException, MessageBusException {
        httpServerPort = wireMockRule.port();
        mqttPort = PortHelper.findFreePort();
        MessageBusMqttConfig messageBusConfig = MessageBusMqttConfig.builder()
                .port(mqttPort)
                .build();
        mqttServer = new MoquetteServer(messageBusConfig);
        mqttServer.start();
        mqttClient = new PahoClient(messageBusConfig);
        mqttClient.start();
    }


    @AfterClass
    public static void stopClass() {
        if (mqttClient != null) {
            mqttClient.stop();
        }
        if (mqttServer != null) {
            mqttServer.stop();
        }
    }


    @Before
    public void init() {
        instanceRule = wireMockRule;
    }


    @After
    public void shutdown() {
        if (service != null) {
            service.stop();
        }
        if (opcuaServer != null) {
            try {
                opcuaServer.shutdown();
            }
            catch (InterruptedException | ExecutionException ex) {
                LOGGER.info("shutdown: error OPC UA Server shutdown", ex);
            }
        }
        if (securityTempDir != null) {
            try {
                FileUtils.forceDelete(securityTempDir.toFile());
            }
            catch (IOException ex) {
                LOGGER.info("shutdown: can't delete securityTempDir", ex);
            }
        }
    }


    @Test
    public void testAimcHttp() throws Exception {
        int httpPort = PortHelper.findFreePort();
        service = new Service(serviceConfig(httpPort, HttpModel.create(httpServerPort)));
        service.start();
        // wait for asset connections to be established
        await().atMost(MAX_TIMEOUT)
                .with()
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> service.getAssetConnectionManager().isFullyConnected());

        String path = HttpModel.P1_URL;
        String newval = Double.toString(74.68);
        instanceRule.stubFor(request("GET", urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(newval)));

        String path2 = HttpModel.P2_URL;
        String newval2 = Integer.toString(156);
        instanceRule.stubFor(request("GET", urlEqualTo(path2))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(newval2)));

        await()
                .alias("check Property value")
                .pollInterval(POLL_TIMEOUT)
                .atMost(MAX_TIMEOUT)
                .until(() -> {
                    Reference prop1Ref = ReferenceBuilder.forSubmodel(HttpModel.SUBMODEL_OPER_DATA_ID, HttpModel.OPER_DATA_HTTP, HttpModel.OPER_DATA_HTTP_P1);
                    String prop1val = readPropertyValue(HttpModel.SUBMODEL_OPER_DATA_ID, prop1Ref);
                    return newval.equals(prop1val);
                });

        Reference prop2Ref = ReferenceBuilder.forSubmodel(HttpModel.SUBMODEL_OPER_DATA_ID, HttpModel.OPER_DATA_HTTP, HttpModel.OPER_DATA_HTTP_P2);
        String prop2val = readPropertyValue(HttpModel.SUBMODEL_OPER_DATA_ID, prop2Ref);
        Assert.assertEquals(newval2, prop2val);
    }


    @Test
    public void testAimcMqtt() throws Exception {
        int httpPort = PortHelper.findFreePort();
        service = new Service(serviceConfig(httpPort, MqttModel.create(mqttPort)));
        service.start();
        // wait for asset connections to be established
        await().atMost(MAX_TIMEOUT)
                .with()
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> service.getAssetConnectionManager().isFullyConnected());

        String newval = Float.toString(12.4f);
        mqttClient.publish(MqttModel.PROP1_TOPIC, newval);
        await()
                .alias("check property value")
                .pollInterval(POLL_TIMEOUT)
                .atMost(MAX_TIMEOUT)
                .until(() -> {
                    Reference prop1Ref = ReferenceBuilder.forSubmodel(MqttModel.SUBMODEL_OPER_DATA_ID, MqttModel.OPER_DATA_MQTT, MqttModel.OPER_DATA_MQTT_P1);
                    String prop1val = readPropertyValue(MqttModel.SUBMODEL_OPER_DATA_ID, prop1Ref);
                    return newval.equals(prop1val);
                });
    }


    @Test
    public void testAimcOpcUa() throws Exception {
        opcuaServer = startDefaultServer();
        int opcuaPort = opcuaServer.getConfig().getProtocolPorts().get(Protocol.TCP);
        LOGGER.info("testAimcOpcUa: Port {}", opcuaPort);

        securityTempDir = Files.createTempDirectory("aimc-smt-processor");
        int httpPort = PortHelper.findFreePort();
        service = new Service(
                serviceConfig(httpPort,
                        OpcUaModel.create(opcuaServer.getEndpoint(Protocol.TCP)),
                        opcuaServer.getEndpoint(Protocol.TCP),
                        securityTempDir));
        service.start();

        // wait for asset connections to be established
        await().atMost(MAX_TIMEOUT)
                .with()
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> service.getAssetConnectionManager().isFullyConnected());

        double d1 = 1.1;
        String newvalDouble = String.valueOf(d1);
        var rv = opcuaServer.writeExampleValue(NodeId.parse(OpcUaModel.P1_NODE_ID), d1);
        Assert.assertEquals(StatusCodes.Good, rv.getValue());

        int i1 = 146;
        String newvalInt = String.valueOf(i1);
        rv = opcuaServer.writeExampleValue(NodeId.parse(OpcUaModel.P2_NODE_ID), i1);
        Assert.assertEquals(StatusCodes.Good, rv.getValue());

        await()
                .alias("check Property value")
                .pollInterval(POLL_TIMEOUT)
                .atMost(MAX_TIMEOUT)
                .until(() -> {
                    Reference prop1Ref = ReferenceBuilder.forSubmodel(OpcUaModel.SUBMODEL_OPER_DATA_ID, OpcUaModel.OPER_DATA_OPC_UA, OpcUaModel.OPER_DATA_OPC_UA_P1);
                    String prop1val = readPropertyValue(OpcUaModel.SUBMODEL_OPER_DATA_ID, prop1Ref);
                    return newvalDouble.equals(prop1val);
                });

        Reference prop2Ref = ReferenceBuilder.forSubmodel(OpcUaModel.SUBMODEL_OPER_DATA_ID, OpcUaModel.OPER_DATA_OPC_UA, OpcUaModel.OPER_DATA_OPC_UA_P2);
        String prop2val = readPropertyValue(OpcUaModel.SUBMODEL_OPER_DATA_ID, prop2Ref);
        Assert.assertEquals(newvalInt, prop2val);
    }


    private static ServiceConfig serviceConfig(int portHttp, Environment initialModel) {
        return serviceConfig(portHttp, initialModel, null, null);
    }


    private static ServiceConfig serviceConfig(int portHttp, Environment initialModel, String server, Path securityBasDir) {
        AimcSubmodelTemplateProcessorConfig.Builder aimcConfig = new AimcSubmodelTemplateProcessorConfig.Builder();
        if (server != null) {
            aimcConfig.opcuaSecurityBaseDir(Map.of(server, securityBasDir));
        }
        return new ServiceConfig.Builder()
                .core(new CoreConfig.Builder().requestHandlerThreadPoolSize(2).build())
                .persistence(PersistenceInMemoryConfig.builder()
                        .initialModel(initialModel)
                        .build())
                .fileStorage(new FileStorageInMemoryConfig())
                .endpoint(HttpEndpointConfig.builder()
                        .port(portHttp)
                        .ssl(false)
                        .build())
                .messageBus(new MessageBusInternalConfig())
                .submodelTemplateProcessors(List.of(aimcConfig.build()))
                .build();
    }


    private String readPropertyValue(String submodelId, Reference refElement) {
        String retval = null;
        GetSubmodelElementByPathRequest request = new GetSubmodelElementByPathRequest.Builder().submodelId(submodelId).path(ReferenceHelper.toPath(refElement)).build();
        Response response = service.execute(request);
        if ((response.getStatusCode() == StatusCode.SUCCESS) && (GetSubmodelElementByPathResponse.class.isAssignableFrom(response.getClass()))) {
            SubmodelElement element = ((GetSubmodelElementByPathResponse) response).getPayload();
            Assert.assertTrue(element instanceof Property);
            retval = ((Property) element).getValue();
        }

        return retval;
    }


    private static EmbeddedOpcUaServer startServer(EmbeddedOpcUaServerConfig config) throws Exception {
        EmbeddedOpcUaServer result = new EmbeddedOpcUaServer(config);
        result.startup();
        return result;
    }


    private static EmbeddedOpcUaServer startDefaultServer() throws Exception {
        return startServer(EmbeddedOpcUaServerConfig.builder()
                .endpointSecurityConfiguration(EndpointSecurityConfiguration.NO_SECURITY_ANONYMOUS)
                .build());
    }
}
