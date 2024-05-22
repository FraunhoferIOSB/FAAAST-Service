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
package de.fraunhofer.iosb.ilt.faaast.service.eventlistener.mqtt;

import de.fraunhofer.iosb.ilt.faaast.service.util.SslHelper;
import java.net.http.HttpClient;


/**
 * The Http Client for event listening.
 */
public class HttpProvider {

    protected HttpClient httpClient;

    public String getBaseUrl() {
        return baseUrl;
    }

    protected final String baseUrl;

    public HttpProvider(String baseUrl) {
        try {
            this.baseUrl = baseUrl;
            httpClient = SslHelper.newClientAcceptingAllCertificates();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public HttpClient getHttpClient() {
        return httpClient;
    }
}
