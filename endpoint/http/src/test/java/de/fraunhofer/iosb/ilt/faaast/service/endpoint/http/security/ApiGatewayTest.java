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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.AccessControlListAuthorizationFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;


public class ApiGatewayTest {

    private static final String ACL_JSON = "{\n" +
            "  \"AllAccessPermissionRules\": {\n" +
            "    \"rules\": [{\n" +
            "      \"ACL\": {\n" +
            "        \"ATTRIBUTES\": [{ \"GLOBAL\": \"ANONYMOUS\" }],\n" +
            "        \"RIGHTS\":     [\"READ\"],\n" +
            "        \"ACCESS\":     \"ALLOW\"\n" +
            "      },\n" +
            "      \"OBJECTS\": [{ \"ROUTE\": \"*\" }],\n" +
            "      \"FORMULA\": { \"$boolean\": true }\n" +
            "    }]\n" +
            "  }\n" +
            "}";

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();
    private Path aclDir;
    private AccessControlListAuthorizationFilter filter;

    private static HttpServletRequest req(String method, String uri) {
        HttpServletRequest r = mock(HttpServletRequest.class);
        when(r.getMethod()).thenReturn(method);
        when(r.getRequestURI()).thenReturn(uri);
        return r;
    }


    @Ignore("Classes changed, need to rewrite")
    @Test
    public void anonymousAccessDependsOnAclFile() throws Exception {

        aclDir = tmp.newFolder("acl").toPath();
        filter = new AccessControlListAuthorizationFilter(new URL("http://whatever-jwks"), aclDir.toString());

        HttpServletRequest request = req("GET", "/api/v3.0/submodels");

        // TODO  I suggest we build a filterChain and fail/succeed if filterChain.doFilter is called (i.e. the next filter)
        //assertFalse(filter.doFilter(null, request);

        Path rule = aclDir.resolve("allow.json");
        Path tmpRule = aclDir.resolve("allow.json.tmp");
        Files.writeString(tmpRule, ACL_JSON, StandardCharsets.UTF_8);
        Files.move(tmpRule, rule, StandardCopyOption.ATOMIC_MOVE);

        Thread.sleep(200);
        //assertTrue(filter.isAuthorized(null, request));

        Files.delete(rule);
        Thread.sleep(200);
        //assertFalse(filter.isAuthorized(null, request));
    }


    @Ignore("Classes changed, need to rewrite")
    @Test
    public void jwtIsVerified() throws Exception {

        aclDir = tmp.newFolder("acl").toPath();
        Files.writeString(aclDir.resolve("allow.json"),
                ACL_JSON, StandardCharsets.UTF_8);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
        RSAPrivateKey priv = (RSAPrivateKey) kp.getPrivate();

        String kid = "unit-test-kid";

        String jwt = JWT.create()
                .withKeyId(kid)
                .withClaim("dummy", "x")
                .sign(Algorithm.RSA256(pub, priv));

        Jwk jwk = mock(Jwk.class);
        when(jwk.getPublicKey()).thenReturn(pub);
        when(jwk.getId()).thenReturn(kid);

        try (MockedConstruction<UrlJwkProvider> mocked = Mockito.mockConstruction(UrlJwkProvider.class,
                (mock, ctx) -> when(mock.get(anyString())).thenReturn(jwk))) {

            filter = new AccessControlListAuthorizationFilter(new URL("http://whatever-jwks"), aclDir.toString());

            HttpServletRequest request = req("GET", "/api/v3.0/submodels");

            //assertTrue(filter.doFilter(jwt, request));
        }
    }
}
