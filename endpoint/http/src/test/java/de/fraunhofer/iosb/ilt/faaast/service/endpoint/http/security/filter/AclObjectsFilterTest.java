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

import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.ObjectItem;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.Test;


public class AclObjectsFilterTest extends AbstractAclFilterTest {

    protected AbstractAclFilter createFilter() {
        return new AclObjectsFilter("/api/v3.1");
    }


    @Test
    public void testKeepsObjects() {
        AccessPermissionRule unfilteredRule = rule();

        ObjectItem[] invalidObjects = new ObjectItem[] {
                objectRoute("/api/v3.2/*")
        };
        AccessPermissionRule filteredRule = rule(invalidObjects);
        List<AccessPermissionRule> rules = List.of(unfilteredRule, filteredRule);

        List<AccessPermissionRule> expected = List.of(unfilteredRule);

        HttpServletRequest mockRequest = mockRequest(rules, HttpMethod.GET, "/api/v3.1/shells/12345/submodels/67890/submodel-elements/Abc.Def.Ghi/invoke-async/$value");

        List<AccessPermissionRule> actual = filter.doFilter(mockRequest, rules);
        assertEquals(expected, actual);
    }
}
