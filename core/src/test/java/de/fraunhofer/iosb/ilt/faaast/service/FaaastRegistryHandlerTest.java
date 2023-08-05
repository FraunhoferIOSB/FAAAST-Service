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

import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultAssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


public class FaaastRegistryHandlerTest {
    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(options().dynamicPort());
    @Rule
    public WireMockClassRule instanceRule = wireMockRule;

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    private static MessageBus MESSAGE_BUS;
    private static final Persistence PERSISTENCE = Mockito.mock(Persistence.class);
    private static FaaastRegistryHandler faaastRegistryHandler;
    private static AssetAdministrationShellEnvironment environment;
    private static CoreConfig coreConfig;

    @BeforeClass
    public static void init() throws Exception {
        coreConfig = CoreConfig.builder()
                .registryPort(wireMockRule.port())
                .registryHost("localhost")
                .build();
        MESSAGE_BUS = Mockito.mock(MessageBus.class);
        setupMockedMessagebus();
        setupMockedPersistence();

        faaastRegistryHandler = new FaaastRegistryHandler(MESSAGE_BUS, PERSISTENCE,
                CoreConfig.builder()
                        .registryPort(wireMockRule.port())
                        .registryHost("localhost")
                        .build());

    }


    private static void setupMockedMessagebus() throws Exception {
        Answer<Void> answer = new Answer<>() {
            public Void answer(InvocationOnMock invocation) {
                ElementCreateEventMessage eventMessage = invocation.getArgument(0);
                try {
                    faaastRegistryHandler.handleCreateEvent(eventMessage);
                }
                catch (Exception e) {
                    fail();
                }
                return null;
            }
        };
        doAnswer(answer).when(MESSAGE_BUS).publish(any(ElementCreateEventMessage.class));

        answer = new Answer<>() {
            public Void answer(InvocationOnMock invocation) {
                ElementUpdateEventMessage eventMessage = invocation.getArgument(0);
                try {
                    faaastRegistryHandler.handleChangeEvent(eventMessage);
                }
                catch (Exception e) {
                    fail();
                }
                return null;
            }
        };
        doAnswer(answer).when(MESSAGE_BUS).publish(any(ElementUpdateEventMessage.class));

        answer = new Answer<>() {
            public Void answer(InvocationOnMock invocation) {
                ElementDeleteEventMessage eventMessage = invocation.getArgument(0);
                try {
                    faaastRegistryHandler.handleDeleteEvent(eventMessage);
                }
                catch (Exception e) {
                    fail();
                }
                return null;
            }
        };
        doAnswer(answer).when(MESSAGE_BUS).publish(any(ElementDeleteEventMessage.class));
    }


    private static void setupMockedPersistence() {
        environment = new DefaultAssetAdministrationShellEnvironment();
        List<AssetAdministrationShell> aasList = new ArrayList<>();
        aasList.add(AASFull.createAAS1());
        aasList.add(AASFull.createAAS2());
        environment.setAssetAdministrationShells(aasList);

        when(PERSISTENCE.getEnvironment()).thenReturn(environment);
    }


    @Test
    public void testInitialRegistration() throws Exception {
        Service service = new Service(coreConfig, PERSISTENCE, MESSAGE_BUS, new ArrayList<>(), new ArrayList<>());

        for (AssetAdministrationShell aas: environment.getAssetAdministrationShells()) {
            stubFor(post(coreConfig.getRegistryBasePath())
                    .withRequestBody(equalToJson(getDescriptorBody(aas)))
                    .willReturn(ok()));
        }

        service.start();

        for (AssetAdministrationShell aas: environment.getAssetAdministrationShells()) {
            verify(postRequestedFor(urlEqualTo(coreConfig.getRegistryBasePath()))
                    .withRequestBody(equalToJson(getDescriptorBody(aas))));
        }
    }


    @Test
    public void testUnregistrationOnExit() throws Exception {
        Service service = new Service(coreConfig, PERSISTENCE, MESSAGE_BUS, new ArrayList<>(), new ArrayList<>());

        for (AssetAdministrationShell aas: environment.getAssetAdministrationShells()) {
            stubFor(post(coreConfig.getRegistryBasePath())
                    .withRequestBody(equalToJson(getDescriptorBody(aas)))
                    .willReturn(ok()));
        }

        for (AssetAdministrationShell aas: environment.getAssetAdministrationShells()) {
            stubFor(delete(coreConfig.getRegistryBasePath() + "/" + getEncodedAasIdentifier(aas))
                    .willReturn(ok()));
        }

        service.start();
        service.stop();

        for (AssetAdministrationShell aas: environment.getAssetAdministrationShells()) {
            verify(deleteRequestedFor(urlEqualTo(coreConfig.getRegistryBasePath() + "/" + getEncodedAasIdentifier(aas))));
        }
    }


    @Test
    public void testAasCreation() throws Exception {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);

        stubFor(post(coreConfig.getRegistryBasePath())
                .withRequestBody(equalToJson(getDescriptorBody(aas)))
                .willReturn(ok()));

        MESSAGE_BUS.publish(ElementCreateEventMessage.builder()
                .element(aas).build());

        verify(postRequestedFor(urlEqualTo(coreConfig.getRegistryBasePath()))
                .withRequestBody(equalToJson(getDescriptorBody(aas))));
    }


    @Test
    public void testAasUpdate() throws Exception {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);

        stubFor(put(coreConfig.getRegistryBasePath() + "/" + getEncodedAasIdentifier(aas))
                .willReturn(ok()));

        MESSAGE_BUS.publish(ElementUpdateEventMessage.builder()
                .element(environment.getAssetAdministrationShells().get(0)).build());

        verify(putRequestedFor(urlEqualTo(coreConfig.getRegistryBasePath() + "/" + getEncodedAasIdentifier(aas))));
    }


    @Test
    public void testAasDeletion() throws Exception {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);

        stubFor(delete(coreConfig.getRegistryBasePath() + "/" + getEncodedAasIdentifier(aas))
                .willReturn(ok()));

        MESSAGE_BUS.publish(ElementDeleteEventMessage.builder()
                .element(aas).build());

        verify(deleteRequestedFor(urlEqualTo(coreConfig.getRegistryBasePath() + "/" + getEncodedAasIdentifier(aas))));
    }


    private String getEncodedAasIdentifier(AssetAdministrationShell aas) {
        return Base64.getEncoder().encodeToString(aas.getIdentification().getIdentifier().getBytes());
    }


    private String getDescriptorBody(AssetAdministrationShell aas) throws Exception {
        return mapper.writeValueAsString(DefaultAssetAdministrationShellDescriptor.builder().from(aas).build());
    }
}
