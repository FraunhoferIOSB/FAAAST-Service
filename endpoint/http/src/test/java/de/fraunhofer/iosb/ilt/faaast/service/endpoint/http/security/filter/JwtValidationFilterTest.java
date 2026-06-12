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

import static de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod.GET;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.util.JwtTestHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import org.junit.Test;


public class JwtValidationFilterTest extends JwtAuthorizationFilterTest {

    @Test
    public void testJwtIsValidated() throws Exception {
        // Test whether the JWK Provider is called with the correct values
        String keyId = "kid";
        JwkJwt jwkJwt = mockJwkProvider(keyId);

        JwtValidationFilter filter = new JwtValidationFilter(jwkJwt.jwkProvider());

        ServletRequest mockRequest = mockRequest(null, GET, "/", jwkJwt.jwt());

        filter.doFilter(mockRequest, mock(ServletResponse.class), mock(FilterChain.class));
        verify(jwkJwt.jwkProvider(), times(1)).get(keyId);
    }


    private JwkJwt mockJwkProvider(String keyId) throws NoSuchAlgorithmException, JwkException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
        RSAPrivateKey priv = (RSAPrivateKey) kp.getPrivate();

        String jwt = JwtTestHelper.from(Map.of(), pub, priv, keyId);

        Jwk jwk = mock(Jwk.class);
        when(jwk.getPublicKey()).thenReturn(pub);
        when(jwk.getId()).thenReturn(keyId);

        JwkProvider mockJwkProvider = mock(UrlJwkProvider.class);

        when(mockJwkProvider.get(keyId)).thenReturn(jwk);
        return new JwkJwt(mockJwkProvider, jwt);
    }

    private record JwkJwt(JwkProvider jwkProvider, String jwt) {}
}
