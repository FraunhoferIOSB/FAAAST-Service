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
import org.apache.maven.shared.utils.StringUtils;


/**
 * Utility class for HTTP communication
 */
public class HttpHelper {

    private HttpHelper() {}


    public static boolean is2xxSuccessful(HttpResponse<?> response) {
        return response != null && is2xxSuccessful(response.statusCode());
    }


    public static boolean is2xxSuccessful(int statusCode) {
        return statusCode >= 200 && statusCode <= 299;
    }


    public static <T> HttpResponse<T> execute(
                                              HttpClient client,
                                              URL baseUrl,
                                              String path,
                                              String format,
                                              String method,
                                              BodyPublisher bodyPublisher,
                                              BodyHandler<T> bodyHandler)
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
        return client.send(builder.method(method, bodyPublisher).build(), bodyHandler);
    }
}
