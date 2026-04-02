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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mqtt.client.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mqtt.client.PahoClient;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mqtt.client.config.MqttClientConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.oauth2.Oauth2Client;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.oauth2.Oauth2CredentialsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.oauth2.Oauth2CredentialsResponse;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of the MQTT client authenticating via bearer tokens with built-in refresh mechanism.
 */
public class TokenBasedPahoClient extends PahoClient {

    private static final Logger logger = LoggerFactory.getLogger(TokenBasedPahoClient.class);

    private final ScheduledExecutorService tokenRefreshScheduler = Executors.newSingleThreadScheduledExecutor();
    private final Oauth2Client client;
    private final Oauth2CredentialsRequest request;

    public TokenBasedPahoClient(MqttClientConfig config) {
        super(config);
        client = new Oauth2Client(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build());
        request = new Oauth2CredentialsRequest(config.identityProviderUrl(), config.oauth2ClientId(), config.oauth2ClientSecret());
    }


    @Override
    public void connect() throws MessageBusException {
        refresh();
        super.connect();
    }


    @Override
    public void disconnect() {
        tokenRefreshScheduler.shutdownNow();
        super.disconnect();
    }


    private void refresh() {
        Oauth2CredentialsResponse token;
        try {
            token = client.requestToken(request);
        }
        catch (JsonProcessingException | MessageBusException e) {
            logger.warn("Token refresh failed, will retry in 60s.", e);
            tokenRefreshScheduler.schedule(this::refresh, 60, TimeUnit.SECONDS);
            return;
        }
        setPassword(token.accessToken());

        tokenRefreshScheduler.schedule(this::refresh, token.expiresIn() - 5, TimeUnit.SECONDS);
    }
}
