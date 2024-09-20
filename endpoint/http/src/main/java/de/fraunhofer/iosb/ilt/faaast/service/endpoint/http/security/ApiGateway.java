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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Objects;


/**
 * A simple ApiGateway that verifies JWT tokens against the provided jwkProvider.
 */
public class ApiGateway {
    private String jwkProvider;

    public ApiGateway(String jwkProvider) {
        this.jwkProvider = jwkProvider;
    }


    /**
     * Verifies the token by decoding it
     * 
     * @param token the JWT token
     * @return true if the token is valid
     */
    public boolean isAuthorized(String token, String path) {
        if (Objects.isNull(token)) {
            return false;
        }
        token = token.startsWith("Bearer ") ? token.substring("Bearer ".length()).trim() : token;
        DecodedJWT jwt = JWT.decode(token);
        JwkProvider provider = new UrlJwkProvider(this.jwkProvider);
        try {
            Jwk jwk = provider.get(jwt.getKeyId());
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            algorithm.verify(jwt);
            JWTVerifier verifier = JWT.require(algorithm)
                    //.withIssuer(this.jwkProvider)
                    .build();
            verifier.verify(token);
        }
        catch (JwkException e) {
            return false;
        }
        if (!verifiedClaims(jwt.getClaims(), path)) {
            return false;
        } ;
        return true;
    }


    private boolean verifiedClaims(Map<String, Claim> claims, String path) {
        //@TODO send to AuthServer to compare with SMT Security Processor
        return true;
    }
}
