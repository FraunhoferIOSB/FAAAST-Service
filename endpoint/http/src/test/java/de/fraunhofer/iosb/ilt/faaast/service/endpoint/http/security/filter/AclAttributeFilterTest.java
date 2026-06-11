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

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AttributeItem;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.Test;


public class AclAttributeFilterTest extends AbstractAclFilterTest {

    protected AbstractAclFilter createFilter() {
        return new AclAttributeFilter();
    }


    @Test
    public void testKeepsAttributes() {
        AccessPermissionRule unfilteredRule = rule();
        AttributeItem nonExistentClaim = new AttributeItem();
        nonExistentClaim.setClaim("non-existent");
        AccessPermissionRule filteredRule = rule(List.of(nonExistentClaim));
        List<AccessPermissionRule> rules = List.of(unfilteredRule, filteredRule);

        List<AccessPermissionRule> expected = List.of(unfilteredRule);

        HttpServletRequest mockRequest = mockRequestWith(rules);

        List<AccessPermissionRule> actual = filter.doFilter(mockRequest, rules);
        assertEquals(expected, actual);
    }
}
