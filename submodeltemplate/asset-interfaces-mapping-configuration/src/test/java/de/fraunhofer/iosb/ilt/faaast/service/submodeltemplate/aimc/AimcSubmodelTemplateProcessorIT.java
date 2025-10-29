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
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;


public class AimcSubmodelTemplateProcessorIT {

    @ClassRule
    public static final WireMockClassRule wireMockRule = new WireMockClassRule(options().port(PortHelper.findFreePort()));

    @Rule
    public WireMockClassRule instanceRule = wireMockRule;

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);
    private static final Duration MAX_TIMEOUT = Duration.ofSeconds(60);

    private static MoquetteServer server;
    private static PahoClient client;
    private static int httpServerPort;
    private static int mqttPort;
    private Service service;

    @BeforeClass
    public static void initClass() throws IOException, MessageBusException {
        httpServerPort = wireMockRule.port();
        mqttPort = PortHelper.findFreePort();
        MessageBusMqttConfig messageBusConfig = MessageBusMqttConfig.builder()
                .port(mqttPort)
                .build();
        server = new MoquetteServer(messageBusConfig);
        server.start();
        client = new PahoClient(messageBusConfig);
        client.start();
    }


    @AfterClass
    public static void stopClass() {
        if (client != null) {
            client.stop();
        }
        if (server != null) {
            server.stop();
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
    }


    @Test
    public void testAimcHttp() throws Exception {
        int http = PortHelper.findFreePort();
        service = new Service(serviceConfig(http, HttpModel.create(httpServerPort)));
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
        int http = PortHelper.findFreePort();
        service = new Service(serviceConfig(http, MqttModel.create(mqttPort)));
        service.start();
        // wait for asset connections to be established
        await().atMost(MAX_TIMEOUT)
                .with()
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> service.getAssetConnectionManager().isFullyConnected());

        String newval = Float.toString(12.4f);
        client.publish(MqttModel.PROP1_TOPIC, newval);
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


    private static ServiceConfig serviceConfig(int portHttp, Environment initialModel) {
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
                .submodelTemplateProcessors(List.of(new AimcSubmodelTemplateProcessorConfig.Builder()
                        //.interfaceConfiguration(ReferenceBuilder.forSubmodel(HttpModel.SUBMODEL_AID_ID, HttpModel.INTERFACE_HTTP),
                        //        new InterfaceConfiguration.Builder().subscriptionInterval(50).build())
                        .build()))
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
}
