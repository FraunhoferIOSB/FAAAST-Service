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

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.util.JwtTestHelper.JOHN_DOE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.LogicalExpression;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.Test;


public class AclAttributeInjectionInterceptorTest extends AbstractAclFilterTest {

    protected AbstractAclFilter createFilter() {
        return new AclAttributeInjectionInterceptor();
    }


    @Test
    public void testInjectAttributes() {
        AccessPermissionRule uninjectedRule = rule();
        List<AccessPermissionRule> rules = List.of(uninjectedRule);

        HttpServletRequest mockRequest = mockRequest(rules);

        List<AccessPermissionRule> actual = filter.doFilter(mockRequest, rules);

        List<LogicalExpression> terms = actual.get(0).getFormula().get$and();
        assertEquals(uninjectedRule.getFormula().get$and().size(), terms.size());

        for (LogicalExpression term: terms) {
            if (!term.get$eq().isEmpty()) {
                assertEquals(JOHN_DOE.get("name"), term.get$eq().get(0).get$strVal());
                assertNull(term.get$eq().get(0).get$attribute());
            }
            else if (!term.get$ge().isEmpty()) {
                assertNotNull(term.get$ge().get(0).get$timeVal());
                assertNull(term.get$ge().get(0).get$attribute());
            }
            else if (!term.get$le().isEmpty()) {
                assertEquals(JOHN_DOE.get("iat"), term.get$le().get(0).get$timeVal());
                assertNull(term.get$le().get(0).get$attribute());
            }
            else if (!term.get$lt().isEmpty()) {
                assertNotNull(term.get$lt().get(0).get$timeVal());
                assertNull(term.get$lt().get(0).get$attribute());
            }
            else if (!term.get$gt().isEmpty()) {
                assertTrue(term.get$gt().get(0).get$boolean());
                assertNull(term.get$gt().get(0).get$attribute());
            }
        }

    }
}
