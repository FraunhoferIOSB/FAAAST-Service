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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mqtt.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.MessageBusCloudEventsConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mqtt.client.config.MqttClientConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mqtt.client.impl.TokenBasedPahoClient;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;


public class TokenBasedPahoClientTest extends AbstractPahoClientTest<TokenBasedPahoClient> {

    @ClassRule
    public static WireMockClassRule server = new WireMockClassRule(options().dynamicPort());
    @Rule
    public WireMockClassRule instanceRule = server;

    @Before
    public void beforeEach() {
        stubFor(post(urlEqualTo("/path"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(String.format("{\"access_token\": \"%s\", \"expires_in\":\"100\"}", PASSWORD))));
    }


    @Override
    protected TokenBasedPahoClient getInstance() {
        var messageBusConfig = MessageBusCloudEventsConfig.builder().build();
        return new TokenBasedPahoClient(new MqttClientConfig(
                messageBusConfig.getClientCertificate(),
                MQTT_BROKER_URL,
                USERNAME,
                PASSWORD,
                "my-client-id",
                "my-client-secret",
                server.baseUrl() + "/path"));
    }
}
