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
package de.fraunhofer.iosb.ilt.faaast.service.test.util;

import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionId;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementChangeEventMessage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.junit.Assert;


/**
 * Helper class for interaction with {@link MessageBus}
 */
public class MessageBusHelper {

    /**
     * Default timeout used when waiting for messages on the message bus to
     * arrive.
     */
    public static final long DEFAULT_TIMEOUT = 5000;

    private MessageBusHelper() {}


    /**
     * Executes a trigger, waits for matching event on message bus and
     * optionally checks if the event contains expected content.
     *
     * @param messageBus the message bus to use
     * @param eventType the event type to wait for; can be a concrete type or an
     *            abstract super type
     * @param expected expected content of the received event message. If null
     *            content is not checked
     * @param trigger code that should be executed to trigger an event message
     *            being sent on the message bus
     * @throws InterruptedException if no matching event if received before
     *             timeout
     * @throws MessageBusException if un-/subscribing from/to the message bus
     *             fails
     */
    public static void assertEvent(MessageBus messageBus, Class<? extends EventMessage> eventType, Object expected, Consumer<Void> trigger)
            throws InterruptedException, MessageBusException {
        assertEvent(messageBus, eventType, expected, trigger, DEFAULT_TIMEOUT);
    }


    /**
     * Executes a trigger, waits for matching event on message bus and
     * optionally checks if the event contains expected content.
     *
     * @param messageBus the message bus to use
     * @param eventType the event type to wait for; can be a concrete type or an
     *            abstract super type
     * @param expected expected content of the received event message. If null
     *            content is not checked
     * @param trigger code that should be executed to trigger an event message
     *            being sent on the message bus
     * @param timeout timeout (in milliseconds)
     * @throws InterruptedException if no matching event if received before
     *             timeout
     * @throws MessageBusException if un-/subscribing from/to the message bus
     *             fails
     */
    public static void assertEvent(MessageBus messageBus, Class<? extends EventMessage> eventType, Object expected, Consumer<Void> trigger, long timeout)
            throws InterruptedException, MessageBusException {
        CountDownLatch condition = new CountDownLatch(1);
        AtomicBoolean eventReceived = new AtomicBoolean(false);
        SubscriptionId subscriptionId = messageBus.subscribe(SubscriptionInfo.create(eventType, x -> {
            if (ReadEventMessage.class.isAssignableFrom(x.getClass())) {
                Assert.assertEquals(expected, ((ReadEventMessage) x).getValue());
            }
            if (ElementChangeEventMessage.class.isAssignableFrom(x.getClass())) {
                Assert.assertEquals(expected, ((ElementChangeEventMessage) x).getValue());
            }
            eventReceived.set(true);
            condition.countDown();
        }));
        trigger.accept(null);
        condition.await(timeout, TimeUnit.MILLISECONDS);
        messageBus.unsubscribe(subscriptionId);
        Assert.assertTrue(String.format("%s expected on message bus but none received", eventType.getSimpleName()), eventReceived.get());
    }
}
