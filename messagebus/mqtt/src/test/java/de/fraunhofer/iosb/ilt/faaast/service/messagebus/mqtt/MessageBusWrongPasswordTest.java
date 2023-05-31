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
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.*;
import org.mockito.Mockito;


public class MessageBusWrongPasswordTest {

    private static final ServiceContext SERVICE_CONTEXT = Mockito.mock(ServiceContext.class);
    private static final long DEFAULT_TIMEOUT = 1000;

    private static final MessageBusMqttConfig CONFIG = MessageBusMqttConfig.builder()
            .internal(true)
            .serverKeystorePath("src/test/resources/serverkeystore.jks")
            .serverKeystorePassword("password")
            .passwordFile("src/test/resources/password_file.conf")
            .username("user")
            .password("passwordd")
            .clientKeystorePath("src/test/resources/clientkeystore.jks")
            .clientKeystorePassword("password")
            .build();

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

    private MessageBusMqtt messageBus;

    private static int findFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Assert.assertNotNull(serverSocket);
            Assert.assertTrue(serverSocket.getLocalPort() > 0);
            return serverSocket.getLocalPort();
        }
    }


    @BeforeClass
    public static void init() throws IOException {
        CONFIG.setPort(findFreePort());
        CONFIG.setSslPort(findFreePort());
        CONFIG.setWebsocketPort(findFreePort());
        CONFIG.setSslWebsocketPort(findFreePort());
    }


    @Before
    public void startMessageBus() throws ConfigurationInitializationException, MessageBusException {
        messageBus = new MessageBusMqtt();
        messageBus.init(CoreConfig.builder().build(), CONFIG, SERVICE_CONTEXT);
        messageBus.start();
    }


    @After
    public void stopMessageBus() throws ConfigurationInitializationException, MessageBusException {
        messageBus.stop();
    }


    @Test
    public void testWrongPassword() throws InterruptedException, MessageBusException, ConfigurationInitializationException {
        //this fails if the broker allows connections with wrong passwords
        assertMessages(
                List.of(ChangeEventMessage.class, ErrorEventMessage.class),
                List.of(ELEMENT_CREATE_MESSAGE, ERROR_MESSAGE),
                Map.of());
    }


    private void assertMessages(List<Class<? extends EventMessage>> subscribeTo, List<EventMessage> toPublish, Map<Class<? extends EventMessage>, List<EventMessage>> expected)
            throws ConfigurationInitializationException, MessageBusException, InterruptedException {
        CountDownLatch condition = new CountDownLatch(2);
        final Map<Class<? extends EventMessage>, List<EventMessage>> actual = Collections.synchronizedMap(new HashMap<>());
        List<SubscriptionId> subscriptions = subscribeTo.stream()
                .map(x -> messageBus.subscribe(SubscriptionInfo.create(x, e -> {
                    if (!actual.containsKey(x)) {
                        actual.put(x, new ArrayList<>());
                    }
                    actual.get(x).add(e);
                    condition.countDown();
                })))
                .collect(Collectors.toList());
        if (Objects.nonNull(toPublish)) {
            toPublish.forEach(LambdaExceptionHelper.rethrowConsumer(messageBus::publish));
        }
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        subscriptions.forEach(messageBus::unsubscribe);
        Assert.assertEquals(Objects.isNull(expected) ? Map.of() : expected, actual);
    }
}
