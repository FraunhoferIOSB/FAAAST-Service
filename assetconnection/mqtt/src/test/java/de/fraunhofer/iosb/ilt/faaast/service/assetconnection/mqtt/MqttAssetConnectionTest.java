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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.MqttOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.MqttSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.MqttValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
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
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;


public class MqttAssetConnectionTest {

    private static final Reference DEFAULT_REFERENCE = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
    private static final long DEFAULT_TIMEOUT = 5000;
    private static final String DEFAULT_TOPIC = "some.mqtt.topic";
    private static final String LOCALHOST = "127.0.0.1";
    private static int mqttPort;
    private static Server mqttServer;
    private static String mqttServerUri;

    @AfterClass
    public static void cleanup() throws IOException {
        mqttServer.stopServer();
    }


    @BeforeClass
    public static void init() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Assert.assertNotNull(serverSocket);
            Assert.assertTrue(serverSocket.getLocalPort() > 0);
            mqttPort = serverSocket.getLocalPort();
        }
        catch (IOException e) {
            Assert.fail("could not find free port");
        }
        mqttServer = new Server();
        MemoryConfig mqttConfig = new MemoryConfig(new Properties());
        mqttConfig.setProperty(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(mqttPort));
        mqttConfig.setProperty(BrokerConstants.HOST_PROPERTY_NAME, LOCALHOST);
        mqttConfig.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.toString(true));
        mqttServer.startServer(mqttConfig, null);
        mqttServerUri = "tcp://" + LOCALHOST + ":" + mqttPort;
    }


    private static boolean isDebugging() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
    }


    private static void publishMqtt(String topic, String content) {
        mqttServer.internalPublish(MqttMessageBuilders.publish()
                .topicName(topic)
                .retained(false)
                .qos(MqttQoS.AT_MOST_ONCE)
                .payload(Unpooled.copiedBuffer(content.getBytes(UTF_8))).build(),
                "unit test " + UUID.randomUUID().toString().replace("-", ""));
    }


    public String invokeValueProvider(ContentFormat contentFormat, DataElementValue newValue, String query)
            throws AssetConnectionException, InterruptedException, MqttException, ConfigurationInitializationException {
        MqttAssetConnection assetConnection = newConnection(MqttValueProviderConfig.builder()
                .contentFormat(contentFormat)
                .query(query)
                .build());
        MqttClient client = newMqttClient();
        final AtomicReference<String> response = new AtomicReference<>();
        CountDownLatch condition = new CountDownLatch(1);
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {}


            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                response.set(new String(mqttMessage.getPayload()));
                condition.countDown();
            }


            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
        });
        client.subscribe(DEFAULT_TOPIC);
        assetConnection.getValueProviders().get(DEFAULT_REFERENCE).setValue(newValue);
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        return response.get();
    }


    public void testSubscriptionProvider(ContentFormat contentFormat, String message, PropertyValue expected)
            throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException {
        testSubscriptionProvider(contentFormat, message, null, expected);
    }


    public void testSubscriptionProvider(ContentFormat contentFormat, String message, String query, PropertyValue expected)
            throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException {
        MqttAssetConnection assetConnection = newConnection(
                ElementValueTypeInfo.builder()
                        .datatype(expected.getValue().getDataType())
                        .type(expected.getClass())
                        .build(),
                MqttSubscriptionProviderConfig.builder()
                        .contentFormat(contentFormat)
                        .query(query)
                        .build());
        CountDownLatch condition = new CountDownLatch(1);
        final AtomicReference<DataElementValue> response = new AtomicReference<>();
        assetConnection.getSubscriptionProviders().get(DEFAULT_REFERENCE).addNewDataListener(new NewDataListener() {
            @Override
            public void newDataReceived(DataElementValue data) {
                response.set(data);
                condition.countDown();
            }
        });
        publishMqtt(DEFAULT_TOPIC, message);
        condition.await(DEFAULT_TIMEOUT, isDebugging() ? TimeUnit.SECONDS : TimeUnit.MILLISECONDS);
        Assert.assertEquals(expected, response.get());
    }


    @Test
    public void testSubscriptionProviderJsonProperty() throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException {
        testSubscriptionProvider(ContentFormat.JSON, "7", PropertyValue.of(Datatype.INT, "7"));
        testSubscriptionProvider(ContentFormat.JSON, "\"hello world\"", PropertyValue.of(Datatype.STRING, "hello world"));
    }


    @Test
    public void testSubscriptionProviderJsonPropertyWithQuery() throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException {
        testSubscriptionProvider(ContentFormat.JSON, "{\"foo\": 123, \"bar\": 7}", "$.bar", PropertyValue.of(Datatype.INT, "7"));
        testSubscriptionProvider(ContentFormat.JSON, "{\"foo\": \"hello\", \"bar\": \"world\"}", "$.bar", PropertyValue.of(Datatype.STRING, "world"));
    }


    @Test
    public void testValueProviderProperty()
            throws AssetConnectionException, InterruptedException, ValueFormatException, MqttException, JSONException, ConfigurationInitializationException {
        String expected = "\"hello world\"";
        String actual = invokeValueProvider(ContentFormat.JSON, PropertyValue.of(Datatype.STRING, "hello world"), null);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }


    @Test(expected = UnsupportedOperationException.class)
    public void testValueProviderPropertyWithQuery()
            throws AssetConnectionException, InterruptedException, ValueFormatException, MqttException, JSONException, ConfigurationInitializationException {
        String expected = "{\"foo\": \"hello world\"}";
        String actual = invokeValueProvider(ContentFormat.JSON, PropertyValue.of(Datatype.STRING, "hello world"), "$.foo");
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }


    private MqttAssetConnection newConnection(MqttValueProviderConfig valueProvider) throws ConfigurationInitializationException {
        return newConnection(DEFAULT_REFERENCE, null, valueProvider, null, null);
    }


    private MqttAssetConnection newConnection(TypeInfo expectedTypeInfo,
                                              MqttSubscriptionProviderConfig subscriptionProvider)
            throws ConfigurationInitializationException {
        return newConnection(DEFAULT_REFERENCE, expectedTypeInfo, null, null, subscriptionProvider);
    }


    private MqttAssetConnection newConnection(Reference reference,
                                              TypeInfo expectedTypeInfo,
                                              MqttValueProviderConfig valueProvider,
                                              MqttOperationProviderConfig operationProvider,
                                              MqttSubscriptionProviderConfig subscriptionProvider)
            throws ConfigurationInitializationException {
        MqttAssetConnectionConfig config = MqttAssetConnectionConfig.builder()
                .serverUri(mqttServerUri)
                .build();
        if (valueProvider != null) {
            if (valueProvider.getTopic() == null || valueProvider.getTopic().isEmpty()) {
                valueProvider.setTopic(DEFAULT_TOPIC);
            }
            config.getValueProviders().put(reference, valueProvider);
        }
        if (operationProvider != null) {
            config.getOperationProviders().put(reference, operationProvider);
        }
        if (subscriptionProvider != null) {
            if (subscriptionProvider.getTopic() == null || subscriptionProvider.getTopic().isEmpty()) {
                subscriptionProvider.setTopic(DEFAULT_TOPIC);
            }
            config.getSubscriptionProviders().put(reference, subscriptionProvider);
        }
        MqttAssetConnection result = new MqttAssetConnection();
        ServiceContext serviceContext = mock(ServiceContext.class);
        if (expectedTypeInfo != null) {
            doReturn(expectedTypeInfo).when(serviceContext).getTypeInfo(reference);
        }
        result.init(CoreConfig.builder().build(), config, serviceContext);
        return result;
    }


    private MqttClient newMqttClient() throws MqttException {
        MqttClient client = new MqttClient(mqttServerUri, UUID.randomUUID().toString(), new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        client.connect(options);
        return client;
    }

}
