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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.mqtt;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionId;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.error.ErrorEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.error.ErrorLevel;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.IntValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


public abstract class AbstractMessageBusMqttTest<T> {

    private static final ServiceContext SERVICE_CONTEXT = Mockito.mock(ServiceContext.class);
    private static final long DEFAULT_TIMEOUT = 1000;

    protected static final String PASSWORD_FILE = "src/test/resources/password_file.conf";
    protected static final String SERVER_KEYSTORE_PATH = "src/test/resources/serverkeystore.jks";
    protected static final String SERVER_KEYSTORE_PASSWORD = "password";
    protected static final String CLIENT_KEYSTORE_PATH = "src/test/resources/clientkeystore.jks";
    protected static final String CLIENT_KEYSTORE_PASSWORD = "password";
    protected static final String USER = "user";
    protected static final String USER_PASSWORD_VALID = "password";
    protected static final String USER_PASSWORD_INVALID = "wrong_password";

    private static final Property PROPERTY = new DefaultProperty.Builder()
            .idShort("ExampleProperty")
            .valueType(Datatype.STRING.getName())
            .value("bar")
            .build();

    private static final Reference PROPERTY_REFERENCE = new DefaultReference.Builder()
            .key(new DefaultKey.Builder()
                    .type(KeyElements.PROPERTY)
                    .idType(KeyType.ID_SHORT)
                    .value("ExampleProperty")
                    .build())
            .build();

    private static final ValueChangeEventMessage VALUE_CHANGE_MESSAGE = ValueChangeEventMessage.builder()
            .oldValue(new PropertyValue(new IntValue(100)))
            .oldValue(new PropertyValue(new IntValue(123)))
            .build();

    private static final ElementCreateEventMessage ELEMENT_CREATE_MESSAGE = ElementCreateEventMessage.builder()
            .element(PROPERTY_REFERENCE)
            .value(PROPERTY)
            .build();

    private static final ErrorEventMessage ERROR_MESSAGE = ErrorEventMessage.builder()
            .element(PROPERTY_REFERENCE)
            .level(ErrorLevel.ERROR)
            .build();

    protected static int findFreePort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Assert.assertNotNull(serverSocket);
            Assert.assertTrue(serverSocket.getLocalPort() > 0);
            return serverSocket.getLocalPort();
        }
        catch (IOException ex) {
            Assert.fail("error finding free port");
        }
        return -1;
    }


    protected abstract MessageBusMqttConfig configureInternalTcpNoSslAsAnonymousSuccess();


    protected abstract MessageBusMqttConfig configureInternalWebsocketNoSslAsAnonymousSuccess();


    protected abstract MessageBusMqttConfig configureInternalTcpWithSslAsAnonymousSuccess();


    protected abstract MessageBusMqttConfig configureInternalWebsocketWithSslAsAnonymousSuccess();


    protected abstract MessageBusMqttConfig configureInternalTcpNoSslAsAnonymousFail();


    protected abstract MessageBusMqttConfig configureInternalWebsocketNoSslAsAnonymousFail();


    protected abstract MessageBusMqttConfig configureInternalTcpWithSslAsAnonymousFail();


    protected abstract MessageBusMqttConfig configureInternalWebsocketWithSslAsAnonymousFail();


    protected abstract MessageBusMqttConfig configureInternalTcpNoSslAsInvalidUser();


    protected abstract MessageBusMqttConfig configureInternalWebsocketNoSslAsInvalidUser();


    protected abstract MessageBusMqttConfig configureInternalTcpWithSslAsInvalidUser();


    protected abstract MessageBusMqttConfig configureInternalWebsocketWithSslAsInvalidUser();


    protected abstract MessageBusMqttConfig configureInternalTcpNoSslAsValidUser();


    protected abstract MessageBusMqttConfig configureInternalWebsocketNoSslAsValidUser();


    protected abstract MessageBusMqttConfig configureInternalTcpWithSslAsValidUser();


    protected abstract MessageBusMqttConfig configureInternalWebsocketWithSslAsValidUser();


    @Test
    public void testInternalTcpNoSslAsAnonymousSuccess() throws Exception {
        assertMessageBusWorks(configureInternalTcpNoSslAsAnonymousSuccess());
    }


    @Test
    public void testInternalWebsocketNoSslAsAnonymousSuccess() throws Exception {
        assertMessageBusWorks(configureInternalWebsocketNoSslAsAnonymousSuccess());
    }


    @Test
    public void testInternalTcpWithSslAsAnonymousSuccess() throws Exception {
        assertMessageBusWorks(configureInternalTcpWithSslAsAnonymousSuccess());
    }


    @Test
    public void testInternalWebsocketWithSslAsAnonymousSuccess() throws Exception {
        assertMessageBusWorks(configureInternalWebsocketWithSslAsAnonymousSuccess());
    }


    @Test
    public void testInternalTcpNoSslAsAnonymousFail() throws Exception {
        assertConnectionFails(configureInternalTcpNoSslAsAnonymousFail());
    }


    @Test
    public void testInternalWebsocketNoSslAsAnonymousFail() throws Exception {
        assertConnectionFails(configureInternalWebsocketNoSslAsAnonymousFail());
    }


    @Test
    public void testInternalTcpWithSslAsAnonymousFail() throws Exception {
        assertConnectionFails(configureInternalTcpWithSslAsAnonymousFail());
    }


    @Test
    public void testInternalWebsocketWithSslAsAnonymousFail() throws Exception {
        assertConnectionFails(configureInternalWebsocketWithSslAsAnonymousFail());
    }


    @Test
    public void testInternalTcpNoSslAsInvalidUser() throws Exception {
        assertConnectionFails(configureInternalTcpNoSslAsInvalidUser());
    }


    @Test
    public void testInternalWebsocketNoSslAsInvalidUser() throws Exception {
        assertConnectionFails(configureInternalWebsocketNoSslAsInvalidUser());
    }


    @Test
    public void testInternalTcpWithSslAsInvalidUser() throws Exception {
        assertConnectionFails(configureInternalTcpWithSslAsInvalidUser());
    }


    @Test
    public void testInternalWebsocketWithSslAsInvalidUser() throws Exception {
        assertConnectionFails(configureInternalWebsocketWithSslAsInvalidUser());
    }


    @Test
    public void testInternalTcpNoSslAsValidUser() throws Exception {
        assertMessageBusWorks(configureInternalTcpNoSslAsValidUser());
    }


    @Test
    public void testInternalWebsocketNoSslAsValidUser() throws Exception {
        assertMessageBusWorks(configureInternalWebsocketNoSslAsValidUser());
    }


    @Test
    public void testInternalTcpWithSslAsValidUser() throws Exception {
        assertMessageBusWorks(configureInternalTcpWithSslAsValidUser());
    }


    @Test
    public void testInternalWebsocketWithSslAsValidUser() throws Exception {
        assertMessageBusWorks(configureInternalWebsocketWithSslAsValidUser());
    }


    protected abstract int getTcpPort();


    protected abstract int getTcpSslPort();


    protected abstract int getWebsocketPort();


    protected abstract int getWebsocketSslPort();


    protected abstract T startServer(MessageBusMqttConfig config) throws Exception;


    protected abstract void stopServer(T server);


    private MessageBusInfo startMessageBus(MessageBusMqttConfig config) throws Exception {
        config.setPort(getTcpPort());
        config.setSslPort(getTcpSslPort());
        config.setWebsocketPort(getWebsocketPort());
        config.setSslWebsocketPort(getWebsocketSslPort());
        T server = startServer(config);
        MessageBusMqtt messageBus = new MessageBusMqtt();
        messageBus.init(CoreConfig.builder().build(), config, SERVICE_CONTEXT);
        messageBus.start();
        return new MessageBusInfo(messageBus, server);
    }


    private void assertMessageBusWorks(MessageBusMqttConfig config) throws Exception {
        assertExactTypeSubscription(config);
        assertSuperTypeSubscription(config);
        assertDistinctTypesSubscription(config);
        assertNotMatchingSubscription(config);
        assertUnsubscribeWorks(config);
    }


    private void assertExactTypeSubscription(MessageBusMqttConfig config) throws Exception {
        assertMessage(
                config,
                ElementCreateEventMessage.class,
                ELEMENT_CREATE_MESSAGE, ELEMENT_CREATE_MESSAGE);
    }


    private void assertSuperTypeSubscription(MessageBusMqttConfig config) throws Exception {
        assertMessages(
                config,
                EventMessage.class,
                List.of(ELEMENT_CREATE_MESSAGE, ERROR_MESSAGE),
                List.of(ELEMENT_CREATE_MESSAGE, ERROR_MESSAGE));
    }


    private void assertDistinctTypesSubscription(MessageBusMqttConfig config) throws Exception {
        assertMessages(
                config,
                List.of(ChangeEventMessage.class, ErrorEventMessage.class),
                List.of(ELEMENT_CREATE_MESSAGE, ERROR_MESSAGE),
                Map.of(
                        ChangeEventMessage.class, List.of(ELEMENT_CREATE_MESSAGE),
                        ErrorEventMessage.class, List.of(ERROR_MESSAGE)));
    }


    private void assertNotMatchingSubscription(MessageBusMqttConfig config) throws Exception {
        assertMessage(
                config,
                ErrorEventMessage.class,
                VALUE_CHANGE_MESSAGE,
                null);
    }


    private void assertUnsubscribeWorks(MessageBusMqttConfig config) throws Exception {
        MessageBusInfo messageBusInfo = startMessageBus(config);
        SubscriptionId subscription = messageBusInfo.messageBus.subscribe(SubscriptionInfo.create(
                EventMessage.class,
                x -> {
                    Assert.fail();
                }));
        messageBusInfo.messageBus.unsubscribe(subscription);
        messageBusInfo.messageBus.publish(VALUE_CHANGE_MESSAGE);
        Thread.sleep(1000);
        messageBusInfo.messageBus.stop();
        stopServer(messageBusInfo.server);
    }


    private void assertConnectionFails(MessageBusMqttConfig config) throws InterruptedException, MessageBusException, ConfigurationInitializationException, IOException {
        MessageBusException expection = Assert.assertThrows(MessageBusException.class, () -> startMessageBus(config));
        Assert.assertTrue(expection.getMessage().equals("Failed to connect to MQTT server"));
    }


    private void assertMessage(
                               MessageBusMqttConfig config,
                               Class<? extends EventMessage> subscribeTo,
                               EventMessage toPublish,
                               EventMessage expected)
            throws Exception {
        assertMessages(
                config,
                subscribeTo,
                List.of(toPublish),
                Objects.isNull(expected)
                        ? List.of()
                        : List.of(expected));
    }


    private void assertMessages(
                                MessageBusMqttConfig config,
                                Class<? extends EventMessage> subscribeTo,
                                List<EventMessage> toPublish,
                                List<EventMessage> expected)
            throws Exception {
        assertMessages(
                config,
                List.of(subscribeTo),
                toPublish,
                Objects.isNull(expected) || expected.isEmpty()
                        ? Map.of()
                        : Map.of(subscribeTo, expected));
    }


    private void assertMessages(
                                MessageBusMqttConfig config,
                                List<Class<? extends EventMessage>> subscribeTo,
                                List<EventMessage> toPublish, Map<Class<? extends EventMessage>, List<EventMessage>> expected)
            throws Exception {
        MessageBusInfo messageBusInfo = startMessageBus(config);
        CountDownLatch condition = new CountDownLatch(expected.size());
        final Map<Class<? extends EventMessage>, List<EventMessage>> actual = Collections.synchronizedMap(new HashMap<>());
        List<SubscriptionId> subscriptions = subscribeTo.stream()
                .map(x -> messageBusInfo.messageBus.subscribe(SubscriptionInfo.create(x, e -> {
                    if (!actual.containsKey(x)) {
                        actual.put(x, new ArrayList<>());
                    }
                    actual.get(x).add(e);
                    condition.countDown();
                })))
                .collect(Collectors.toList());
        if (Objects.nonNull(toPublish)) {
            toPublish.forEach(LambdaExceptionHelper.rethrowConsumer(messageBusInfo.messageBus::publish));
        }
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        subscriptions.forEach(messageBusInfo.messageBus::unsubscribe);
        messageBusInfo.messageBus.stop();
        stopServer(messageBusInfo.server);
        Assert.assertEquals(Objects.isNull(expected) ? Map.of() : expected, actual);
    }

    private class MessageBusInfo {

        MessageBusMqtt messageBus;
        T server;

        MessageBusInfo(MessageBusMqtt messageBus, T server) {
            this.messageBus = messageBus;
            this.server = server;
        }

    }
}
