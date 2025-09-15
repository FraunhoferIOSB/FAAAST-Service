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
import jakarta.servlet.http.HttpServletRequest;


/**
 * Abstract filter for HTTP requests with JWT headers.
 */
public abstract class JwtAuthorizationFilter implements Filter {
    private static final String BEARER_KWD = "Bearer";

    /**
     * Extracts a JWT from an HTTP request by reading its Authorization header,
     * checking the presence of the "Bearer" keyword and returning the decoded token.
     *
     * @param request An incoming HTTP request.
     *
     * @return The decoded JWT if present, else null
     */
    protected DecodedJWT extractAndDecodeJwt(HttpServletRequest request) {
        var authHeaderValue = request.getHeader("Authorization");

        if (authHeaderValue == null || !authHeaderValue.startsWith(BEARER_KWD.concat(" "))) {
            return null;
        }

        // Remove "Bearer "
        String token = authHeaderValue.substring(BEARER_KWD.length()).trim();

        return JWT.decode(token);
    }
}
