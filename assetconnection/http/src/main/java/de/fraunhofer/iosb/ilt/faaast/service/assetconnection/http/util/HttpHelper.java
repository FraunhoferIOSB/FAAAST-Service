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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.util;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.format.FormatFactory;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;


/**
 * Utility class for HTTP communication.
 */
public class HttpHelper {

    private HttpHelper() {}


    /**
     * Checks if a response has a 2xx return code indicating success.
     *
     * @param response the response to check
     * @return true if response is not null and has a return code 2xx.
     */
    public static boolean is2xxSuccessful(HttpResponse<?> response) {
        return response != null && is2xxSuccessful(response.statusCode());
    }


    /**
     * Checks if a statuc code is 2xx, i.e.successfull.
     *
     * @param statusCode the status code
     * @return true is in 2xx, otherweise false
     */
    public static boolean is2xxSuccessful(int statusCode) {
        return statusCode >= 200 && statusCode <= 299;
    }


    /**
     * Executes an HTTP request.
     *
     * @param <T> type of the payload
     * @param client the HTTP client
     * @param baseUrl the base URL
     * @param path the path
     * @param format the format key
     * @param method the HTTP method to use
     * @param bodyPublisher the body publisher
     * @param bodyHandler the body handler
     * @param headers the headers to use for the request
     * @return an HTTP response
     * @throws URISyntaxException if the URL is invalid
     * @throws IOException if URL is invalid or HTTP communication fails
     * @throws InterruptedException if HTTP communication fails
     * @throws IllegalArgumentException if client is null
     * @throws IllegalArgumentException if baseUrl is null
     * @throws IllegalArgumentException if path is null
     * @throws IllegalArgumentException if method is null
     * @throws IllegalArgumentException if format is null or invalid
     */
    public static <T> HttpResponse<T> execute(
                                              HttpClient client,
                                              URL baseUrl,
                                              String path,
                                              String format,
                                              String method,
                                              BodyPublisher bodyPublisher,
                                              BodyHandler<T> bodyHandler,
                                              Map<String, String> headers)
            throws URISyntaxException, IOException, InterruptedException {
        Ensure.requireNonNull(client, "client must be non-null");
        Ensure.requireNonNull(baseUrl, "baseUrl must be non-null");
        Ensure.requireNonNull(path, "path must be non-null");
        Ensure.requireNonNull(method, "method must be non-null");
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(new URL(baseUrl, path).toURI());
        String mimeType = FormatFactory.create(format).getMimeType();
        if (!StringUtils.isBlank(mimeType)) {
            builder = builder.header(HttpConstants.CONTENT_TYPE, mimeType);
        }
        if (headers != null) {
            for (var header: headers.entrySet()) {
                builder = builder.header(header.getKey(), header.getValue());
            }
        }
        return client.send(builder.method(method, bodyPublisher).build(), bodyHandler);
    }


    /**
     * Merges multiple header definitions into one. Redefinitions of the same header get overridden by later mentions.
     *
     * @param values header definitions to merge
     * @return merged header definition
     */
    public static Map<String, String> mergeHeaders(Map<String, String>... values) {
        if (values == null) {
            return Map.of();
        }
        return Stream.of(values)
                .filter(Objects::nonNull)
                .flatMap(x -> x.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (x, y) -> y));
    }
}
