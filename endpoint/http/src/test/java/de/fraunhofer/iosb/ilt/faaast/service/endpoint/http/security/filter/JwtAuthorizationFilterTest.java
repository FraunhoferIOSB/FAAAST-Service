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

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.AbstractJwtFilter.AUTHORIZATION;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.AbstractJwtFilter.BEARER;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.AuthState.AUTHENTICATED;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.SharedAttributes.ACL;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.util.JwtTestHelper.JOHN_DOE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;


public class JwtAuthorizationFilterTest {

    protected static HttpServletRequest mockRequest(List<AccessPermissionRule> rules) {
        return mockRequest(rules, HttpMethod.GET, "/");
    }


    protected static HttpServletRequest mockRequest(List<AccessPermissionRule> rules, HttpMethod method, String path) {
        return mockRequest(rules, method, path, JOHN_DOE.jwtString());
    }


    protected static HttpServletRequest mockRequest(List<AccessPermissionRule> rules, HttpMethod method, String path, String jwtString) {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getAttribute(ACL.getName())).thenReturn(rules);
        when(mockRequest.getMethod()).thenReturn(method.name());
        when(mockRequest.getPathInfo()).thenReturn(path);
        when(mockRequest.getAttribute(SharedAttributes.AUTH_STATE.getName())).thenReturn(AUTHENTICATED.getName());
        when(mockRequest.getHeader(AUTHORIZATION)).thenReturn(BEARER.concat(" ")
                .concat(jwtString));

        return mockRequest;
    }
}
