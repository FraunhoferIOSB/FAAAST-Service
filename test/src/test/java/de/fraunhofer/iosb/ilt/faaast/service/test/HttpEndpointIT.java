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
import com.google.common.net.MediaType;
import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.EnvironmentSerializationManager;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpConstants;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.memory.FileStorageInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.EnvironmentContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.util.QueryModifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.util.Path;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.ApiPaths;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ExtendHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.InMemoryFile;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetID;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;


public class HttpEndpointIT {

    public static MessageBus messageBus;

    private static final String HOST = "http://localhost";
    private static int PORT;
    private static ApiPaths API_PATHS;
    private static Environment environment;
    private static Service service;
    private final ObjectMapper mapper;
    private static final Path pathForTestSubmodel3 = Path.builder()
            .child("ExampleRelationshipElement")
            .child("ExampleAnnotatedRelationshipElement")
            .child("ExampleOperation")
            .child("ExampleCapability")
            .child("ExampleBasicEvent")
            .child(Path.builder()
                    .isList()
                    .id("ExampleSubmodelElementListOrdered")
                    .child("ExampleProperty")
                    .child("ExampleMultiLanguageProperty")
                    .child("ExampleRange")
                    .build())
            .child(Path.builder()
                    .id("ExampleSubmodelElementCollection")
                    .child("ExampleBlob")
                    .child("ExampleFile")
                    .child("ExampleReferenceElement")
                    .build())
            .build();

    public HttpEndpointIT() {
        this.mapper = new ObjectMapper();
    }


    @BeforeClass
    public static void initClass() throws IOException {
        PORT = PortHelper.findFreePort();
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
                        .initialModel(DeepCopyHelper.deepCopy(environment))
                        .build())
                .fileStorage(new FileStorageInMemoryConfig())
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
        SpecificAssetID newIdentifier = new DefaultSpecificAssetID.Builder()
                .name("foo")
                .value("bar")
                .build();
        List<SpecificAssetID> expected = new ArrayList<>();
        expected.add(newIdentifier);
        expected.addAll(aas.getAssetInformation().getSpecificAssetIds());
        expected.add(new DefaultSpecificAssetID.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(aas.getAssetInformation().getGlobalAssetID())
                .build());
        assertExecuteMultiple(
                HttpMethod.POST,
                API_PATHS.aasBasicDiscovery().assetAdministrationShell(aas),
                StatusCode.SUCCESS_CREATED,
                List.of(newIdentifier),
                expected,
                SpecificAssetID.class);
    }


    @Test
    public void testAASBasicDiscoveryDelete() throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);
        aas.getAssetInformation().getSpecificAssetIds().clear();
        aas.getAssetInformation().setGlobalAssetID(null);
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
                SpecificAssetID.class);
    }


    @Test
    public void testAASBasicDiscoveryGetAssetAdministrationShells()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        List<String> expected = environment.getAssetAdministrationShells().stream()
                .map(x -> x.getId())
                .collect(Collectors.toList());
        assertExecutePage(
                HttpMethod.GET,
                API_PATHS.aasBasicDiscovery().assetAdministrationShells(),
                StatusCode.SUCCESS,
                null,
                expected,
                String.class);
    }


    @Test
    public void testAASBasicDiscoveryGetAssetAdministrationShellsByGlobalAssetId()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        String assetIdValue = "https://acplt.org/Test_Asset";
        List<String> expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getAssetInformation().getGlobalAssetID().equalsIgnoreCase(assetIdValue))
                .map(x -> x.getId())
                .collect(Collectors.toList());
        assertExecutePage(
                HttpMethod.GET,
                API_PATHS.aasBasicDiscovery().assetAdministrationShells(Map.of(FaaastConstants.KEY_GLOBAL_ASSET_ID, assetIdValue)),
                StatusCode.SUCCESS,
                null,
                expected,
                String.class);
    }


    @Test
    public void testAASBasicDiscoveryGetAssetLinks()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);
        List<SpecificAssetID> expected = new ArrayList<>(aas.getAssetInformation().getSpecificAssetIds());
        expected.add(new DefaultSpecificAssetID.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(aas.getAssetInformation().getGlobalAssetID())
                .build());
        assertExecuteMultiple(
                HttpMethod.GET,
                API_PATHS.aasBasicDiscovery().assetAdministrationShell(aas),
                StatusCode.SUCCESS,
                null,
                expected,
                SpecificAssetID.class);
    }


    @Test
    public void testAASRepositoryCreateAssetAdministrationShells()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        AssetAdministrationShell expected = new DefaultAssetAdministrationShell.Builder()
                .id("http://newOne")
                .idShort("newOne")
                .description(new DefaultLangStringTextType.Builder()
                        .text("Täst")
                        .build())
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
        Assert.assertTrue(HttpHelper.getPage(
                API_PATHS.aasRepository().assetAdministrationShells(),
                AssetAdministrationShell.class)
                .getContent()
                .contains(expected));
    }


    @Test
    public void testAASRepositoryDeleteAssetAdministrationShell()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        Page<AssetAdministrationShell> before = HttpHelper.getPage(
                API_PATHS.aasRepository().assetAdministrationShells(),
                AssetAdministrationShell.class);
        Assert.assertTrue(before.getContent().contains(expected));
        assertEvent(
                messageBus,
                ElementDeleteEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecute(HttpMethod.DELETE,
                                API_PATHS.aasRepository().assetAdministrationShell(expected),
                                StatusCode.SUCCESS_NO_CONTENT)));
        Page<AssetAdministrationShell> actual = HttpHelper.getPage(
                API_PATHS.aasRepository().assetAdministrationShells(),
                AssetAdministrationShell.class);
        Assert.assertFalse(actual.getContent().contains(expected));
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
    public void testAASRepositoryGetAssetAdministrationShellContentReference()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Reference expected = ReferenceBuilder.forAas(aas);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                aas,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                API_PATHS.aasRepository().assetAdministrationShell(aas, Content.REFERENCE),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                Reference.class)));
    }


    @Test
    public void testAASRepositoryGetAssetAdministrationShellUsingSubmodelIdReturnsResourceNotFound()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        String submodelId = environment.getSubmodels().get(1).getId();
        assertExecuteSingle(HttpMethod.GET,
                API_PATHS.aasRepository().assetAdministrationShell(submodelId),
                StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND,
                null,
                null,
                AssetAdministrationShell.class);
    }


    @Test
    public void testAASRepositoryGetAssetAdministrationShellNotExists()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        HttpResponse<String> response = HttpHelper.get(API_PATHS.aasRepository().assetAdministrationShell("non-existant"));
        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND), response.statusCode());
    }


    @Test
    public void testAASRepositoryGetAssetAdministrationShells() throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        List<AssetAdministrationShell> expected = environment.getAssetAdministrationShells();
        assertExecutePage(
                HttpMethod.GET,
                API_PATHS.aasRepository().assetAdministrationShells(),
                StatusCode.SUCCESS,
                null,
                expected,
                AssetAdministrationShell.class);
    }


    @Test
    public void testAASRepositoryGetAssetAdministrationShellsContentReference()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        List<Reference> expected = environment.getAssetAdministrationShells().stream()
                .map(ReferenceBuilder::forAas)
                .collect(Collectors.toList());
        assertExecutePage(
                HttpMethod.GET,
                API_PATHS.aasRepository().assetAdministrationShells(Content.REFERENCE),
                StatusCode.SUCCESS,
                null,
                expected,
                Reference.class);
    }


    @Test
    public void testAASRepositoryGetAssetAdministrationShellsWithPaging()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        // 4 elements in total are available, query data with increasing page size 1, 2, 3.
        String cursor = null;
        List<AssetAdministrationShell> allExpectedShells = environment.getAssetAdministrationShells();
        List<AssetAdministrationShell> allActualShells = new ArrayList<>();
        Page<AssetAdministrationShell> page1 = assertExecutePage(
                HttpMethod.GET,
                API_PATHS.aasRepository().assetAdministrationShells(cursor, 1),
                StatusCode.SUCCESS,
                null,
                allExpectedShells.stream()
                        .limit(1)
                        .collect(Collectors.toList()),
                AssetAdministrationShell.class);
        allActualShells.addAll(page1.getContent());
        cursor = page1.getMetadata().getCursor();
        Assert.assertNotNull(cursor);
        Page<AssetAdministrationShell> page2 = assertExecutePage(
                HttpMethod.GET,
                API_PATHS.aasRepository().assetAdministrationShells(cursor, 2),
                StatusCode.SUCCESS,
                null,
                allExpectedShells.stream()
                        .skip(1)
                        .limit(2)
                        .collect(Collectors.toList()),
                AssetAdministrationShell.class);
        allActualShells.addAll(page2.getContent());
        cursor = page2.getMetadata().getCursor();
        Assert.assertNotNull(cursor);
        Page<AssetAdministrationShell> page3 = assertExecutePage(
                HttpMethod.GET,
                API_PATHS.aasRepository().assetAdministrationShells(cursor, 3),
                StatusCode.SUCCESS,
                null,
                allExpectedShells.stream()
                        .skip(3)
                        .collect(Collectors.toList()),
                AssetAdministrationShell.class);
        allActualShells.addAll(page3.getContent());
        cursor = page3.getMetadata().getCursor();
        Assert.assertEquals(allExpectedShells, allActualShells);
        Assert.assertNull(cursor);
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
    public void testAASSerializationJSON()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, ResourceNotFoundException {
        assertSerialization(
                List.of(environment.getAssetAdministrationShells().get(0)),
                true,
                MediaType.JSON_UTF_8,
                DataFormat.JSON);
    }


    @Test
    public void testAASSerializationXML()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, ResourceNotFoundException {
        assertSerialization(
                List.of(environment.getAssetAdministrationShells().get(0)),
                true,
                MediaType.XML_UTF_8,
                DataFormat.XML);
    }


    @Ignore("Failing because of charset issues, probably caused by admin-shell.io library not respecting charset for de-/serialization")
    @Test
    public void testAASSerializationRDF()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, ResourceNotFoundException {
        assertSerialization(
                List.of(environment.getAssetAdministrationShells().get(0)),
                true,
                DataFormat.RDF.getContentType(),
                DataFormat.RDF);
    }


    @Ignore("Failing because aas4j modifies the environment: SubmodelElementCollection values are set to null. Waiting for new aas4j release.")
    @Test
    public void testAASSerializationAASX()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, ResourceNotFoundException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        byte[] content = new byte[20];
        new Random().nextBytes(content);
        Environment defaultEnvironment = new DefaultEnvironment.Builder()
                .assetAdministrationShells(aas)
                .submodels(aas.getSubmodels().stream()
                        .map(x -> {
                            try {
                                return EnvironmentHelper.resolve(x, environment, Submodel.class);
                            }
                            catch (ResourceNotFoundException ex) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList()))
                .conceptDescriptions(environment.getConceptDescriptions())
                .build();
        String fileName = "file:///TestFile.pdf";
        EnvironmentContext expected = EnvironmentContext.builder()
                .environment(defaultEnvironment)
                .file(new InMemoryFile(content, fileName))
                .build();
        HttpEntity httpEntity = MultipartEntityBuilder.create()
                .addPart("fileName",
                        new StringBody(fileName,
                                ContentType.create("text/plain", StandardCharsets.UTF_8)))
                .addBinaryBody("file", content, ContentType.APPLICATION_PDF,
                        fileName)
                .build();
        HttpResponse<byte[]> putFileResponse = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder()
                        .uri(new URI(API_PATHS.submodelRepository()
                                .submodelInterface(defaultEnvironment.getSubmodels().get(0)).submodelElement("ExampleSubmodelElementCollection.ExampleFile")
                                + "/attachment"))
                        .header(HttpConstants.HEADER_ACCEPT, DataFormat.JSON.getContentType().toString())
                        .header(HttpConstants.HEADER_CONTENT_TYPE, httpEntity.getContentType())
                        .PUT(BodyPublishers.ofInputStream(LambdaExceptionHelper.wrap(httpEntity::getContent)))
                        .build(),
                        HttpResponse.BodyHandlers.ofByteArray());
        Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), putFileResponse.statusCode());
        HttpResponse<byte[]> response = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder()
                        .uri(new URI(API_PATHS.aasSerialization().serialization(List.of(aas), defaultEnvironment.getSubmodels(), true)))
                        .header(HttpConstants.HEADER_ACCEPT, DataFormat.AASX.getContentType().toString())
                        .GET()
                        .build(),
                        HttpResponse.BodyHandlers.ofByteArray());
        Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), response.statusCode());
        MediaType responseContentType = MediaType.parse(response.headers()
                .firstValue(HttpConstants.HEADER_CONTENT_TYPE)
                .orElseThrow());
        Assert.assertTrue("content-type out of range", responseContentType.is(DataFormat.AASX.getContentType()));
        try (InputStream in = new ByteArrayInputStream(response.body())) {
            EnvironmentContext actual = EnvironmentSerializationManager
                    .deserializerFor(DataFormat.AASX)
                    .read(in, responseContentType.charset().or(StandardCharsets.UTF_8));
            Assert.assertEquals(expected, actual);
        }
    }


    @Test
    public void testAASSerializationWildcard()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, ResourceNotFoundException {
        assertSerialization(
                List.of(environment.getAssetAdministrationShells().get(0)),
                true,
                MediaType.ANY_TYPE,
                DataFormat.JSON);
    }


    @Test
    public void testAASSerializationInvalidDataformat()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException {
        HttpResponse<byte[]> response = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder()
                        .uri(new URI(API_PATHS.aasSerialization().serialization(List.of(), List.of(), false)))
                        .header(HttpConstants.HEADER_ACCEPT, MediaType.ANY_VIDEO_TYPE.toString())
                        .GET()
                        .build(),
                        HttpResponse.BodyHandlers.ofByteArray());
        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_ERROR_BAD_REQUEST), response.statusCode());
    }


    private void assertSerialization(List<AssetAdministrationShell> aass, boolean includeConceptDescriptions, MediaType contentType, DataFormat expectedFormat)
            throws IOException, InterruptedException, SerializationException, URISyntaxException, DeserializationException, ResourceNotFoundException {
        Environment expected = new DefaultEnvironment.Builder()
                .assetAdministrationShells(aass)
                .submodels(aass.stream().flatMap(x -> x.getSubmodels().stream())
                        .map(x -> {
                            try {
                                return EnvironmentHelper.resolve(x, environment, Submodel.class);
                            }
                            catch (ResourceNotFoundException ex) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList()))
                .conceptDescriptions(includeConceptDescriptions
                        ? environment.getConceptDescriptions()
                        : List.of())
                .build();
        HttpResponse<byte[]> response = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder()
                        .uri(new URI(API_PATHS.aasSerialization().serialization(aass, expected.getSubmodels(), includeConceptDescriptions)))
                        .header(HttpConstants.HEADER_ACCEPT, contentType.toString())
                        .GET()
                        .build(),
                        HttpResponse.BodyHandlers.ofByteArray());
        Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), response.statusCode());
        MediaType responseContentType = MediaType.parse(response.headers()
                .firstValue(HttpConstants.HEADER_CONTENT_TYPE)
                .orElseThrow());
        Assert.assertTrue("content-type out of range", responseContentType.is(contentType));
        try (InputStream in = new ByteArrayInputStream(response.body())) {
            Environment actual = EnvironmentSerializationManager
                    .deserializerFor(expectedFormat)
                    .read(in, responseContentType.charset().or(StandardCharsets.UTF_8))
                    .getEnvironment();
            Assert.assertEquals(expected, actual);
        }
    }


    @Test
    public void testAssetAdministrationShellInterfaceCreateSubmodel()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        List<Reference> expected = aas.getSubmodels();
        Reference newReference = new DefaultReference.Builder()
                .keys(new DefaultKey.Builder()
                        .value("test")
                        .build())
                .build();
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
        List<Reference> actual = HttpHelper.getWithMultipleResult(
                API_PATHS.aasInterface(aas).submodels(),
                Reference.class);
        Assert.assertFalse(actual.contains(submodelToDelete));
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
    public void testAssetAdministrationShellInterfaceGetAssetAdministrationShellContentReference()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Reference expected = ReferenceBuilder.forAas(aas);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                aas,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                API_PATHS.aasInterface(aas).assetAdministrationShell(Content.REFERENCE),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                expected.getClass())));
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
        expected.getSpecificAssetIds().add(new DefaultSpecificAssetID.Builder()
                .name("foo")
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
                .id("http://example.org/foo")
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
        Assert.assertTrue(HttpHelper.getPage(
                API_PATHS.conceptDescriptionRepository().conceptDescriptions(),
                ConceptDescription.class)
                .getContent()
                .contains(expected));
    }


    @Test
    public void testConceptDescriptionRepositoryDeleteConceptDescription()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        ConceptDescription expected = environment.getConceptDescriptions().get(0);
        Page<ConceptDescription> before = HttpHelper.getPage(
                API_PATHS.conceptDescriptionRepository().conceptDescriptions(),
                ConceptDescription.class);
        Assert.assertTrue(before.getContent().contains(expected));
        assertEvent(
                messageBus,
                ElementDeleteEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecute(
                                HttpMethod.DELETE,
                                API_PATHS.conceptDescriptionRepository().conceptDescription(expected),
                                StatusCode.SUCCESS_NO_CONTENT)));
        Page<ConceptDescription> actual = HttpHelper.getPage(
                API_PATHS.conceptDescriptionRepository().conceptDescriptions(),
                ConceptDescription.class);
        Assert.assertFalse(actual.getContent().contains(expected));
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
        List<ConceptDescription> expected = environment.getConceptDescriptions();
        assertExecutePage(
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
        Assert.assertTrue(HttpHelper.getPage(
                API_PATHS.conceptDescriptionRepository().conceptDescriptions(),
                ConceptDescription.class)
                .getContent()
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
        Page<SubmodelElement> actual = HttpHelper.getPage(
                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertTrue(actual.getContent().contains(expected));
    }


    @Test
    public void testSubmodelInterfaceCreateSubmodelElementInsideList()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel submodel = environment.getSubmodels().get(2);
        SubmodelElementList submodelElementList = (SubmodelElementList) submodel.getSubmodelElements().get(5);
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
                                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElement(submodelElementList),
                                StatusCode.SUCCESS_CREATED,
                                expected,
                                expected,
                                SubmodelElement.class)));
        SubmodelElement actual = HttpHelper.getWithSingleResult(
                API_PATHS.submodelRepository()
                        .submodelInterface(submodel)
                        .submodelElement(IdShortPath.builder()
                                .idShort(submodelElementList.getIdShort())
                                .index(submodelElementList.getValue().size())
                                .build()),
                SubmodelElement.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSubmodelInterfaceCreateSubmodelElementInsideCollection()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel submodel = environment.getSubmodels().get(2);
        SubmodelElementCollection submodelElementCollection = (SubmodelElementCollection) submodel.getSubmodelElements().get(6);
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
                                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElement(submodelElementCollection),
                                StatusCode.SUCCESS_CREATED,
                                expected,
                                expected,
                                SubmodelElement.class)));
        SubmodelElement actual = HttpHelper.getWithSingleResult(
                API_PATHS.submodelRepository()
                        .submodelInterface(submodel)
                        .submodelElement(IdShortPath.builder()
                                .idShort(submodelElementCollection.getIdShort())
                                .idShort(expected.getIdShort())
                                .build()),
                SubmodelElement.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSubmodelInterfaceDeleteSubmodelElement()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel submodel = environment.getSubmodels().get(0);
        SubmodelElement expected = submodel.getSubmodelElements().get(0);
        Page<SubmodelElement> before = HttpHelper.getPage(
                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertTrue(before.getContent().contains(expected));
        assertEvent(
                messageBus,
                ElementDeleteEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecute(
                                HttpMethod.DELETE,
                                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElement(expected),
                                StatusCode.SUCCESS_NO_CONTENT)));
        Page<SubmodelElement> actual = HttpHelper.getPage(
                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertFalse(actual.getContent().contains(expected));
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
    public void testSubmodelInterfaceGetSubmodelContentReference()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel submodel = environment.getSubmodels().get(0);
        Reference expected = ReferenceBuilder.forSubmodel(submodel);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                submodel,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                API_PATHS.submodelRepository().submodelInterface(submodel).submodel(Content.REFERENCE),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                Reference.class)));
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
    public void testSubmodelInterfaceGetSubmodelElementContentReference()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel submodel = environment.getSubmodels().get(0);
        SubmodelElement submodelElement = submodel.getSubmodelElements().get(0);
        Reference expected = ReferenceBuilder.forSubmodel(submodel.getId(), submodelElement.getIdShort());
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                submodelElement,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElement(submodelElement, Content.REFERENCE),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                Reference.class)));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelElements() throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        Submodel submodel = environment.getSubmodels().get(0);
        List<SubmodelElement> expected = submodel.getSubmodelElements();
        assertExecutePage(
                HttpMethod.GET,
                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElements(),
                StatusCode.SUCCESS,
                null,
                expected,
                SubmodelElement.class);
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelElementsContentReference()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        Submodel submodel = environment.getSubmodels().get(0);
        List<Reference> expected = submodel
                .getSubmodelElements()
                .stream()
                .map(x -> ReferenceBuilder.forSubmodel(submodel, x))
                .collect(Collectors.toList());
        assertExecutePage(
                HttpMethod.GET,
                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElements(Content.REFERENCE),
                StatusCode.SUCCESS,
                null,
                expected,
                Reference.class);
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelContentValue()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel submodel = environment.getSubmodels().get(3);
        String expected = new JsonApiSerializer().write(submodel, new OutputModifier.Builder()
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


    private void assertSubmodelInterfaceFileAttachment(String fileName)
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        byte[] content = new byte[20];
        new Random().nextBytes(content);
        Environment defaultEnvironment = new DefaultEnvironment.Builder()
                .assetAdministrationShells(aas)
                .submodels(aas.getSubmodels().stream()
                        .map(x -> {
                            try {
                                return EnvironmentHelper.resolve(x, environment, Submodel.class);
                            }
                            catch (ResourceNotFoundException ex) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList()))
                .conceptDescriptions(environment.getConceptDescriptions())
                .build();
        HttpEntity httpEntity = MultipartEntityBuilder.create()
                .addPart("fileName",
                        new StringBody(fileName,
                                ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), StandardCharsets.UTF_8)))
                .addBinaryBody("file", content, ContentType.APPLICATION_PDF,
                        fileName)
                .build();
        HttpResponse<byte[]> putFileResponse = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder()
                        .uri(new URI(API_PATHS.submodelRepository()
                                .submodelInterface(defaultEnvironment.getSubmodels().get(0)).submodelElement("ExampleSubmodelElementCollection.ExampleFile")
                                + "/attachment"))
                        .header(HttpConstants.HEADER_ACCEPT, DataFormat.JSON.getContentType().toString())
                        .header(HttpConstants.HEADER_CONTENT_TYPE, httpEntity.getContentType())
                        .PUT(BodyPublishers.ofInputStream(LambdaExceptionHelper.wrap(httpEntity::getContent)))
                        .build(),
                        HttpResponse.BodyHandlers.ofByteArray());
        Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), putFileResponse.statusCode());
        HttpResponse<byte[]> getFileResponse = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder()
                        .uri(new URI(API_PATHS.submodelRepository()
                                .submodelInterface(defaultEnvironment.getSubmodels().get(0)).submodelElement("ExampleSubmodelElementCollection.ExampleFile")
                                + "/attachment"))
                        .header(HttpConstants.HEADER_ACCEPT, DataFormat.JSON.getContentType().toString())
                        .header(HttpConstants.HEADER_CONTENT_TYPE, httpEntity.getContentType())
                        .GET()
                        .build(),
                        HttpResponse.BodyHandlers.ofByteArray());
        Assert.assertArrayEquals(content, getFileResponse.body());
        HttpResponse<byte[]> deleteFileResponse = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder()
                        .uri(new URI(API_PATHS.submodelRepository()
                                .submodelInterface(defaultEnvironment.getSubmodels().get(0)).submodelElement("ExampleSubmodelElementCollection.ExampleFile")
                                + "/attachment"))
                        .header(HttpConstants.HEADER_ACCEPT, DataFormat.JSON.getContentType().toString())
                        .DELETE()
                        .build(),
                        HttpResponse.BodyHandlers.ofByteArray());
        Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), deleteFileResponse.statusCode());
    }

    @Test
    public void testSubmodelInterfaceFileAttachmentWithFilePrefix()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        assertSubmodelInterfaceFileAttachment("file:///TestFile.pdf");
    }

    @Test
    public void testSubmodelInterfaceFileAttachmentWithRelativePath()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        assertSubmodelInterfaceFileAttachment("/aasx/files/documentation.pdf");
    }


    @Test
    public void testAASThumbnail() throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        AssetInformation expected = aas.getAssetInformation();
        String imageName = "file:///image.png";
        byte[] content = new byte[20];
        new Random().nextBytes(content);
        DefaultResource thumbnail = new DefaultResource.Builder()
                .path(imageName)
                .contentType(ContentType.IMAGE_PNG.getMimeType())
                .build();
        expected.setDefaultThumbnail(thumbnail);
        HttpEntity httpEntity = MultipartEntityBuilder.create()
                .addPart("fileName",
                        new StringBody(imageName,
                                ContentType.create("text/plain", StandardCharsets.UTF_8)))
                .addBinaryBody("file", content, ContentType.IMAGE_PNG,
                        imageName)
                .build();
        HttpResponse<byte[]> putThumbnailResponse = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder()
                        .uri(new URI(API_PATHS.aasInterface(aas).assetInformation()
                                + "/thumbnail"))
                        .header(HttpConstants.HEADER_ACCEPT, DataFormat.JSON.getContentType().toString())
                        .header(HttpConstants.HEADER_CONTENT_TYPE, httpEntity.getContentType())
                        .PUT(BodyPublishers.ofInputStream(LambdaExceptionHelper.wrap(httpEntity::getContent)))
                        .build(),
                        HttpResponse.BodyHandlers.ofByteArray());
        Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), putThumbnailResponse.statusCode());
        assertExecuteSingle(
                HttpMethod.GET,
                API_PATHS.aasInterface(aas).assetInformation(),
                StatusCode.SUCCESS,
                null,
                expected,
                AssetInformation.class);
        HttpResponse<byte[]> deleteThumbnailResponse = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder()
                        .uri(new URI(API_PATHS.aasInterface(aas).assetInformation()
                                + "/thumbnail"))
                        .header(HttpConstants.HEADER_ACCEPT, DataFormat.JSON.getContentType().toString())
                        .header(HttpConstants.HEADER_CONTENT_TYPE, httpEntity.getContentType())
                        .DELETE()
                        .build(),
                        HttpResponse.BodyHandlers.ofByteArray());
        Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), deleteThumbnailResponse.statusCode());
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelContentMetadata()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel submodel = DeepCopyHelper.deepCopy(environment.getSubmodels().get(3));
        String expected = new JsonApiSerializer().write(submodel, new OutputModifier.Builder()
                .content(Content.METADATA)
                .build());
        QueryModifierHelper.applyQueryModifier(
                submodel,
                new OutputModifier.Builder()
                        .content(Content.METADATA)
                        .build());
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                submodel,
                LambdaExceptionHelper.wrap(
                        x -> {
                            HttpResponse<String> response = HttpHelper.get(API_PATHS.submodelRepository()
                                    .submodelInterface(submodel)
                                    .submodel(Content.METADATA));
                            Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), response.statusCode());
                            JSONAssert.assertEquals(expected, response.body(), false);
                        }));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelLevelCore()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel expected = DeepCopyHelper.deepCopy(environment.getSubmodels().get(2), Submodel.class);
        clearSubmodelElementCollections(expected);
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
        Path expected = pathForTestSubmodel3;
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
        clearSubmodelElementCollections(submodel);
        Path expected = pathForTestSubmodel3;
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
        expected.getDescription().add(new DefaultLangStringTextType.Builder()
                .language("en")
                .text("foo")
                .build());
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
        Page<SubmodelElement> actual = HttpHelper.getPage(
                API_PATHS.submodelRepository().submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertTrue(actual.getContent().contains(expected));
    }


    @Test
    public void testSubmodelInterfaceCreateSubmodelElementInAasContext()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, ResourceNotFoundException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Submodel submodel = EnvironmentHelper.resolve(aas.getSubmodels().get(0), environment, Submodel.class);
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
                                API_PATHS.aasInterface(aas).submodelInterface(submodel).submodelElements(),
                                StatusCode.SUCCESS_CREATED,
                                expected,
                                expected,
                                SubmodelElement.class)));
        Page<SubmodelElement> actual = HttpHelper.getPage(
                API_PATHS.aasInterface(aas).submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertTrue(actual.getContent().contains(expected));
    }


    @Test
    public void testSubmodelInterfaceCreateSubmodelElementInsideListInAasContext()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, ResourceNotFoundException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);
        Submodel submodel = environment.getSubmodels().get(2);
        SubmodelElementList submodelElementList = (SubmodelElementList) submodel.getSubmodelElements().get(5);
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
                                API_PATHS.aasInterface(aas)
                                        .submodelInterface(submodel)
                                        .submodelElement(submodelElementList),
                                StatusCode.SUCCESS_CREATED,
                                expected,
                                expected,
                                SubmodelElement.class)));
        SubmodelElement actual = HttpHelper.getWithSingleResult(
                API_PATHS.aasInterface(aas)
                        .submodelInterface(submodel)
                        .submodelElement(IdShortPath.builder()
                                .idShort(submodelElementList.getIdShort())
                                .index(submodelElementList.getValue().size())
                                .build()),
                SubmodelElement.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSubmodelInterfaceCreateSubmodelElementInsideCollectionInAasContext()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);
        Submodel submodel = environment.getSubmodels().get(2);
        SubmodelElementCollection submodelElementCollection = (SubmodelElementCollection) submodel.getSubmodelElements().get(6);
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
                                API_PATHS.aasInterface(aas.getId())
                                        .submodelInterface(submodel)
                                        .submodelElement(submodelElementCollection),
                                StatusCode.SUCCESS_CREATED,
                                expected,
                                expected,
                                SubmodelElement.class)));
        SubmodelElement actual = HttpHelper.getWithSingleResult(
                API_PATHS.aasInterface(aas.getId())
                        .submodelInterface(submodel)
                        .submodelElement(IdShortPath.builder()
                                .idShort(submodelElementCollection.getIdShort())
                                .idShort(expected.getIdShort())
                                .build()),
                SubmodelElement.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSubmodelInterfaceDeleteSubmodelElementInAasContext()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, ResourceNotFoundException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Submodel submodel = EnvironmentHelper.resolve(aas.getSubmodels().get(0), environment, Submodel.class);
        SubmodelElement expected = submodel.getSubmodelElements().get(0);
        Page<SubmodelElement> before = HttpHelper.getPage(
                API_PATHS.aasInterface(aas).submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertTrue(before.getContent().contains(expected));
        assertEvent(
                messageBus,
                ElementDeleteEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecute(
                                HttpMethod.DELETE,
                                API_PATHS.aasInterface(aas).submodelInterface(submodel).submodelElement(expected),
                                StatusCode.SUCCESS_NO_CONTENT)));
        Page<SubmodelElement> actual = HttpHelper.getPage(
                API_PATHS.aasInterface(aas).submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertFalse(actual.getContent().contains(expected));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelInAasContext()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, ResourceNotFoundException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Submodel expected = EnvironmentHelper.resolve(aas.getSubmodels().get(0), environment, Submodel.class);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                API_PATHS.aasInterface(aas).submodelInterface(expected).submodel(),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                Submodel.class)));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelElementInAasContext()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, ResourceNotFoundException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Submodel submodel = EnvironmentHelper.resolve(aas.getSubmodels().get(0), environment, Submodel.class);
        SubmodelElement expected = submodel.getSubmodelElements().get(0);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                API_PATHS.aasInterface(aas).submodelInterface(submodel).submodelElement(expected),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                SubmodelElement.class)));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelElementsInAasContext()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, ResourceNotFoundException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Submodel submodel = EnvironmentHelper.resolve(aas.getSubmodels().get(0), environment, Submodel.class);
        List<SubmodelElement> expected = submodel.getSubmodelElements();
        assertExecutePage(
                HttpMethod.GET,
                API_PATHS.aasInterface(aas).submodelInterface(submodel).submodelElements(),
                StatusCode.SUCCESS,
                null,
                expected,
                SubmodelElement.class);
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelContentValueInAasContext()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, ResourceNotFoundException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Submodel submodel = EnvironmentHelper.resolve(aas.getSubmodels().get(0), environment, Submodel.class);
        String expected = new JsonApiSerializer().write(submodel, new OutputModifier.Builder()
                .content(Content.VALUE)
                .build());
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                submodel,
                LambdaExceptionHelper.wrap(
                        x -> {
                            HttpResponse<String> response = HttpHelper.get(
                                    API_PATHS.aasInterface(aas).submodelInterface(submodel).submodel(Content.VALUE));
                            Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), response.statusCode());
                            JSONAssert.assertEquals(expected, response.body(), false);
                        }));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelLevelCoreInAasContext()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, ResourceNotFoundException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Submodel expected = DeepCopyHelper.deepCopy(EnvironmentHelper.resolve(aas.getSubmodels().get(0), environment, Submodel.class), Submodel.class);
        clearSubmodelElementCollections(expected);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                API_PATHS.aasInterface(aas).submodelInterface(expected).submodel(Level.CORE),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                Submodel.class)));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelContentPathInAasContext()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);
        Submodel submodel = environment.getSubmodels().get(2);
        ExtendHelper.withoutBlobValue(submodel);
        Path expected = pathForTestSubmodel3;
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                submodel,
                LambdaExceptionHelper.wrap(
                        x -> {
                            HttpResponse<String> response = HttpHelper.get(API_PATHS.aasInterface(aas).submodelInterface(submodel).submodel(Level.DEEP, Content.PATH));
                            Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), response.statusCode());
                            JSONAssert.assertEquals(mapper.writeValueAsString(expected.getPaths()), response.body(), false);
                        }));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelLevelCoreContentPathInAasContext()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);
        Submodel submodel = DeepCopyHelper.deepCopy(environment.getSubmodels().get(2), Submodel.class);
        clearSubmodelElementCollections(submodel);
        Path expected = pathForTestSubmodel3;
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                submodel,
                LambdaExceptionHelper.wrap(
                        x -> {
                            HttpResponse<String> response = HttpHelper.get(API_PATHS.aasInterface(aas).submodelInterface(submodel).submodel(Level.CORE, Content.PATH));
                            Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), response.statusCode());
                            JSONAssert.assertEquals(mapper.writeValueAsString(expected.asCorePath().getPaths()), response.body(), false);
                        }));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelLevelDeepInAasContext()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, ResourceNotFoundException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Submodel expected = DeepCopyHelper.deepCopy(EnvironmentHelper.resolve(aas.getSubmodels().get(0), environment, Submodel.class), Submodel.class);
        ExtendHelper.withoutBlobValue(expected);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                API_PATHS.aasInterface(aas).submodelInterface(expected).submodel(Level.DEEP),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                Submodel.class)));
    }


    @Test
    public void testSubmodelInterfaceUpdateSubmodelInAasContext()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, ResourceNotFoundException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Submodel expected = EnvironmentHelper.resolve(aas.getSubmodels().get(0), environment, Submodel.class);
        expected.setIdShort("changed");
        assertEvent(
                messageBus,
                ElementUpdateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.PUT,
                                API_PATHS.aasInterface(aas).submodelInterface(expected).submodel(),
                                StatusCode.SUCCESS,
                                expected,
                                expected,
                                Submodel.class)));
    }


    @Test
    public void testSubmodelInterfaceUpdateSubmodelElementInAasContext()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, ResourceNotFoundException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Submodel submodel = EnvironmentHelper.resolve(aas.getSubmodels().get(0), environment, Submodel.class);
        SubmodelElement expected = submodel.getSubmodelElements().get(0);
        expected.getDescription().add(new DefaultLangStringTextType.Builder()
                .language("en")
                .text("foo")
                .build());
        assertEvent(
                messageBus,
                ElementUpdateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.PUT,
                                API_PATHS.aasInterface(aas).submodelInterface(submodel).submodelElement(expected),
                                StatusCode.SUCCESS,
                                expected,
                                expected,
                                SubmodelElement.class)));
        Page<SubmodelElement> actual = HttpHelper.getPage(
                API_PATHS.aasInterface(aas).submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertTrue(actual.getContent().contains(expected));
    }


    @Test
    public void testSubmodelRepositoryCreateSubmodel()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel expected = new DefaultSubmodel.Builder()
                .id("newSubmodel")
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
        Assert.assertTrue(HttpHelper.getPage(
                API_PATHS.submodelRepository().submodels(),
                Submodel.class)
                .getContent()
                .contains(expected));
    }


    @Test
    public void testSubmodelRepositoryDeleteSubmodel()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel expected = environment.getSubmodels().get(1);
        Page<Submodel> before = HttpHelper.getPage(
                API_PATHS.submodelRepository().submodels(),
                Submodel.class);
        Assert.assertTrue(before.getContent().contains(expected));
        assertEvent(
                messageBus,
                ElementDeleteEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecute(HttpMethod.DELETE,
                                API_PATHS.submodelRepository().submodel(expected),
                                StatusCode.SUCCESS_NO_CONTENT)));
        Page<Submodel> actual = HttpHelper.getPage(
                API_PATHS.submodelRepository().submodels(),
                Submodel.class);
        Assert.assertFalse(actual.getContent().contains(expected));
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
    public void testSubmodelRepositoryGetSubmodelContentReference()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        Submodel submodel = environment.getSubmodels().get(1);
        Reference expected = ReferenceBuilder.forSubmodel(submodel);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                submodel,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                API_PATHS.submodelRepository().submodel(submodel, Content.REFERENCE),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                Reference.class)));
    }


    @Test
    public void testSubmodelRepositoryGetSubmodelNotExists()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        HttpResponse<String> response = HttpHelper.get(API_PATHS.submodelRepository().submodel("non-existant"));
        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND), response.statusCode());
    }


    @Test
    public void testSubmodelRepositoryGetSubmodels() throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        List<Submodel> expected = environment.getSubmodels();
        ExtendHelper.withoutBlobValue(expected);
        assertExecutePage(
                HttpMethod.GET,
                API_PATHS.submodelRepository().submodels(),
                StatusCode.SUCCESS,
                null,
                expected,
                Submodel.class);
    }


    @Test
    public void testSubmodelRepositoryGetSubmodelsContentReference()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        List<Reference> expected = environment.getSubmodels().stream()
                .map(ReferenceBuilder::forSubmodel)
                .collect(Collectors.toList());
        assertExecutePage(
                HttpMethod.GET,
                API_PATHS.submodelRepository().submodels(Content.REFERENCE),
                StatusCode.SUCCESS,
                null,
                expected,
                Reference.class);
    }


    @Test
    public void testSubmodelRepositoryGetSubmodelsByIdShort() throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException {
        Submodel expected = environment.getSubmodels().get(1);
        assertExecutePage(
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
        assertExecutePage(HttpMethod.GET,
                String.format("%s?semanticId=%s",
                        API_PATHS.submodelRepository().submodels(),
                        EncodingHelper.base64UrlEncode(new JsonApiSerializer().write(expected.getSemanticID()))),
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


    @Test
    public void testMethodNotAllowed() throws Exception {
        HttpResponse response = HttpHelper.execute(HttpMethod.PUT, API_PATHS.aasRepository().assetAdministrationShells());
        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_METHOD_NOT_ALLOWED), response.statusCode());
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


    private <T> Page<T> assertExecutePage(HttpMethod method, String url, StatusCode statusCode, Object input, List<T> expected, Class<T> type)
            throws IOException, InterruptedException, URISyntaxException, SerializationException, DeserializationException {
        HttpResponse response = HttpHelper.execute(method, url, input);
        Assert.assertEquals(toHttpStatusCode(statusCode), response.statusCode());
        Page<T> actual = HttpHelper.readResponsePage(response, type);
        if (expected != null) {
            Assert.assertEquals(expected, actual.getContent());
        }
        return actual;
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


    private static void clearSubmodelElementCollections(Submodel submodel) {
        submodel.getSubmodelElements().forEach(x -> {
            if (SubmodelElementCollection.class.isAssignableFrom(x.getClass())) {
                ((SubmodelElementCollection) x).getValue().clear();
            }
        });
    }

}
