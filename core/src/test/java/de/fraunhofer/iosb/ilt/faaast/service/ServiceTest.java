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
package de.fraunhofer.iosb.ilt.faaast.service;

import static org.mockito.Mockito.*;

import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


public class ServiceTest {
    private static MessageBus messageBus;
    private static Service service;

    @BeforeClass
    public static void init() throws Exception {
        ServiceConfig config = ServiceConfig.builder().build();

        service = new Service(config);
        service.start();
    }


    @Test
    public void testMessagebusEvent() throws Exception {
        /*ChangeEventMessage event = ElementDeleteEventMessage.builder().build();
        Answer answer = new Answer() {
            public Integer answer(InvocationOnMock invocation) {
                ChangeEventMessage eventMessage = invocation.getArgument(0);
                return null;
            }
        };
        doAnswer(answer).when(messageBus).publish(any(ChangeEventMessage.class));

        messageBus.publish(event);
        verify(messageBus).publish(any(ChangeEventMessage.class));*/
    }

}
