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

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.SigningKeyNotFoundException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Filters any incoming request by verifying its JWT if available. If no Authorization: Bearer <...> header is
 * available, assumes an anonymous request.
 */
public class JwtValidationFilter extends JwtAuthorizationFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtValidationFilter.class);

    private final JwkProvider jwkProvider;

    public JwtValidationFilter(JwkProvider jwkProvider) {
        this.jwkProvider = jwkProvider;
    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException,
            ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

        // Do not allow multiple auth headers
        if (httpRequest.getHeaders(AUTHORIZATION) != null && Collections.list(httpRequest.getHeaders(AUTHORIZATION)).size() > 1) {
            LOGGER.debug("Multiple authorization headers present! Not authorizing request.");
            respondUnauthorized((HttpServletResponse) servletResponse);
            return;
        }

        String authHeader = httpRequest.getHeader(AUTHORIZATION);
        if (authHeader == null) {
            // No JWT in request, anonymous requestor
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // Extract JWT
        DecodedJWT jwt = extractAndDecodeJwt(authHeader);
        if (!validateJWT(jwt)) {
            respondUnauthorized((HttpServletResponse) servletResponse);
        }
        else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }


    private static void respondUnauthorized(HttpServletResponse httpResponse) throws IOException {
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.getWriter().write("Invalid or expired token.");
    }


    private boolean validateJWT(DecodedJWT decodedJWT) throws IllegalArgumentException {
        Jwk jwk;
        try {
            jwk = jwkProvider.get(decodedJWT.getKeyId());
        }
        catch (SigningKeyNotFoundException getJwkException) {
            LOGGER.debug("No jwk can be found using the given kid: {}", decodedJWT.getKeyId(), getJwkException);
            return false;
        }
        catch (JwkException jwkException) {
            LOGGER.info("General exception thrown", jwkException);
            return false;
        }
        Algorithm algorithm;
        try {
            algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
        }
        catch (InvalidPublicKeyException invalidPublicKeyException) {
            LOGGER.debug("JWK's public key could not be built.", invalidPublicKeyException);
            return false;
        }

        try {
            algorithm.verify(decodedJWT);
        }
        catch (SignatureVerificationException signatureVerificationException) {
            LOGGER.debug("Could not verify JWT using algorithm {}", algorithm.getName());
            return false;
        }
        JWTVerifier verifier = JWT.require(algorithm).build();

        try {
            verifier.verify(decodedJWT);
        }
        catch (JWTVerificationException verificationException) {
            LOGGER.debug("Could not verify JWT");
            return false;
        }
        return true;
    }
}
