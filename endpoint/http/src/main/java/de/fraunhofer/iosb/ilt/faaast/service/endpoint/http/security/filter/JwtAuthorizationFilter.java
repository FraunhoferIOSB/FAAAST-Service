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

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.Filter;


/**
 * Abstract filter for HTTP requests with JWT headers.
 */
public abstract class JwtAuthorizationFilter implements Filter {
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer";

    /**
     * Extracts a JWT from an HTTP request's auth header, checking the presence of the "Bearer" keyword and returning the
     * decoded token.
     *
     * @param authHeaderValue The header value of the "Authorization: ..." header.
     * @return The decoded JWT
     * @throws IllegalArgumentException if the input did not contain the bearer keyword
     */
    protected DecodedJWT extractAndDecodeJwt(String authHeaderValue) throws IllegalArgumentException {

        if (authHeaderValue == null || !authHeaderValue.startsWith(BEARER.concat(" "))) {
            throw new IllegalArgumentException(String.format("Authorization header value did not contain bearer keyword: %s", authHeaderValue));
        }

        // Remove "Bearer "
        String token = authHeaderValue.substring(BEARER.length()).trim();

        return JWT.decode(token);
    }
}
