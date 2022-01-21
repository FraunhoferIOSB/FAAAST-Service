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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt;

import static io.netty.util.CharsetUtil.UTF_8;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.content.ContentFormat;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Reference;
import io.moquette.BrokerConstants;
import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttQoS;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


public class MqttAssetConnectionTest {

    private static final long DEFAULT_TIMEOUT = 1000;
    private static final String LOCALHOST = "127.0.0.1";

    private static int mqttPort;
    private static Server mqttServer;

    @AfterClass
    public static void cleanup() throws IOException {
        mqttServer.stopServer();
    }


    @BeforeClass
    public static void init() throws IOException {
        // find free TCP port
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Assert.assertNotNull(serverSocket);
            Assert.assertTrue(serverSocket.getLocalPort() > 0);
            mqttPort = serverSocket.getLocalPort();
        }
        catch (IOException e) {
            Assert.fail("could not find free port");
        }
        // start embedded mqtt server
        mqttServer = new Server();
        MemoryConfig mqttConfig = new MemoryConfig(new Properties());
        mqttConfig.setProperty(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(mqttPort));
        mqttConfig.setProperty(BrokerConstants.HOST_PROPERTY_NAME, LOCALHOST);
        mqttConfig.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.toString(true));
        mqttServer.startServer(mqttConfig, null);
    }


    private static void publishMqtt(String topic, String content) {
        mqttServer.internalPublish(MqttMessageBuilders.publish()
                .topicName(topic)
                .retained(false)
                .qos(MqttQoS.AT_MOST_ONCE)
                .payload(Unpooled.copiedBuffer(content.getBytes(UTF_8))).build(),
                "unit test " + UUID.randomUUID().toString().replace("-", ""));
    }


    @Test
    public void testSubscriptionProvider() throws AssetConnectionException, InterruptedException {
        final String topic = "some.mqtt.topic";
        PropertyValue expected = new PropertyValue();
        expected.setValue("hello world");
        MqttAssetConnectionConfig config = new MqttAssetConnectionConfig();
        config.setServerURI("tcp://" + LOCALHOST + ":" + mqttPort);
        config.setClientID("FAST MQTT Client");
        MqttSubscriptionProviderConfig subscriptionConfig = new MqttSubscriptionProviderConfig();
        subscriptionConfig.setTopic(topic);
        // TODO change ID_SHORT to IdShort once dataformat-core 1.2.1 hotfix is released
        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        config.getSubscriptionProviders().put(reference, subscriptionConfig);
        MqttAssetConnection assetConnection = new MqttAssetConnection();
        ServiceContext serviceContext = mock(ServiceContext.class);
        doReturn(Property.class).when(serviceContext).getElementType(reference);
        assetConnection.init(CoreConfig.builder().build(), config, serviceContext);
        CountDownLatch condition = new CountDownLatch(1);
        final AtomicReference<DataElementValue> response = new AtomicReference<>();
        assetConnection.getSubscriptionProviders().get(reference).addNewDataListener(new NewDataListener() {
            @Override
            public void newDataReceived(DataElementValue data) {
                // we received data via MQTT
                response.set(data);
                condition.countDown();
            }
        });
        // send message via MQTT
        publishMqtt(topic, expected.getValue());
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(expected, response.get());
    }


    @Ignore("missing JSON deserialization")
    @Test
    public void testSubscriptionProviderWithJsonPath() throws AssetConnectionException, InterruptedException {
        final String topic = "some.mqtt.topic";
        PropertyValue expected = new PropertyValue();
        expected.setValue("{value1:5, value2:7}");
        MqttAssetConnectionConfig config = new MqttAssetConnectionConfig();
        config.setServerURI("tcp://" + LOCALHOST + ":" + mqttPort);
        config.setClientID("FAST MQTT Client");
        MqttSubscriptionProviderConfig subscriptionConfig = new MqttSubscriptionProviderConfig();
        subscriptionConfig.setTopic(topic);
        subscriptionConfig.setContentFormat(ContentFormat.JSON);
        //extract value2 from json
        subscriptionConfig.setQuery("$.value2");
        // TODO change ID_SHORT to IdShort once dataformat-core 1.2.1 hotfix is released
        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        config.getSubscriptionProviders().put(reference, subscriptionConfig);
        MqttAssetConnection assetConnection = new MqttAssetConnection();
        ServiceContext serviceContext = mock(ServiceContext.class);
        doReturn(Property.class).when(serviceContext).getElementType(reference);
        assetConnection.init(CoreConfig.builder().build(), config, serviceContext);
        CountDownLatch condition = new CountDownLatch(1);
        final AtomicReference<DataElementValue> response = new AtomicReference<>();
        assetConnection.getSubscriptionProviders().get(reference).addNewDataListener(new NewDataListener() {
            @Override
            public void newDataReceived(DataElementValue data) {
                response.set(data);
                condition.countDown();
            }
        });
        publishMqtt(topic, expected.getValue());
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        PropertyValue extracted = new PropertyValue();
        extracted.setValue("7");
        Assert.assertEquals(extracted, response.get());
    }


    @Test
    public void testValueProvider() throws AssetConnectionException, InterruptedException {
        final String topic = "some.mqtt.topic";
        PropertyValue expected = new PropertyValue();
        expected.setValue("hello world");
        MqttAssetConnectionConfig config = new MqttAssetConnectionConfig();
        config.setServerURI("tcp://" + LOCALHOST + ":" + mqttPort);
        config.setClientID("FAST MQTT Client");
        MqttValueProviderConfig valueConfig = new MqttValueProviderConfig();
        valueConfig.setTopic(topic);
        // TODO change ID_SHORT to IdShort once dataformat-core 1.2.1 hotfix is released
        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        config.getValueProviders().put(reference, valueConfig);
        MqttAssetConnection assetConnection = new MqttAssetConnection();
        ServiceContext serviceContext = mock(ServiceContext.class);
        doReturn(Property.class).when(serviceContext).getElementType(reference);
        assetConnection.init(CoreConfig.builder().build(), config, serviceContext);

        MqttSubscriptionProviderConfig subscriptionConfig = new MqttSubscriptionProviderConfig();
        subscriptionConfig.setTopic(topic);
        config.getSubscriptionProviders().put(reference, subscriptionConfig);
        MqttAssetConnection assetConnectionForSubscribe = new MqttAssetConnection();
        config.setClientID("FAST MQTT Client Subscribe");
        assetConnectionForSubscribe.init(CoreConfig.builder().build(), config, serviceContext);
        final AtomicReference<DataElementValue> response = new AtomicReference<>();
        CountDownLatch condition = new CountDownLatch(1);
        assetConnectionForSubscribe.getSubscriptionProviders().get(reference).addNewDataListener(new NewDataListener() {
            @Override
            public void newDataReceived(DataElementValue data) {
                // receive data from value provider
                System.out.println(((PropertyValue) data).getValue());
                response.set(data);
                condition.countDown();
            }
        });
        // publish message with value provider
        assetConnection.getValueProviders().get(reference).setValue(expected);
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(expected, response.get());

    }
}
