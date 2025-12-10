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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import org.junit.Test;


public class JwtAuthorizationFilterTest {

    private JwtAuthorizationFilter testSubject = new JwtAuthorizationFilter() {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
            // Intentionally left blank
        }
    };

    @Test
    public void testExtractAndDecodeJwt() {
        // TODO
    }


    protected FilterChain mockFilterChain() {
        return mock(FilterChain.class);
    }


    protected static HttpServletRequest mockRequest(String method, String uri, String jwt) {
        HttpServletRequest r = mock(HttpServletRequest.class);
        //when(r.getMethod()).thenReturn(method);
        //when(r.getRequestURI()).thenReturn(uri);
        when(r.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(r.getHeaders("Authorization")).thenReturn(Collections.enumeration(List.of("Bearer " + jwt)));

        return r;
    }


    protected static HttpServletResponse mockResponse() {
        HttpServletResponse r = mock(HttpServletResponse.class);
        return r;
    }

}
