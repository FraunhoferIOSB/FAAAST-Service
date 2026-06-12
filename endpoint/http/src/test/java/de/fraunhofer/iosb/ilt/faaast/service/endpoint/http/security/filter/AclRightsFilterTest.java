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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.RightsEnum;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.Test;


public class AclRightsFilterTest extends AbstractAclFilterTest {

    protected AbstractAclFilter createFilter() {
        return new AclRightsFilter();
    }


    @Test
    public void testDetectsOperationRequest() {
        AccessPermissionRule unfilteredRule = rule(RightsEnum.EXECUTE);
        List<AccessPermissionRule> rules = List.of(unfilteredRule);

        List<AccessPermissionRule> expected = List.of(unfilteredRule);

        HttpServletRequest mockRequest = mockRequest(rules, HttpMethod.POST, "/api/v3.0/submodels/imaginary/submodel-elements/1234.5678/invoke");

        List<AccessPermissionRule> actual = filter.doFilter(mockRequest, rules);
        assertEquals(expected, actual);
    }


    @Test
    public void testDetectsOperationAsyncValueOnlyRequest() {
        AccessPermissionRule unfilteredRule = rule(RightsEnum.EXECUTE);
        List<AccessPermissionRule> rules = List.of(unfilteredRule);

        List<AccessPermissionRule> expected = List.of(unfilteredRule);

        HttpServletRequest mockRequest = mockRequest(rules, HttpMethod.POST, "/api/v3.0/submodels/imaginary/submodel-elements/1234.5678/invoke-async/$value");

        List<AccessPermissionRule> actual = filter.doFilter(mockRequest, rules);
        assertEquals(expected, actual);
    }


    @Test
    public void testDetectsNonOperationRequest() {
        AccessPermissionRule unfilteredRule = rule(RightsEnum.EXECUTE);
        List<AccessPermissionRule> rules = List.of(unfilteredRule);

        HttpServletRequest mockRequest = mockRequest(rules, HttpMethod.POST, "/api/v3.0/submodels/imaginary/submodel-elements/1234.5678");

        List<AccessPermissionRule> actual = filter.doFilter(mockRequest, rules);
        assertTrue(actual.isEmpty());
    }


    @Test
    public void testDeleteRequest() {
        AccessPermissionRule unfilteredRule = rule(RightsEnum.DELETE);
        List<AccessPermissionRule> rules = List.of(unfilteredRule);
        List<AccessPermissionRule> expected = List.of(unfilteredRule);

        HttpServletRequest mockRequest = mockRequest(rules, HttpMethod.DELETE, "/api/v3.0/submodels/imaginary/submodel-elements/1234.5678");

        List<AccessPermissionRule> actual = filter.doFilter(mockRequest, rules);
        assertEquals(expected, actual);
    }


    @Test
    public void testEmptyPathRequest() {
        AccessPermissionRule unfilteredRule = rule(RightsEnum.DELETE);
        List<AccessPermissionRule> rules = List.of(unfilteredRule);

        HttpServletRequest mockRequest = mockRequest(rules, HttpMethod.GET, "/");

        List<AccessPermissionRule> actual = filter.doFilter(mockRequest, rules);
        assertTrue(actual.isEmpty());
    }

}
