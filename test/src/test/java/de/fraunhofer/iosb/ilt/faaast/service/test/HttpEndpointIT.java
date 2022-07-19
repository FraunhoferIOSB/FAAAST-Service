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
package de.fraunhofer.iosb.ilt.faaast.service.test;

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper.toHttpStatusCode;
import static de.fraunhofer.iosb.ilt.faaast.service.test.util.MessageBusHelper.assertEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.util.Path;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.ApiPaths;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ExtendHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.AssetInformation;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShell;
import io.adminshell.aas.v3.model.impl.DefaultConceptDescription;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultIdentifierKeyValuePair;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;


public class HttpEndpointIT {

    public static MessageBus messageBus;

    private static final String HOST = "http://localhost";
    private static int PORT;
    private static ApiPaths API_PATHS;
    private static AssetAdministrationShellEnvironment environment;
    private static Service service;
    private final ObjectMapper mapper;

    public HttpEndpointIT() {
        this.mapper = new ObjectMapper();
    }


    private static int findFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Assert.assertNotNull(serverSocket);
            Assert.assertTrue(serverSocket.getLocalPort() > 0);
            return serverSocket.getLocalPort();
        }
    }


    @BeforeClass
    public static void initClass() throws IOException {
        PORT = findFreePort();
        API_PATHS = new ApiPaths(HOST, PORT);
    }


    @Before
    public void init() throws Exception {
        environment = AASFull.createEnvironment();
        ServiceConfig serviceConfig = ServiceConfig.builder()
                .core(CoreConfig.builder()
                        .requestHandlerThreadPoolSize(2)
                        .build())
                .persistence(PersistenceInMemoryConfig.builder()
                        .environment(environment)
                        .build())
                .endpoints(List.of(HttpEndpointConfig.builder()
                        .port(PORT)
                        .build()))
                .messageBus(MessageBusInternalConfig.builder()
                        .build())
                .build();
        service = new Service(serviceConfig);
        messageBus = service.getMessageBus();
        service.start();
    }


    @After
    public void shutdown() {
        service.stop();
    }


    @Test
    public void testAASBasicDiscoveryCreate() throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);
        IdentifierKeyValuePair newIdentifier = new DefaultIdentifierKeyValuePair.Builder()
                .key("foo")
                .value("bar")
                .build();
        List<IdentifierKeyValuePair> expected = new ArrayList<>();
        expected.add(newIdentifier);
        expected.addAll(aas.getAssetInformation().getSpecificAssetIds());
        expected.add(new DefaultIdentifierKeyValuePair.Builder()
                .key(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(aas.getAssetInformation().getGlobalAssetId().getKeys().get(aas.getAssetInformation().getGlobalAssetId().getKeys().size() - 1).getValue())
                .build());
        assertExecuteMultiple(
                HttpMethod.POST,
                API_PATHS.aasBasicDiscovery().assetAdministrationShell(aas),
                StatusCode.SUCCESS_CREATED,
                List.of(newIdentifier),
                expected,
                IdentifierKeyValuePair.class);
    }


    @Test
    public void testAASBasicDiscoveryDelete() throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);
        aas.getAssetInformation().getSpecificAssetIds().clear();
        aas.getAssetInformation().setGlobalAssetId(null);
        assertExecute(
                HttpMethod.DELETE,
                API_PATHS.aasBasicDiscovery().assetAdministrationShell(aas),
                StatusCode.SUCCESS_NO_CONTENT);
        assertExecuteMultiple(
                HttpMethod.GET,
                API_PATHS.aasBasicDiscovery().assetAdministrationShell(aas),
                StatusCode.SUCCESS,
                null,
                List.of(),
                IdentifierKeyValuePair.class);
    }


    @Test
    public void testAASBasicDiscoveryGetAssetAdministrationShells()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        Object expected = environment.getAssetAdministrationShells().stream()
                .map(x -> x.getIdentification())
                .collect(Collectors.toList());
        assertExecuteMultiple(
                HttpMethod.GET,
                API_PATHS.aasBasicDiscovery().assetAdministrationShells(),
                StatusCode.SUCCESS,
                null,
                expected,
                Identifier.class);
    }


    @Test
    public void testAASBasicDiscoveryGetAssetAdministrationShellsByGlobalAssetId()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        String assetIdValue = "https://acplt.org/Test_Asset";
        List<Identifier> expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getAssetInformation().getGlobalAssetId().getKeys().stream()
                        .anyMatch(y -> y.getValue().equalsIgnoreCase(assetIdValue)))
                .map(x -> x.getIdentification())
                .collect(Collectors.toList());
        assertExecuteMultiple(HttpMethod.GET,
                API_PATHS.aasBasicDiscovery().assetAdministrationShells(Map.of(FaaastConstants.KEY_GLOBAL_ASSET_ID, assetIdValue)),
                StatusCode.SUCCESS,
                null,
                expected,
                Identifier.class);
    }


    @Test
    public void testAASBasicDiscoveryGetAssetLinks()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);
        List<IdentifierKeyValuePair> expected = new ArrayList<>(aas.getAssetInformation().getSpecificAssetIds());
        expected.add(new DefaultIdentifierKeyValuePair.Builder()
                .key(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(aas.getAssetInformation().getGlobalAssetId().getKeys().get(aas.getAssetInformation().getGlobalAssetId().getKeys().size() - 1).getValue())
                .build());
        assertExecuteMultiple(
                HttpMethod.GET,
                API_PATHS.aasBasicDiscovery().assetAdministrationShell(aas),
                StatusCode.SUCCESS,
                null,
                expected,
                IdentifierKeyValuePair.class);
    }


    @Test
    public void testAASRepositoryCreateAssetAdministrationShells()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        AssetAdministrationShell expected = new DefaultAssetAdministrationShell.Builder()
                .identification(new DefaultIdentifier.Builder()
                        .identifier("http://newOne")
                        .idType(IdentifierType.IRI)
                        .build())
                .idShort("newOne")
                .description(new LangString("Täst"))
                .build();
        assertEvent(
                messageBus,
                ElementCreateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.POST,
                                API_PATHS.aasRepository().assetAdministrationShells(),
                                StatusCode.SUCCESS_CREATED,
                                expected,
                                expected,
                                AssetAdministrationShell.class)));
        Assert.assertTrue(HttpHelper.getWithMultipleResult(
                API_PATHS.aasRepository().assetAdministrationShells(),
                AssetAdministrationShell.class)
                .contains(expected));
    }


    @Test
    public void testAASRepositoryDeleteAssetAdministrationShell()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        List<AssetAdministrationShell> before = HttpHelper.getWithMultipleResult(
                API_PATHS.aasRepository().assetAdministrationShells(),
                AssetAdministrationShell.class);
        Assert.assertTrue(before.contains(expected));
        assertEvent(
                messageBus,
                ElementDeleteEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecute(HttpMethod.DELETE,
                                API_PATHS.aasRepository().assetAdministrationShell(expected),
                                StatusCode.SUCCESS_NO_CONTENT)));
        List<AssetAdministrationShell> after = HttpHelper.getWithMultipleResult(
                API_PATHS.aasRepository().assetAdministrationShells(),
                AssetAdministrationShell.class);
        Assert.assertFalse(after.contains(expected));
    }


    @Test
    public void testAASRepositoryDeleteAssetAdministrationShellNotExists()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        HttpResponse<String> response = HttpHelper.delete(API_PATHS.aasRepository().assetAdministrationShell("non-existant"));
        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND), response.statusCode());
    }


    @Test
    public void testAASRepositoryGetAssetAdministrationShell()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                API_PATHS.aasRepository().assetAdministrationShell(expected),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                AssetAdministrationShell.class)));
    }


    @Test
    public void testAASRepositoryGetAssetAdministrationShellNotExists()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        HttpResponse<String> response = HttpHelper.get(API_PATHS.aasRepository().assetAdministrationShell("non-existant"));
        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND), response.statusCode());
    }


    @Test
    public void testAASRepositoryGetAssetAdministrationShells() throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        Object expected = environment.getAssetAdministrationShells();
        assertExecuteMultiple(
                HttpMethod.GET,
                API_PATHS.aasRepository().assetAdministrationShells(),
                StatusCode.SUCCESS,
                null,
                expected,
                AssetAdministrationShell.class);
    }


    @Test
    public void testAASRepositoryUpdateAssetAdministrationShell()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        expected.setIdShort("changed");
        assertEvent(
                messageBus,
                ElementUpdateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.PUT,
                                API_PATHS.aasRepository().assetAdministrationShell(expected),
                                StatusCode.SUCCESS,
                                expected,
                                expected,
                                AssetAdministrationShell.class)));
    }


    @Test
    public void testAssetAdministrationShellInterfaceCreateSubmodel()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        List<Reference> expected = aas.getSubmodels();
        Reference newReference = new DefaultReference.Builder().key(new DefaultKey.Builder().value("test").idType(KeyType.IRI).build()).build();
        expected.add(newReference);
        assertEvent(
                messageBus,
                ElementUpdateEventMessage.class,
                aas,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.POST,
                                API_PATHS.aasInterface(aas).submodels(),
                                StatusCode.SUCCESS_CREATED,
                                newReference,
                                newReference,
                                Reference.class)));
        assertExecuteMultiple(
                HttpMethod.GET,
                API_PATHS.aasInterface(aas).submodels(),
                StatusCode.SUCCESS,
                null,
                expected,
                Reference.class);
    }


    @Test
    public void testAssetAdministrationShellInterfaceDeleteSubmodel()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Reference submodelToDelete = aas.getSubmodels().get(0);
        aas.getSubmodels().remove(submodelToDelete);
        List<Reference> before = HttpHelper.getWithMultipleResult(
                API_PATHS.aasInterface(aas).submodels(),
                Reference.class);
        Assert.assertTrue(before.contains(submodelToDelete));
        assertEvent(
                messageBus,
                EventMessage.class,
                aas,
                LambdaExceptionHelper.wrap(
                        x -> assertExecute(
                                HttpMethod.DELETE,
                                API_PATHS.aasInterface(aas).submodel(submodelToDelete),
                                StatusCode.SUCCESS_NO_CONTENT)));
        List<Reference> after = HttpHelper.getWithMultipleResult(
                API_PATHS.aasInterface(aas).submodels(),
                Reference.class);
        Assert.assertFalse(after.contains(submodelToDelete));
    }


    @Test
    public void testAssetAdministrationShellInterfaceGetAssetAdministrationShell()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                API_PATHS.aasInterface(expected).assetAdministrationShell(),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                AssetAdministrationShell.class)));
    }


    @Test
    public void testAssetAdministrationShellInterfaceGetAssetAdministrationShellNotExists()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        HttpResponse<String> response = HttpHelper.get(API_PATHS.aasInterface("non-existant").assetAdministrationShell());
        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND), response.statusCode());
    }


    @Test
    public void testAssetAdministrationShellInterfaceGetAssetInformation()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        AssetInformation expected = aas.getAssetInformation();
        // TODO does this trigger any message bus event?
        assertExecuteSingle(
                HttpMethod.GET,
                API_PATHS.aasInterface(aas).assetInformation(),
                StatusCode.SUCCESS,
                null,
                expected,
                AssetInformation.class);
    }


    @Test
    public void testAssetAdministrationShellInterfaceGetSubmodels()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Object expected = aas.getSubmodels();
        assertExecuteMultiple(
                HttpMethod.GET,
                API_PATHS.aasInterface(aas).submodels(),
                StatusCode.SUCCESS,
                null,
                expected,
                Reference.class);
    }


    @Test
    public void testAssetAdministrationShellInterfaceUpdateAssetAdministrationShell()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        expected.setIdShort("changed");
        assertEvent(
                messageBus,
                ElementUpdateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.PUT,
                                API_PATHS.aasRepository().assetAdministrationShell(expected),
                                StatusCode.SUCCESS,
                                expected,
                                expected,
                                AssetAdministrationShell.class)));
    }


    @Test
    public void testAssetAdministrationShellInterfaceUpdateAssetInformation()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        AssetInformation expected = aas.getAssetInformation();
        expected.getSpecificAssetIds().add(new DefaultIdentifierKeyValuePair.Builder()
                .key("foo")
                .value("bar")
                .build());
        // TODO does this trigger any message bus event?
        assertExecuteSingle(
                HttpMethod.PUT,
                API_PATHS.aasInterface(aas).assetInformation(),
                StatusCode.SUCCESS_NO_CONTENT,
                expected,
                null,
                AssetInformation.class);
    }


    @Test
    public void testConceptDescriptionRepositoryCreateConceptDescription()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        ConceptDescription expected = new DefaultConceptDescription.Builder()
                .identification(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.CUSTOM)
                        .identifier("http://example.org/foo")
                        .build())
                .idShort("created")
                .build();
        assertEvent(
                messageBus,
                ElementCreateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.POST,
                                API_PATHS.conceptDescriptionRepository().conceptDescriptions(),
                                StatusCode.SUCCESS_CREATED,
                                expected,
                                expected,
                                ConceptDescription.class)));
        Assert.assertTrue(HttpHelper.getWithMultipleResult(
                API_PATHS.conceptDescriptionRepository().conceptDescriptions(),
                ConceptDescription.class)
                .contains(expected));
    }


    @Test
    public void testConceptDescriptionRepositoryDeleteConceptDescription()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        ConceptDescription expected = environment.getConceptDescriptions().get(0);
        List<ConceptDescription> before = HttpHelper.getWithMultipleResult(
                API_PATHS.conceptDescriptionRepository().conceptDescriptions(),
                ConceptDescription.class);
        Assert.assertTrue(before.contains(expected));
        assertEvent(
                messageBus,
                ElementDeleteEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecute(
                                HttpMethod.DELETE,
                                API_PATHS.conceptDescriptionRepository().conceptDescription(expected),
                                StatusCode.SUCCESS_NO_CONTENT)));
        List<ConceptDescription> after = HttpHelper.getWithMultipleResult(
                API_PATHS.conceptDescriptionRepository().conceptDescriptions(),
                ConceptDescription.class);
        Assert.assertFalse(after.contains(expected));
    }


    @Test
    public void testConceptDescriptionRepositoryDeleteConceptDescriptionNotExists()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        HttpResponse<String> response = HttpHelper.delete(API_PATHS.conceptDescriptionRepository().conceptDescription("non-existant"));
        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND), response.statusCode());
    }


    @Test
    public void testConceptDescriptionRepositoryGetConceptDescription()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        ConceptDescription expected = environment.getConceptDescriptions().get(0);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                API_PATHS.conceptDescriptionRepository().conceptDescription(expected),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                ConceptDescription.class)));
    }


    @Test
    public void testConceptDescriptionRepositoryGetConceptDescriptionNotExists()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        HttpResponse<String> response = HttpHelper.get(API_PATHS.conceptDescriptionRepository().conceptDescription("non-existant"));
        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND), response.statusCode());
    }


    @Test
    public void testConceptDescriptionRepositoryGetConceptDescriptions()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        Object expected = environment.getConceptDescriptions();
        assertExecuteMultiple(
                HttpMethod.GET,
                API_PATHS.conceptDescriptionRepository().conceptDescriptions(),
                StatusCode.SUCCESS,
                null,
                expected,
                ConceptDescription.class);
    }


    @Test
    public void testConceptDescriptionRepositoryUpdateConceptDescription()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException {
        ConceptDescription expected = environment.getConceptDescriptions().get(0);
        expected.setIdShort("changed");
        assertEvent(
                messageBus,
                ElementUpdateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.PUT,
                                API_PATHS.conceptDescriptionRepository().conceptDescription(expected),
                                StatusCode.SUCCESS,
                                expected,
                                expected,
                                ConceptDescription.class)));
        Assert.assertTrue(HttpHelper.getWithMultipleResult(
                API_PATHS.conceptDescriptionRepository().conceptDescriptions(),
                ConceptDescription.class)
                .contains(expected));
    }


    @Test
    public void testSubmodelInterfaceCreateSubmodelElement()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException {
        Submodel submodel = environment.getSubmodels().get(0);
        SubmodelElement expected = new DefaultProperty.Builder()
                .idShort("newProperty")
                .build();
        assertEvent(
                messageBus,
                ElementCreateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.POST,
                                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElements(),
                                StatusCode.SUCCESS_CREATED,
                                expected,
                                expected,
                                SubmodelElement.class)));
        List<SubmodelElement> after = HttpHelper.getWithMultipleResult(
                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertTrue(after.contains(expected));
    }


    @Test
    public void testSubmodelInterfaceCreateSubmodelElementWithIdInUrl()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel submodel = environment.getSubmodels().get(0);
        SubmodelElement expected = new DefaultProperty.Builder()
                .idShort("newProperty")
                .build();
        assertEvent(
                messageBus,
                ElementCreateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.POST,
                                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElement(expected),
                                StatusCode.SUCCESS_CREATED,
                                expected,
                                expected,
                                SubmodelElement.class)));
        List<SubmodelElement> after = HttpHelper.getWithMultipleResult(
                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertTrue(after.contains(expected));
    }


    @Test
    public void testSubmodelInterfaceDeleteSubmodelElement()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel submodel = environment.getSubmodels().get(0);
        SubmodelElement expected = submodel.getSubmodelElements().get(0);
        List<SubmodelElement> before = HttpHelper.getWithMultipleResult(
                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertTrue(before.contains(expected));
        assertEvent(
                messageBus,
                ElementDeleteEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecute(
                                HttpMethod.DELETE,
                                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElement(expected),
                                StatusCode.SUCCESS)));
        List<SubmodelElement> after = HttpHelper.getWithMultipleResult(
                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertFalse(after.contains(expected));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodel()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel expected = environment.getSubmodels().get(0);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                API_PATHS.submodelRepository().submodelInterface(expected).submodel(),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                Submodel.class)));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelElement()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel submodel = environment.getSubmodels().get(0);
        SubmodelElement expected = submodel.getSubmodelElements().get(0);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElement(expected),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                SubmodelElement.class)));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelElements() throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        Submodel submodel = environment.getSubmodels().get(0);
        List<SubmodelElement> expected = submodel.getSubmodelElements();
        assertExecuteMultiple(
                HttpMethod.GET,
                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElements(),
                StatusCode.SUCCESS,
                null,
                expected,
                SubmodelElement.class);
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelContentValue()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel submodel = environment.getSubmodels().get(3);
        String expected = new JsonSerializer().write(submodel, new OutputModifier.Builder()
                .content(Content.VALUE)
                .build());
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                submodel,
                LambdaExceptionHelper.wrap(
                        x -> {
                            HttpResponse<String> response = HttpHelper.get(API_PATHS.submodelRepository().submodelInterface(submodel).submodel(Content.VALUE));
                            Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), response.statusCode());
                            JSONAssert.assertEquals(expected, response.body(), false);
                        }));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelLevelCore()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel expected = DeepCopyHelper.deepCopy(environment.getSubmodels().get(2), Submodel.class);
        expected.getSubmodelElements().forEach(x -> {
            if (SubmodelElementCollection.class.isAssignableFrom(x.getClass())) {
                ((SubmodelElementCollection) x).getValues().clear();
            }
        });
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                API_PATHS.submodelRepository().submodelInterface(expected).submodel(Level.CORE),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                Submodel.class)));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelContentPath()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel submodel = environment.getSubmodels().get(2);
        ExtendHelper.withoutBlobValue(submodel);
        Path expected = Path.builder()
                .id("TestSubmodel3")
                .child("ExampleRelationshipElement")
                .child("ExampleAnnotatedRelationshipElement")
                .child("ExampleOperation")
                .child("ExampleCapability")
                .child("ExampleBasicEvent")
                .child(Path.builder()
                        .id("ExampleSubmodelCollectionOrdered")
                        .child("ExampleProperty")
                        .child("ExampleMultiLanguageProperty")
                        .child("ExampleRange")
                        .build())
                .child(Path.builder()
                        .id("ExampleSubmodelCollectionUnordered")
                        .child("ExampleBlob")
                        .child("ExampleFile")
                        .child("ExampleReferenceElement")
                        .build())
                .build();
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                submodel,
                LambdaExceptionHelper.wrap(
                        x -> {
                            HttpResponse<String> response = HttpHelper.get(API_PATHS.submodelRepository().submodelInterface(submodel).submodel(Level.DEEP, Content.PATH));
                            Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), response.statusCode());
                            JSONAssert.assertEquals(mapper.writeValueAsString(expected.getPaths()), response.body(), false);
                        }));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelLevelCoreContentPath()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel submodel = DeepCopyHelper.deepCopy(environment.getSubmodels().get(2), Submodel.class);
        submodel.getSubmodelElements().forEach(x -> {
            if (SubmodelElementCollection.class.isAssignableFrom(x.getClass())) {
                ((SubmodelElementCollection) x).getValues().clear();
            }
        });
        Path expected = Path.builder()
                .id("TestSubmodel3")
                .child("ExampleRelationshipElement")
                .child("ExampleAnnotatedRelationshipElement")
                .child("ExampleOperation")
                .child("ExampleCapability")
                .child("ExampleBasicEvent")
                .child(Path.builder()
                        .id("ExampleSubmodelCollectionOrdered")
                        .child("ExampleProperty")
                        .child("ExampleMultiLanguageProperty")
                        .child("ExampleRange")
                        .build())
                .child(Path.builder()
                        .id("ExampleSubmodelCollectionUnordered")
                        .child("ExampleBlob")
                        .child("ExampleFile")
                        .child("ExampleReferenceElement")
                        .build())
                .build();
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                submodel,
                LambdaExceptionHelper.wrap(
                        x -> {
                            HttpResponse<String> response = HttpHelper.get(API_PATHS.submodelRepository().submodelInterface(submodel).submodel(Level.CORE, Content.PATH));
                            Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), response.statusCode());
                            JSONAssert.assertEquals(mapper.writeValueAsString(expected.asCorePath().getPaths()), response.body(), false);
                        }));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelLevelDeep()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel expected = environment.getSubmodels().get(2);
        ExtendHelper.withoutBlobValue(expected);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                API_PATHS.submodelRepository().submodelInterface(expected).submodel(Level.DEEP),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                Submodel.class)));
    }


    @Test
    public void testSubmodelInterfaceUpdateSubmodel()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException {
        Submodel expected = environment.getSubmodels().get(0);
        expected.setIdShort("changed");
        assertEvent(
                messageBus,
                ElementUpdateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.PUT,
                                API_PATHS.submodelRepository().submodelInterface(expected).submodel(),
                                StatusCode.SUCCESS,
                                expected,
                                expected,
                                Submodel.class)));
    }


    @Test
    public void testSubmodelInterfaceUpdateSubmodelElement()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel submodel = environment.getSubmodels().get(0);
        SubmodelElement expected = submodel.getSubmodelElements().get(0);
        expected.getDescriptions().add(new LangString("foo", "en"));
        assertEvent(
                messageBus,
                ElementUpdateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.PUT,
                                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElement(expected),
                                StatusCode.SUCCESS,
                                expected,
                                expected,
                                SubmodelElement.class)));
        List<SubmodelElement> after = HttpHelper.getWithMultipleResult(
                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertTrue(after.contains(expected));
    }


    @Test
    public void testSubmodelRepositoryCreateSubmodel()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel expected = new DefaultSubmodel.Builder()
                .identification(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.IRI)
                        .identifier("newSubmodel")
                        .build())
                .build();
        assertEvent(
                messageBus,
                ElementCreateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.POST,
                                API_PATHS.submodelRepository().submodels(),
                                StatusCode.SUCCESS_CREATED,
                                expected,
                                expected,
                                Submodel.class)));
        Assert.assertTrue(HttpHelper.getWithMultipleResult(
                API_PATHS.submodelRepository().submodels(),
                Submodel.class)
                .contains(expected));
    }


    @Test
    public void testSubmodelRepositoryDeleteSubmodel()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel expected = environment.getSubmodels().get(1);
        List<Submodel> before = HttpHelper.getWithMultipleResult(
                API_PATHS.submodelRepository().submodels(),
                Submodel.class);
        Assert.assertTrue(before.contains(expected));
        assertEvent(
                messageBus,
                ElementDeleteEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecute(HttpMethod.DELETE,
                                API_PATHS.submodelRepository().submodel(expected),
                                StatusCode.SUCCESS)));
        List<Submodel> after = HttpHelper.getWithMultipleResult(
                API_PATHS.submodelRepository().submodels(),
                Submodel.class);
        Assert.assertFalse(after.contains(expected));
    }


    @Test
    public void testSubmodelRepositoryGetSubmodel()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel expected = environment.getSubmodels().get(1);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                API_PATHS.submodelRepository().submodel(expected),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                Submodel.class)));
    }


    @Test
    public void testSubmodelRepositoryGetSubmodelNotExists()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        HttpResponse<String> response = HttpHelper.get(API_PATHS.submodelRepository().submodel("non-existant"));
        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND), response.statusCode());
    }


    @Test
    public void testSubmodelRepositoryGetSubmodels() throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        Object expected = environment.getSubmodels();
        ExtendHelper.withoutBlobValue(expected);
        assertExecuteMultiple(
                HttpMethod.GET,
                API_PATHS.submodelRepository().submodels(),
                StatusCode.SUCCESS,
                null,
                expected,
                Submodel.class);
    }


    @Test
    public void testSubmodelRepositoryGetSubmodelsByIdShort() throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        Submodel expected = environment.getSubmodels().get(1);
        assertExecuteMultiple(
                HttpMethod.GET,
                String.format("%s?idShort=%s", API_PATHS.submodelRepository().submodels(), expected.getIdShort()),
                StatusCode.SUCCESS,
                null,
                List.of(expected),
                Submodel.class);
    }


    @Test
    public void testSubmodelRepositoryGetSubmodelsBySemanticId() throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        Submodel expected = environment.getSubmodels().get(1);
        assertExecuteMultiple(
                HttpMethod.GET,
                String.format("%s?semanticId=%s",
                        API_PATHS.submodelRepository().submodels(),
                        EncodingHelper.base64UrlEncode(new JsonSerializer().write(expected.getSemanticId()))),
                StatusCode.SUCCESS,
                null,
                List.of(expected),
                Submodel.class);
    }


    @Test
    public void testSubmodelRepositoryUpdateSubmodel()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException {
        Submodel expected = environment.getSubmodels().get(1);
        expected.setIdShort("changed");
        assertEvent(
                messageBus,
                ElementUpdateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.PUT,
                                API_PATHS.submodelRepository().submodel(expected),
                                StatusCode.SUCCESS,
                                expected,
                                expected,
                                Submodel.class)));
    }


    private void assertExecute(HttpMethod method, String url, StatusCode statusCode)
            throws IOException, InterruptedException, URISyntaxException, SerializationException, DeserializationException {
        assertExecuteSingle(method, url, statusCode, null, null, null);
    }


    private void assertExecuteMultiple(HttpMethod method, String url, StatusCode statusCode, Object input, Object expected, Class<?> type)
            throws IOException, InterruptedException, URISyntaxException, SerializationException, DeserializationException {
        HttpResponse response = HttpHelper.execute(method, url, input);
        Assert.assertEquals(toHttpStatusCode(statusCode), response.statusCode());
        if (expected != null) {
            Object actual = HttpHelper.readResponseList(response, type);
            Assert.assertEquals(expected, actual);
        }
    }


    private void assertExecuteSingle(HttpMethod method, String url, StatusCode statusCode, Object input, Object expected, Class<?> type)
            throws IOException, InterruptedException, URISyntaxException, SerializationException, DeserializationException {
        HttpResponse response = HttpHelper.execute(method, url, input);
        Assert.assertEquals(toHttpStatusCode(statusCode), response.statusCode());
        if (expected != null) {
            Object actual = HttpHelper.readResponse(response, type);
            Assert.assertEquals(expected, actual);
        }
    }

}
