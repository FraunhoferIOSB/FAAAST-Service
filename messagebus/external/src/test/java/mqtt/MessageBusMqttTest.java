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
package mqtt;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.mqtt.MessageBusMqtt;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.mqtt.MessageBusMqttConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.error.ErrorEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.error.ErrorLevel;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.IntValue;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.*;


public class MessageBusMqttTest {

    private static ValueChangeEventMessage valueChangeMessage;
    private static ElementCreateEventMessage elementCreateMessage;
    private static final ServiceContext SERVICE_CONTEXT = Mockito.mock(ServiceContext.class);
    private static ErrorEventMessage errorMessage;
    // default timeout in milliseconds
    private static final long DEFAULT_TIMEOUT = 1000;

    private static final Property property1 = new DefaultProperty.Builder()
            .idShort("foo")
            .valueType(Datatype.STRING.getName())
            .value("bar")
            .build();

    private static final Reference property1Reference = new DefaultReference.Builder()
            .key(new DefaultKey.Builder()
                    .type(KeyElements.PROPERTY)
                    .idType(KeyType.ID_SHORT)
                    .value("property1")
                    .build())
            .build();

    private static MessageBusMqttConfig internalMessageBusMqttConfig;

    @BeforeClass
    public static void init() {
        valueChangeMessage = new ValueChangeEventMessage();
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.setValue(new IntValue(100));
        valueChangeMessage.setOldValue(propertyValue);
        propertyValue.setValue(new IntValue(123));
        valueChangeMessage.setNewValue(propertyValue);

        elementCreateMessage = ElementCreateEventMessage.builder()
                .element(property1Reference)
                .value(property1)
                .build();

        errorMessage = new ErrorEventMessage();
        errorMessage.setElement(property1Reference);
        errorMessage.setLevel(ErrorLevel.ERROR);
        initMessageBusMqttConfig();
    }


    private static void initMessageBusMqttConfig() {
        internalMessageBusMqttConfig = MessageBusMqttConfig.builder()
                .internal(true)
                .brokerKeystorePath("src/test/resources/serverkeystore.jks")
                .brokerKeystorePass("password")
                .passwordFile("src/test/resources/password_file.conf")
                .username("user")
                .password("password")
                .clientKeystorePath("src/test/resources/clientkeystore.jks")
                .clientKeystorePass("password")
                .build();
    }


    @Test
    public void testExactTypeSubscription() throws InterruptedException {
        MessageBusMqtt messageBus = new MessageBusMqtt();

        try {
            messageBus.init(CoreConfig.builder().build(), internalMessageBusMqttConfig, SERVICE_CONTEXT);
        }
        catch (ConfigurationInitializationException e) {
            throw new RuntimeException(e);
        }
        messageBus.start();
        CountDownLatch condition = new CountDownLatch(1);
        final AtomicReference<EventMessage> response = new AtomicReference<>();
        messageBus.subscribe(SubscriptionInfo.create(
                ElementCreateEventMessage.class,
                x -> {
                    response.set(x);
                    condition.countDown();
                }));
        messageBus.publish(elementCreateMessage);
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(elementCreateMessage, response.get());
        messageBus.stop();
    }


    @Test
    public void testSuperTypeSubscription() throws InterruptedException {
        MessageBusMqtt messageBus = new MessageBusMqtt();
        try {
            messageBus.init(CoreConfig.builder().build(), internalMessageBusMqttConfig, SERVICE_CONTEXT); // TODO this config was not internal.
        }
        catch (ConfigurationInitializationException e) {
            throw new RuntimeException(e);
        }
        messageBus.start();
        Set<EventMessage> messages = Set.of(elementCreateMessage, errorMessage);
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
        ArrayList<EventMessage> expected = new ArrayList<>(messages);
        ArrayList<EventMessage> actual = new ArrayList<>(responses);
        messageBus.stop();
        Assert.assertTrue(expected.size() == actual.size() && expected.containsAll(actual) && actual.containsAll(expected));
    }


    @Test
    public void testDistinctTypesSubscription() throws InterruptedException {
        MessageBusMqtt messageBus = new MessageBusMqtt();
        try {
            messageBus.init(CoreConfig.builder().build(), internalMessageBusMqttConfig, SERVICE_CONTEXT);
        }
        catch (ConfigurationInitializationException e) {
            throw new RuntimeException(e);
        }
        messageBus.start();
        Map<Class<? extends EventMessage>, Set<EventMessage>> messages = Map.of(
                ChangeEventMessage.class, Set.of(elementCreateMessage),
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

    /*
     * @Test
     * public void testValueChangeTypeSubscription() throws InterruptedException {
     * MessageBusMqtt messageBus = new MessageBusMqtt();
     * try {
     * messageBus.init(CoreConfig.builder().build(), internalMessageBusMqttConfig,
     * SERVICE_CONTEXT);
     * }
     * catch (ConfigurationInitializationException e) {
     * throw new RuntimeException(e);
     * }
     * messageBus.start();
     * Map<Class<? extends EventMessage>, Set<EventMessage>> messages = Map.of(
     * ChangeEventMessage.class, Set.of(valueChangeMessage),
     * ErrorEventMessage.class, Set.of(errorMessage));
     * Map<Class<? extends EventMessage>, Set<EventMessage>> responses = Collections.synchronizedMap(Map.of(
     * ChangeEventMessage.class, new HashSet<>(),
     * ErrorEventMessage.class, new HashSet<>()));
     * CountDownLatch condition = new CountDownLatch(messages.values().stream().mapToInt(x -> x.size()).sum());
     * responses.entrySet().forEach(entry -> messageBus.subscribe(
     * SubscriptionInfo.create(
     * entry.getKey(),
     * x -> {
     * entry.getValue().add(x);
     * condition.countDown();
     * })));
     * messages.values().stream().flatMap(x -> x.stream()).forEach(x -> {
     * try {
     * messageBus.publish(x);
     * }
     * catch (Exception e) {
     * Assert.fail();
     * }
     * });
     * condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
     * Assert.assertEquals(messages, responses);
     * messageBus.stop();
     * }
     */


    @Test
    public void testNotMatchingSubscription() throws InterruptedException {
        MessageBusMqtt messageBus = new MessageBusMqtt();
        try {
            messageBus.init(CoreConfig.builder().build(), internalMessageBusMqttConfig, SERVICE_CONTEXT);
        }
        catch (ConfigurationInitializationException e) {
            throw new RuntimeException(e);
        }
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
    public void testSubscribeUnsubscribe() throws InterruptedException {
        MessageBusMqtt messageBus = new MessageBusMqtt();
        try {
            messageBus.init(CoreConfig.builder().build(), internalMessageBusMqttConfig, SERVICE_CONTEXT);
        }
        catch (ConfigurationInitializationException e) {
            throw new RuntimeException(e);
        }
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
}
