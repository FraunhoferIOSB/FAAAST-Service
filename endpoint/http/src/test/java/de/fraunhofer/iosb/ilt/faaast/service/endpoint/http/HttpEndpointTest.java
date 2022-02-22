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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllAssetAdministrationShellsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllSubmodelElementsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAssetAdministrationShellResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetSubmodelByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PostSubmodelResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeExtractor;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultRange;
import java.net.ServerSocket;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringRequestContent;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpEndpointTest {

    private static Logger logger = LoggerFactory.getLogger(HttpEndpointTest.class);
    private static final String HOST = "localhost";
    private static int port;
    private static HttpClient client;
    private static HttpEndpoint endpoint;
    private static ServiceContext serviceContext;
    private static HttpJsonDeserializer deserializer;

    @BeforeClass
    public static void init() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Assert.assertNotNull(serverSocket);
            Assert.assertTrue(serverSocket.getLocalPort() > 0);
            port = serverSocket.getLocalPort();
        }
        deserializer = new HttpJsonDeserializer();
        HttpEndpointConfig endpointConfig = new HttpEndpointConfig();
        endpointConfig.setPort(port);
        serviceContext = mock(ServiceContext.class);
        endpoint = new HttpEndpoint();
        endpoint.init(CoreConfig.builder()
                .build(),
                endpointConfig,
                serviceContext);
        endpoint.start();
        client = new HttpClient();
        client.start();
    }


    @AfterClass
    public static void cleanUp() {
        if (client != null) {
            try {
                client.stop();
            }
            catch (Exception ex) {
                logger.info("error stopping HTTP client", ex);
            }
        }
        if (endpoint != null) {
            try {
                endpoint.stop();
            }
            catch (Exception ex) {
                logger.info("error stopping HTTP endpoint", ex);
            }
        }
    }


    public ContentResponse execute(HttpMethod method, String path, Map<String, String> parameters) throws Exception {
        return execute(method, path, parameters, null, null);
    }


    public ContentResponse execute(HttpMethod method, String path) throws Exception {
        return execute(method, path, null, null, null);
    }


    public ContentResponse execute(HttpMethod method, String path, OutputModifier outputModifier) throws Exception {
        return execute(method, path, Map.of(
                "content", outputModifier.getContent().name().toLowerCase(),
                "level", outputModifier.getLevel().name().toLowerCase(),
                "extend", outputModifier.getExtend().name().toLowerCase()),
                null, null);
    }


    public ContentResponse execute(HttpMethod method, String path, Map<String, String> parameters, String body, String contentType) throws Exception {
        Request request = client.newRequest(HOST, port)
                .method(method)
                .path(path);
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
        return request.send();
    }


    @Test
    public void testInvalidUrl() throws Exception {
        ContentResponse response = execute(HttpMethod.GET, "/foo/bar");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testParamContentNormal() throws Exception {
        Identifier id = new DefaultIdentifier();
        when(serviceContext.execute(any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.Success)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/" + EncodingHelper.base64UrlEncode(id.toString()) + "/aas?content=normal");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
    }


    @Test
    public void testParamContentTrimmed() throws Exception {
        Identifier id = new DefaultIdentifier();
        when(serviceContext.execute(any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.Success)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/" + EncodingHelper.base64UrlEncode(id.toString()) + "/aas?content=trimmed");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
    }


    @Test
    public void testParamContentReference() throws Exception {
        Identifier id = new DefaultIdentifier();
        when(serviceContext.execute(any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.Success)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/" + EncodingHelper.base64UrlEncode(id.toString()) + "/aas?content=reference");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
    }


    @Test
    public void testParamContentLevelBogus() throws Exception {
        Identifier id = new DefaultIdentifier();
        when(serviceContext.execute(any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.Success)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/" + EncodingHelper.base64UrlEncode(id.toString()) + "/aas?content=bogus&level=bogus");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
    }


    @Test
    public void testInvalidAASIdentifier() throws Exception {
        when(serviceContext.execute(any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.Success)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/bogus/aas");
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
        when(serviceContext.execute(any())).thenReturn(GetSubmodelByIdResponse.builder()
                .statusCode(StatusCode.ClientErrorResourceNotFound)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/" + EncodingHelper.base64UrlEncode(idShort));
        Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
    }


    @Test
    public void testWrongResponse() throws Exception {
        when(serviceContext.execute(any())).thenReturn(GetAllAssetAdministrationShellsResponse.builder()
                .statusCode(StatusCode.Success)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/GetAllSubmodels");
        // TODO: Discuss which status code is applicable 400/500 ?
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testPostSubmodelNoData() throws Exception {
        when(serviceContext.execute(any())).thenReturn(PostSubmodelResponse.builder()
                .statusCode(StatusCode.Success)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.POST, "/submodels");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testGetAllAssetAdministrationShells() throws Exception {
        List<AssetAdministrationShell> expectedPayload = List.of(AASFull.AAS_1);
        when(serviceContext.execute(any())).thenReturn(GetAllAssetAdministrationShellsResponse.builder()
                .statusCode(StatusCode.Success)
                .payload(expectedPayload)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        // TODO: server not returning character encoding
        logger.info("http response encoding: {}", response.getEncoding());
        logger.info("http response content: {}", new String(response.getContent(), "UTF-8"));
        List<AssetAdministrationShell> actualPayload = deserializer.readList(new String(response.getContent(), "UTF-8"), AssetAdministrationShell.class);
        Assert.assertEquals(expectedPayload, actualPayload);
    }


    @Test
    @Ignore("value only serialization not defined for AssetAdministrationShells")
    public void testGetAllAssetAdministrationShells_ValueOnly() throws Exception {
        List<AssetAdministrationShell> expectedPayload = List.of(AASFull.AAS_1);
        when(serviceContext.execute(any())).thenReturn(GetAllAssetAdministrationShellsResponse.builder()
                .statusCode(StatusCode.Success)
                .payload(expectedPayload)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells", new OutputModifier.Builder()
                .content(Content.Value)
                .build());
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        List<AssetAdministrationShell> actualPayload = deserializer.readList(new String(response.getContent()), AssetAdministrationShell.class);
        Assert.assertEquals(expectedPayload, actualPayload);
    }


    @Test
    public void testGetAllSubmodelElements_ValueOnly() throws Exception {
        List<SubmodelElement> submodelElements = List.of(
                new DefaultProperty.Builder()
                        .idShort("property1")
                        .value("hello world")
                        .valueType("string")
                        .build(),
                new DefaultRange.Builder()
                        .idShort("range1")
                        .min("1.1")
                        .max("2.0")
                        .valueType("double")
                        .build());
        when(serviceContext.execute(any())).thenReturn(GetAllSubmodelElementsResponse.builder()
                .statusCode(StatusCode.Success)
                .payload(submodelElements)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/foo/submodel/submodel-elements", new OutputModifier.Builder()
                .content(Content.Value)
                .build());
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        List<ElementValue> actual = deserializer.readValueList(new String(response.getContent()), TypeExtractor.extractTypeInfo(submodelElements));
        List<ElementValue> expected = submodelElements.stream()
                .map(x -> (ElementValue) ElementValueMapper.toValue(x))
                .collect(Collectors.toList());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodelElements() throws Exception {
        List<SubmodelElement> expected = List.of(
                new DefaultProperty.Builder()
                        .idShort("property1")
                        .value("hello world")
                        .valueType("string")
                        .build(),
                new DefaultRange.Builder()
                        .idShort("range1")
                        .min("1.1")
                        .max("2.0")
                        .valueType("double")
                        .build());
        when(serviceContext.execute(any())).thenReturn(GetAllSubmodelElementsResponse.builder()
                .statusCode(StatusCode.Success)
                .payload(expected)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/foo/submodel/submodel-elements", new OutputModifier.Builder()
                .content(Content.Normal)
                .build());
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        List<SubmodelElement> actual = deserializer.readList(new String(response.getContent()), SubmodelElement.class);
        Assert.assertEquals(expected, actual);
    }
}
