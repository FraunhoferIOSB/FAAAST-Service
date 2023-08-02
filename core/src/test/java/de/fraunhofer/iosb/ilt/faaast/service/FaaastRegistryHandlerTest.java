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

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.mockito.Mockito.*;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


public class FaaastRegistryHandlerTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    private static MessageBus MESSAGE_BUS;
    private static final Persistence PERSISTENCE = Mockito.mock(Persistence.class);
    private static FaaastRegistryHandler faaastRegistryHandler;
    private static AssetAdministrationShellEnvironment environment;

    @BeforeClass
    public static void init() throws Exception {
        MESSAGE_BUS = Mockito.mock(MessageBus.class);
        setupMockedMessagebus();
        setupMockedPersistence();

        faaastRegistryHandler = new FaaastRegistryHandler(MESSAGE_BUS, PERSISTENCE, CoreConfig.builder().build());

    }


    private static void setupMockedMessagebus() throws Exception {
        Answer answer = new Answer() {
            public Integer answer(InvocationOnMock invocation) {
                ElementCreateEventMessage eventMessage = invocation.getArgument(0);
                faaastRegistryHandler.handleCreateEvent(eventMessage);
                return null;
            }
        };
        doAnswer(answer).when(MESSAGE_BUS).publish(any(ElementCreateEventMessage.class));

        answer = new Answer() {
            public Integer answer(InvocationOnMock invocation) {
                ElementUpdateEventMessage eventMessage = invocation.getArgument(0);
                faaastRegistryHandler.handleChangeEvent(eventMessage);
                return null;
            }
        };
        doAnswer(answer).when(MESSAGE_BUS).publish(any(ElementUpdateEventMessage.class));

        answer = new Answer() {
            public Integer answer(InvocationOnMock invocation) {
                ElementDeleteEventMessage eventMessage = invocation.getArgument(0);
                faaastRegistryHandler.handleDeleteEvent(eventMessage);
                return null;
            }
        };
        doAnswer(answer).when(MESSAGE_BUS).publish(any(ElementDeleteEventMessage.class));
    }


    private static void setupMockedPersistence() {
        environment = new DefaultAssetAdministrationShellEnvironment();
        List<AssetAdministrationShell> aasList = new ArrayList<>();
        aasList.add(AASFull.createAAS1());
        environment.setAssetAdministrationShells(aasList);

        when(PERSISTENCE.getEnvironment()).thenReturn(environment);
    }


    @Test
    public void testMessagebusEvent() throws Exception {
        MESSAGE_BUS.publish(ElementCreateEventMessage.builder()
                .element(environment.getAssetAdministrationShells().get(0)).build());
        MESSAGE_BUS.publish(ElementUpdateEventMessage.builder()
                .element(environment.getAssetAdministrationShells().get(0)).build());
        MESSAGE_BUS.publish(ElementDeleteEventMessage.builder()
                .element(environment.getAssetAdministrationShells().get(0)).build());
        //verify(MESSAGE_BUS).publish(any(ChangeEventMessage.class));
    }
}
