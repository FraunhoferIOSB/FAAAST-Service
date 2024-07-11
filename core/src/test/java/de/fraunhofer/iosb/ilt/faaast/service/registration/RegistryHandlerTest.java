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
package de.fraunhofer.iosb.ilt.faaast.service.registration;

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
import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultAssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultSubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


public class RegistryHandlerTest {
    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(options().dynamicPort());
    @Rule
    public WireMockClassRule instanceRule = wireMockRule;

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    private static MessageBus MESSAGE_BUS;
    private static FileStorage FILESTORAGE;
    private static final Persistence PERSISTENCE = Mockito.mock(Persistence.class);
    private static RegistryHandler registryHandler;
    private static Environment environment;
    private static CoreConfig coreConfig;

    @BeforeClass
    public static void init() throws Exception {
        coreConfig = CoreConfig.builder()
                .registryPort(wireMockRule.port())
                .registryHost("localhost")
                .build();
        MESSAGE_BUS = Mockito.mock(MessageBus.class);
        FILESTORAGE = Mockito.mock(FileStorage.class);
        setupMockedMessagebus();
        setupMockedPersistence();

        // Throws registry exception because http request for creation is not mocked
        registryHandler = new RegistryHandler(MESSAGE_BUS, PERSISTENCE, ServiceConfig.builder()
                .core(CoreConfig.builder()
                        .registryPort(wireMockRule.port())
                        .registryHost("localhost")
                        .registryProtocol("HTTP")
                        .build())
                .build());

    }


    private static void setupMockedMessagebus() throws Exception {
        Answer<Void> answer = new Answer<>() {
            public Void answer(InvocationOnMock invocation) {
                ElementCreateEventMessage eventMessage = invocation.getArgument(0);
                try {
                    registryHandler.handleCreateEvent(eventMessage);
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
                    registryHandler.handleChangeEvent(eventMessage);
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
                    registryHandler.handleDeleteEvent(eventMessage);
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
        environment = AASFull.createEnvironment();
        when(PERSISTENCE.getAllAssetAdministrationShells(any(), any()))
                .thenReturn(Page.<AssetAdministrationShell> builder().result(environment.getAssetAdministrationShells()).build());
        List<Submodel> foo = List.of();
        when(PERSISTENCE.getAllSubmodels(any(), any())).thenReturn(Page.<Submodel> builder().result(environment.getSubmodels()).build());
    }


    @Test
    public void testInitialRegistration() throws Exception {
        Service service = new Service(coreConfig, PERSISTENCE, FILESTORAGE, MESSAGE_BUS, new ArrayList<>(), new ArrayList<>());

        for (AssetAdministrationShell aas: environment.getAssetAdministrationShells()) {
            stubFor(post(coreConfig.getAasRegistryBasePath())
                    .withRequestBody(equalToJson(getAasDescriptorBody(aas)))
                    .willReturn(ok()));
        }

        service.start();

        for (AssetAdministrationShell aas: environment.getAssetAdministrationShells()) {
            verify(postRequestedFor(urlEqualTo(coreConfig.getAasRegistryBasePath()))
                    .withRequestBody(equalToJson(getAasDescriptorBody(aas))));
        }
    }


    @Test
    public void testUnregistrationOnExit() throws Exception {
        Service service = new Service(coreConfig, PERSISTENCE, FILESTORAGE, MESSAGE_BUS, new ArrayList<>(), new ArrayList<>());

        for (AssetAdministrationShell aas: environment.getAssetAdministrationShells()) {
            stubFor(post(coreConfig.getAasRegistryBasePath())
                    .withRequestBody(equalToJson(getAasDescriptorBody(aas)))
                    .willReturn(ok()));
        }

        for (AssetAdministrationShell aas: environment.getAssetAdministrationShells()) {
            stubFor(delete(coreConfig.getAasRegistryBasePath() + "/" + getEncodedIdentifier(aas))
                    .willReturn(ok()));
        }

        service.start();
        service.stop();

        for (AssetAdministrationShell aas: environment.getAssetAdministrationShells()) {
            verify(deleteRequestedFor(urlEqualTo(coreConfig.getAasRegistryBasePath() + "/" + getEncodedIdentifier(aas))));
        }
    }


    @Test
    public void testAasCreation() throws Exception {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);

        stubFor(post(coreConfig.getAasRegistryBasePath())
                .withRequestBody(equalToJson(getAasDescriptorBody(aas)))
                .willReturn(ok()));

        MESSAGE_BUS.publish(ElementCreateEventMessage.builder()
                .element(aas).build());

        verify(postRequestedFor(urlEqualTo(coreConfig.getAasRegistryBasePath()))
                .withRequestBody(equalToJson(getAasDescriptorBody(aas))));
    }


    @Test
    public void testAasUpdate() throws Exception {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);
        String oldIdShort = aas.getIdShort();
        aas.setIdShort("Changed Id Short");

        stubFor(put(coreConfig.getAasRegistryBasePath() + "/" + getEncodedIdentifier(aas))
                .willReturn(ok()));

        MESSAGE_BUS.publish(ElementUpdateEventMessage.builder()
                .element(aas).build());

        verify(putRequestedFor(urlEqualTo(coreConfig.getAasRegistryBasePath() + "/" + getEncodedIdentifier(aas)))
                .withRequestBody(equalToJson(getAasDescriptorBody(aas))));

        aas.setIdShort(oldIdShort);
    }


    @Test
    public void testAasDeletion() throws Exception {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);

        stubFor(delete(coreConfig.getAasRegistryBasePath() + "/" + getEncodedIdentifier(aas))
                .willReturn(ok()));

        MESSAGE_BUS.publish(ElementDeleteEventMessage.builder()
                .element(aas).build());

        verify(deleteRequestedFor(urlEqualTo(coreConfig.getAasRegistryBasePath() + "/" + getEncodedIdentifier(aas))));
    }


    @Test
    public void testSubmodelCreation() throws Exception {
        Submodel submodel = environment.getSubmodels().get(0);

        stubFor(post(coreConfig.getSubmodelRegistryBasePath())
                .withRequestBody(equalToJson(getSubmodelDescriptorBody(submodel)))
                .willReturn(ok()));

        MESSAGE_BUS.publish(ElementCreateEventMessage.builder()
                .element(submodel).build());

        verify(postRequestedFor(urlEqualTo(coreConfig.getSubmodelRegistryBasePath()))
                .withRequestBody(equalToJson(getSubmodelDescriptorBody(submodel))));
    }


    @Test
    public void testSubmodelUpdate() throws Exception {
        Submodel submodel = environment.getSubmodels().get(0);
        String oldIdShort = submodel.getIdShort();
        submodel.setIdShort("Changed Id Short");

        stubFor(put(coreConfig.getSubmodelRegistryBasePath() + "/" + getEncodedIdentifier(submodel))
                .willReturn(ok()));

        MESSAGE_BUS.publish(ElementUpdateEventMessage.builder()
                .element(submodel).build());

        verify(putRequestedFor(urlEqualTo(coreConfig.getSubmodelRegistryBasePath() + "/" + getEncodedIdentifier(submodel)))
                .withRequestBody(equalToJson(getSubmodelDescriptorBody(submodel))));

        submodel.setIdShort(oldIdShort);
    }


    @Test
    public void testSubmodelDeletion() throws Exception {
        Submodel submodel = environment.getSubmodels().get(0);

        stubFor(delete(coreConfig.getSubmodelRegistryBasePath() + "/" + getEncodedIdentifier(submodel))
                .willReturn(ok()));

        MESSAGE_BUS.publish(ElementDeleteEventMessage.builder()
                .element(submodel).build());

        verify(deleteRequestedFor(urlEqualTo(coreConfig.getSubmodelRegistryBasePath() + "/" + getEncodedIdentifier(submodel))));
    }


    private String getEncodedIdentifier(AssetAdministrationShell aas) {
        return Base64.getEncoder().encodeToString(aas.getId().getBytes());
    }


    private String getEncodedIdentifier(Submodel submodel) {
        return Base64.getEncoder().encodeToString(submodel.getId().getBytes());
    }


    private String getAasDescriptorBody(AssetAdministrationShell aas) throws Exception {
        return mapper.writeValueAsString(DefaultAssetAdministrationShellDescriptor.builder()
                .from(aas)
                .submodels(getSubmodelDescriptorsFromAas(aas))
                .build());
    }


    private String getSubmodelDescriptorBody(Submodel submodel) throws Exception {
        return mapper.writeValueAsString(DefaultSubmodelDescriptor.builder()
                .from(submodel)
                .build());
    }


    private List<SubmodelDescriptor> getSubmodelDescriptorsFromAas(AssetAdministrationShell aas) {
        List<SubmodelDescriptor> submodelDescriptors = new ArrayList<>();
        for (Reference submodelReference: aas.getSubmodels())
            submodelDescriptors.add(DefaultSubmodelDescriptor.builder().from(
                    getSubmodelFromIdentifier(submodelReference.getKeys().get(0).getValue())).build());
        return submodelDescriptors;
    }


    private Submodel getSubmodelFromIdentifier(String identifier) {
        for (Submodel submodel: environment.getSubmodels()) {
            if (submodel.getId().equals(identifier))
                return submodel;
        }
        throw new IllegalArgumentException("Identifier not found!");
    }

}
