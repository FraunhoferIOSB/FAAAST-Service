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
import static org.junit.Assert.assertTrue;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.EqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.GreaterThanEqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.GreaterThanOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.LessThanEqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.LessThanOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical.AndOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.ClaimAttribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.Anonymous;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.ClientNow;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.GlobalAttribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.LocalNow;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.UtcNow;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.BooleanValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.StringValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TimeValue;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.Test;


public class AclAttributeInjectionInterceptorTest extends AbstractAclFilterTest {

    protected AbstractAclFilter createFilter() {
        return new AclAttributeInjectionInterceptor();
    }


    @Test
    public void testInjectAttributes() throws ValueFormatException {
        AccessPermissionRule uninjectedRule = rule(formulaWithAttributes());
        List<AccessPermissionRule> rules = List.of(uninjectedRule);

        HttpServletRequest mockRequest = mockRequest(rules);

        List<AccessPermissionRule> actual = filter.doFilter(mockRequest, rules);

        assertEquals(1, actual.size());
        LogicalExpression formula = actual.get(0).formula();
        assertTrue(formula.isBoolean());
        assertTrue(((BooleanValue) formula.asTypedValue()).getValue());
    }


    private LogicalExpression formulaWithAttributes() throws ValueFormatException {
        ClaimAttribute claimAttribute = new ClaimAttribute("name");
        GlobalAttribute utcNow = new UtcNow();
        GlobalAttribute clientNow = new ClientNow();
        GlobalAttribute localNow = new LocalNow();
        GlobalAttribute anonymous = new Anonymous();
        return new AndOperation(List.of(
                new EqualsOperation(claimAttribute, new StringValue(JOHN_DOE.get(claimAttribute.getClaim()))),
                new GreaterThanEqualsOperation(utcNow, fromString("00:00")),
                new LessThanEqualsOperation(clientNow, fromString("23:59:59")),
                new LessThanOperation(localNow, fromString("00:00")),
                new GreaterThanOperation(anonymous, new StringValue("abc-test"))));
    }


    private TimeValue fromString(String from) throws ValueFormatException {
        TimeValue value = new TimeValue();
        value.fromString(from);
        return value;
    }
}
