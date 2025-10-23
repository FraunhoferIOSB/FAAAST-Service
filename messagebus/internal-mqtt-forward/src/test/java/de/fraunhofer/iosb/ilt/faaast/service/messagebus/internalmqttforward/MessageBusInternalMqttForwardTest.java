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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.internalmqttforward;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonEventDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.error.ErrorEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.error.ErrorLevel;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.IntValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import io.moquette.BrokerConstants;
import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;


public class MessageBusInternalMqttForwardTest {

    private static final String LOCALHOST = "127.0.0.1";
    private static final long DEFAULT_TIMEOUT = 10000;

    private static final ServiceContext SERVICE_CONTEXT = Mockito.mock(ServiceContext.class);
    private static final Reference property1Reference = new DefaultReference.Builder()
            .keys(new DefaultKey.Builder()
                    .type(KeyTypes.PROPERTY)
                    .value("property1")
                    .build())
            .build();

    private static int port;
    private static Server server;
    private static MqttClient client;
    private static ValueChangeEventMessage valueChangeMessage;
    private static ErrorEventMessage errorMessage;
    private static Queue<EventMessage> receivedMessages;
    private static JsonEventDeserializer deserializer;

    @BeforeClass
    public static void init() throws Exception {
        valueChangeMessage = new ValueChangeEventMessage();
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.setValue(new IntValue(100));
        valueChangeMessage.setOldValue(propertyValue);
        propertyValue.setValue(new IntValue(123));
        valueChangeMessage.setNewValue(propertyValue);

        errorMessage = new ErrorEventMessage();
        errorMessage.setElement(property1Reference);
        errorMessage.setLevel(ErrorLevel.ERROR);

        port = PortHelper.findFreePort();
        MemoryConfig serverConfig = new MemoryConfig(new Properties());
        serverConfig.setProperty(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(port));
        serverConfig.setProperty(BrokerConstants.HOST_PROPERTY_NAME, LOCALHOST);
        server = new Server();
        server.startServer(serverConfig);
        receivedMessages = new ConcurrentLinkedQueue<>();
        deserializer = new JsonEventDeserializer();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        client = new MqttClient(
                String.format("tcp://%s:%d", LOCALHOST, port),
                "unit-test",
                new MemoryPersistence());
        client.connect(options);
        client.subscribe("#", (topic, message) -> {
            receivedMessages.add(deserializer.read(new String(message.getPayload())));
        });
    }


    @AfterClass
    public static void cleanUp() throws MqttException {
        client.disconnect();
        server.stopServer();
    }


    @Test
    public void testMqttForwardedExactMatch() throws Exception {
        MessageBusInternalMqttForward messageBus = startMessageBus(ValueChangeEventMessage.class);
        messageBus.publish(valueChangeMessage);
        messageBus.publish(errorMessage);
        assertMqttMessages(valueChangeMessage);
        assertNotMqttMessages(errorMessage);
        messageBus.stop();
    }


    @Test
    public void testMqttForwardedSuperTypeSubscription() throws Exception {
        MessageBusInternalMqttForward messageBus = startMessageBus(EventMessage.class);
        messageBus.publish(valueChangeMessage);
        messageBus.publish(errorMessage);
        assertMqttMessages(valueChangeMessage, errorMessage);
        messageBus.stop();
    }


    @Test
    public void testMqttNotForwarded() throws Exception {
        MessageBusInternalMqttForward messageBus = startMessageBus();
        messageBus.start();
        CountDownLatch condition = new CountDownLatch(1);
        final AtomicReference<EventMessage> response = new AtomicReference<>();
        messageBus.subscribe(SubscriptionInfo.create(
                ValueChangeEventMessage.class,
                x -> {
                    response.set(x);
                    condition.countDown();
                }));
        messageBus.publish(valueChangeMessage);
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(valueChangeMessage, response.get());
        messageBus.stop();
    }


    @Test
    public void testExactTypeSubscription() throws InterruptedException, MessageBusException, Exception {
        MessageBusInternalMqttForward messageBus = startMessageBus();
        messageBus.start();
        CountDownLatch condition = new CountDownLatch(1);
        final AtomicReference<EventMessage> response = new AtomicReference<>();
        messageBus.subscribe(SubscriptionInfo.create(
                ValueChangeEventMessage.class,
                x -> {
                    response.set(x);
                    condition.countDown();
                }));
        messageBus.publish(valueChangeMessage);
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(valueChangeMessage, response.get());
        messageBus.stop();
    }


    @Test
    public void testSuperTypeSubscription() throws InterruptedException, Exception {
        MessageBusInternalMqttForward messageBus = startMessageBus();
        messageBus.start();
        Set<EventMessage> messages = Set.of(valueChangeMessage, errorMessage);
        Set<EventMessage> responses = Collections.synchronizedSet(new HashSet<>());
        CountDownLatch condition = new CountDownLatch(messages.size());
        messageBus.subscribe(SubscriptionInfo.create(
                EventMessage.class,
                x -> {
                    responses.add(x);
                    condition.countDown();
                }));
        messages.forEach(x -> {
            try {
                messageBus.publish(x);
            }
            catch (Exception e) {
                Assert.fail();
            }
        });
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(messages, responses);
        messageBus.stop();
    }


    @Test
    public void testDistinctTypesSubscription() throws InterruptedException, Exception {
        MessageBusInternalMqttForward messageBus = startMessageBus();
        messageBus.start();
        Map<Class<? extends EventMessage>, Set<EventMessage>> messages = Map.of(
                ChangeEventMessage.class, Set.of(valueChangeMessage),
                ErrorEventMessage.class, Set.of(errorMessage));
        Map<Class<? extends EventMessage>, Set<EventMessage>> responses = Collections.synchronizedMap(Map.of(
                ChangeEventMessage.class, new HashSet<>(),
                ErrorEventMessage.class, new HashSet<>()));
        CountDownLatch condition = new CountDownLatch(messages.values().stream().mapToInt(x -> x.size()).sum());
        responses.entrySet().forEach(entry -> messageBus.subscribe(
                SubscriptionInfo.create(
                        entry.getKey(),
                        x -> {
                            entry.getValue().add(x);
                            condition.countDown();
                        })));
        messages.values().stream().flatMap(x -> x.stream()).forEach(x -> {
            try {
                messageBus.publish(x);
            }
            catch (Exception e) {
                Assert.fail();
            }
        });
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(messages, responses);
        messageBus.stop();
    }


    @Test
    public void testNotMatchingSubscription() throws InterruptedException, Exception {
        MessageBusInternalMqttForward messageBus = startMessageBus();
        messageBus.start();
        CountDownLatch condition = new CountDownLatch(1);
        messageBus.subscribe(SubscriptionInfo.create(
                ErrorEventMessage.class,
                x -> {
                    Assert.fail();
                    condition.countDown();
                }));
        try {
            messageBus.publish(valueChangeMessage);
        }
        catch (Exception e) {
            Assert.fail();
        }
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        messageBus.stop();
    }


    @Test
    public void testSubscribeUnsubscribe() throws InterruptedException, Exception {
        MessageBusInternalMqttForward messageBus = startMessageBus();
        messageBus.start();
        CountDownLatch condition = new CountDownLatch(1);
        final AtomicReference<EventMessage> response = new AtomicReference<>();
        messageBus.unsubscribe(messageBus.subscribe(SubscriptionInfo.create(
                ValueChangeEventMessage.class,
                x -> {
                    response.set(x);
                    condition.countDown();
                })));
        try {
            messageBus.publish(valueChangeMessage);
        }
        catch (Exception e) {
            Assert.fail();
        }
        Assert.assertFalse(condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS));
        messageBus.stop();
    }


    private MessageBusInternalMqttForward startMessageBus(Class<? extends EventMessage>... eventsToForward) throws Exception {
        MessageBusInternalMqttForwardConfig config = MessageBusInternalMqttForwardConfig.builder()
                .host(LOCALHOST)
                .port(port)
                .eventsToForward(Arrays.asList(eventsToForward))
                .build();
        MessageBusInternalMqttForward messageBus = new MessageBusInternalMqttForward();
        messageBus.init(CoreConfig.builder().build(), config, SERVICE_CONTEXT);
        messageBus.start();
        return messageBus;
    }


    private void assertMqttMessages(EventMessage... events) {
        if (Objects.isNull(events)) {
            return;
        }
        await().atMost(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
                .until(() -> receivedMessages.size() >= events.length);
        for (var event: events) {
            await().atMost(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
                    .until(() -> receivedMessages.remove(event));
        }
    }


    private void assertNotMqttMessages(EventMessage... events) {
        if (Objects.isNull(events)) {
            return;
        }
        assertTrue(Stream.of(events).noneMatch(receivedMessages::contains));
    }
}
