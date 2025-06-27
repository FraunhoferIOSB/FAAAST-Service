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
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.AimcSubmodelTemplateProcessorConfig;
import de.fraunhofer.iosb.ilt.faaast.service.test.model.MqttModel;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import java.io.IOException;
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
import org.junit.Test;


public class AimcSubmodelTemplateProcessorMqttIT {

    private static MessageBusMqttConfig messageBusConfig;
    private static MoquetteServer server;
    private static PahoClient client;
    private static int mqttPort;
    private Service service;

    @BeforeClass
    public static void initClass() throws IOException, MessageBusException {
        mqttPort = PortHelper.findFreePort();
        //mqttPort = 1883;
        messageBusConfig = MessageBusMqttConfig.builder()
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
    public void init() {}


    @After
    public void shutdown() {
        if (service != null) {
            service.stop();
        }
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
                        .build()))
                .build();
    }

}
