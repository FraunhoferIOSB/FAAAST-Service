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

import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.EqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical.AndOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.Attribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.ClaimAttribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.Anonymous;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.ClientNow;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.LocalNow;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.UtcNow;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.AccessObject;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.RouteObject;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule.AccessRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule.Right;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.StringValue;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;


public abstract class AbstractAclFilterTest extends JwtAuthorizationFilterTest {

    protected AbstractAclFilter filter;

    /**
     * Implement this in concrete subclasses to provide the filter under test.
     */
    protected abstract AbstractAclFilter createFilter();


    @Before
    public void setUp() {
        this.filter = createFilter();
    }


    @Test
    public void testEmptyListRemovesNone() {
        List<AccessPermissionRule> rules = List.of();

        HttpServletRequest mockRequest = mockRequest(rules);

        List<AccessPermissionRule> actual = filter.doFilter(mockRequest, rules);
        assertTrue(actual.isEmpty());
    }


    @Test
    public void testRemovesNoValidRules() {
        AccessPermissionRule unfilteredRule = rule();
        List<AccessPermissionRule> rules = List.of(unfilteredRule, unfilteredRule);

        List<AccessPermissionRule> expected = List.of(unfilteredRule, unfilteredRule);

        HttpServletRequest mockRequest = mockRequest(rules, HttpMethod.GET, "/api/v3.1/shells/12345/submodels/");

        List<AccessPermissionRule> actual = filter.doFilter(mockRequest, rules);
        assertEquals(expected.size(), actual.size());
        // Can't check this since a filter evaluates formula partially.
        // assertEquals(expected, actual);
    }


    protected static AccessPermissionRule rule() {
        ClaimAttribute claimAttribute = new ClaimAttribute("name");
        UtcNow utcNow = new UtcNow();
        ClientNow clientNow = new ClientNow();
        LocalNow localNow = new LocalNow();
        Anonymous anonymous = new Anonymous();

        AndOperation formula = new AndOperation(List.of(
                new EqualsOperation(claimAttribute, new StringValue(JOHN_DOE.get(claimAttribute.getClaim()))),
                new EqualsOperation(utcNow, new StringValue("0:00")),
                new EqualsOperation(clientNow, new StringValue("23:59:59")),
                new EqualsOperation(localNow, new StringValue("0:00")),
                new EqualsOperation(anonymous, new StringValue("abc-test"))));

        var routeNoWildcard = new RouteObject("/shells/12345/submodels/67890/submodel-elements/Abc.Def.Ghi/invoke-async/$value");
        var routePrefixWildcard = new RouteObject("*/12345/submodels/67890/submodel-elements/Abc.Def.Ghi/invoke-async/$value");
        var routeSuffixWildcard = new RouteObject("/shells/12345/submodels/*");

        List<AccessObject> objects = List.of(routeNoWildcard, routePrefixWildcard, routeSuffixWildcard);

        return rule(false, List.of(Right.ALL), List.of(claimAttribute, utcNow, clientNow, localNow), formula, objects);
    }


    protected static AccessPermissionRule rule(boolean disabled) {
        return rule(disabled, null, null, null, null);
    }


    protected static AccessPermissionRule rule(Right right) {
        return rule(false, List.of(right), null, null, null);
    }


    protected static AccessPermissionRule rule(List<Attribute> attributes) {
        return rule(false, null, attributes, null, null);
    }


    protected static AccessPermissionRule rule(LogicalExpression formula) {
        return rule(false, null, null, formula, null);
    }


    protected static AccessPermissionRule rule(AccessObject... objects) {
        return rule(false, null, null, null, Arrays.asList(objects));
    }


    protected static AccessPermissionRule rule(boolean disabled, List<Right> rights, List<Attribute> attributes, LogicalExpression formula,
                                               List<AccessObject> objects) {
        AccessRule accessRule = new AccessRule(!disabled, rights, attributes);

        return new AccessPermissionRule(accessRule, objects, formula, List.of());
    }
}
