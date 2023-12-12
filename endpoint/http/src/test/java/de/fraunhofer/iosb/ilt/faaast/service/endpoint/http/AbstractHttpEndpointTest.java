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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.QueryParameters;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonApiDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpConstants;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.EnvironmentContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.MessageType;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.ExecutionState;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasserialization.GenerateSerializationByIdsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.GetAssetAdministrationShellResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.GetAllAssetAdministrationShellsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.PostAssetAdministrationShellResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasserialization.GenerateSerializationByIdsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.PostConceptDescriptionResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetAllSubmodelElementsReferenceResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetAllSubmodelElementsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetOperationAsyncResultResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetOperationAsyncStatusResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.InvokeOperationAsyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.PostSubmodelElementResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.GetSubmodelByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.PostSubmodelResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeExtractor;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ResponseHelper;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultRange;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringRequestContent;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractHttpEndpointTest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractHttpEndpointTest.class);
    protected static final String HOST = "localhost";
    protected static String scheme;
    protected static int port;
    protected static HttpClient client;
    protected static HttpEndpoint endpoint;
    protected static Service service;
    protected static Persistence persistence;
    protected static FileStorage fileStorage;
    protected static HttpJsonApiDeserializer deserializer;
    protected static HttpJsonApiSerializer serializer;
    protected static Server server;

    @Before
    public void setUp() {
        serializer = new HttpJsonApiSerializer();
        deserializer = new HttpJsonApiDeserializer();
        Mockito.reset(persistence);
        Mockito.reset(fileStorage);
        Mockito.reset(service);
    }


    @AfterClass
    public static void cleanUp() {
        if (client != null) {
            try {
                client.stop();
            }
            catch (Exception e) {
                LOGGER.info("error stopping HTTP client", e);
            }
        }
        if (endpoint != null) {
            try {
                endpoint.stop();
            }
            catch (Exception e) {
                LOGGER.info("error stopping HTTP endpoint", e);
            }
            if (server != null) {
                try {
                    server.stop();
                }
                catch (Exception e) {
                    LOGGER.info("error stopping HTTP Server", e);
                }
            }
        }
    }


    @Test
    public void testInvalidUrl() throws Exception {
        ContentResponse response = execute(HttpMethod.GET, "/foo/bar");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testInvalidMethod() throws Exception {
        ContentResponse response = execute(HttpMethod.PATCH, "/shells");
        Assert.assertEquals(HttpStatus.METHOD_NOT_ALLOWED_405, response.getStatus());
    }


    @Test
    public void testCORSEnabled() throws Exception {
        ContentResponse response = execute(HttpMethod.GET, "/foo/bar");
        Assert.assertEquals("*", response.getHeaders().get(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER));
        Assert.assertEquals("true", response.getHeaders().get(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER));
        Assert.assertEquals("Content-Type", response.getHeaders().get(CrossOriginFilter.ACCESS_CONTROL_ALLOW_HEADERS_HEADER));
    }


    @Test
    public void testConfigHttpResponseHeaderServerVersionNotFound() throws Exception {
        HttpFields headers = client.newRequest(HOST, port)
                .method(HttpMethod.GET)
                .scheme(scheme)
                .send()
                .getHeaders();
        Assert.assertFalse(headers.contains(HttpHeader.SERVER));
        Assert.assertFalse(headers.contains(HttpHeader.DATE));
        Assert.assertFalse(headers.contains(HttpHeader.X_POWERED_BY));
    }


    @Test
    public void testPreflightedCORSRequestSupported() throws Exception {
        ContentResponse response = execute(HttpMethod.OPTIONS, "/shells", null, null, null, null,
                Map.of(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER, "GET,POST"));
        assertAccessControllAllowMessageHeader(response, HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS);
        Assert.assertEquals(204, response.getStatus());
    }


    @Test
    public void testPreflightedCORSRequestUnsupported() throws Exception {
        ContentResponse response = execute(HttpMethod.OPTIONS, "/shells", null, null, null, null,
                Map.of(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER, "PUT"));
        assertAccessControllAllowMessageHeader(response, HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS);
        Assert.assertEquals(400, response.getStatus());
    }


    @Test
    public void testPreflightedCORSRequestInvalidRequestMethodHeader() throws Exception {
        ContentResponse response = execute(HttpMethod.OPTIONS, "/shells", null, null, null, null,
                Map.of(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER, "FOO"));
        Assert.assertEquals(400, response.getStatus());
    }


    @Test
    public void testPreflightedCORSRequestNoRequestMethodHeader() throws Exception {
        ContentResponse response = execute(HttpMethod.OPTIONS, "/", null, null, null, null, null);
        assertAccessControllAllowMessageHeader(response, HttpMethod.OPTIONS);
        Assert.assertEquals(204, response.getStatus());
    }


    @Test
    public void testPreflightedCORSRequestEmptyRequestMethodHeader() throws Exception {
        ContentResponse response = execute(HttpMethod.OPTIONS, "/", null, null, null, null,
                Map.of(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER, ""));
        assertAccessControllAllowMessageHeader(response, HttpMethod.OPTIONS);
        Assert.assertEquals(204, response.getStatus());
    }


    @Test
    public void testParamContentNormal() throws Exception {
        String id = "foo";
        when(service.execute(any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/" + EncodingHelper.base64UrlEncode(id) + "/aas?content=normal");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
    }


    @Test
    @Ignore("content=trimmed currently under discussion")
    public void testParamContentTrimmed() throws Exception {
        String id = "";
        when(service.execute(any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/" + EncodingHelper.base64UrlEncode(id) + "/aas?content=trimmed");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
    }


    @Test
    public void testParamContentReference() throws Exception {
        String id = "foo";
        when(service.execute(any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/" + EncodingHelper.base64UrlEncode(id) + "/aas?content=reference");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
    }


    @Test
    public void testParamContentAndLevelInvalid() throws Exception {
        String id = "foo";
        when(service.execute(any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/" + EncodingHelper.base64UrlEncode(id) + "/aas/$foo?level=foo");
        assertContainsErrorText(response, HttpStatus.BAD_REQUEST_400, "unsupported content modifier 'foo'");
    }


    @Test
    public void testInvalidAASIdentifier() throws Exception {
        when(service.execute(any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/bogus/aas");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testInvalidAASIdentifierAndAdditionalPathElement() throws Exception {
        when(service.execute(any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .build());
        ContentResponse response = execute(HttpMethod.DELETE, "/shells/bogus/test");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testInvalidBase64Param() throws Exception {
        ContentResponse response = execute(HttpMethod.GET, "/concept-descriptions/InvalidBase64");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testNonExistentId() throws Exception {
        String idShort = AASFull.SUBMODEL_3.getIdShort() + "123";
        when(service.execute(any())).thenReturn(GetSubmodelByIdResponse.builder()
                .statusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/" + EncodingHelper.base64UrlEncode(idShort));
        Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
    }


    @Test
    public void testDoubleQueryValue() throws Exception {
        String idShort = AASFull.SUBMODEL_3.getIdShort() + "123";
        when(service.execute(any())).thenReturn(GetSubmodelByIdResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/" + EncodingHelper.base64UrlEncode(idShort)
                + "/submodel/submodel-elements/ExampleRelationshipElement?level=normal&level=deep");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
    }


    @Test
    public void testMissingQueryValue() throws Exception {
        String idShort = AASFull.SUBMODEL_3.getIdShort() + "123";
        when(service.execute(any())).thenReturn(GetSubmodelByIdResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/" + EncodingHelper.base64UrlEncode(idShort)
                + "/submodel/submodel-elements/ExampleRelationshipElement?level=normal&content=");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testBogusAndMissingQueryValue() throws Exception {
        String idShort = AASFull.SUBMODEL_3.getIdShort() + "123";
        when(service.execute(any())).thenReturn(GetSubmodelByIdResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/" + EncodingHelper.base64UrlEncode(idShort)
                + "/submodel/submodel-elements/ExampleRelationshipElement?level=normal&bogus");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testWrongResponse() throws Exception {
        when(service.execute(any())).thenReturn(GetAllAssetAdministrationShellsResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/GetAllSubmodels");
        // TODO: Discuss which status code is applicable 400/500 ?
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testPostSubmodelNoData() throws Exception {
        when(service.execute(any())).thenReturn(PostSubmodelResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.POST, "/submodels");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testGetAllAssetAdministrationShellsWithSinglePage() throws Exception {
        Page<AssetAdministrationShell> expected = Page.of(AASFull.AAS_1);
        when(service.execute(any())).thenReturn(GetAllAssetAdministrationShellsResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(expected)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        // TODO: server not returning character encoding
        LOGGER.info("http response encoding: {}", response.getEncoding());
        LOGGER.info("http response content: {}", new String(response.getContent(), "UTF-8"));
        Page<AssetAdministrationShell> actual = deserializer.read(new String(response.getContent(), "UTF-8"), new TypeReference<Page<AssetAdministrationShell>>() {});
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllAssetAdministrationShellsWithMultiplePages() throws Exception {
        Page<AssetAdministrationShell> expected = Page.<AssetAdministrationShell> builder()
                .result(AASFull.AAS_2)
                .metadata(PagingMetadata.builder()
                        .cursor("foo")
                        .build())
                .build();
        when(service.execute(any())).thenReturn(GetAllAssetAdministrationShellsResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(expected)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        // TODO: server not returning character encoding
        LOGGER.info("http response encoding: {}", response.getEncoding());
        LOGGER.info("http response content: {}", new String(response.getContent(), "UTF-8"));
        Page<AssetAdministrationShell> actual = deserializer.read(new String(response.getContent(), "UTF-8"), new TypeReference<Page<AssetAdministrationShell>>() {});
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSerializationJson() throws Exception {
        Environment expected = new DefaultEnvironment.Builder()
                .assetAdministrationShells(AASFull.AAS_2)
                .submodels(AASFull.SUBMODEL_4)
                .submodels(AASFull.SUBMODEL_5)
                .build();
        when(service.execute(any())).thenReturn(GenerateSerializationByIdsResponse.builder()
                .dataformat(DataFormat.JSON)
                .payload(EnvironmentContext.builder()
                        .environment(expected)
                        .build())
                .statusCode(StatusCode.SUCCESS)
                .build());
        ContentResponse response = execute(
                HttpMethod.GET,
                "/serialization",
                Map.of(
                        "aasIds", EncodingHelper.base64UrlEncode(expected.getAssetAdministrationShells().stream()
                                .map(x -> x.getId())
                                .collect(Collectors.joining(","))),
                        "submodelIds", EncodingHelper.base64UrlEncode(expected.getSubmodels().stream()
                                .map(x -> x.getId())
                                .collect(Collectors.joining(","))),
                        QueryParameters.INCLUDE_CONCEPT_DESCRIPTIONS, "false"),
                null,
                null,
                null,
                Map.of(
                        HttpConstants.HEADER_ACCEPT, DataFormat.JSON.getContentType().withoutParameters().toString()));
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        LOGGER.info("http response encoding: {}", response.getEncoding());
        LOGGER.info("http response content: {}", response.getContentAsString());
        Environment actual = deserializer.read(response.getContentAsString(), Environment.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSerializationWildcard() throws Exception {
        Environment expected = new DefaultEnvironment.Builder()
                .assetAdministrationShells(AASFull.AAS_2)
                .submodels(AASFull.SUBMODEL_4)
                .submodels(AASFull.SUBMODEL_5)
                .build();
        when(service.execute(argThat((GenerateSerializationByIdsRequest request) -> request.getSerializationFormat() == DataFormat.JSON)))
                .thenReturn(GenerateSerializationByIdsResponse.builder()
                        .dataformat(DataFormat.JSON)
                        .payload(EnvironmentContext.builder()
                                .environment(expected)
                                .build())
                        .statusCode(StatusCode.SUCCESS)
                        .build());
        ContentResponse response = execute(
                HttpMethod.GET,
                "/serialization",
                Map.of(
                        "aasIds", EncodingHelper.base64UrlEncode(expected.getAssetAdministrationShells().stream()
                                .map(x -> x.getId())
                                .collect(Collectors.joining(","))),
                        "submodelIds", EncodingHelper.base64UrlEncode(expected.getSubmodels().stream()
                                .map(x -> x.getId())
                                .collect(Collectors.joining(","))),
                        QueryParameters.INCLUDE_CONCEPT_DESCRIPTIONS, "false"),
                null,
                null,
                null,
                Map.of(
                        HttpConstants.HEADER_ACCEPT, "*/*"));
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        LOGGER.info("http response encoding: {}", response.getEncoding());
        LOGGER.info("http response content: {}", response.getContentAsString());
        Environment actual = deserializer.read(response.getContentAsString(), Environment.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    @Ignore("value only serialization not defined for AssetAdministrationShells")
    public void testGetAllAssetAdministrationShellsValueOnly() throws Exception {
        Page<AssetAdministrationShell> expectedPayload = Page.of(AASFull.AAS_1);
        when(service.execute(any())).thenReturn(GetAllAssetAdministrationShellsResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(expectedPayload)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells", new OutputModifier.Builder()
                .content(Content.VALUE)
                .build());
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        Page<AssetAdministrationShell> actualPayload = deserializer.read(new String(response.getContent()), Page.class);
        Assert.assertEquals(expectedPayload, actualPayload);
    }


    @Test
    public void testGetAllSubmodelElementsValueOnly() throws Exception {
        List<SubmodelElement> submodelElements = List.of(
                new DefaultProperty.Builder()
                        .idShort("property1")
                        .value("hello world")
                        .valueType(DataTypeDefXsd.STRING)
                        .build(),
                new DefaultRange.Builder()
                        .idShort("range1")
                        .min("1.1")
                        .max("2.0")
                        .valueType(DataTypeDefXsd.DOUBLE)
                        .build());
        when(service.execute(any())).thenReturn(GetAllSubmodelElementsResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(Page.of(submodelElements))
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/foo/submodel/submodel-elements", new OutputModifier.Builder()
                .content(Content.VALUE)
                .build());
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        Page<ElementValue> actual = deserializer.readValuePage(new String(response.getContent()), TypeExtractor.extractTypeInfo(submodelElements));
        Page<ElementValue> expected = Page.of(submodelElements.stream()
                .map(LambdaExceptionHelper.rethrowFunction(x -> (ElementValue) ElementValueMapper.toValue(x)))
                .collect(Collectors.toList()));
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodelElementsMetadata() throws Exception {
        Page<SubmodelElement> expected = Page.of(
                new DefaultProperty.Builder()
                        .idShort("property1")
                        .valueType(DataTypeDefXsd.STRING)
                        .build(),
                new DefaultRange.Builder()
                        .idShort("range1")
                        .valueType(DataTypeDefXsd.DOUBLE)
                        .build());
        when(service.execute(any())).thenReturn(GetAllSubmodelElementsResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(expected)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/foo/submodel/submodel-elements", new OutputModifier.Builder()
                .content(Content.METADATA)
                .build());
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        Page<SubmodelElement> actual = deserializer.read(new String(response.getContent()), new TypeReference<Page<SubmodelElement>>() {});
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodelElementsReference() throws Exception {
        Page<Reference> expected = Page.of(
                ReferenceBuilder.forSubmodel("submodelId", "property1"),
                ReferenceBuilder.forSubmodel("submodelId", "range1"));
        when(service.execute(any())).thenReturn(GetAllSubmodelElementsReferenceResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(expected)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/foo/submodel/submodel-elements", Content.REFERENCE);
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        Page<Reference> actual = deserializer.read(new String(response.getContent()), new TypeReference<Page<Reference>>() {});
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodelElements() throws Exception {
        Page<SubmodelElement> expected = Page.of(
                new DefaultProperty.Builder()
                        .idShort("property1")
                        .value("hello world")
                        .valueType(DataTypeDefXsd.STRING)
                        .build(),
                new DefaultRange.Builder()
                        .idShort("range1")
                        .min("1.1")
                        .max("2.0")
                        .valueType(DataTypeDefXsd.DOUBLE)
                        .build());
        when(service.execute(any())).thenReturn(GetAllSubmodelElementsResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(expected)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/foo/submodel/submodel-elements", new OutputModifier.Builder()
                .content(Content.NORMAL)
                .build());
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        Page<SubmodelElement> actual = deserializer.read(new String(response.getContent()), new TypeReference<Page<SubmodelElement>>() {});
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodelElementsInAasContext() throws Exception {
        String aasId = "aasId";
        String submodelId = "submodelId";
        Reference submodelRef = new ReferenceBuilder()
                .aas(aasId)
                .submodel(submodelId)
                .build();
        AssetAdministrationShell aas = new DefaultAssetAdministrationShell.Builder()
                .id(aasId)
                .submodels(submodelRef)
                .build();
        Page<SubmodelElement> expected = Page.of(
                new DefaultProperty.Builder()
                        .idShort("property1")
                        .value("hello world")
                        .valueType(DataTypeDefXsd.STRING)
                        .build(),
                new DefaultRange.Builder()
                        .idShort("range1")
                        .min("1.1")
                        .max("2.0")
                        .valueType(DataTypeDefXsd.DOUBLE)
                        .build());
        when(service.execute(any())).thenReturn(
                GetAllSubmodelElementsResponse.builder()
                        .statusCode(StatusCode.SUCCESS)
                        .payload(expected)
                        .build());
        when(persistence.getAssetAdministrationShell(
                aas.getId(),
                new OutputModifier.Builder()
                        .level(Level.CORE)
                        .build()))
                                .thenReturn(aas);
        mockAasContext(service, aasId);
        ContentResponse response = execute(
                HttpMethod.GET,
                String.format("/shells/%s/aas/submodels/%s/submodel/submodel-elements",
                        EncodingHelper.base64UrlEncode(aasId),
                        EncodingHelper.base64UrlEncode(submodelId)),
                new OutputModifier.Builder()
                        .content(Content.NORMAL)
                        .build());
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        Page<SubmodelElement> actual = deserializer.read(new String(response.getContent()), new TypeReference<Page<SubmodelElement>>() {});
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testAASAlreadyExists() throws Exception {
        when(service.execute(any())).thenReturn(
                PostAssetAdministrationShellResponse.builder()
                        .statusCode(StatusCode.CLIENT_RESOURCE_CONFLICT)
                        .build());
        ContentResponse response = execute(
                HttpMethod.POST,
                "/shells",
                new DefaultAssetAdministrationShell.Builder().build());
        Assert.assertEquals(HttpStatus.CONFLICT_409, response.getStatus());
    }


    @Test
    public void testSubmodelAlreadyExists() throws Exception {
        when(service.execute(any())).thenReturn(
                PostSubmodelResponse.builder()
                        .statusCode(StatusCode.CLIENT_RESOURCE_CONFLICT)
                        .build());
        ContentResponse response = execute(
                HttpMethod.POST,
                "/submodels",
                new DefaultSubmodel.Builder().build());
        Assert.assertEquals(HttpStatus.CONFLICT_409, response.getStatus());
    }


    @Test
    public void testSubmodelElementAlreadyExists() throws Exception {
        String id = "foo";
        when(service.execute(any())).thenReturn(
                PostSubmodelElementResponse.builder()
                        .statusCode(StatusCode.CLIENT_RESOURCE_CONFLICT)
                        .build());
        ContentResponse response = execute(
                HttpMethod.POST, "/submodels/" + EncodingHelper.base64UrlEncode(id) + "/submodel/submodel-elements",
                new DefaultProperty.Builder().build());
        Assert.assertEquals(HttpStatus.CONFLICT_409, response.getStatus());
    }


    @Test
    public void testConceptDescriptionAlreadyExists() throws Exception {
        when(service.execute(any())).thenReturn(
                PostConceptDescriptionResponse.builder()
                        .statusCode(StatusCode.CLIENT_RESOURCE_CONFLICT)
                        .build());
        ContentResponse response = execute(
                HttpMethod.POST, "/concept-descriptions",
                new DefaultConceptDescription.Builder().build());
        Assert.assertEquals(HttpStatus.CONFLICT_409, response.getStatus());
    }


    @Test
    public void testOperationAsync() throws Exception {
        OperationHandle handle = OperationHandle.builder().build();
        String handleId = EncodingHelper.base64UrlEncode(handle.getHandleId());
        when(service.execute(any())).thenReturn(
                InvokeOperationAsyncResponse.builder()
                        .payload(handle)
                        .statusCode(StatusCode.SUCCESS_ACCEPTED)
                        .build());
        OperationRequest operationRequest = new OperationRequest();
        URI urlInvoke = new URI("/submodels/foo/submodel/submodel-elements/bar/invoke-async");
        ContentResponse responseInvoke = execute(HttpMethod.POST, urlInvoke.toString(), operationRequest);
        Assert.assertEquals(HttpStatus.ACCEPTED_202, responseInvoke.getStatus());
        Assert.assertTrue(responseInvoke.getHeaders().contains(HttpHeader.LOCATION));

        URI urlStatus = urlInvoke.resolve(responseInvoke.getHeaders().getField(HttpHeader.LOCATION).getValue());
        when(service.execute(any())).thenReturn(
                GetOperationAsyncStatusResponse.builder()
                        .payload(ExecutionState.RUNNING)
                        .success()
                        .build());
        ContentResponse responseStatusRunning = execute(HttpMethod.GET, urlStatus.toString());
        Assert.assertEquals(HttpStatus.OK_200, responseStatusRunning.getStatus());
        // check content for state == RUNNING

        // check result = 404
        when(service.execute(any())).thenReturn(
                GetOperationAsyncStatusResponse.builder()
                        .payload(null)
                        .statusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND)
                        .build());
        ContentResponse responseStatusNotFound = execute(HttpMethod.GET, "/submodels/foo/submodel/submodel-elements/bar/operation-status/" + handleId);
        Assert.assertEquals(HttpStatus.NOT_FOUND_404, responseStatusNotFound.getStatus());

        // check COMPLETED = 302
        when(service.execute(any())).thenReturn(
                GetOperationAsyncStatusResponse.builder()
                        .payload(ExecutionState.COMPLETED)
                        .success()
                        .build());
        client.setFollowRedirects(false);
        ContentResponse responseStatusCompleted = execute(HttpMethod.GET, urlStatus.toString());
        Assert.assertEquals(HttpStatus.MOVED_TEMPORARILY_302, responseStatusCompleted.getStatus());
        Assert.assertTrue(responseStatusCompleted.getHeaders().contains(HttpHeader.LOCATION));
        // check content for state == COMPLETED

        URI urlResult = urlStatus.resolve(responseStatusCompleted.getHeaders().getField(HttpHeader.LOCATION).getValue());
        when(service.execute(any())).thenReturn(
                GetOperationAsyncResultResponse.builder()
                        .payload(OperationResult.builder()
                                .executionState(ExecutionState.COMPLETED)
                                .build())
                        .success()
                        .build());
        ContentResponse responseResult = execute(HttpMethod.GET, urlResult.toString(), operationRequest);
        Assert.assertEquals(HttpStatus.OK_200, responseResult.getStatus());
        // assert state == COMPLETED
    }


    private void mockAasContext(ServiceContext serviceContext, String aasId) {
        doReturn(new DefaultEnvironment.Builder()
                .assetAdministrationShells(new DefaultAssetAdministrationShell.Builder()
                        .id(aasId)
                        .build())
                .build())
                        .when(serviceContext)
                        .getAASEnvironment();
    }


    @Test
    public void testResultServerError() throws Exception {
        Result expected = Result.error(HttpStatus.getMessage(500));
        when(service.execute(any())).thenReturn(GetSubmodelElementByPathResponse.builder()
                .statusCode(StatusCode.SERVER_INTERNAL_ERROR)
                .result(expected)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/" + EncodingHelper.base64UrlEncode("Whatever") + "/aas");
        Result actual = deserializer.read(new String(response.getContent()), Result.class);
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testResultBadRequest() throws Exception {
        Result expected = Result.error("no matching request mapper found for URL 'shellsX'");
        ContentResponse response = execute(HttpMethod.GET, "/shellsX/");
        Result actual = deserializer.read(new String(response.getContent()), Result.class);
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testMethodNotAllowed() throws Exception {
        Result expected = Result.error("method 'PUT' not allowed for URL 'shells' (allowed methods: GET, POST)");
        ContentResponse response = execute(HttpMethod.PUT, "/shells");
        Result actual = deserializer.read(new String(response.getContent()), Result.class);
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testResultNotFound() throws Exception {
        Result expected = Result.error(HttpStatus.getMessage(404));
        when(service.execute(any())).thenReturn(GetSubmodelElementByPathResponse.builder()
                .statusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND)
                .payload(null)
                .result(expected)
                .build());
        String id = "foo";
        ContentResponse response = execute(HttpMethod.GET, "/submodels/" + EncodingHelper.base64UrlEncode(id) + "/submodel/submodel-elements/Invalid");
        Result actual = deserializer.read(new String(response.getContent()), Result.class);
        Assert.assertFalse(actual.getSuccess());
        Assert.assertEquals(MessageType.ERROR, actual.getMessages().get(0).getMessageType());
    }


    protected ContentResponse execute(HttpMethod method, String path, Map<String, String> parameters) throws Exception {
        return execute(method, path, parameters, null, null, null, null);
    }


    protected ContentResponse execute(HttpMethod method, String path) throws Exception {
        return execute(method, path, null, null, null, null, null);
    }


    protected ContentResponse execute(HttpMethod method, String path, Object payload) throws Exception {
        return execute(
                method,
                path,
                null,
                null,
                serializer.write(payload),
                DataFormat.JSON.getContentType().toString(),
                null);
    }


    protected ContentResponse execute(HttpMethod method, String path, Content contentModifier) throws Exception {
        return execute(
                method,
                path,
                null,
                contentModifier,
                null,
                null,
                null);
    }


    protected ContentResponse execute(HttpMethod method, String path, OutputModifier outputModifier) throws Exception {
        return execute(
                method,
                path,
                Map.of(
                        "level", outputModifier.getLevel().name().toLowerCase(),
                        "extend", outputModifier.getExtent().name().toLowerCase()),
                outputModifier.getContent(),
                null,
                null,
                null);
    }


    protected ContentResponse execute(
                                      HttpMethod method,
                                      String path,
                                      Map<String, String> parameters,
                                      Content content,
                                      String body,
                                      String contentType,
                                      Map<String, String> headers)
            throws Exception {
        String actualPath = path;
        if (Objects.nonNull(content) && !Objects.equals(content, Content.NORMAL)) {
            actualPath = String.format("%s/$%s", path, content.name().toLowerCase());
        }
        org.eclipse.jetty.client.api.Request request = client.newRequest(HOST, port)
                // TODO remove
                .timeout(1, TimeUnit.HOURS)
                .idleTimeout(1, TimeUnit.HOURS)
                .method(method)
                .path(actualPath)
                .scheme(scheme);
        if (parameters != null) {
            for (Map.Entry<String, String> parameter: parameters.entrySet()) {
                request = request.param(parameter.getKey(), parameter.getValue());
            }
        }
        if (body != null) {
            if (contentType != null) {
                request = request.body(new StringRequestContent(contentType, body));
            }
            else {
                request = request.body(new StringRequestContent(body));
            }
        }
        if (headers != null) {
            for (Map.Entry<String, String> header: headers.entrySet()) {
                request = request.header(header.getKey(), header.getValue());
            }
        }
        return request.send();
    }


    private void assertAccessControllAllowMessageHeader(ContentResponse response, HttpMethod... expected) {
        List<String> actual = Arrays.asList(response.getHeaders().get(CrossOriginFilter.ACCESS_CONTROL_ALLOW_METHODS_HEADER).split(HttpConstants.HEADER_VALUE_SEPARATOR));
        List<String> expectedAsString = Stream.of(expected).map(Enum::name).collect(Collectors.toList());
        Assert.assertTrue(actual.size() == expectedAsString.size()
                && actual.containsAll(expectedAsString)
                && expectedAsString.containsAll(actual));
    }


    private void assertContainsErrorText(ContentResponse response, int status, String... textSnippets) throws DeserializationException {
        Assert.assertEquals(status, response.getStatus());
        Result actual = new HttpJsonApiDeserializer().read(response.getContentAsString(), Result.class);
        Assert.assertFalse(actual.getSuccess());
        Assert.assertNotNull(actual.getMessages());
        Assert.assertEquals(1, actual.getMessages().size());
        if (Objects.nonNull(textSnippets)) {
            for (var text: textSnippets) {
                Assert.assertTrue(actual.getMessages().get(0).getText().contains(text));
            }
        }
    }

    private static class OperationRequest {

        private List<OperationVariable> inputArguments = new ArrayList<>();
        private List<OperationVariable> inoutputArguments = new ArrayList<>();
        private long timeout = 1000;

        public List<OperationVariable> getInputArguments() {
            return inputArguments;
        }


        public void setInputArguments(List<OperationVariable> inputArguments) {
            this.inputArguments = inputArguments;
        }


        public List<OperationVariable> getInoutputArguments() {
            return inoutputArguments;
        }


        public void setInoutputArguments(List<OperationVariable> inoutputArguments) {
            this.inoutputArguments = inoutputArguments;
        }


        public long getTimeout() {
            return timeout;
        }


        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }
    }
}
