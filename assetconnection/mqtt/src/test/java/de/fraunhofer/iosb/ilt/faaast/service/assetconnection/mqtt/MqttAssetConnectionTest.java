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
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.provider.MultiFormatSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.config.MqttOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.config.MqttSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.config.MqttValueProviderConfig;
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
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import org.apache.commons.lang3.ArrayUtils;
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
import org.slf4j.LoggerFactory;


public class MqttAssetConnectionTest {

    private static final Reference DEFAULT_REFERENCE = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
    private static final long DEFAULT_TIMEOUT = 10000;
    private static final String DEFAULT_TOPIC = "some.mqtt.topic";
    private static final String LOCALHOST = "127.0.0.1";
    private static final Predicate<ILoggingEvent> LOG_CONNECTION_LOST = x -> x.getLevel() == Level.WARN && x.getMessage().startsWith("MQTT asset connection lost");
    private static final Predicate<ILoggingEvent> LOG_MSG_DESERIALIZATION_FAILED = x -> x.getLevel() == Level.ERROR
            && x.getMessage().startsWith("error deserializing message");
    private static int mqttPort;
    private static Server mqttServer;
    private static String mqttServerUri;
    private static final String FORMAT_JSON = "JSON";

    @AfterClass
    public static void cleanup() throws IOException {
        mqttServer.stopServer();
    }


    @BeforeClass
    public static void init() throws IOException {
        try {
            mqttPort = findFreePort();
        }
        catch (IOException e) {
            Assert.fail("could not find free port");
        }
        mqttServer = startMqttServer(mqttPort);
        mqttServerUri = "tcp://" + LOCALHOST + ":" + mqttPort;
    }


    private static int findFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Assert.assertNotNull(serverSocket);
            Assert.assertTrue(serverSocket.getLocalPort() > 0);
            return serverSocket.getLocalPort();
        }
    }


    private static ListAppender<ILoggingEvent> getListLogger(Class<?> clazz) {
        ListAppender<ILoggingEvent> result = new ListAppender<>();
        result.start();
        ((Logger) LoggerFactory.getLogger(clazz)).addAppender(result);
        return result;
    }


    private static boolean hasLogEvent(ListAppender<ILoggingEvent> listLogger, Predicate<ILoggingEvent> predicate) {
        return listLogger != null
                && predicate != null
                && listLogger.list.stream()
                        .anyMatch(predicate);
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


    private static Server startMqttServer(int port) throws IOException {
        Server result = new Server();
        MemoryConfig mqttConfig = new MemoryConfig(new Properties());
        mqttConfig.setProperty(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(port));
        mqttConfig.setProperty(BrokerConstants.HOST_PROPERTY_NAME, LOCALHOST);
        mqttConfig.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.toString(true));
        result.startServer(mqttConfig, null);
        return result;
    }


    private String invokeValueProvider(String format, DataElementValue newValue, String template)
            throws AssetConnectionException, InterruptedException, MqttException, ConfigurationInitializationException {
        MqttAssetConnection assetConnection = newConnection(MqttValueProviderConfig.builder()
                .format(format)
                .template(template)
                .build());
        MqttClient client = newMqttClient();
        final AtomicReference<String> response = new AtomicReference<>();
        CountDownLatch condition = new CountDownLatch(1);
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                // intentionally left empty
            }


            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                response.set(new String(mqttMessage.getPayload()));
                condition.countDown();
            }


            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                // intentionally left empty
            }
        });
        client.subscribe(DEFAULT_TOPIC);
        assetConnection.getValueProviders().get(DEFAULT_REFERENCE).setValue(newValue);
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        return response.get();
    }


    private void assertSubscriptionProvider(String format, String message, PropertyValue expected)
            throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException {
        assertSubscriptionProvider(format, message, null, expected);
    }


    private void assertSubscriptionProvider(String format, String message, String query, PropertyValue expected)
            throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException {
        assertSubscriptionProvider(
                MqttSubscriptionProviderConfig.builder()
                        .format(format)
                        .query(query)
                        .build(),
                () -> publishMqtt(DEFAULT_TOPIC, message),
                null,
                expected);
    }


    private void assertSubscriptionProvider(MqttSubscriptionProviderConfig config, Runnable publisher, Callable<Boolean> waitCondition, PropertyValue... expected)
            throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException, InterruptedException {
        MqttAssetConnection assetConnection = newConnection(
                ArrayUtils.isEmpty(expected)
                        ? null
                        : ElementValueTypeInfo.builder()
                                .datatype(expected[0].getValue().getDataType())
                                .type(expected[0].getClass())
                                .build(),
                config);
        NewDataListener listener = null;
        try {
            if (!ArrayUtils.isEmpty(expected)) {
                CountDownLatch condition = new CountDownLatch(expected.length);
                final DataElementValue[] response = new DataElementValue[expected.length];
                final AtomicInteger pointer = new AtomicInteger(0);
                listener = (DataElementValue data) -> {
                    response[pointer.getAndIncrement()] = data;
                    condition.countDown();
                };
                assetConnection.getSubscriptionProviders().get(DEFAULT_REFERENCE).addNewDataListener(listener);
                publisher.run();
                condition.await(DEFAULT_TIMEOUT, isDebugging() ? TimeUnit.SECONDS : TimeUnit.MILLISECONDS);
                Assert.assertArrayEquals(expected, response);
            }
            else {
                listener = (DataElementValue data) -> {
                    // empty on purpose
                };
                assetConnection.getSubscriptionProviders().get(DEFAULT_REFERENCE).addNewDataListener(listener);
                publisher.run();
            }
            if (waitCondition != null) {
                await().atMost(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS).until(waitCondition);
            }
        }
        finally {
            assetConnection.getSubscriptionProviders().get(DEFAULT_REFERENCE).removeNewDataListener(listener);
            assetConnection.close();
        }
    }


    @Test
    public void testSubscriptionProviderConnectionLost()
            throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException, IOException {
        int port = findFreePort();
        Server localServer = startMqttServer(port);
        ListAppender<ILoggingEvent> listLogger = getListLogger(MqttAssetConnection.class);
        new MqttAssetConnection().init(CoreConfig.builder()
                .build(),
                MqttAssetConnectionConfig.builder()
                        .serverUri("tcp://" + LOCALHOST + ":" + port)
                        .build(),
                mock(ServiceContext.class));
        localServer.stopServer();
        await().atMost(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
                .until(() -> !listLogger.list.isEmpty());
        Assert.assertTrue(hasLogEvent(listLogger, LOG_CONNECTION_LOST));
    }


    @Test
    public void testSubscriptionProviderJsonProperty() throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException {
        //        assertSubscriptionProvider(FORMAT_JSON, "7", PropertyValue.of(Datatype.INT, "7"));
        assertSubscriptionProvider(FORMAT_JSON, "\"hello world\"", PropertyValue.of(Datatype.STRING, "hello world"));
    }


    @Test
    public void testSubscriptionProviderJsonPropertyInvalidMessage()
            throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException, IOException {
        ListAppender<ILoggingEvent> listLogger = getListLogger(MultiFormatSubscriptionProvider.class);
        String message = "7";
        PropertyValue expected = PropertyValue.of(Datatype.INT, message);
        assertSubscriptionProvider(MqttSubscriptionProviderConfig.builder()
                .format(FORMAT_JSON)
                .build(),
                () -> {
                    publishMqtt(DEFAULT_TOPIC, message);
                    publishMqtt(DEFAULT_TOPIC, "foo");
                    publishMqtt(DEFAULT_TOPIC, message);
                },
                null,
                expected, expected);
        Assert.assertTrue(hasLogEvent(listLogger, LOG_MSG_DESERIALIZATION_FAILED));
    }


    @Test
    public void testSubscriptionProviderJsonPropertyWithInvalidQuery()
            throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException, IOException {
        ListAppender<ILoggingEvent> listLogger = getListLogger(MultiFormatSubscriptionProvider.class);
        assertSubscriptionProvider(
                MqttSubscriptionProviderConfig.builder()
                        .format(FORMAT_JSON)
                        .query("~some#invalid#query~")
                        .build(),
                () -> publishMqtt(DEFAULT_TOPIC, "7"),
                () -> !listLogger.list.isEmpty());
        Assert.assertTrue(hasLogEvent(listLogger, LOG_MSG_DESERIALIZATION_FAILED));
    }


    @Test
    public void testSubscriptionProviderJsonPropertyWithQuery()
            throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException {
        assertSubscriptionProvider(FORMAT_JSON, "{\"foo\": 123, \"bar\": 7}", "$.bar", PropertyValue.of(Datatype.INT, "7"));
        assertSubscriptionProvider(FORMAT_JSON, "{\"foo\": \"hello\", \"bar\": \"world\"}", "$.bar", PropertyValue.of(Datatype.STRING, "world"));
    }


    @Test
    public void testValueProviderProperty()
            throws AssetConnectionException, InterruptedException, ValueFormatException, MqttException, JSONException, ConfigurationInitializationException {
        String expected = "\"hello world\"";
        String actual = invokeValueProvider(FORMAT_JSON, PropertyValue.of(Datatype.STRING, "hello world"), null);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }


    @Test
    public void testValueProviderPropertyWithTemplate()
            throws AssetConnectionException, InterruptedException, ValueFormatException, MqttException, JSONException, ConfigurationInitializationException {
        String expected = "{\"foo\": \"hello world\"}";
        String template = "{\"foo\": ${value}}";
        String actual = invokeValueProvider(FORMAT_JSON, PropertyValue.of(Datatype.STRING, "hello world"), template);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }


    private MqttAssetConnection newConnection(MqttValueProviderConfig valueProvider) throws ConfigurationInitializationException {
        return newConnection(DEFAULT_REFERENCE, null, valueProvider, null, null);
    }


    private MqttAssetConnection newConnection(TypeInfo expectedTypeInfo, MqttSubscriptionProviderConfig subscriptionProvider)
            throws ConfigurationInitializationException {
        return newConnection(DEFAULT_REFERENCE, expectedTypeInfo, null, null, subscriptionProvider);
    }


    private MqttAssetConnection newConnection(Reference reference,
                                              TypeInfo expectedTypeInfo,
                                              MqttValueProviderConfig valueProvider,
                                              MqttOperationProviderConfig operationProvider,
                                              MqttSubscriptionProviderConfig subscriptionProvider)
            throws ConfigurationInitializationException {
        return newConnection(mqttServerUri, reference, expectedTypeInfo, valueProvider, operationProvider, subscriptionProvider);
    }


    private MqttAssetConnection newConnection(String url,
                                              Reference reference,
                                              TypeInfo expectedTypeInfo,
                                              MqttValueProviderConfig valueProvider,
                                              MqttOperationProviderConfig operationProvider,
                                              MqttSubscriptionProviderConfig subscriptionProvider)
            throws ConfigurationInitializationException {
        MqttAssetConnectionConfig config = MqttAssetConnectionConfig.builder()
                .serverUri(url)
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
