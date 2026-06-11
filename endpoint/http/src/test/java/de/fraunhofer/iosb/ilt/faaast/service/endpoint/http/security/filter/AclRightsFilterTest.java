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

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.SharedAttributes.ACL;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.RightsEnum;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.junit.Test;


public class AclRightsFilterTest extends AbstractAclFilterTest {

    protected AbstractAclFilter createFilter() {
        return new AclRightsFilter();
    }


    @Test
    public void testDetectsOperationRequest() throws ServletException, IOException {
        AccessPermissionRule unfilteredRule = rule(RightsEnum.EXECUTE);
        List<AccessPermissionRule> rules = List.of(unfilteredRule);

        List<AccessPermissionRule> expected = List.of(unfilteredRule);

        ServletRequest mockRequest = mockRequestWith(rules, HttpMethod.POST, "/api/v3.0/submodels/imaginary/submodel-elements/1234.5678/invoke");

        filter.doFilter(mockRequest, mock(ServletResponse.class), mock(FilterChain.class));

        verify(mockRequest).setAttribute(ACL.getName(), expected);
    }


    @Test
    public void testDetectsOperationAsyncValueOnlyRequest() throws ServletException, IOException {
        AccessPermissionRule unfilteredRule = rule(RightsEnum.EXECUTE);
        List<AccessPermissionRule> rules = List.of(unfilteredRule);

        List<AccessPermissionRule> expected = List.of(unfilteredRule);

        ServletRequest mockRequest = mockRequestWith(rules, HttpMethod.POST, "/api/v3.0/submodels/imaginary/submodel-elements/1234.5678/invoke-async/$value");

        filter.doFilter(mockRequest, mock(ServletResponse.class), mock(FilterChain.class));

        verifyReturn(mockRequest, expected);
    }


    @Test
    public void testDetectsNonOperationRequest() throws ServletException, IOException {
        AccessPermissionRule unfilteredRule = rule(RightsEnum.EXECUTE);
        List<AccessPermissionRule> rules = List.of(unfilteredRule);

        ServletRequest mockRequest = mockRequestWith(rules, HttpMethod.POST, "/api/v3.0/submodels/imaginary/submodel-elements/1234.5678");
        HttpServletResponse mockResponse = mockResponse();

        filter.doFilter(mockRequest, mockResponse, mock(FilterChain.class));

        verifyEmptyRules(mockRequest, mockResponse);
    }


    @Test
    public void testDeleteRequest() throws ServletException, IOException {
        AccessPermissionRule unfilteredRule = rule(RightsEnum.DELETE);
        List<AccessPermissionRule> rules = List.of(unfilteredRule);
        List<AccessPermissionRule> expected = List.of(unfilteredRule);

        ServletRequest mockRequest = mockRequestWith(rules, HttpMethod.DELETE, "/api/v3.0/submodels/imaginary/submodel-elements/1234.5678");
        HttpServletResponse mockResponse = mockResponse();

        filter.doFilter(mockRequest, mockResponse, mock(FilterChain.class));

        verifyReturn(mockRequest, expected);
    }


    @Test
    public void testEmptyPathRequest() throws ServletException, IOException {
        AccessPermissionRule unfilteredRule = rule(RightsEnum.DELETE);
        List<AccessPermissionRule> rules = List.of(unfilteredRule);

        ServletRequest mockRequest = mockRequestWith(rules, HttpMethod.GET, "/");
        HttpServletResponse mockResponse = mockResponse();

        filter.doFilter(mockRequest, mockResponse, mock(FilterChain.class));

        verifyEmptyRules(mockRequest, mockResponse);
    }

}
