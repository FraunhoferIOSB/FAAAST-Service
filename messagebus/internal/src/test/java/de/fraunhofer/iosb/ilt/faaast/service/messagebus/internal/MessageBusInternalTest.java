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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal;

import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.error.ErrorEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.error.ErrorLevel;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.IntValue;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
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


public class MessageBusInternalTest {

    private static ValueChangeEventMessage valueChangeMessage;
    private static ErrorEventMessage errorMessage;
    // default timeout in milliseconds
    private static final long DEFAULT_TIMEOUT = 1000;

    private static final Reference property1Reference = new DefaultReference.Builder()
            .key(new DefaultKey.Builder()
                    .type(KeyElements.PROPERTY)
                    .idType(KeyType.ID_SHORT)
                    .value("property1")
                    .build())
            .build();

    @BeforeClass
    public static void init() {
        valueChangeMessage = new ValueChangeEventMessage();
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.setValue(new IntValue(100));
        valueChangeMessage.setOldValue(propertyValue);
        propertyValue.setValue(new IntValue(123));
        valueChangeMessage.setNewValue(propertyValue);

        errorMessage = new ErrorEventMessage();
        errorMessage.setElement(property1Reference);
        errorMessage.setErrorLevel(ErrorLevel.ERROR);
        errorMessage.setThrowingSource(MessageBusInternalTest.class);
    }


    @Test
    public void testExactTypeSubscription() throws InterruptedException, MessageBusException {
        MessageBusInternal messageBus = new MessageBusInternal();
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
    public void testSuperTypeSubscription() throws InterruptedException {
        MessageBusInternal messageBus = new MessageBusInternal();
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
    public void testDistinctTypesSubscription() throws InterruptedException {
        MessageBusInternal messageBus = new MessageBusInternal();
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
    public void testNotMatchingSubscription() throws InterruptedException {
        MessageBusInternal messageBus = new MessageBusInternal();
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
        MessageBusInternal messageBus = new MessageBusInternal();
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
