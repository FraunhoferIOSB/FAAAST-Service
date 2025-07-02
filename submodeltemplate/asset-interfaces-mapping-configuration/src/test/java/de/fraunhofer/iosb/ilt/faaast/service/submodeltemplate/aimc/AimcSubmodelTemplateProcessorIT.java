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
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.model.HttpModel;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.model.MqttModel;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
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
    public static WireMockClassRule wireMockRule = new WireMockClassRule(options().port(PortHelper.findFreePort()));

    @Rule
    public WireMockClassRule instanceRule = wireMockRule;

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    //private static final String KEY_PASSWORD = "changeit";
    //private static final String KEY_STORE_PASSWORD = "changeit";
    //private static final String KEYSTORE_TYPE = "PKCS12";
    //private static final CertificateInformation SELF_SIGNED_SERVER_CERTIFICATE_INFO = CertificateInformation.builder()
    //        .applicationUri("urn:de:fraunhofer:iosb:ilt:faaast:service:aimc:test")
    //        .commonName("FA³ST Service AIMC Test")
    //        .countryCode("DE")
    //        .localityName("Karlsruhe")
    //        .organization("Fraunhofer IOSB")
    //        .organizationUnit("ILT")
    //        .ipAddress("127.0.0.1")
    //        .dnsName("localhost")
    //        .build();

    //private static File keyStoreFile;
    private static MessageBusMqttConfig messageBusConfig;
    private static MoquetteServer server;
    private static PahoClient client;
    private static int httpServerPort;
    private static int mqttPort;
    private Service service;
    //private static URL httpUrl;
    //private static URL httpsUrl;

    //private WireMockServer wireMockServer;

    @BeforeClass
    public static void initClass() throws IOException, GeneralSecurityException, MessageBusException {
        //httpServerPort = PortHelper.findFreePort();
        httpServerPort = wireMockRule.port();
        mqttPort = PortHelper.findFreePort();
        messageBusConfig = MessageBusMqttConfig.builder()
                .port(mqttPort)
                .build();
        server = new MoquetteServer(messageBusConfig);
        server.start();
        client = new PahoClient(messageBusConfig);
        client.start();
        //wireMockRule = new WireMockClassRule(options().port(PortHelper.findFreePort()));
        //generateSelfSignedServerCertificate();
        //server = new WireMockClassRule(options()
        //        .dynamicHttpsPort()
        //        .dynamicPort()
        //        .httpDisabled(false)
        //        .keystoreType(KEYSTORE_TYPE)
        //        .keystorePath(keyStoreFile.getAbsolutePath())
        //        .keystorePassword(KEY_PASSWORD)
        //        .keyManagerPassword(KEY_STORE_PASSWORD));
        //server.start();
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
    public void initUrls() throws MalformedURLException {
        instanceRule = wireMockRule;
        //initWireMock();
    }

    //private static void initWireMock(WireMockServer wireMockServer) {
    //    //wireMockServer = new WireMockServer(WireMockConfiguration.options().port(httpServerPort));
    //    wireMockServer.start();
    //    WireMock.configureFor("localhost", httpServerPort);
    //    //httpUrl = new URL("http", "localhost", server.port(), "");
    //    //httpsUrl = new URL("https", "localhost", server.httpsPort(), "");
    //}


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
        // it takes some time to establish the AssetConnection
        Thread.sleep(1000);
        var connections = service.getAssetConnectionManager().getConnections();
        Assert.assertNotNull(connections);
        Assert.assertEquals(1, connections.size());
        Assert.assertNotNull(connections.get(0).getSubscriptionProviders());
        Assert.assertEquals(1, connections.get(0).getSubscriptionProviders().size());

        String path = HttpModel.P1_URL;
        String newval = Double.toString(74.68);
        instanceRule.stubFor(request("GET", urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(newval)));

        //CloseableHttpClient httpClient = HttpClients.createDefault();
        //HttpGet request = new HttpGet(String.format("http://localhost:%d%s", httpServerPort, path));
        //CloseableHttpResponse httpResponse = httpClient.execute(request);
        //String stringResponse = convertHttpResponseToString(httpResponse);

        //WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo((path)))
        //        .willReturn(WireMock.ok()
        //                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        //                .withBody(newval)));
        Thread.sleep(5000);
        Optional<Submodel> submodel = service.getAASEnvironment().getSubmodels().stream().filter(s -> HttpModel.SUBMODEL_OPER_DATA_ID.equals(s.getId())).findFirst();
        Assert.assertTrue(submodel.isPresent());
        Optional<SubmodelElement> coll = submodel.get().getSubmodelElements().stream().filter(e -> HttpModel.OPER_DATA_HTTP.equals(e.getIdShort())).findFirst();
        Assert.assertTrue(coll.isPresent());
        Assert.assertTrue(coll.get() instanceof SubmodelElementCollection);
        Optional<SubmodelElement> element = ((SubmodelElementCollection) coll.get()).getValue().stream().filter(e -> HttpModel.OPER_DATA_HTTP_P1.equals(e.getIdShort()))
                .findFirst();
        Assert.assertTrue(element.isPresent());
        Assert.assertTrue(element.get() instanceof Property);
        Property prop = (Property) element.get();
        Assert.assertEquals(newval, prop.getValue());
    }


    @Test
    public void testAimcMqtt() throws Exception {
        int http = PortHelper.findFreePort();
        service = new Service(serviceConfig(http, MqttModel.create(mqttPort)));
        service.start();
        // it takes some time to establish the AssetConnection
        Thread.sleep(500);
        var connections = service.getAssetConnectionManager().getConnections();
        Assert.assertNotNull(connections);
        Assert.assertEquals(1, connections.size());
        Assert.assertNotNull(connections.get(0).getSubscriptionProviders());
        Assert.assertEquals(1, connections.get(0).getSubscriptionProviders().size());

        String newval = Float.toString(12.4f);
        client.publish(MqttModel.PROP1_TOPIC, newval);
        Thread.sleep(5000);
        Optional<Submodel> submodel = service.getAASEnvironment().getSubmodels().stream().filter(s -> MqttModel.SUBMODEL_OPER_DATA_ID.equals(s.getId())).findFirst();
        Assert.assertTrue(submodel.isPresent());
        Optional<SubmodelElement> coll = submodel.get().getSubmodelElements().stream().filter(e -> MqttModel.OPER_DATA_MQTT.equals(e.getIdShort())).findFirst();
        Assert.assertTrue(coll.isPresent());
        Assert.assertTrue(coll.get() instanceof SubmodelElementCollection);
        Optional<SubmodelElement> element = ((SubmodelElementCollection) coll.get()).getValue().stream().filter(e -> MqttModel.OPER_DATA_MQTT_P1.equals(e.getIdShort()))
                .findFirst();
        Assert.assertTrue(element.isPresent());
        Assert.assertTrue(element.get() instanceof Property);
        Property prop = (Property) element.get();
        Assert.assertEquals(newval, prop.getValue());
    }

    //private static String convertHttpResponseToString(CloseableHttpResponse httpResponse) throws IOException {
    //    InputStream inputStream = httpResponse.getEntity().getContent();
    //    return convertInputStreamToString(inputStream);
    //}

    //private static String convertInputStreamToString(InputStream inputStream) {
    //    Scanner scanner = new Scanner(inputStream, "UTF-8");
    //    String string = scanner.useDelimiter("\\Z").next();
    //    scanner.close();
    //    return string;
    //}

    //private static void generateSelfSignedServerCertificate() throws IOException, GeneralSecurityException {
    //    keyStoreFile = Files.createTempFile("faaast-aimc-cert", ".p12").toFile();
    //    keyStoreFile.deleteOnExit();
    //    CertificateData certificateData = KeyStoreHelper.generateSelfSigned(SELF_SIGNED_SERVER_CERTIFICATE_INFO);
    //    KeyStoreHelper.save(certificateData, keyStoreFile, KEYSTORE_TYPE, null, KEY_PASSWORD, KEY_STORE_PASSWORD);
    //}


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
                        .interfaceConfiguration(ReferenceBuilder.forSubmodel(HttpModel.SUBMODEL_AID_ID, HttpModel.INTERFACE_HTTP),
                                new AimcSubmodelTemplateProcessorConfigData.Builder().subscriptionInterval(50).build())
                        .build()))
                .build();
    }
}
