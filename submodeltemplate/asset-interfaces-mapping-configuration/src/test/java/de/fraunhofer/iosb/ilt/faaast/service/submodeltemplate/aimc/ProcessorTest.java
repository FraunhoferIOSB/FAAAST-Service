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

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.HttpAssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.HttpAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.MqttAssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.MqttAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.config.MqttSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.memory.FileStorageInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config.AimcSubmodelTemplateProcessorConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config.Credentials;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.util.Util;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class ProcessorTest {

    private static final String SERVER_URL = "http://plugfest.thingweb.io:8083";

    private ServiceConfig config;

    @Before
    public void init() {

        //Reference submodelRef = ReferenceBuilder.forSubmodel("https://example.com/ids/sm/AssetInterfacesDescription", "InterfaceHTTP");
        Map<String, List<Credentials>> credentials = new HashMap<>();
        config = new ServiceConfig.Builder()
                .core(new CoreConfig.Builder().requestHandlerThreadPoolSize(2).build())
                .persistence(new PersistenceInMemoryConfig())
                .fileStorage(new FileStorageInMemoryConfig())
                .messageBus(new MessageBusInternalConfig())
                .submodelTemplateProcessors(List.of(new AimcSubmodelTemplateProcessorConfig.Builder()
                        .connectionLevelCredentials(credentials)
                        //.interfaceConfiguration(submodelRef, new InterfaceConfiguration.Builder().username("user1").password("pw1").build())
                        .build()))
                .build();
    }


    @Test
    @Ignore
    public void testAimc() throws ConfigurationException, AssetConnectionException, PersistenceException, MessageBusException, MalformedURLException {
        File initialModelFile = new File("src/test/resources/Test-Example.json");
        config.getPersistence().setInitialModelFile(initialModelFile);
        Service service = new Service(config);
        Assert.assertNotNull(service);
        AssetConnectionManager manager = service.getAssetConnectionManager();
        Assert.assertNotNull(manager);
        var assetConns = manager.getConnections();
        Assert.assertEquals(2, assetConns.size());
        Assert.assertTrue(assetConns.get(0) instanceof HttpAssetConnection);
        Assert.assertTrue(assetConns.get(1) instanceof MqttAssetConnection);

        HttpAssetConnectionConfig httpExpected = new HttpAssetConnectionConfig.Builder()
                .baseUrl(SERVER_URL)
                .subscriptionProvider(new DefaultReference.Builder()
                        .type(ReferenceTypes.MODEL_REFERENCE)
                        .keys(new DefaultKey.Builder().type(KeyTypes.SUBMODEL).value("https://example.com/ids/sm/OperationalData").build())
                        .keys(new DefaultKey.Builder().type(KeyTypes.SUBMODEL_ELEMENT_COLLECTION).value("HTTP_Data").build())
                        .keys(new DefaultKey.Builder().type(KeyTypes.PROPERTY).value("voltage").build())
                        .build(),
                        HttpSubscriptionProviderConfig.builder()
                                .format("JSON")
                                .path("/sampleDevice/properties/voltage")
                                .interval(1000)
                                .build())
                .valueProvider(new DefaultReference.Builder()
                        .type(ReferenceTypes.MODEL_REFERENCE)
                        .keys(new DefaultKey.Builder().type(KeyTypes.SUBMODEL).value("https://example.com/ids/sm/OperationalData").build())
                        .keys(new DefaultKey.Builder().type(KeyTypes.SUBMODEL_ELEMENT_COLLECTION).value("HTTP_Data").build())
                        .keys(new DefaultKey.Builder().type(KeyTypes.PROPERTY).value("status").build())
                        .build(),
                        HttpValueProviderConfig.builder()
                                .format("JSON")
                                .path("/sampleDevice/properties/status")
                                .build())
                .build();

        HttpAssetConnection httpAssetConn = (HttpAssetConnection) assetConns.get(0);
        HttpAssetConnectionConfig httpConfig = httpAssetConn.asConfig();
        Assert.assertEquals(new URL(SERVER_URL), httpConfig.getBaseUrl());
        Assert.assertEquals(1, httpConfig.getSubscriptionProviders().size());
        Assert.assertEquals(1, httpConfig.getValueProviders().size());
        Assert.assertEquals(httpExpected, httpConfig);

        MqttAssetConnectionConfig mqttExpected = new MqttAssetConnectionConfig.Builder()
                .serverUri("mqtt://iot.platform.com:8088")
                .subscriptionProvider(new DefaultReference.Builder()
                        .type(ReferenceTypes.MODEL_REFERENCE)
                        .keys(new DefaultKey.Builder().type(KeyTypes.SUBMODEL).value("https://example.com/ids/sm/OperationalData").build())
                        .keys(new DefaultKey.Builder().type(KeyTypes.SUBMODEL_ELEMENT_COLLECTION).value("MQTT_Data").build())
                        .keys(new DefaultKey.Builder().type(KeyTypes.PROPERTY).value("voltage").build())
                        .build(),
                        MqttSubscriptionProviderConfig.builder()
                                .format("JSON")
                                .topic("/sampleDevice/properties/voltage")
                                .build())
                .subscriptionProvider(new DefaultReference.Builder()
                        .type(ReferenceTypes.MODEL_REFERENCE)
                        .keys(new DefaultKey.Builder().type(KeyTypes.SUBMODEL).value("https://example.com/ids/sm/OperationalData").build())
                        .keys(new DefaultKey.Builder().type(KeyTypes.SUBMODEL_ELEMENT_COLLECTION).value("MQTT_Data").build())
                        .keys(new DefaultKey.Builder().type(KeyTypes.PROPERTY).value("status").build())
                        .build(),
                        MqttSubscriptionProviderConfig.builder()
                                .format("JSON")
                                .topic("/sampleDevice/properties/status")
                                .build())
                .build();

        MqttAssetConnection mqttAssetConn = (MqttAssetConnection) assetConns.get(1);
        MqttAssetConnectionConfig mqttConfig = mqttAssetConn.asConfig();
        Assert.assertEquals(2, mqttConfig.getSubscriptionProviders().size());

        // ignore clientId
        mqttExpected.setClientId(mqttConfig.getClientId());
        Assert.assertEquals(mqttExpected, mqttConfig);
    }


    @Test
    public void testGlobalContentType() {
        String expectedContentType = "application/json";
        SubmodelElementCollection smec = new DefaultSubmodelElementCollection.Builder()
                .idShort("EndpointMetadata")
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value("https://admin-shell.io/idta/AssetInterfacesDescription/1/0/EndpointMetadata")
                                .build())
                        .build())
                .value(new DefaultProperty.Builder()
                        .idShort("contentType")
                        .semanticId(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value("https://www.w3.org/2019/wot/hypermedia#forContentType")
                                        .build())
                                .build())
                        .valueType(DataTypeDefXsd.STRING)
                        .value(expectedContentType)
                        .build())
                .build();
        String actualContentType = Util.getContentType(smec);
        Assert.assertEquals(expectedContentType, actualContentType);
    }


    @Test
    public void testPropertyContentType() {
        String expectedContentType = "application/xml";
        SubmodelElementCollection smec = new DefaultSubmodelElementCollection.Builder()
                .idShort("forms")
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value("https://www.w3.org/2019/wot/td#hasForm")
                                .build())
                        .build())
                .value(new DefaultProperty.Builder()
                        .semanticId(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value("https://www.w3.org/2019/wot/hypermedia#forContentType")
                                        .build())
                                .build())
                        .valueType(DataTypeDefXsd.STRING)
                        .value(expectedContentType)
                        .build())
                .build();
        String actualContentType = Util.getContentType("application/json", smec);
        Assert.assertEquals(expectedContentType, actualContentType);
    }


    @Test
    public void testForms() throws PersistenceException, ResourceNotFoundException {
        SubmodelElementCollection expectedForms = new DefaultSubmodelElementCollection.Builder()
                .idShort("forms")
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value("https://www.w3.org/2019/wot/td#hasForm")
                                .build())
                        .build())
                .build();
        SubmodelElementCollection smec = new DefaultSubmodelElementCollection.Builder()
                .idShort("voltage")
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value("https://admin-shell.io/idta/AssetInterfacesDescription/1/0/PropertyDefinition")
                                .build())
                        .build())
                .value(expectedForms)
                .build();
        SubmodelElementCollection actualForms = Util.getPropertyForms(smec, null, null);
        Assert.assertEquals(expectedForms, actualForms);
    }


    @Test
    public void testFormsHref() {
        String expectedHref = "/path/value";
        SubmodelElementCollection smec = new DefaultSubmodelElementCollection.Builder()
                .idShort("forms")
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value("https://www.w3.org/2019/wot/td#hasForm")
                                .build())
                        .build())
                .value(new DefaultProperty.Builder()
                        .idShort("href")
                        .semanticId(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value("https://www.w3.org/2019/wot/hypermedia#hasTarget")
                                        .build())
                                .build())
                        .valueType(DataTypeDefXsd.STRING)
                        .value(expectedHref)
                        .build())
                .build();
        String actualHref = Util.getFormsHref(smec);
        Assert.assertEquals(expectedHref, actualHref);
    }


    @Test
    public void testBaseUrl() {
        String expectedBase = "http://localhost:9000";
        SubmodelElementCollection smec = new DefaultSubmodelElementCollection.Builder()
                .idShort("EndpointMetadata")
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value("https://admin-shell.io/idta/AssetInterfacesDescription/1/0/EndpointMetadata")
                                .build())
                        .build())
                .value(new DefaultProperty.Builder()
                        .idShort("base")
                        .semanticId(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value("https://www.w3.org/2019/wot/td#baseURI")
                                        .build())
                                .build())
                        .valueType(DataTypeDefXsd.STRING)
                        .value(expectedBase)
                        .build())
                .build();
        String actualBase = Util.getBaseUrl(smec);
        Assert.assertEquals(expectedBase, actualBase);
    }


    @Test
    public void testInterfaceTitle() {
        String expectedTitle = "DeviceSample";
        SubmodelElementCollection smec = new DefaultSubmodelElementCollection.Builder()
                .idShort("InterfaceHTTP")
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value("https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Interface")
                                .build())
                        .build())
                .value(new DefaultProperty.Builder()
                        .idShort("title")
                        .semanticId(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value("https://www.w3.org/2019/wot/td#title")
                                        .build())
                                .build())
                        .valueType(DataTypeDefXsd.STRING)
                        .value(expectedTitle)
                        .build())
                .build();
        String actualTitle = Util.getInterfaceTitle(smec);
        Assert.assertEquals(expectedTitle, actualTitle);
    }


    @Test
    public void testEndpointMetadata() {
        SubmodelElementCollection expectedMetadata = new DefaultSubmodelElementCollection.Builder()
                .idShort("EndpointMetadata")
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value("https://admin-shell.io/idta/AssetInterfacesDescription/1/0/EndpointMetadata")
                                .build())
                        .build())
                .build();
        SubmodelElementCollection smec = new DefaultSubmodelElementCollection.Builder()
                .idShort("InterfaceHTTP")
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value("https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Interface")
                                .build())
                        .build())
                .value(expectedMetadata)
                .build();
        SubmodelElementCollection actualMetadata = Util.getEndpointMetadata(smec);
        Assert.assertEquals(expectedMetadata, actualMetadata);
    }


    /**
     * Tests the key value of a Property.
     * If a Property has a key element, the value of this is used.
     * Otherwise the IdShort of the Property itself is used.
     */
    @Test
    public void testPropertyKey() {
        String idShort = "foo";
        SubmodelElementCollection smec = new DefaultSubmodelElementCollection.Builder()
                .idShort(idShort)
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value("https://www.w3.org/2019/wot/json-schema#propertyName")
                                .build())
                        .build())
                .build();
        String actualKey = Util.getKey(smec);
        Assert.assertEquals(idShort, actualKey);

        String keyValue = "bar";
        smec.setValue(List.of(new DefaultProperty.Builder()
                .idShort("key")
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value("https://admin-shell.io/idta/AssetInterfacesDescription/1/0/key")
                                .build())
                        .build())
                .valueType(DataTypeDefXsd.STRING)
                .value(keyValue)
                .build()));
        actualKey = Util.getKey(smec);
        Assert.assertEquals(keyValue, actualKey);
    }
}
