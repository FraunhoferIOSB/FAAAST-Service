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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter;

/**
 * Authentication state of a request. If a servlet request contains the attribute "AuthState: authenticated", the bearer
 * token in its "Authorization" header was validated
 * against the configured JWK provider.
 */
public enum AuthState {
    AUTHENTICATED("authenticated");

    private final String name;

    /**
     * Serialization of an auth state.
     *
     * @return String version of an auth state.
     */
    public String getName() {
        return name;
    }


    AuthState(String name) {
        this.name = name;
    }
}
