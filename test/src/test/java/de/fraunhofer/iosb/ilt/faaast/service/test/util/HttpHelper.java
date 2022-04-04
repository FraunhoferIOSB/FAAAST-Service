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

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;


public class HttpHelper {

    public static <T> List<T> getWithMultipleResult(String url, Class<T> type) throws IOException, InterruptedException, URISyntaxException, DeserializationException {
        return (List<T>) readResponseList(get(url), type);
    }


    public static <T> T getWithSingleResult(String url, Class<T> type) throws IOException, InterruptedException, URISyntaxException, DeserializationException {
        return (T) readResponse(get(url), type);
    }


    public static <T> T postWithSingleResult(String url, T payload, Class<T> type)
            throws IOException, URISyntaxException, InterruptedException, SerializationException, DeserializationException {
        return (T) readResponse(post(url, payload), type);
    }


    public static <T> T putWithSingleResult(String url, T payload, Class<T> type)
            throws IOException, InterruptedException, URISyntaxException, DeserializationException, SerializationException {
        return (T) readResponse(put(url, payload), type);
    }


    public static <T> T deleteWithSingleResult(String url, Class<T> type) throws IOException, InterruptedException, URISyntaxException, DeserializationException {
        return (T) readResponse(delete(url), type);
    }


    public static HttpResponse<String> execute(HttpMethod method, String url, Object payload) throws IOException, InterruptedException, URISyntaxException, SerializationException {
        switch (method) {
            case GET:
                return get(url);
            case PUT:
                return put(url, payload);
            case POST:
                return post(url, payload);
            case DELETE:
                return delete(url);
            default:
                throw new UnsupportedOperationException(String.format("unsupported HTTP method: %s", method));
        }
    }


    public static HttpResponse<String> put(String url, Object payload) throws IOException, InterruptedException, URISyntaxException, SerializationException {
        return HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .PUT(HttpRequest.BodyPublishers.ofString(new JsonSerializer().write(payload)))
                        .build(),
                        BodyHandlers.ofString());
    }


    public static HttpResponse<String> post(String url, Object payload) throws IOException, InterruptedException, URISyntaxException, SerializationException {
        return HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .POST(HttpRequest.BodyPublishers.ofString(new JsonSerializer().write(payload)))
                        .build(),
                        BodyHandlers.ofString());
    }


    public static HttpResponse<String> delete(String url) throws IOException, InterruptedException, URISyntaxException {
        return HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .DELETE()
                        .build(),
                        BodyHandlers.ofString());
    }


    public static HttpResponse<String> get(String url) throws IOException, InterruptedException, URISyntaxException {
        return HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .GET()
                        .build(),
                        BodyHandlers.ofString());
    }


    public static <T> T readResponse(HttpResponse<String> response, Class<T> type) throws DeserializationException {
        return new JsonDeserializer().read(response.body(), type);
    }


    public static <T> List<T> readResponseList(HttpResponse<String> response, Class<T> type) throws DeserializationException {
        return new JsonDeserializer().readList(response.body(), type);
    }
}
