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
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config.AimcSubmodelTemplateProcessorConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config.InterfaceConfiguration;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ProcessorTest {

    private ServiceConfig config;

    @Before
    public void init() {

        Reference submodelRef = ReferenceBuilder.forSubmodel("https://example.com/ids/sm/AssetInterfacesDescription", "InterfaceHTTP");
        config = new ServiceConfig.Builder()
                .core(new CoreConfig.Builder().requestHandlerThreadPoolSize(2).build())
                .persistence(new PersistenceInMemoryConfig())
                .fileStorage(new FileStorageInMemoryConfig())
                .messageBus(new MessageBusInternalConfig())
                .submodelTemplateProcessors(List.of(new AimcSubmodelTemplateProcessorConfig.Builder()
                        .interfaceConfiguration(submodelRef, new InterfaceConfiguration.Builder().username("user1").password("pw1").build()).build()))
                .build();
    }


    @Test
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
                .baseUrl("http://plugfest.thingweb.io:8083")
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
        Assert.assertEquals(new URL("http://plugfest.thingweb.io:8083"), httpConfig.getBaseUrl());
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
}
