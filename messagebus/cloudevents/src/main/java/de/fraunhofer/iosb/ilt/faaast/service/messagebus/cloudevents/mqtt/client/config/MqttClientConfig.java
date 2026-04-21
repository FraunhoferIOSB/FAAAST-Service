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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mqtt.client.config;

import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.MessageBusCloudEventsConfig;
import java.util.Objects;


/**
 * Configuration for MQTT clients used in the context of the CloudEvents message bus.
 * 
 * @param clientCertificate MQTT client certificate
 * @param host MQTT broker URL
 * @param user static auth username
 * @param password static auth password
 * @param oauth2ClientId oauth2 client ID
 * @param oauth2ClientSecret oauth2 client secret
 * @param identityProviderUrl oauth2 IdP URL
 */
public record MqttClientConfig(
        CertificateConfig clientCertificate,
        String host,
        String user,
        String password,
        String oauth2ClientId,
        String oauth2ClientSecret,
        String identityProviderUrl) {

    /**
     * Record constructor.
     */
    public MqttClientConfig {
        Objects.requireNonNull(host);
    }


    /**
     * Create a MqttClientConfig from a MessageBusCloudEventsConfig which is provided by users.
     *
     * @param cloudEventsConfig The cloudEvents config
     * @return MqttClientConfig instance
     */
    public static MqttClientConfig from(MessageBusCloudEventsConfig cloudEventsConfig) {
        return new MqttClientConfig(
                cloudEventsConfig.getClientCertificate(),
                cloudEventsConfig.getHost(),
                cloudEventsConfig.getUser(),
                cloudEventsConfig.getPassword(),
                cloudEventsConfig.getOauth2ClientId(),
                cloudEventsConfig.getOauth2ClientSecret(),
                cloudEventsConfig.getIdentityProviderUrl());
    }
}
