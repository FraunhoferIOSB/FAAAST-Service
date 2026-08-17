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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.dpp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.dpp.serialization.HttpJsonApiDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.dpp.serialization.HttpJsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.GetAssetAdministrationShellResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.GetAllAssetAdministrationShellsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.PostSubmodelResponse;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.HashHelper;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.MessageType;
import org.eclipse.digitaltwin.aas4j.v3.model.ProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.Result;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultResult;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.StringRequestContent;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractDppEndpointTest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractDppEndpointTest.class);
    protected static final String HOST = "localhost";
    private static final String API_PREFIX = "/api/v3.1";
    protected static String scheme;
    protected static int port;
    protected static HttpClient client;
    protected static DppEndpoint endpoint;
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
    public void testGetAasEndpointInformationEvaluatesSubprotocolBodyTemplateFunctions() {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell";
        String hashedAasId = HashHelper.sha256(aasId);

        List<Endpoint> actual = endpoint.getAasEndpointInformation(aasId);

        Assert.assertFalse(actual.isEmpty());

        String subprotocolBody = actual.get(0).getProtocolInformation().getSubprotocolBody();

        Assert.assertEquals(
                String.format("id: %s. hash: %s.MyTestSubprotocolBody", aasId, hashedAasId),
                subprotocolBody);
    }


    @Test
    public void testInvalidAASIdentifier() throws Exception {
        when(service.execute(any(), any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/bogus");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testInvalidAASIdentifierAndAdditionalPathElement() throws Exception {
        when(service.execute(any(), any())).thenReturn(GetAssetAdministrationShellResponse.builder()
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
    public void testMissingQueryValue() throws Exception {
        String idShort = AASFull.SUBMODEL_3.getIdShort() + "123";
        when(service.execute(any(), any())).thenReturn(GetSubmodelResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/"
                + EncodingHelper.base64UrlEncode(idShort)
                + "/submodel-elements/ExampleRelationshipElement?level=normal&content=");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testBogusAndMissingQueryValue() throws Exception {
        String idShort = AASFull.SUBMODEL_3.getIdShort() + "123";
        when(service.execute(any(), any())).thenReturn(GetSubmodelResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.GET,
                "/submodels/" + EncodingHelper.base64UrlEncode(idShort)
                        + "/submodel-elements/ExampleRelationshipElement?level=normal&bogus");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testGetAasEndpointInformationWithCustomHostname() {
        List<Endpoint> actual = endpoint.getAasEndpointInformation(UUID.randomUUID().toString());

        ProtocolInformation protocolInformation = actual.get(0).getProtocolInformation();

        DppEndpointConfig config = endpoint.asConfig();

        Assert.assertEquals(config.getHostname().concat(endpoint.getPathPrefix()).concat("/shells"),
                protocolInformation.getHref());
        Assert.assertEquals(config.getSubprotocol(), protocolInformation.getSubprotocol());
        Assert.assertEquals(config.getSubprotocolBodyEncoding(),
                protocolInformation.getSubprotocolBodyEncoding());
    }


    @Test
    public void testWrongResponse() throws Exception {
        when(service.execute(any(), any())).thenReturn(GetAllAssetAdministrationShellsResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/GetAllSubmodels");
        // TODO: Discuss which status code is applicable 400/500 ?
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testPostSubmodelNoData() throws Exception {
        when(service.execute(any(), any())).thenReturn(PostSubmodelResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.POST, "/submodels");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    @Ignore("value only serialization not defined for AssetAdministrationShells")
    public void testGetAllAssetAdministrationShellsValueOnly() throws Exception {
        Page<AssetAdministrationShell> expectedPayload = Page.of(AASFull.AAS_1);
        when(service.execute(any(), any())).thenReturn(GetAllAssetAdministrationShellsResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(expectedPayload)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells", new OutputModifier.Builder()
                .content(Content.VALUE)
                .build());
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        Page<AssetAdministrationShell> actualPayload = deserializer.read(new String(response.getContent()),
                Page.class);
        Assert.assertEquals(expectedPayload, actualPayload);
    }


    @Test
    public void testResultNotFound() throws Exception {
        Result expected = new DefaultResult.Builder()
                .messages(Message.builder()
                        .messageType(MessageType.ERROR)
                        .text(HttpStatus.getMessage(404))
                        .build())
                .build();
        when(service.execute(any(), any())).thenReturn(GetSubmodelElementByPathResponse.builder()
                .statusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND)
                .payload(null)
                .result(expected)
                .build());
        String id = "foo";
        ContentResponse response = execute(HttpMethod.GET,
                "/submodels/" + EncodingHelper.base64UrlEncode(id) + "/submodel-elements/Invalid");
        Result actual = deserializer.read(new String(response.getContent()), Result.class);
        Assert.assertEquals(MessageType.ERROR, actual.getMessages().get(0).getMessageType());
    }


    protected ContentResponse execute(HttpMethod method, String path) throws Exception {
        return execute(method, path, null, null, null, null, null);
    }


    protected ContentResponse execute(HttpMethod method, String path, OutputModifier outputModifier)
            throws Exception {
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
        actualPath = API_PREFIX + actualPath;
        Request request = client.newRequest(HOST, port)
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
                request = request.headers(x -> x.add(header.getKey(), header.getValue()));
            }
        }
        return request.send();
    }
}
