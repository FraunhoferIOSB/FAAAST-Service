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
package de.fraunhofer.iosb.ilt.faaast.service.test.util;

import com.apicatalog.jsonld.http.media.MediaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpConstants;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.UnsupportedModifierException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;


/**
 * This class offers HTTP-related helper methods for the integration tests. IMPORTANT: by default, this classes disables
 * SSL and hostname validation
 */
public class HttpHelper {

    public static <T> List<T> getWithMultipleResult(HttpClient client, String url, Class<T> type)
            throws IOException, InterruptedException, URISyntaxException, DeserializationException, NoSuchAlgorithmException, KeyManagementException {
        return (List<T>) readResponseList(get(client, url), type);
    }


    public static <T> Page<T> getPage(HttpClient client, String url, Class<T> type)
            throws IOException, InterruptedException, URISyntaxException, DeserializationException, NoSuchAlgorithmException, KeyManagementException {
        return readResponsePage(get(client, url), type);
    }


    public static <T> T getWithSingleResult(HttpClient client, String url, Class<T> type)
            throws IOException, InterruptedException, URISyntaxException, DeserializationException, NoSuchAlgorithmException, KeyManagementException {
        return (T) readResponse(get(client, url), type);
    }


    public static <T> T postWithSingleResult(HttpClient client, String url, T payload, Class<T> type)
            throws IOException, URISyntaxException, InterruptedException, SerializationException, DeserializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        return (T) readResponse(post(client, url, payload), type);
    }


    public static <T> T putWithSingleResult(HttpClient client, String url, T payload, Class<T> type)
            throws IOException, InterruptedException, URISyntaxException, DeserializationException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        return (T) readResponse(put(client, url, payload), type);
    }


    public static <T> T deleteWithSingleResult(HttpClient client, String url, Class<T> type)
            throws IOException, InterruptedException, URISyntaxException, DeserializationException, NoSuchAlgorithmException, KeyManagementException {
        return (T) readResponse(delete(client, url), type);
    }


    public static HttpResponse<String> execute(HttpClient client, HttpMethod method, String url, Object payload)
            throws IOException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException, UnsupportedModifierException {
        switch (method) {
            case GET:
                return get(client, url);
            case PUT:
                return put(client, url, payload);
            case POST:
                return post(client, url, payload);
            case DELETE:
                return delete(client, url);
            default:
                throw new UnsupportedOperationException(String.format("unsupported HTTP method: %s", method));
        }
    }


    public static HttpResponse<String> execute(HttpClient client, HttpMethod method, String url)
            throws IOException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException, UnsupportedModifierException {
        return execute(client, method, url, null);
    }


    public static HttpResponse<String> put(HttpClient client, String url, Object payload)
            throws IOException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException, UnsupportedModifierException {
        return client
                .send(HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .header(HttpConstants.HEADER_CONTENT_TYPE, MediaType.JSON.toString())
                        .PUT(HttpRequest.BodyPublishers.ofString(new JsonApiSerializer().write(payload)))
                        .build(),
                        BodyHandlers.ofString());
    }


    public static HttpResponse<String> post(HttpClient client, String url, Object payload)
            throws IOException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException, UnsupportedModifierException {
        return client
                .send(HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .header(HttpConstants.HEADER_CONTENT_TYPE, MediaType.JSON.toString())
                        .POST(HttpRequest.BodyPublishers.ofString(Objects.nonNull(payload) && String.class.isAssignableFrom(payload.getClass())
                                ? (String) payload
                                : new JsonApiSerializer().write(payload)))
                        .build(),
                        BodyHandlers.ofString());
    }


    public static HttpResponse<String> delete(HttpClient client, String url)
            throws IOException, InterruptedException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        return client
                .send(HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .DELETE()
                        .build(),
                        BodyHandlers.ofString());
    }


    public static HttpResponse<String> get(HttpClient client, String url)
            throws IOException, InterruptedException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        return client
                .send(HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .GET()
                        .build(),
                        BodyHandlers.ofString());
    }


    public static <T> T readResponse(HttpResponse<String> response, Class<T> type) throws DeserializationException {
        return new JsonApiDeserializer().read(response.body(), type);
    }


    public static <T> List<T> readResponseList(HttpResponse<String> response, Class<T> type) throws DeserializationException {
        return new JsonApiDeserializer().readList(response.body(), type);
    }


    public static <T> Page<T> readResponsePage(HttpResponse<String> response, Class<T> type) throws DeserializationException {
        return new JsonApiDeserializer().read(response.body(), TypeFactory.defaultInstance().constructParametricType(Page.class, type));
    }
}
