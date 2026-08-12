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
import static org.junit.Assert.assertTrue;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.GreaterThanEqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.GreaterThanOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.LessThanEqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.LessThanOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical.AndOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.ClaimAttribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.AccessPermissionRule;
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

        List<LogicalExpression> terms = ((AndOperation) actual.get(0).formula()).getOperands();
        assertEquals(((AndOperation) uninjectedRule.formula()).getOperands().size(), terms.size());

        for (LogicalExpression term: terms) {
            if (term instanceof ClaimAttribute claimAttribute) {
                assertEquals(JOHN_DOE.get("name"), claimAttribute.getClaim());
            }
            else if (term instanceof GreaterThanEqualsOperation ge) {
                assertNotNull(ge.getLeft());
                assertNotNull(ge.getRight());
            }
            else if (term instanceof LessThanEqualsOperation le) {
                assertEquals(JOHN_DOE.get("iat"), le.getLeft().asLiteral().asString().value());
                assertNotNull(le.getRight());
            }
            else if (term instanceof LessThanOperation lt) {
                assertNotNull(lt.getLeft());
                assertNotNull(lt.getRight());
            }
            else if (term instanceof GreaterThanOperation gt) {
                assertTrue(gt.getLeft().asLiteral().asBoolean().value());
                assertNotNull(gt.getRight());
            }
            else {
                throw new IllegalStateException(String.format("Error at %s", term));
            }
        }
    }
}
