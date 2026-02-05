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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.junit.Test;


public class JwtValidationFilterTest extends JwtAuthorizationFilterTest {

    private JwtValidationFilter filter;

    @Test
    public void jwtIsVerified() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
        RSAPrivateKey priv = (RSAPrivateKey) kp.getPrivate();

        String kid = "unit-test-kid";

        String jwt = JWT.create()
                .withKeyId(kid)
                .sign(Algorithm.RSA256(pub, priv));

        Jwk jwk = mock(Jwk.class);
        when(jwk.getPublicKey()).thenReturn(pub);
        when(jwk.getId()).thenReturn(kid);

        JwkProvider mockJwkProvider = mock(UrlJwkProvider.class);

        when(mockJwkProvider.get(kid)).thenReturn(jwk);

        filter = new JwtValidationFilter(mockJwkProvider);

        HttpServletRequest request = mockRequest("GET", "/api/v3.0/submodels", jwt);
        HttpServletResponse response = mockResponse();
        FilterChain filterChain = mockFilterChain();

        filter.doFilter(request, response, filterChain);

        // The filter passed this request onto the next filter -> Did not block
        verify(filterChain, times(1)).doFilter(any(), any());
        // The filter called the JWK provider to verify the request
        verify(mockJwkProvider, times(1)).get(kid);
    }

}
