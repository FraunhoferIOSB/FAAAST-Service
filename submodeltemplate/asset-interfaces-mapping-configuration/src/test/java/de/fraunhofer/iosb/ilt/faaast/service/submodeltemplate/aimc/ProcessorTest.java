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

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.HttpAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.MqttAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.config.MqttSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.EnvironmentSerializationManager;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.StaticRequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config.AimcSubmodelTemplateProcessorConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.util.Util;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReflectionHelper;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.junit.Assert;
import org.junit.Test;


public class ProcessorTest {

    private static final String SERVER_URL = "http://plugfest.thingweb.io:8083";
    private static final String SUBMODEL_ID_AIMC = "https://example.com/ids/sm/AssetInterfacesMappingConfiguration";

    private Service service;
    private Persistence persistence;
    private AimcSubmodelTemplateProcessor smtProcessor;
    private static AssetConnectionManager assetConnectionManager;

    private void initMocks(Environment model) throws Exception {
        persistence = PersistenceInMemoryConfig.builder()
                .initialModel(DeepCopyHelper.deepCopy(model))
                .build()
                .newInstance(CoreConfig.DEFAULT, mock(ServiceContext.class));
        smtProcessor = AimcSubmodelTemplateProcessorConfig.builder()
                .connectionLevelCredentials(Map.of())
                .build()
                .newInstance(CoreConfig.DEFAULT, null);
        service = spy(new Service(
                CoreConfig.DEFAULT,
                persistence,
                mock(FileStorage.class),
                mock(MessageBus.class),
                null,
                null,
                List.of(smtProcessor)));
        assetConnectionManager = spy(service.getAssetConnectionManager());
        ReflectionHelper.setField(service, "assetConnectionManager", assetConnectionManager);
        StaticRequestExecutionContext requestExecutionContext = new StaticRequestExecutionContext(
                CoreConfig.DEFAULT,
                service.getPersistence(),
                service.getFileStorage(),
                service.getMessageBus(),
                assetConnectionManager);
        ReflectionHelper.setField(service, "requestExecutionContext", requestExecutionContext);
        ReflectionHelper.setField(smtProcessor, "serviceContext", service);
    }


    @Test
    public void testAimc() throws Exception {
        Environment model = EnvironmentSerializationManager.deserialize(new File("src/test/resources/Test-Example.json")).getEnvironment();
        initMocks(model);
        Optional<Submodel> submodel = model.getSubmodels().stream()
                .filter(x -> x.getId().equals(SUBMODEL_ID_AIMC))
                .findFirst();
        assertTrue(submodel.isPresent());
        smtProcessor.add(submodel.get(), assetConnectionManager);

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
        verify(assetConnectionManager).updateConnections(isNull(), argThat((List<AssetConnectionConfig> actual) -> {
            if (actual.size() != 2) {
                return false;
            }
            Optional<MqttAssetConnectionConfig> mqttConfig = actual.stream()
                    .filter(MqttAssetConnectionConfig.class::isInstance)
                    .map(MqttAssetConnectionConfig.class::cast)
                    .findFirst();
            if (mqttConfig.isEmpty()) {
                return false;
            }
            mqttExpected.setClientId(mqttConfig.get().getClientId());
            return Objects.equals(Set.of(mqttExpected, httpExpected), new HashSet<>(actual));
        }));
        // TODO httpsubscription interval is 0 instead of 1000 as expected
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
     * Tests the key value of a Property. If a Property has a key element, the
     * value of this is used. Otherwise the IdShort of the Property itself is
     * used.
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
