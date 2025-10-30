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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class ApiGatewayFilterTest extends JwtAuthorizationFilterTest {

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
    private ApiGateway apiGateway;

    private static HttpServletRequest req(String method, String uri) {
        HttpServletRequest r = mock(HttpServletRequest.class);
        when(r.getMethod()).thenReturn(method);
        when(r.getRequestURI()).thenReturn(uri);
        return r;
    }


    @Test
    public void anonymousAccessDependsOnAclFile() throws Exception {

        aclDir = tmp.newFolder("acl").toPath();
        apiGateway = new ApiGateway(aclDir.toString());

        HttpServletRequest request = req("GET", "/api/v3.0/submodels");
        HttpServletResponse response = mockResponse();
        FilterChain filterChain = mockFilterChain();

        // TODO  I suggest we build a filterChain and fail/succeed if filterChain.doFilter is called (i.e. the next filter)
        //assertFalse(filter.doFilter(null, request));
        apiGateway.isAuthorized(request);
        // Verify that request was blocked off
        verify(filterChain, never()).doFilter(any(), any());
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
}
