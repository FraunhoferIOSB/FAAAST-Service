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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.oauth2;

import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;


/**
 * Implements logic to request tokens using oauth2 flow.
 */
public class Oauth2Client {

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    /**
     * Class constructor.
     *
     * @param httpClient HTTP client used to request tokens
     */
    public Oauth2Client(HttpClient httpClient) {
        this.httpClient = httpClient;
        mapper = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
    }


    /**
     * Request a token using oauth2 flow.
     *
     * @param request Token request containing required credentials
     * @return Token response containing token and expiry information
     * @throws MessageBusException If the status code of the token response was not 2xx.
     * @Failed deserializing token response.
     */
    public Oauth2CredentialsResponse requestToken(Oauth2CredentialsRequest request) throws MessageBusException {

        HttpResponse<String> response;
        try {
            response = httpClient.send(toHttpRequest(request), HttpResponse.BodyHandlers.ofString());
        }
        catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MessageBusException("Oauth2 token request failed", e);
        }

        if (response.statusCode() < 200 || response.statusCode() > 299) {
            throw new MessageBusException(String.format("Oauth2 token request failed: code: %d, response: %s ", response.statusCode(), response.body()));
        }
        return mapper.readValue(response.body(), Oauth2CredentialsResponse.class);
    }


    private HttpRequest toHttpRequest(Oauth2CredentialsRequest request) {
        return HttpRequest.newBuilder(URI.create(request.url()))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(request.body()))
                .build();
    }
}
