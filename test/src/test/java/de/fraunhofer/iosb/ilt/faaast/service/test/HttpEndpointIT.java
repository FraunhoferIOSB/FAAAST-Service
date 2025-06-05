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
import static de.fraunhofer.iosb.ilt.faaast.service.test.util.MessageBusHelper.DEFAULT_TIMEOUT;
import static de.fraunhofer.iosb.ilt.faaast.service.test.util.MessageBusHelper.assertEvent;
import static de.fraunhofer.iosb.ilt.faaast.service.test.util.MessageBusHelper.assertEvents;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AbstractAssetOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.EnvironmentSerializationManager;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.ValueOnlyJsonSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.Endpoint;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpConstants;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.memory.FileStorageInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.EnvironmentContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationAsyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.proprietary.ImportResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.UnsupportedContentModifierException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.UnsupportedModifierException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationFinishEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationInvokeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.util.QueryModifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.request.RequestHandlerManager;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.StaticRequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.util.Path;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.ApiPaths;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ExtendHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReflectionHelper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.awaitility.Awaitility;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.InMemoryFile;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.BaseOperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultBaseOperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultFile;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultResource;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;


public class HttpEndpointIT extends AbstractIntegrationTest {

    private static SubmodelElementIdentifier operationSquareIdentifier;
    private static Operation operationSquare;
    private static final String OPERATION_SQUARE_INPUT_PARAMETER_ID = "in";
    private static final String OPERATION_SQUARE_INOUTPUT_PARAMETER_ID = "note";
    private static final String OPERATION_SQUARE_OUTPUT_PARAMETER_ID = "square";
    private static final String OPERATION_SQUARE_INOUTPUT_PARAMETER_INITIAL_VALUE = "original value";
    private static final String OPERATION_SQUARE_INOUTPUT_PARAMETER_EXPECTED_VALUE = "updated value";
    private static final Duration DEFAULT_OPERATION_TIMEOUT = DatatypeFactory.newDefaultInstance().newDuration("PT10S");
    private static final String LOCATION_HEADER = "Location";
    private static Service service;
    private static Environment environment;
    private static ApiPaths apiPaths;
    private static MessageBus messageBus;
    private static AssetConnectionManager assetConnectionManager;
    private static RequestHandlerManager requestHandlerManager;
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

    private final ObjectMapper mapper;

    public HttpEndpointIT() {
        this.mapper = new ObjectMapper();
    }


    @BeforeClass
    public static void initClass() throws IOException, GeneralSecurityException {
        PORT = PortHelper.findFreePort();
        apiPaths = new ApiPaths(HOST, PORT);
    }


    @Before
    public void init() throws Exception {
        environment = AASFull.createEnvironment();
        operationSquare = (Operation) AASFull.getFAAASTSubmodel(environment)
                .getSubmodelElements().stream()
                .filter(x -> Operation.class.isAssignableFrom(x.getClass()))
                .findFirst().get();
        operationSquareIdentifier = SubmodelElementIdentifier.fromReference(EnvironmentHelper.asReference(operationSquare, environment));
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
                        .sni(false)
                        .certificate(CertificateConfig.builder()
                                .keyStorePath(httpEndpointKeyStoreFile)
                                .keyStoreType(HTTP_ENDPOINT_KEYSTORE_TYPE)
                                .keyPassword(HTTP_ENDPOINT_KEYSTORE_PASSWORD)
                                .keyStorePassword(HTTP_ENDPOINT_KEYSTORE_PASSWORD)
                                .build())
                        .build()))
                .messageBus(MessageBusInternalConfig.builder()
                        .build())
                .build();
        service = spy(new Service(serviceConfig));
        messageBus = service.getMessageBus();
        injectSpyAssetConnectionManager(serviceConfig);
        service.start();
    }


    private void injectSpyAssetConnectionManager(ServiceConfig serviceConfig) throws SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
        assetConnectionManager = spy(service.getAssetConnectionManager());
        ReflectionHelper.setField(service, "assetConnectionManager", assetConnectionManager);
        StaticRequestExecutionContext requestExecutionContext = new StaticRequestExecutionContext(
                serviceConfig.getCore(),
                ReflectionHelper.getField(service, "persistence", Persistence.class),
                ReflectionHelper.getField(service, "fileStorage", FileStorage.class),
                messageBus,
                assetConnectionManager);
        ReflectionHelper.setField(service, "requestExecutionContext", requestExecutionContext);
        List<Endpoint> endpoints = (List<Endpoint>) ReflectionHelper.getField(service, "endpoints", List.class);
        for (var endpoint: endpoints) {
            ReflectionHelper.setField(endpoint, "serviceContext", service);
        }
    }


    @After
    public void shutdown() {
        service.stop();
    }


    private void mockOperation(Reference reference, BiFunction<OperationVariable[], OperationVariable[], OperationVariable[]> logic) {
        when(assetConnectionManager.hasOperationProvider(reference))
                .thenReturn(true);
        when(assetConnectionManager.getOperationProvider(reference))
                .thenReturn(new AssetOperationProvider() {
                    @Override
                    public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
                        return logic.apply(input, inoutput);
                    }


                    public AssetOperationProviderConfig getConfig() {
                        return new AbstractAssetOperationProviderConfig() {};
                    }
                });
    }


    @Test
    public void testAASBasicDiscoveryCreate()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);
        SpecificAssetId newIdentifier = new DefaultSpecificAssetId.Builder()
                .name("foo")
                .value("bar")
                .build();
        List<SpecificAssetId> expected = new ArrayList<>();
        expected.add(newIdentifier);
        expected.addAll(aas.getAssetInformation().getSpecificAssetIds());
        expected.add(new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(aas.getAssetInformation().getGlobalAssetId())
                .build());
        assertExecuteMultiple(
                HttpMethod.POST,
                apiPaths.aasBasicDiscovery().assetAdministrationShell(aas),
                StatusCode.SUCCESS_CREATED,
                List.of(newIdentifier),
                expected,
                SpecificAssetId.class);
    }


    @Test
    public void testAASBasicDiscoveryDelete()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);
        aas.getAssetInformation().getSpecificAssetIds().clear();
        aas.getAssetInformation().setGlobalAssetId(null);
        assertExecute(
                HttpMethod.DELETE,
                apiPaths.aasBasicDiscovery().assetAdministrationShell(aas),
                StatusCode.SUCCESS_NO_CONTENT);
        assertExecuteMultiple(
                HttpMethod.GET,
                apiPaths.aasBasicDiscovery().assetAdministrationShell(aas),
                StatusCode.SUCCESS,
                null,
                List.of(),
                SpecificAssetId.class);
    }


    @Test
    public void testAASBasicDiscoveryGetAssetAdministrationShells()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        List<String> expected = environment.getAssetAdministrationShells().stream()
                .map(x -> x.getId())
                .collect(Collectors.toList());
        assertExecutePage(
                HttpMethod.GET,
                apiPaths.aasBasicDiscovery().assetAdministrationShells(),
                StatusCode.SUCCESS,
                null,
                expected,
                String.class);
    }


    @Test
    public void testAASBasicDiscoveryGetAssetAdministrationShellsByGlobalAssetId()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        String assetIdValue = "https://acplt.org/Test_Asset";
        List<String> expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getAssetInformation().getGlobalAssetId().equalsIgnoreCase(assetIdValue))
                .map(x -> x.getId())
                .collect(Collectors.toList());
        assertExecutePage(
                HttpMethod.GET,
                apiPaths.aasBasicDiscovery().assetAdministrationShells(Map.of(FaaastConstants.KEY_GLOBAL_ASSET_ID, assetIdValue)),
                StatusCode.SUCCESS,
                null,
                expected,
                String.class);
    }


    @Test
    public void testAASBasicDiscoveryGetAssetLinks()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);
        List<SpecificAssetId> expected = new ArrayList<>(aas.getAssetInformation().getSpecificAssetIds());
        expected.add(new DefaultSpecificAssetId.Builder()
                .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                .value(aas.getAssetInformation().getGlobalAssetId())
                .build());
        assertExecuteMultiple(
                HttpMethod.GET,
                apiPaths.aasBasicDiscovery().assetAdministrationShell(aas),
                StatusCode.SUCCESS,
                null,
                expected,
                SpecificAssetId.class);
    }


    @Test
    public void testAASRepositoryCreateAssetAdministrationShells()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
        String id = "http://newOne";
        AssetAdministrationShell expected = new DefaultAssetAdministrationShell.Builder()
                .id(id)
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
                        x -> {
                            var response = assertExecuteSingle(
                                    HttpMethod.POST,
                                    apiPaths.aasRepository().assetAdministrationShells(),
                                    StatusCode.SUCCESS_CREATED,
                                    expected,
                                    expected,
                                    AssetAdministrationShell.class);
                            Optional<String> location = response.headers().firstValue(LOCATION_HEADER);
                            Assert.assertTrue(location.isPresent());
                            Assert.assertEquals(String.format("/%s", EncodingHelper.base64UrlEncode(id)), location.get());
                        }));
        Assert.assertTrue(HttpHelper.getPage(
                httpClient,
                apiPaths.aasRepository().assetAdministrationShells(),
                AssetAdministrationShell.class)
                .getContent()
                .contains(expected));
    }


    @Test
    public void testAASRepositoryDeleteAssetAdministrationShell()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        Page<AssetAdministrationShell> before = HttpHelper.getPage(
                httpClient,
                apiPaths.aasRepository().assetAdministrationShells(),
                AssetAdministrationShell.class);
        Assert.assertTrue(before.getContent().contains(expected));
        assertEvent(
                messageBus,
                ElementDeleteEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecute(HttpMethod.DELETE,
                                apiPaths.aasRepository().assetAdministrationShell(expected),
                                StatusCode.SUCCESS_NO_CONTENT)));
        Page<AssetAdministrationShell> actual = HttpHelper.getPage(
                httpClient,
                apiPaths.aasRepository().assetAdministrationShells(),
                AssetAdministrationShell.class);
        Assert.assertFalse(actual.getContent().contains(expected));
    }


    @Test
    public void testAASRepositoryDeleteAssetAdministrationShellNotExists()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
        HttpResponse<String> response = HttpHelper.delete(
                httpClient,
                apiPaths.aasRepository().assetAdministrationShell("non-existant"));
        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND), response.statusCode());
    }


    @Test
    public void testAASRepositoryGetAssetAdministrationShellById()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                apiPaths.aasRepository().assetAdministrationShell(expected),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                AssetAdministrationShell.class)));
    }


    @Test
    public void testAASRepositoryGetAssetAdministrationShellByIdContentReference()
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
                                apiPaths.aasRepository().assetAdministrationShell(aas, Content.REFERENCE),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                Reference.class)));
    }


    @Test
    public void testAASRepositoryGetAssetAdministrationShellByIdUsingSubmodelIdReturnsResourceNotFound()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException, UnsupportedModifierException {
        String submodelId = environment.getSubmodels().get(1).getId();
        assertExecuteSingle(HttpMethod.GET,
                apiPaths.aasRepository().assetAdministrationShell(submodelId),
                StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND,
                null,
                null,
                AssetAdministrationShell.class);
    }


    @Test
    public void testAASRepositoryGetAssetAdministrationShellByIdNotExists()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
        HttpResponse<String> response = HttpHelper.get(
                httpClient,
                apiPaths.aasRepository().assetAdministrationShell("non-existant"));
        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND), response.statusCode());
    }


    @Test
    public void testAASRepositoryGetAssetAdministrationShells()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        List<AssetAdministrationShell> expected = environment.getAssetAdministrationShells();
        assertExecutePage(
                HttpMethod.GET,
                apiPaths.aasRepository().assetAdministrationShells(),
                StatusCode.SUCCESS,
                null,
                expected,
                AssetAdministrationShell.class);
    }


    @Test
    public void testAASRepositoryGetAssetAdministrationShellsContentReference()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        List<Reference> expected = environment.getAssetAdministrationShells().stream()
                .map(ReferenceBuilder::forAas)
                .collect(Collectors.toList());
        assertExecutePage(
                HttpMethod.GET,
                apiPaths.aasRepository().assetAdministrationShells(Content.REFERENCE),
                StatusCode.SUCCESS,
                null,
                expected,
                Reference.class);
    }


    @Test
    public void testAASRepositoryGetAssetAdministrationShellsWithPaging()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        // 4 elements in total are available, query data with increasing page size 1, 2, 3.
        String cursor = null;
        List<AssetAdministrationShell> allExpectedShells = environment.getAssetAdministrationShells();
        List<AssetAdministrationShell> allActualShells = new ArrayList<>();
        Page<AssetAdministrationShell> page1 = assertExecutePage(
                HttpMethod.GET,
                apiPaths.aasRepository().assetAdministrationShells(cursor, 1),
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
                apiPaths.aasRepository().assetAdministrationShells(cursor, 2),
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
                apiPaths.aasRepository().assetAdministrationShells(cursor, 3),
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
                                apiPaths.aasRepository().assetAdministrationShell(expected),
                                StatusCode.SUCCESS_NO_CONTENT,
                                expected,
                                null,
                                AssetAdministrationShell.class)));
    }


    @Test
    public void testAASSerializationJSON()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, ResourceNotFoundException,
            NoSuchAlgorithmException, KeyManagementException {
        assertSerialization(
                List.of(environment.getAssetAdministrationShells().get(0)),
                true,
                MediaType.JSON_UTF_8,
                DataFormat.JSON);
    }


    @Test
    public void testAASSerializationXML()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, ResourceNotFoundException,
            NoSuchAlgorithmException, KeyManagementException {
        assertSerialization(
                List.of(environment.getAssetAdministrationShells().get(0)),
                true,
                MediaType.APPLICATION_XML_UTF_8,
                DataFormat.XML);
    }


    @Ignore("Failing because of charset issues, probably caused by admin-shell.io library not respecting charset for de-/serialization")
    @Test
    public void testAASSerializationRDF()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, ResourceNotFoundException,
            NoSuchAlgorithmException, KeyManagementException {
        assertSerialization(
                List.of(environment.getAssetAdministrationShells().get(0)),
                true,
                DataFormat.RDF.getContentType(),
                DataFormat.RDF);
    }


    @Test
    public void testAASSerializationAASX()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, ResourceNotFoundException,
            NoSuchAlgorithmException, KeyManagementException {
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
                .addPart("fileName", new StringBody(fileName, ContentType.create("text/plain", StandardCharsets.UTF_8)))
                .addBinaryBody("file", content, ContentType.APPLICATION_PDF, fileName)
                .build();
        HttpResponse<byte[]> putFileResponse = httpClient.send(HttpRequest.newBuilder()
                .uri(new URI(apiPaths.submodelRepository()
                        .submodelInterface(defaultEnvironment.getSubmodels().get(0)).submodelElement("ExampleSubmodelElementCollection.ExampleFile")
                        + "/attachment"))
                .header(HttpConstants.HEADER_ACCEPT, DataFormat.JSON.getContentType().toString())
                .header(HttpConstants.HEADER_CONTENT_TYPE, httpEntity.getContentType())
                .PUT(BodyPublishers.ofInputStream(LambdaExceptionHelper.wrap(httpEntity::getContent)))
                .build(),
                HttpResponse.BodyHandlers.ofByteArray());
        Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS_NO_CONTENT), putFileResponse.statusCode());
        HttpResponse<byte[]> response = httpClient.send(HttpRequest.newBuilder()
                .uri(new URI(apiPaths.aasSerialization().serialization(List.of(aas), defaultEnvironment.getSubmodels(), true)))
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
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, ResourceNotFoundException,
            NoSuchAlgorithmException, KeyManagementException {
        assertSerialization(
                List.of(environment.getAssetAdministrationShells().get(0)),
                true,
                MediaType.ANY_TYPE,
                DataFormat.JSON);
    }


    @Test
    public void testAASSerializationInvalidDataformat()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, NoSuchAlgorithmException,
            KeyManagementException, GeneralSecurityException {
        HttpResponse<byte[]> response = httpClient.send(HttpRequest.newBuilder()
                .uri(new URI(apiPaths.aasSerialization().serialization(List.of(), List.of(), false)))
                .header(HttpConstants.HEADER_ACCEPT, MediaType.ANY_VIDEO_TYPE.toString())
                .GET()
                .build(),
                HttpResponse.BodyHandlers.ofByteArray());
        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_ERROR_BAD_REQUEST), response.statusCode());
    }


    private void assertSerialization(List<AssetAdministrationShell> aass, boolean includeConceptDescriptions, MediaType contentType, DataFormat expectedFormat)
            throws IOException, InterruptedException, SerializationException, URISyntaxException, DeserializationException, ResourceNotFoundException, NoSuchAlgorithmException,
            KeyManagementException {
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
        HttpResponse<byte[]> response = httpClient.send(HttpRequest.newBuilder()
                .uri(new URI(apiPaths.aasSerialization().serialization(aass, expected.getSubmodels(), includeConceptDescriptions)))
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
    public void testAssetAdministrationShellInterfaceCreateSubmodelRef()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException, UnsupportedModifierException {
        String name = "test";
        Reference newReference = new DefaultReference.Builder()
                .keys(new DefaultKey.Builder()
                        .value(name)
                        .build())
                .build();
        AssetAdministrationShell aas = DeepCopyHelper.deepCopy(environment.getAssetAdministrationShells().get(1));
        aas.getSubmodels().add(newReference);
        List<Reference> expected = aas.getSubmodels();
        assertEvent(
                messageBus,
                ElementUpdateEventMessage.class,
                aas,
                LambdaExceptionHelper.wrap(
                        x -> {
                            var response = assertExecuteSingle(
                                    HttpMethod.POST,
                                    apiPaths.aasInterface(aas).submodels(),
                                    StatusCode.SUCCESS_CREATED,
                                    newReference,
                                    newReference,
                                    Reference.class);
                            Optional<String> location = response.headers().firstValue(LOCATION_HEADER);
                            Assert.assertTrue(location.isPresent());
                            Assert.assertEquals(String.format("/%s", EncodingHelper.base64UrlEncode(name)), location.get());
                        }));
        assertExecutePage(
                HttpMethod.GET,
                apiPaths.aasInterface(aas).submodels(),
                StatusCode.SUCCESS,
                null,
                expected,
                Reference.class);
    }


    @Test
    public void testAssetAdministrationShellInterfaceDeleteSubmodelRef()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException, UnsupportedModifierException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Reference submodelToDelete = aas.getSubmodels().get(0);
        aas.getSubmodels().remove(submodelToDelete);
        Page<Reference> before = HttpHelper.readResponsePage(
                HttpHelper.execute(httpClient, HttpMethod.GET, apiPaths.aasInterface(aas).submodels()),
                Reference.class);
        Assert.assertTrue(before.getContent().contains(submodelToDelete));
        assertEvent(
                messageBus,
                EventMessage.class,
                aas,
                LambdaExceptionHelper.wrap(
                        x -> assertExecute(
                                HttpMethod.DELETE,
                                apiPaths.aasInterface(aas).submodelRefs(submodelToDelete),
                                StatusCode.SUCCESS_NO_CONTENT)));
        Page<Reference> actual = HttpHelper.readResponsePage(
                HttpHelper.execute(httpClient, HttpMethod.GET, apiPaths.aasInterface(aas).submodels()),
                Reference.class);
        Assert.assertFalse(actual.getContent().contains(submodelToDelete));
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
                                apiPaths.aasInterface(expected).assetAdministrationShell(),
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
                                apiPaths.aasInterface(aas).assetAdministrationShell(Content.REFERENCE),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                expected.getClass())));
    }


    @Test
    public void testAssetAdministrationShellInterfaceGetAssetAdministrationShellNotExists()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
        HttpResponse<String> response = HttpHelper.get(
                httpClient,
                apiPaths.aasInterface("non-existant").assetAdministrationShell());
        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND), response.statusCode());
    }


    @Test
    public void testAssetAdministrationShellInterfaceGetAssetInformation()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        AssetInformation expected = aas.getAssetInformation();
        // TODO does this trigger any message bus event?
        assertExecuteSingle(
                HttpMethod.GET,
                apiPaths.aasInterface(aas).assetInformation(),
                StatusCode.SUCCESS,
                null,
                expected,
                AssetInformation.class);
    }


    @Test
    public void testAssetAdministrationShellInterfaceGetSubmodelRefs()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        List<Reference> expected = aas.getSubmodels();
        assertExecutePage(
                HttpMethod.GET,
                apiPaths.aasInterface(aas).submodels(),
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
                                apiPaths.aasRepository().assetAdministrationShell(expected),
                                StatusCode.SUCCESS_NO_CONTENT,
                                expected,
                                null,
                                AssetAdministrationShell.class)));
    }


    @Test
    public void testAssetAdministrationShellInterfaceUpdateAssetInformation()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, NoSuchAlgorithmException,
            KeyManagementException, UnsupportedModifierException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        AssetInformation expected = aas.getAssetInformation();
        expected.getSpecificAssetIds().add(new DefaultSpecificAssetId.Builder()
                .name("foo")
                .value("bar")
                .build());
        // TODO does this trigger any message bus event?
        assertExecuteSingle(
                HttpMethod.PUT,
                apiPaths.aasInterface(aas).assetInformation(),
                StatusCode.SUCCESS_NO_CONTENT,
                expected,
                null,
                AssetInformation.class);
    }


    @Test
    public void testConceptDescriptionRepositoryCreateConceptDescription()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
        String id = "http://example.org/foo";
        ConceptDescription expected = new DefaultConceptDescription.Builder()
                .id(id)
                .idShort("created")
                .build();
        assertEvent(
                messageBus,
                ElementCreateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> {
                            var response = assertExecuteSingle(
                                    HttpMethod.POST,
                                    apiPaths.conceptDescriptionRepository().conceptDescriptions(),
                                    StatusCode.SUCCESS_CREATED,
                                    expected,
                                    expected,
                                    ConceptDescription.class);
                            Optional<String> location = response.headers().firstValue(LOCATION_HEADER);
                            Assert.assertTrue(location.isPresent());
                            Assert.assertEquals(String.format("/%s", EncodingHelper.base64UrlEncode(id)), location.get());
                        }));
        Assert.assertTrue(HttpHelper.getPage(
                httpClient,
                apiPaths.conceptDescriptionRepository().conceptDescriptions(),
                ConceptDescription.class)
                .getContent()
                .contains(expected));
    }


    @Test
    public void testConceptDescriptionRepositoryDeleteConceptDescription()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
        ConceptDescription expected = environment.getConceptDescriptions().get(0);
        Page<ConceptDescription> before = HttpHelper.getPage(
                httpClient,
                apiPaths.conceptDescriptionRepository().conceptDescriptions(),
                ConceptDescription.class);
        Assert.assertTrue(before.getContent().contains(expected));
        assertEvent(
                messageBus,
                ElementDeleteEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecute(
                                HttpMethod.DELETE,
                                apiPaths.conceptDescriptionRepository().conceptDescription(expected),
                                StatusCode.SUCCESS_NO_CONTENT)));
        Page<ConceptDescription> actual = HttpHelper.getPage(
                httpClient,
                apiPaths.conceptDescriptionRepository().conceptDescriptions(),
                ConceptDescription.class);
        Assert.assertFalse(actual.getContent().contains(expected));
    }


    @Test
    public void testConceptDescriptionRepositoryDeleteConceptDescriptionNotExists()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
        HttpResponse<String> response = HttpHelper.delete(
                httpClient,
                apiPaths.conceptDescriptionRepository().conceptDescription("non-existant"));
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
                                apiPaths.conceptDescriptionRepository().conceptDescription(expected),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                ConceptDescription.class)));
    }


    @Test
    public void testConceptDescriptionRepositoryGetConceptDescriptionNotExists()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
        HttpResponse<String> response = HttpHelper.get(
                httpClient,
                apiPaths.conceptDescriptionRepository().conceptDescription("non-existant"));
        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND), response.statusCode());
    }


    @Test
    public void testConceptDescriptionRepositoryGetConceptDescriptions()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        List<ConceptDescription> expected = environment.getConceptDescriptions();
        assertExecutePage(
                HttpMethod.GET,
                apiPaths.conceptDescriptionRepository().conceptDescriptions(),
                StatusCode.SUCCESS,
                null,
                expected,
                ConceptDescription.class);
    }


    @Test
    public void testConceptDescriptionRepositoryUpdateConceptDescription()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, NoSuchAlgorithmException,
            KeyManagementException {
        ConceptDescription expected = environment.getConceptDescriptions().get(0);
        expected.setIdShort("changed");
        assertEvent(
                messageBus,
                ElementUpdateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.PUT,
                                apiPaths.conceptDescriptionRepository().conceptDescription(expected),
                                StatusCode.SUCCESS_NO_CONTENT,
                                expected,
                                null,
                                ConceptDescription.class)));
        Assert.assertTrue(HttpHelper.getPage(
                httpClient,
                apiPaths.conceptDescriptionRepository().conceptDescriptions(),
                ConceptDescription.class)
                .getContent()
                .contains(expected));
    }


    @Test
    public void testSubmodelInterfaceCreateSubmodelElement()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, NoSuchAlgorithmException,
            KeyManagementException {
        Submodel submodel = environment.getSubmodels().get(0);
        String id = "newProperty";
        SubmodelElement expected = new DefaultProperty.Builder()
                .idShort(id)
                .build();
        assertEvent(
                messageBus,
                ElementCreateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> {
                            var response = assertExecuteSingle(
                                    HttpMethod.POST,
                                    apiPaths.submodelRepository().submodelInterface(submodel).submodelElements(),
                                    StatusCode.SUCCESS_CREATED,
                                    expected,
                                    expected,
                                    SubmodelElement.class);
                            Optional<String> location = response.headers().firstValue(LOCATION_HEADER);
                            Assert.assertTrue(location.isPresent());
                            Assert.assertEquals(String.format("/%s", id), location.get());
                        }));
        Page<SubmodelElement> actual = HttpHelper.getPage(
                httpClient,
                apiPaths.submodelRepository().submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertTrue(actual.getContent().contains(expected));
    }


    @Test
    public void testSubmodelInterfaceCreateSubmodelElementInsideList()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
        Submodel submodel = environment.getSubmodels().get(2);
        SubmodelElementList submodelElementList = (SubmodelElementList) submodel.getSubmodelElements().get(5);
        SubmodelElement expected = new DefaultProperty.Builder()
                .idShort("newProperty")
                .build();
        int expectedIndex = submodelElementList.getValue().size();
        assertEvent(
                messageBus,
                ElementCreateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> {
                            var response = assertExecuteSingle(
                                    HttpMethod.POST,
                                    apiPaths.submodelRepository().submodelInterface(submodel).submodelElement(submodelElementList),
                                    StatusCode.SUCCESS_CREATED,
                                    expected,
                                    expected,
                                    SubmodelElement.class);
                            Optional<String> location = response.headers().firstValue(LOCATION_HEADER);
                            Assert.assertTrue(location.isPresent());
                            Assert.assertEquals(String.format(".%d", expectedIndex), location.get());
                        }));
        SubmodelElement actual = HttpHelper.getWithSingleResult(
                httpClient,
                apiPaths.submodelRepository()
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
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
        Submodel submodel = environment.getSubmodels().get(2);
        SubmodelElementCollection submodelElementCollection = (SubmodelElementCollection) submodel.getSubmodelElements().get(6);
        String id = "newProperty";
        SubmodelElement expected = new DefaultProperty.Builder()
                .idShort(id)
                .build();
        assertEvent(
                messageBus,
                ElementCreateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> {
                            var response = assertExecuteSingle(
                                    HttpMethod.POST,
                                    apiPaths.submodelRepository().submodelInterface(submodel).submodelElement(submodelElementCollection),
                                    StatusCode.SUCCESS_CREATED,
                                    expected,
                                    expected,
                                    SubmodelElement.class);
                            Optional<String> location = response.headers().firstValue(LOCATION_HEADER);
                            Assert.assertTrue(location.isPresent());
                            Assert.assertEquals(String.format(".%s", id), location.get());
                        }));
        SubmodelElement actual = HttpHelper.getWithSingleResult(
                httpClient,
                apiPaths.submodelRepository()
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
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
        Submodel submodel = environment.getSubmodels().get(0);
        SubmodelElement expected = submodel.getSubmodelElements().get(0);
        Page<SubmodelElement> before = HttpHelper.getPage(
                httpClient,
                apiPaths.submodelRepository().submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertTrue(before.getContent().contains(expected));
        assertEvent(
                messageBus,
                ElementDeleteEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecute(
                                HttpMethod.DELETE,
                                apiPaths.submodelRepository().submodelInterface(submodel).submodelElement(expected),
                                StatusCode.SUCCESS_NO_CONTENT)));
        Page<SubmodelElement> actual = HttpHelper.getPage(
                httpClient,
                apiPaths.submodelRepository().submodelInterface(submodel).submodelElements(),
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
                                apiPaths.submodelRepository().submodelInterface(expected).submodel(),
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
                                apiPaths.submodelRepository().submodelInterface(submodel).submodel(Content.REFERENCE),
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
                                apiPaths.submodelRepository().submodelInterface(submodel).submodelElement(expected),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                SubmodelElement.class)));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelElementExtentWithoutBlobValue()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, ResourceNotFoundException {
        SubmodelElementIdentifier identifier = SubmodelElementIdentifier.builder()
                .submodelId("https://acplt.org/Test_Submodel_Missing")
                .idShortPath(IdShortPath.builder()
                        .pathSegment("ExampleSubmodelElementCollection")
                        .pathSegment("ExampleBlob")
                        .build())
                .build();
        Blob blob = EnvironmentHelper.resolve(identifier.toReference(), environment, Blob.class);
        Blob expected = DeepCopyHelper.deepCopy(blob);
        ExtendHelper.withoutBlobValue(expected);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                apiPaths.submodelRepository().submodelInterface(identifier.getSubmodelId()).submodelElement(identifier.getIdShortPath(), Extent.WITHOUT_BLOB_VALUE),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                SubmodelElement.class)));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelElementExtentWithBlobValue()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, ResourceNotFoundException {
        SubmodelElementIdentifier identifier = SubmodelElementIdentifier.builder()
                .submodelId("https://acplt.org/Test_Submodel_Missing")
                .idShortPath(IdShortPath.builder()
                        .pathSegment("ExampleSubmodelElementCollection")
                        .pathSegment("ExampleBlob")
                        .build())
                .build();
        Blob expected = EnvironmentHelper.resolve(identifier.toReference(), environment, Blob.class);
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                apiPaths.submodelRepository().submodelInterface(identifier.getSubmodelId()).submodelElement(identifier.getIdShortPath(), Extent.WITH_BLOB_VALUE),
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
        Reference expected = new ReferenceBuilder()
                .submodel(submodel)
                .element(submodelElement)
                .build();
        assertEvent(
                messageBus,
                ElementReadEventMessage.class,
                submodelElement,
                LambdaExceptionHelper.wrap(
                        x -> assertExecuteSingle(
                                HttpMethod.GET,
                                apiPaths.submodelRepository().submodelInterface(submodel).submodelElement(submodelElement, Content.REFERENCE),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                Reference.class)));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelElements()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        Submodel submodel = environment.getSubmodels().get(0);
        List<SubmodelElement> expected = submodel.getSubmodelElements();
        assertExecutePage(
                HttpMethod.GET,
                apiPaths.submodelRepository().submodelInterface(submodel).submodelElements(),
                StatusCode.SUCCESS,
                null,
                expected,
                SubmodelElement.class);
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelElementsContentReference()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        Submodel submodel = environment.getSubmodels().get(0);
        List<Reference> expected = submodel
                .getSubmodelElements()
                .stream()
                .map(x -> ReferenceBuilder.forSubmodel(submodel, x))
                .collect(Collectors.toList());
        assertExecutePage(
                HttpMethod.GET,
                apiPaths.submodelRepository().submodelInterface(submodel).submodelElements(Content.REFERENCE),
                StatusCode.SUCCESS,
                null,
                expected,
                Reference.class);
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelContentValue()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, UnsupportedModifierException {
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
                            HttpResponse<String> response = HttpHelper.get(
                                    httpClient,
                                    apiPaths.submodelRepository().submodelInterface(submodel).submodel(Content.VALUE));
                            Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), response.statusCode());
                            JSONAssert.assertEquals(expected, response.body(), false);
                        }));
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
    public void testAASThumbnail()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
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
        HttpResponse<byte[]> putThumbnailResponse = httpClient.send(HttpRequest.newBuilder()
                .uri(new URI(apiPaths.aasInterface(aas).assetInformation()
                        + "/thumbnail"))
                .header(HttpConstants.HEADER_ACCEPT, DataFormat.JSON.getContentType().toString())
                .header(HttpConstants.HEADER_CONTENT_TYPE, httpEntity.getContentType())
                .PUT(BodyPublishers.ofInputStream(LambdaExceptionHelper.wrap(httpEntity::getContent)))
                .build(),
                HttpResponse.BodyHandlers.ofByteArray());
        Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS_NO_CONTENT), putThumbnailResponse.statusCode());
        assertExecuteSingle(
                HttpMethod.GET,
                apiPaths.aasInterface(aas).assetInformation(),
                StatusCode.SUCCESS,
                null,
                expected,
                AssetInformation.class);
        HttpResponse<byte[]> deleteThumbnailResponse = httpClient.send(HttpRequest.newBuilder()
                .uri(new URI(apiPaths.aasInterface(aas).assetInformation()
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
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, UnsupportedModifierException {
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
                            HttpResponse<String> response = HttpHelper.get(
                                    httpClient,
                                    apiPaths.submodelRepository()
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
                                apiPaths.submodelRepository().submodelInterface(expected).submodel(Level.CORE),
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
                            HttpResponse<String> response = HttpHelper.get(
                                    httpClient,
                                    apiPaths.submodelRepository()
                                            .submodelInterface(submodel)
                                            .submodel(Level.DEEP, Content.PATH));
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
                            HttpResponse<String> response = HttpHelper.get(
                                    httpClient,
                                    apiPaths.submodelRepository()
                                            .submodelInterface(submodel)
                                            .submodel(Level.CORE, Content.PATH));
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
                                apiPaths.submodelRepository().submodelInterface(expected).submodel(Level.DEEP),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                Submodel.class)));
    }


    @Test
    public void testSubmodelInterfaceInvokeOperationAsync()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException, UnsupportedModifierException {
        int inputValue = 4;
        Reference reference = operationSquareIdentifier.toReference();
        CountDownLatch condition = new CountDownLatch(1);
        mockOperation(reference, (input, inoutput) -> {
            try {
                condition.await();
                return operationSqaureDefaultImplementation(input, inoutput);
            }
            catch (InterruptedException ex) {
                throw new RuntimeException();
            }
        });
        AtomicReference<String> operationStatusUrl = new AtomicReference<>();
        // assert OperationStarted on messagebus

        assertEvent(
                messageBus,
                OperationInvokeEventMessage.class,
                null,
                LambdaExceptionHelper.wrap(
                        x -> {
                            HttpResponse response = assertExecuteSingle(
                                    HttpMethod.POST,
                                    apiPaths.submodelRepository().submodelInterface(operationSquareIdentifier.getSubmodelId())
                                            .invokeAsync(operationSquareIdentifier.getIdShortPath()),
                                    StatusCode.SUCCESS_ACCEPTED,
                                    getOperationSqaureInvokeRequest(InvokeOperationAsyncRequest.builder(), inputValue), //input
                                    null,
                                    null);
                            Optional<String> locationHeader = response.headers().firstValue(HttpConstants.HEADER_LOCATION);

                            Assert.assertTrue(locationHeader.isPresent());
                            Assert.assertTrue(locationHeader.get().contains("operation-status/"));
                            operationStatusUrl.set(response.uri().resolve(locationHeader.get()).toString());
                        }));
        // assert operation is still running
        BaseOperationResult expectedStatusRunning = new DefaultBaseOperationResult();

        expectedStatusRunning.setExecutionState(ExecutionState.RUNNING);

        assertExecuteSingle(
                HttpMethod.GET,
                operationStatusUrl.get(),
                StatusCode.SUCCESS,
                null, //input
                expectedStatusRunning,
                BaseOperationResult.class);
        // assert OperationFinished on messagebus
        assertEvent(messageBus, OperationFinishEventMessage.class,
                null, x -> {
                    condition.countDown();

                    Awaitility.await()
                            .pollInterval(100, TimeUnit.MILLISECONDS)
                            .atMost(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
                            .until(() -> HttpHelper.execute(httpClient, HttpMethod.GET, operationStatusUrl.get()).statusCode() == toHttpStatusCode(StatusCode.SUCCESS_FOUND));
                });
        // assert status is finished and returns 302 with correct Location header
        AtomicReference<String> operationResultUrl = new AtomicReference<>();
        HttpResponse responseStatusFinished = assertExecuteSingle(
                HttpMethod.GET,
                operationStatusUrl.get(),
                StatusCode.SUCCESS_FOUND,
                null,
                null,
                null);
        Optional<String> locationHeader = responseStatusFinished.headers().firstValue(HttpConstants.HEADER_LOCATION);

        Assert.assertTrue(locationHeader.isPresent());
        Assert.assertTrue(locationHeader.get().contains("operation-results/"));
        operationResultUrl.set(responseStatusFinished.uri().resolve(locationHeader.get()).toString());
        // assert operation result
        OperationResult expectedResult = getOperationSqaureExpectedResult(ExecutionState.COMPLETED, inputValue);
        assertExecuteSingle(
                HttpMethod.GET,
                operationResultUrl.get(),
                StatusCode.SUCCESS,
                null,
                expectedResult,
                OperationResult.class);
    }


    public static OperationResult getOperationSqaureExpectedResult(ExecutionState executionState, int inputValue) {
        return getOperationSqaureExpectedResult(executionState, inputValue, OPERATION_SQUARE_INOUTPUT_PARAMETER_EXPECTED_VALUE);
    }


    public static OperationResult getOperationSqaureExpectedResult(ExecutionState executionState, int inputValue, String inoutputValue) {
        DefaultOperationResult.Builder builder = new DefaultOperationResult.Builder()
                .executionState(executionState)
                .inoutputArguments(List.of(new DefaultOperationVariable.Builder()
                        .value(new DefaultProperty.Builder()
                                .idShort(OPERATION_SQUARE_INOUTPUT_PARAMETER_ID)
                                .valueType(DataTypeDefXsd.STRING)
                                .value(inoutputValue)
                                .build())
                        .build()));
        if (executionState == ExecutionState.COMPLETED) {
            builder
                    .success(true)
                    .outputArguments(List.of(new DefaultOperationVariable.Builder()
                            .value(new DefaultProperty.Builder()
                                    .idShort(OPERATION_SQUARE_OUTPUT_PARAMETER_ID)
                                    .valueType(DataTypeDefXsd.INT)
                                    .value(Integer.toString(inputValue * inputValue))
                                    .build())
                            .build()));

        }
        return builder.build();
    }


    private static <T extends InvokeOperationRequest> T getOperationSqaureInvokeRequest(T.AbstractBuilder<T, ?> builder, int inputValue) {
        return getOperationSqaureInvokeRequest(
                builder,
                inputValue,
                OPERATION_SQUARE_INOUTPUT_PARAMETER_INITIAL_VALUE,
                DEFAULT_OPERATION_TIMEOUT.toString());
    }


    private static <T extends InvokeOperationRequest> T getOperationSqaureInvokeRequest(
                                                                                        T.AbstractBuilder<T, ?> builder,
                                                                                        int inputValue,
                                                                                        String inoutputValue,
                                                                                        String timeout) {
        return builder.inputArgument(new DefaultOperationVariable.Builder()
                .value(new DefaultProperty.Builder()
                        .idShort(OPERATION_SQUARE_INPUT_PARAMETER_ID)
                        .valueType(DataTypeDefXsd.INT)
                        .value(Integer.toString(inputValue))
                        .build())
                .build())
                .inoutputArgument(new DefaultOperationVariable.Builder()
                        .value(new DefaultProperty.Builder()
                                .idShort(OPERATION_SQUARE_INOUTPUT_PARAMETER_ID)
                                .valueType(DataTypeDefXsd.STRING)
                                .value(inoutputValue)
                                .build())
                        .build())
                .timeout(DatatypeFactory.newDefaultInstance().newDuration(timeout))
                .build();
    }


    @Test
    public void testSubmodelInterfaceInvokeOperationAsyncValueOnly()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException, JSONException, UnsupportedContentModifierException, UnsupportedModifierException {
        int inputValue = 4;

        Reference reference = operationSquareIdentifier.toReference();
        CountDownLatch condition = new CountDownLatch(1);
        mockOperation(reference, (input, inoutput) -> {
            try {
                condition.await();
                return operationSqaureDefaultImplementation(input, inoutput);
            }
            catch (InterruptedException ex) {
                throw new RuntimeException();
            }
        });
        AtomicReference<String> operationStatusUrl = new AtomicReference<>();
        // assert OperationStarted on messagebus
        assertEvent(
                messageBus,
                OperationInvokeEventMessage.class,
                null,
                LambdaExceptionHelper.wrap(
                        x -> {
                            HttpResponse response = assertExecuteSingle(
                                    HttpMethod.POST,
                                    apiPaths.submodelRepository().submodelInterface(operationSquareIdentifier.getSubmodelId())
                                            .invokeAsyncValueOnly(operationSquareIdentifier.getIdShortPath()),
                                    StatusCode.SUCCESS_ACCEPTED,
                                    //                                    getOperationSqaureInvokeRequest(InvokeOperationAsyncRequest.builder(), inputValue),
                                    new JsonApiSerializer().write(
                                            getOperationSqaureInvokeRequest(InvokeOperationAsyncRequest.builder(), inputValue),
                                            new OutputModifier.Builder()
                                                    .content(Content.VALUE)
                                                    .build()),
                                    null,
                                    null);
                            Optional<String> locationHeader = response.headers().firstValue(HttpConstants.HEADER_LOCATION);

                            Assert.assertTrue(locationHeader.isPresent());
                            Assert.assertTrue(locationHeader.get().contains("operation-status/"));
                            operationStatusUrl.set(response.uri().resolve(locationHeader.get()).toString());
                        }));
        // assert operation is still running
        BaseOperationResult expectedStatusRunning = new DefaultBaseOperationResult();
        expectedStatusRunning.setExecutionState(ExecutionState.RUNNING);
        assertExecuteSingle(
                HttpMethod.GET,
                operationStatusUrl.get(),
                StatusCode.SUCCESS,
                null, //input
                expectedStatusRunning,
                BaseOperationResult.class);
        // assert OperationFinished on messagebus
        assertEvent(messageBus, OperationFinishEventMessage.class,
                null, x -> {
                    condition.countDown();
                    Awaitility.await()
                            .pollInterval(100, TimeUnit.MILLISECONDS)
                            .atMost(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
                            .until(() -> HttpHelper.execute(httpClient, HttpMethod.GET, operationStatusUrl.get()).statusCode() == toHttpStatusCode(StatusCode.SUCCESS_FOUND));
                });
        // assert status is finished and returns 302 with correct Location header
        AtomicReference<String> operationResultUrl = new AtomicReference<>();
        HttpResponse responseStatusFinished = assertExecuteSingle(
                HttpMethod.GET,
                operationStatusUrl.get(),
                StatusCode.SUCCESS_FOUND,
                null,
                null,
                null);
        Optional<String> locationHeader = responseStatusFinished.headers().firstValue(HttpConstants.HEADER_LOCATION);
        Assert.assertTrue(locationHeader.isPresent());
        Assert.assertTrue(locationHeader.get().contains("operation-results/"));
        operationResultUrl.set(responseStatusFinished.uri().resolve(locationHeader.get()).toString());
        // assert operation result
        OperationResult expextecResult = getOperationSqaureExpectedResult(ExecutionState.COMPLETED, inputValue);
        HttpResponse response = assertExecuteSingle(
                HttpMethod.GET,
                operationResultUrl.get() + "/$value",
                StatusCode.SUCCESS,
                null,
                null,
                OperationResult.class);
        String expectedPayload = new ValueOnlyJsonSerializer().write(expextecResult);
        JSONAssert.assertEquals(expectedPayload, response.body().toString(), false);
    }


    @Test
    public void testSubmodelInterfaceInvokeOperationSync()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException, ValueFormatException, ValueMappingException {
        int inputValue = 4;
        Reference reference = operationSquareIdentifier.toReference();
        mockOperation(reference, HttpEndpointIT::operationSqaureDefaultImplementation);
        // assert OperationStarted on messagebus
        InvokeOperationSyncRequest request = getOperationSqaureInvokeRequest(InvokeOperationSyncRequest.builder(), inputValue);
        OperationResult expectedResult = getOperationSqaureExpectedResult(ExecutionState.COMPLETED, inputValue);
        // assert both messages
        assertEvents(messageBus,
                List.of(
                        OperationInvokeEventMessage.builder()
                                .element(reference)
                                .input(ElementValueHelper.toValueMap(request.getInputArguments()))
                                .inoutput(ElementValueHelper.toValueMap(request.getInoutputArguments()))
                                .build(),
                        OperationFinishEventMessage.builder()
                                .element(reference)
                                .inoutput(ElementValueHelper.toValueMap(expectedResult.getInoutputArguments()))
                                .output(ElementValueHelper.toValueMap(expectedResult.getOutputArguments()))
                                .build()),
                LambdaExceptionHelper.wrap(x -> assertExecuteSingle(
                        HttpMethod.POST,
                        apiPaths.submodelRepository().submodelInterface(operationSquareIdentifier.getSubmodelId()).invoke(operationSquareIdentifier.getIdShortPath()),
                        StatusCode.SUCCESS,
                        request,
                        expectedResult,
                        OperationResult.class)));
    }


    @Test
    public void testSubmodelInterfaceInvokeOperationSyncValueOnly()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException, ValueFormatException, ValueMappingException {
        int inputValue = 4;
        Reference reference = operationSquareIdentifier.toReference();
        mockOperation(reference, HttpEndpointIT::operationSqaureDefaultImplementation);
        // assert OperationStarted on messagebus
        InvokeOperationSyncRequest request = getOperationSqaureInvokeRequest(InvokeOperationSyncRequest.builder(), inputValue);
        final OperationResult expectedResult = getOperationSqaureExpectedResult(ExecutionState.COMPLETED, inputValue);
        // assert both messages
        assertEvents(messageBus,
                List.of(
                        OperationInvokeEventMessage.builder()
                                .element(reference)
                                .input(ElementValueHelper.toValueMap(request.getInputArguments()))
                                .inoutput(ElementValueHelper.toValueMap(request.getInoutputArguments()))
                                .build(),
                        OperationFinishEventMessage.builder()
                                .element(reference)
                                .inoutput(ElementValueHelper.toValueMap(expectedResult.getInoutputArguments()))
                                .output(ElementValueHelper.toValueMap(expectedResult.getOutputArguments()))
                                .build()),
                LambdaExceptionHelper.wrap(x -> {
                    var response = assertExecuteSingle(
                            HttpMethod.POST,
                            apiPaths.submodelRepository().submodelInterface(operationSquareIdentifier.getSubmodelId()).invokeValueOnly(operationSquareIdentifier.getIdShortPath()),
                            StatusCode.SUCCESS,
                            new ValueOnlyJsonSerializer().write(request),
                            null,
                            OperationResult.class);
                    String expectedPayload = new ValueOnlyJsonSerializer().write(expectedResult);
                    JSONAssert.assertEquals(expectedPayload, response.body().toString(), false);
                }));
    }


    @Test
    public void testSubmodelInterfaceInvokeOperationSyncWithExceptionInOperation()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException, ValueFormatException, ValueMappingException {
        Reference reference = operationSquareIdentifier.toReference();
        mockOperation(reference, (input, inoutput) -> {
            throw new IllegalArgumentException();
        });
        // assert OperationStarted on messagebus
        InvokeOperationSyncRequest request = getOperationSqaureInvokeRequest(InvokeOperationSyncRequest.builder(), -1);
        OperationResult expectedResult = new DefaultOperationResult.Builder()
                .executionState(ExecutionState.FAILED)
                .inoutputArguments(request.getInoutputArguments())
                .build();
        assertEvents(messageBus,
                List.of(
                        OperationInvokeEventMessage.builder()
                                .element(reference)
                                .input(ElementValueHelper.toValueMap(request.getInputArguments()))
                                .inoutput(ElementValueHelper.toValueMap(request.getInoutputArguments()))
                                .build(),
                        OperationFinishEventMessage.builder()
                                .element(reference)
                                .inoutput(ElementValueHelper.toValueMap(request.getInoutputArguments()))
                                .build()),
                LambdaExceptionHelper.wrap(x -> assertExecuteSingle(
                        HttpMethod.POST,
                        apiPaths.submodelRepository().submodelInterface(operationSquareIdentifier.getSubmodelId()).invoke(operationSquareIdentifier.getIdShortPath()),
                        StatusCode.SUCCESS,
                        getOperationSqaureInvokeRequest(InvokeOperationSyncRequest.builder(), -1),
                        expectedResult,
                        OperationResult.class)));
    }


    @Test
    public void testSubmodelInterfaceInvokeOperationAsyncWithExceptionInOperation()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException, ValueMappingException, UnsupportedModifierException {
        Reference reference = operationSquareIdentifier.toReference();
        mockOperation(reference, (input, inoutput) -> {
            throw new UnsupportedOperationException("not implemented");
        });
        AtomicReference<String> operationStatusUrl = new AtomicReference<>();
        InvokeOperationAsyncRequest request = getOperationSqaureInvokeRequest(InvokeOperationAsyncRequest.builder(), -1);
        assertEvents(
                messageBus,
                List.of(
                        OperationInvokeEventMessage.builder()
                                .element(reference)
                                .input(ElementValueHelper.toValueMap(request.getInputArguments()))
                                .inoutput(ElementValueHelper.toValueMap(request.getInoutputArguments()))
                                .build(),
                        OperationFinishEventMessage.builder()
                                .element(reference)
                                .inoutput(ElementValueHelper.toValueMap(request.getInoutputArguments()))
                                .build()),
                LambdaExceptionHelper.wrap(
                        x -> {
                            HttpResponse response = assertExecuteSingle(
                                    HttpMethod.POST,
                                    apiPaths.submodelRepository().submodelInterface(operationSquareIdentifier.getSubmodelId())
                                            .invokeAsync(operationSquareIdentifier.getIdShortPath()),
                                    StatusCode.SUCCESS_ACCEPTED,
                                    request,
                                    null,
                                    null);
                            Optional<String> locationHeader = response.headers().firstValue(HttpConstants.HEADER_LOCATION);
                            Assert.assertTrue(locationHeader.isPresent());
                            Assert.assertTrue(locationHeader.get().contains("operation-status/"));
                            operationStatusUrl.set(response.uri().resolve(locationHeader.get()).toString());
                        }));
        // assert status is finished and returns 302 with correct Location header
        AtomicReference<String> operationResultUrl = new AtomicReference<>();
        HttpResponse responseStatusFinished = assertExecuteSingle(
                HttpMethod.GET,
                operationStatusUrl.get(),
                StatusCode.SUCCESS_FOUND,
                null,
                null,
                null);
        Optional<String> locationHeader = responseStatusFinished.headers().firstValue(HttpConstants.HEADER_LOCATION);
        Assert.assertTrue(locationHeader.isPresent());
        Assert.assertTrue(locationHeader.get().contains("operation-results/"));
        operationResultUrl.set(responseStatusFinished.uri().resolve(locationHeader.get()).toString());
        // assert operation result
        HttpResponse responseResult = HttpHelper.execute(
                httpClient,
                HttpMethod.GET,
                operationResultUrl.get());
        Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), responseResult.statusCode());
        OperationResult actualResult = HttpHelper.readResponse(responseResult, OperationResult.class);
        Assert.assertEquals(ExecutionState.FAILED, actualResult.getExecutionState());
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
                                apiPaths.submodelRepository().submodelInterface(expected).submodel(),
                                StatusCode.SUCCESS_NO_CONTENT,
                                expected,
                                null,
                                Submodel.class)));
    }


    @Test
    public void testSubmodelInterfaceUpdateSubmodelElement()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
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
                                apiPaths.submodelRepository().submodelInterface(submodel).submodelElement(expected),
                                StatusCode.SUCCESS_NO_CONTENT,
                                expected,
                                null,
                                SubmodelElement.class)));
        Page<SubmodelElement> actual = HttpHelper.getPage(
                httpClient,
                apiPaths.submodelRepository().submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertTrue(actual.getContent().contains(expected));
    }


    @Test
    public void testSubmodelInterfaceCreateSubmodelElementInAasContext()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, ResourceNotFoundException,
            NoSuchAlgorithmException, KeyManagementException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Submodel submodel = EnvironmentHelper.resolve(aas.getSubmodels().get(0), environment, Submodel.class);
        String id = "newProperty";
        SubmodelElement expected = new DefaultProperty.Builder()
                .idShort(id)
                .build();
        assertEvent(
                messageBus,
                ElementCreateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> {
                            var response = assertExecuteSingle(
                                    HttpMethod.POST,
                                    apiPaths.aasInterface(aas).submodelInterface(submodel).submodelElements(),
                                    StatusCode.SUCCESS_CREATED,
                                    expected,
                                    expected,
                                    SubmodelElement.class);
                            Optional<String> location = response.headers().firstValue(LOCATION_HEADER);
                            Assert.assertTrue(location.isPresent());
                            Assert.assertEquals(String.format("/%s", id), location.get());
                        }));
        Page<SubmodelElement> actual = HttpHelper.getPage(
                httpClient,
                apiPaths.aasInterface(aas).submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertTrue(actual.getContent().contains(expected));
    }


    @Test
    public void testSubmodelInterfaceCreateSubmodelElementInsideListInAasContext()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, ResourceNotFoundException,
            NoSuchAlgorithmException, KeyManagementException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);
        Submodel submodel = environment.getSubmodels().get(2);
        SubmodelElementList submodelElementList = (SubmodelElementList) submodel.getSubmodelElements().get(5);
        SubmodelElement expected = new DefaultProperty.Builder()
                .idShort("newProperty")
                .build();
        int expectedIndex = submodelElementList.getValue().size();
        assertEvent(
                messageBus,
                ElementCreateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> {
                            var response = assertExecuteSingle(
                                    HttpMethod.POST,
                                    apiPaths.aasInterface(aas)
                                            .submodelInterface(submodel)
                                            .submodelElement(submodelElementList),
                                    StatusCode.SUCCESS_CREATED,
                                    expected,
                                    expected,
                                    SubmodelElement.class);
                            Optional<String> location = response.headers().firstValue(LOCATION_HEADER);
                            Assert.assertTrue(location.isPresent());
                            Assert.assertEquals(String.format(".%d", expectedIndex), location.get());
                        }));
        SubmodelElement actual = HttpHelper.getWithSingleResult(
                httpClient,
                apiPaths.aasInterface(aas)
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
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);
        Submodel submodel = environment.getSubmodels().get(2);
        SubmodelElementCollection submodelElementCollection = (SubmodelElementCollection) submodel.getSubmodelElements().get(6);
        String id = "newProperty";
        SubmodelElement expected = new DefaultProperty.Builder()
                .idShort(id)
                .build();
        assertEvent(
                messageBus,
                ElementCreateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> {
                            var response = assertExecuteSingle(
                                    HttpMethod.POST,
                                    apiPaths.aasInterface(aas.getId())
                                            .submodelInterface(submodel)
                                            .submodelElement(submodelElementCollection),
                                    StatusCode.SUCCESS_CREATED,
                                    expected,
                                    expected,
                                    SubmodelElement.class);
                            Optional<String> location = response.headers().firstValue(LOCATION_HEADER);
                            Assert.assertTrue(location.isPresent());
                            Assert.assertEquals(String.format(".%s", id), location.get());
                        }));
        SubmodelElement actual = HttpHelper.getWithSingleResult(
                httpClient,
                apiPaths.aasInterface(aas.getId())
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
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, ResourceNotFoundException,
            NoSuchAlgorithmException, KeyManagementException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Submodel submodel = EnvironmentHelper.resolve(aas.getSubmodels().get(0), environment, Submodel.class);
        SubmodelElement expected = submodel.getSubmodelElements().get(0);
        Page<SubmodelElement> before = HttpHelper.getPage(
                httpClient,
                apiPaths.aasInterface(aas).submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertTrue(before.getContent().contains(expected));
        assertEvent(
                messageBus,
                ElementDeleteEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecute(
                                HttpMethod.DELETE,
                                apiPaths.aasInterface(aas).submodelInterface(submodel).submodelElement(expected),
                                StatusCode.SUCCESS_NO_CONTENT)));
        Page<SubmodelElement> actual = HttpHelper.getPage(
                httpClient,
                apiPaths.aasInterface(aas).submodelInterface(submodel).submodelElements(),
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
                                apiPaths.aasInterface(aas).submodelInterface(expected).submodel(),
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
                                apiPaths.aasInterface(aas).submodelInterface(submodel).submodelElement(expected),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                SubmodelElement.class)));
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelElementsInAasContext()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, ResourceNotFoundException, NoSuchAlgorithmException,
            KeyManagementException, UnsupportedModifierException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        Submodel submodel = EnvironmentHelper.resolve(aas.getSubmodels().get(0), environment, Submodel.class);
        List<SubmodelElement> expected = submodel.getSubmodelElements();
        assertExecutePage(
                HttpMethod.GET,
                apiPaths.aasInterface(aas).submodelInterface(submodel).submodelElements(),
                StatusCode.SUCCESS,
                null,
                expected,
                SubmodelElement.class);
    }


    @Test
    public void testSubmodelInterfaceGetSubmodelContentValueInAasContext()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, ResourceNotFoundException,
            UnsupportedModifierException {
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
                                    httpClient,
                                    apiPaths.aasInterface(aas).submodelInterface(submodel).submodel(Content.VALUE));

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
                                apiPaths.aasInterface(aas).submodelInterface(expected).submodel(Level.CORE),
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
                            HttpResponse<String> response = HttpHelper.get(
                                    httpClient,
                                    apiPaths.aasInterface(aas)
                                            .submodelInterface(submodel)
                                            .submodel(Level.DEEP, Content.PATH));

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
                            HttpResponse<String> response = HttpHelper.get(
                                    httpClient,
                                    apiPaths.aasInterface(aas)
                                            .submodelInterface(submodel)
                                            .submodel(Level.CORE, Content.PATH));

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
                                apiPaths.aasInterface(aas).submodelInterface(expected).submodel(Level.DEEP),
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
                                apiPaths.aasInterface(aas).submodelInterface(expected).submodel(),
                                StatusCode.SUCCESS_NO_CONTENT,
                                expected,
                                null,
                                Submodel.class)));
    }


    @Test
    public void testSubmodelInterfaceUpdateSubmodelElementInAasContext()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, ResourceNotFoundException,
            NoSuchAlgorithmException, KeyManagementException {
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
                                apiPaths.aasInterface(aas).submodelInterface(submodel).submodelElement(expected),
                                StatusCode.SUCCESS_NO_CONTENT,
                                expected,
                                null,
                                SubmodelElement.class)));
        Page<SubmodelElement> actual = HttpHelper.getPage(
                httpClient,
                apiPaths.aasInterface(aas).submodelInterface(submodel).submodelElements(),
                SubmodelElement.class);
        Assert.assertTrue(actual.getContent().contains(expected));
    }


    @Test
    public void testSubmodelRepositoryCreateSubmodel()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
        String id = "newSubmodel";
        Submodel expected = new DefaultSubmodel.Builder()
                .id(id)
                .build();
        assertEvent(
                messageBus,
                ElementCreateEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> {
                            var response = assertExecuteSingle(
                                    HttpMethod.POST,
                                    apiPaths.submodelRepository().submodels(),
                                    StatusCode.SUCCESS_CREATED,
                                    expected,
                                    expected,
                                    Submodel.class);
                            Optional<String> location = response.headers().firstValue(LOCATION_HEADER);
                            Assert.assertTrue(location.isPresent());
                            Assert.assertEquals(String.format("/%s", EncodingHelper.base64UrlEncode(id)), location.get());
                        }));
        Assert.assertTrue(HttpHelper.getPage(
                httpClient,
                apiPaths.submodelRepository().submodels(),
                Submodel.class)
                .getContent()
                .contains(expected));
    }


    @Test
    public void testSubmodelRepositoryDeleteSubmodel()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
        Submodel expected = environment.getSubmodels().get(1);
        Page<Submodel> before = HttpHelper.getPage(
                httpClient,
                apiPaths.submodelRepository().submodels(),
                Submodel.class);
        Assert.assertTrue(before.getContent().contains(expected));
        assertEvent(
                messageBus,
                ElementDeleteEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecute(HttpMethod.DELETE,
                                apiPaths.submodelRepository().submodel(expected),
                                StatusCode.SUCCESS_NO_CONTENT)));
        Page<Submodel> actual = HttpHelper.getPage(
                httpClient,
                apiPaths.submodelRepository().submodels(),
                Submodel.class);
        Assert.assertFalse(actual.getContent().contains(expected));
    }


    @Test
    public void testSubmodelRepositoryDeleteSubmodelInAasContext()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(0);
        Submodel expected = environment.getSubmodels().get(1);
        Page<Submodel> before = HttpHelper.getPage(
                httpClient,
                apiPaths.submodelRepository().submodels(),
                Submodel.class);
        Assert.assertTrue(before.getContent().contains(expected));
        assertEvent(
                messageBus,
                ElementDeleteEventMessage.class,
                expected,
                LambdaExceptionHelper.wrap(
                        x -> assertExecute(HttpMethod.DELETE,
                                apiPaths.aasRepository().submodelRepositoryInterface(aas).submodel(expected),
                                StatusCode.SUCCESS_NO_CONTENT)));
        Page<Submodel> actual = HttpHelper.getPage(
                httpClient,
                apiPaths.submodelRepository().submodels(),
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
                                apiPaths.submodelRepository().submodel(expected),
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
                                apiPaths.submodelRepository().submodel(submodel, Content.REFERENCE),
                                StatusCode.SUCCESS,
                                null,
                                expected,
                                Reference.class)));
    }


    @Test
    public void testSubmodelRepositoryGetSubmodelNotExists()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, MessageBusException, NoSuchAlgorithmException,
            KeyManagementException {
        HttpResponse<String> response = HttpHelper.get(
                httpClient,
                apiPaths.submodelRepository().submodel("non-existant"));
        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND), response.statusCode());
    }


    @Test
    public void testSubmodelRepositoryGetSubmodels()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        List<Submodel> expected = environment.getSubmodels();
        ExtendHelper.withoutBlobValue(expected);
        assertExecutePage(
                HttpMethod.GET,
                apiPaths.submodelRepository().submodels(),
                StatusCode.SUCCESS,
                null,
                expected,
                Submodel.class);
    }


    @Test
    public void testSubmodelRepositoryGetSubmodelsContentReference()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        List<Reference> expected = environment.getSubmodels().stream()
                .map(ReferenceBuilder::forSubmodel)
                .collect(Collectors.toList());
        assertExecutePage(
                HttpMethod.GET,
                apiPaths.submodelRepository().submodels(Content.REFERENCE),
                StatusCode.SUCCESS,
                null,
                expected,
                Reference.class);
    }


    @Test
    public void testSubmodelRepositoryGetSubmodelsByIdShort()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        Submodel expected = environment.getSubmodels().get(1);
        assertExecutePage(
                HttpMethod.GET,
                String.format("%s?idShort=%s", apiPaths.submodelRepository().submodels(), expected.getIdShort()),
                StatusCode.SUCCESS,
                null,
                List.of(expected),
                Submodel.class);
    }


    @Test
    public void testSubmodelRepositoryGetSubmodelsBySemanticId()
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        Submodel expected = environment.getSubmodels().get(1);
        assertExecutePage(HttpMethod.GET,
                String.format("%s?semanticId=%s",
                        apiPaths.submodelRepository().submodels(),
                        EncodingHelper.base64UrlEncode(new JsonApiSerializer().write(expected.getSemanticId()))),
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
                                apiPaths.submodelRepository().submodel(expected),
                                StatusCode.SUCCESS_NO_CONTENT,
                                expected,
                                null,
                                Submodel.class)));
    }


    @Test
    public void testMethodNotAllowed() throws Exception {
        HttpResponse response = HttpHelper.execute(
                httpClient,
                HttpMethod.PUT, apiPaths.aasRepository().assetAdministrationShells(),
                true);
        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_METHOD_NOT_ALLOWED), response.statusCode());
    }


    @Test
    public void testProprietaryReset()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, NoSuchAlgorithmException,
            KeyManagementException, UnsupportedModifierException {
        HttpResponse response = HttpHelper.execute(
                httpClient,
                HttpMethod.GET,
                apiPaths.proprietaryInterface().reset());
        Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS_NO_CONTENT), response.statusCode());
        assertExecutePage(
                HttpMethod.GET,
                apiPaths.aasRepository().assetAdministrationShells(),
                StatusCode.SUCCESS,
                null,
                List.of(),
                AssetAdministrationShell.class);
        assertExecutePage(
                HttpMethod.GET,
                apiPaths.submodelRepository().submodels(),
                StatusCode.SUCCESS,
                null,
                List.of(),
                Submodel.class);
        assertExecutePage(
                HttpMethod.GET,
                apiPaths.conceptDescriptionRepository().conceptDescriptions(),
                StatusCode.SUCCESS,
                null,
                List.of(),
                ConceptDescription.class);
    }


    @Test
    public void testProprietaryImport()
            throws InterruptedException, MessageBusException, IOException, URISyntaxException, SerializationException, DeserializationException, NoSuchAlgorithmException,
            KeyManagementException, UnsupportedModifierException {
        ImportResult expected = ImportResult.builder().build();
        byte[] fileContent = new byte[10];
        new Random().nextBytes(fileContent);

        for (var dataFormat: DataFormat.values()) {
            // TODO remove once RDF support is implemented
            if (dataFormat == DataFormat.RDF || dataFormat == DataFormat.JSONLD) {
                continue;
            }
            String filename = String.format("/dummy-file-%s.bin", dataFormat);
            String fileIdShort = String.format("dummy-file-%s", dataFormat);
            DefaultAssetAdministrationShell aas = new DefaultAssetAdministrationShell.Builder()
                    .id(String.format("http://example.org/imported-aas-%s", dataFormat))
                    .build();
            DefaultSubmodel submodel = new DefaultSubmodel.Builder()
                    .id(String.format("http://example.org/imported-submodel-%s", dataFormat))
                    .submodelElements(new DefaultFile.Builder()
                            .idShort(fileIdShort)
                            .contentType("application/octet-stream")
                            .value(filename)
                            .build())
                    .build();
            DefaultConceptDescription cd = new DefaultConceptDescription.Builder()
                    .id(String.format("http://example.org/imported-cd-%s", dataFormat))
                    .build();
            HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder()
                    .uri(new URI(apiPaths.proprietaryInterface().importFile()))
                    .header(HttpConstants.HEADER_CONTENT_TYPE, dataFormat.getContentType().toString())
                    .POST(BodyPublishers.ofByteArray(
                            EnvironmentSerializationManager
                                    .serializerFor(dataFormat)
                                    .write(EnvironmentContext.builder()
                                            .environment(new DefaultEnvironment.Builder()
                                                    .assetAdministrationShells(aas)
                                                    .submodels(submodel)
                                                    .conceptDescriptions(cd)
                                                    .build())
                                            .file(fileContent, filename)
                                            .build())))
                    .build(),
                    HttpResponse.BodyHandlers.ofString());
            Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), response.statusCode());
            ImportResult actual = HttpHelper.readResponse(response, ImportResult.class);
            Assert.assertEquals(expected, actual);

            assertExecuteSingle(
                    HttpMethod.GET,
                    apiPaths.aasRepository().assetAdministrationShell(aas),
                    StatusCode.SUCCESS,
                    null,
                    aas,
                    AssetAdministrationShell.class);
            assertExecuteSingle(
                    HttpMethod.GET,
                    apiPaths.submodelRepository().submodel(submodel),
                    StatusCode.SUCCESS,
                    null,
                    submodel,
                    Submodel.class);
            assertExecuteSingle(
                    HttpMethod.GET,
                    apiPaths.conceptDescriptionRepository().conceptDescription(cd),
                    StatusCode.SUCCESS,
                    null,
                    cd,
                    ConceptDescription.class);
            if (dataFormat.getCanStoreFiles()) {
                HttpResponse<byte[]> getFileResponse = httpClient.send(HttpRequest.newBuilder()
                        .uri(new URI(apiPaths.submodelRepository()
                                .submodelInterface(submodel)
                                .submodelElement(fileIdShort)
                                + "/attachment"))
                        .header(HttpConstants.HEADER_ACCEPT, DataFormat.JSON.getContentType().toString())
                        .GET()
                        .build(),
                        HttpResponse.BodyHandlers.ofByteArray());
                Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), getFileResponse.statusCode());
                Assert.assertArrayEquals(fileContent, getFileResponse.body());
            }

        }

    }


    private void assertExecute(HttpMethod method, String url, StatusCode statusCode)
            throws IOException, InterruptedException, URISyntaxException, SerializationException, DeserializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        assertExecuteSingle(method, url, statusCode, null, null, null);
    }


    private void assertExecuteMultiple(HttpMethod method, String url, StatusCode statusCode, Object input, Object expected, Class<?> type)
            throws IOException, InterruptedException, URISyntaxException, SerializationException, DeserializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        HttpResponse response = HttpHelper.execute(httpClient, method, url, input);
        Assert.assertEquals(toHttpStatusCode(statusCode), response.statusCode());
        if (expected != null) {
            Object actual = HttpHelper.readResponseList(response, type);
            Assert.assertEquals(expected, actual);
        }
    }


    private <T> Page<T> assertExecutePage(HttpMethod method, String url, StatusCode statusCode, Object input, List<T> expected, Class<T> type)
            throws IOException, InterruptedException, URISyntaxException, SerializationException, DeserializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        HttpResponse response = HttpHelper.execute(httpClient, method, url, input);
        Assert.assertEquals(toHttpStatusCode(statusCode), response.statusCode());
        Page<T> actual = HttpHelper.readResponsePage(response, type);
        if (expected != null) {
            Assert.assertEquals(expected, actual.getContent());
        }
        return actual;
    }


    private HttpResponse assertExecuteSingle(HttpMethod method, String url, StatusCode statusCode, Object input, Object expected, Class<?> type)
            throws IOException, InterruptedException, URISyntaxException, SerializationException, DeserializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        HttpResponse response = HttpHelper.execute(httpClient, method, url, input);
        Assert.assertEquals(toHttpStatusCode(statusCode), response.statusCode());
        if (expected != null) {
            Object actual = HttpHelper.readResponse(response, type);
            Assert.assertEquals(expected, actual);
        }
        return response;
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
        HttpResponse<byte[]> putFileResponse = httpClient.send(HttpRequest.newBuilder()
                .uri(new URI(apiPaths.submodelRepository()
                        .submodelInterface(defaultEnvironment.getSubmodels().get(0)).submodelElement("ExampleSubmodelElementCollection.ExampleFile")
                        + "/attachment"))
                .header(HttpConstants.HEADER_ACCEPT, DataFormat.JSON.getContentType().toString())
                .header(HttpConstants.HEADER_CONTENT_TYPE, httpEntity.getContentType())
                .PUT(BodyPublishers.ofInputStream(LambdaExceptionHelper.wrap(httpEntity::getContent)))
                .build(),
                HttpResponse.BodyHandlers.ofByteArray());
        Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS_NO_CONTENT), putFileResponse.statusCode());
        HttpResponse<byte[]> getFileResponse = httpClient.send(HttpRequest.newBuilder()
                .uri(new URI(apiPaths.submodelRepository()
                        .submodelInterface(defaultEnvironment.getSubmodels().get(0)).submodelElement("ExampleSubmodelElementCollection.ExampleFile")
                        + "/attachment"))
                .header(HttpConstants.HEADER_ACCEPT, DataFormat.JSON.getContentType().toString())
                .header(HttpConstants.HEADER_CONTENT_TYPE, httpEntity.getContentType())
                .GET()
                .build(),
                HttpResponse.BodyHandlers.ofByteArray());
        Assert.assertArrayEquals(content, getFileResponse.body());
        HttpResponse<byte[]> deleteFileResponse = httpClient.send(HttpRequest.newBuilder()
                .uri(new URI(apiPaths.submodelRepository()
                        .submodelInterface(defaultEnvironment.getSubmodels().get(0)).submodelElement("ExampleSubmodelElementCollection.ExampleFile")
                        + "/attachment"))
                .header(HttpConstants.HEADER_ACCEPT, DataFormat.JSON.getContentType().toString())
                .DELETE()
                .build(),
                HttpResponse.BodyHandlers.ofByteArray());
        Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), deleteFileResponse.statusCode());
    }


    private static void clearSubmodelElementCollections(Submodel submodel) {
        submodel.getSubmodelElements().forEach(x -> {
            if (SubmodelElementCollection.class
                    .isAssignableFrom(x.getClass())) {
                ((SubmodelElementCollection) x).getValue().clear();
            }
        });
    }


    private static OperationVariable[] operationSqaureDefaultImplementation(OperationVariable[] input, OperationVariable[] inoutput) {
        Ensure.requireNonNull(input, "input must be non-null");
        Ensure.require(input.length == 1 && Objects.nonNull(input[0].getValue()), "operation must have exactly one input parameter");
        Ensure.require(Property.class
                .isAssignableFrom(input[0].getValue().getClass()), "operation input parameter must be of type Property");
        Property propertyIn = Property.class
                .cast(input[0].getValue());
        Ensure.require(
                propertyIn.getIdShort().equals(OPERATION_SQUARE_INPUT_PARAMETER_ID),
                String.format("operation input parameter must have idShort '%s'", OPERATION_SQUARE_INPUT_PARAMETER_ID));
        Ensure.require(propertyIn.getValueType() == DataTypeDefXsd.INT, "operation input parameter must have datatype 'xs:int'");
        int in = Integer.parseInt(propertyIn.getValue());

        Ensure.requireNonNull(inoutput, "inoutput must be non-null");
        Ensure.require(inoutput.length == 1 && Objects.nonNull(inoutput[0].getValue()), "operation must have exactly one inputput parameter");
        Ensure.require(Property.class
                .isAssignableFrom(inoutput[0].getValue().getClass()), "operation inoutput parameter must be of type Property");
        Property propertyInOut = Property.class
                .cast(inoutput[0].getValue());
        Ensure.require(
                propertyInOut.getIdShort().equals(OPERATION_SQUARE_INOUTPUT_PARAMETER_ID),
                String.format("operation inoutput parameter must have idShort '%s'", OPERATION_SQUARE_INOUTPUT_PARAMETER_ID));
        Ensure.require(propertyInOut.getValueType() == DataTypeDefXsd.STRING, "operation inoutput parameter must have datatype 'xs:string'");
        propertyInOut.setValue(OPERATION_SQUARE_INOUTPUT_PARAMETER_EXPECTED_VALUE);
        return new OperationVariable[] {
                new DefaultOperationVariable.Builder()
                        .value(new DefaultProperty.Builder()
                                .idShort(OPERATION_SQUARE_OUTPUT_PARAMETER_ID)
                                .valueType(DataTypeDefXsd.INT)
                                .value(Integer.toString(in * in))
                                .build())
                        .build()
        };

    }
}
