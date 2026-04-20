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

/**
 * Record containing relevant information to acquire oauth2 tokens from an IdP.
 *
 * @param url oauth2 IdP URL
 * @param grantType The grant type
 * @param clientId The client id
 * @param clientSecret The client secret
 */
public record Oauth2CredentialsRequest(String url, String grantType, String clientId, String clientSecret) {

    public Oauth2CredentialsRequest(String url, String clientId, String clientSecret) {
        this(url, "client_credentials", clientId, clientSecret);
    }


    String body() {
        String grantTypeBody = String.join("=", "grant_type", grantType());
        String clientIdBody = String.join("=", "client_id", clientId());
        String clientSecretBody = String.join("=", "client_secret", clientSecret());

        return String.join("&", grantTypeBody, clientIdBody, clientSecretBody);
    }
}
