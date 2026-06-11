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

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.JwtAuthorizationFilter.AUTHORIZATION;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.JwtAuthorizationFilter.BEARER;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.SharedAttributes.ACL;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.util.JwtTestHelper.JOHN_DOE;
import static de.fraunhofer.iosb.ilt.faaast.service.model.query.json.RightsEnum.ALL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.util.JwtTestHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Acl;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AttributeItem;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.ObjectItem;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.RightsEnum;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Value;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import org.junit.Before;
import org.junit.Test;


public abstract class AbstractAclFilterTest {

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

        HttpServletRequest mockRequest = mockRequestWith(rules);

        List<AccessPermissionRule> actual = filter.doFilter(mockRequest, rules);
        assertTrue(actual.isEmpty());
    }


    @Test
    public void testRemovesNoValidRules() {
        AccessPermissionRule unfilteredRule = rule();
        List<AccessPermissionRule> rules = List.of(unfilteredRule, unfilteredRule);

        List<AccessPermissionRule> expected = List.of(unfilteredRule, unfilteredRule);

        HttpServletRequest mockRequest = mockRequestWith(rules, HttpMethod.GET, "/api/v3.1/shells/12345/submodels/");

        List<AccessPermissionRule> actual = filter.doFilter(mockRequest, rules);
        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }


    protected static AccessPermissionRule rule() {
        AttributeItem claimAttribute = new AttributeItem();
        claimAttribute.setClaim("name");
        AttributeItem utcNow = global(AttributeItem.Global.UTCNOW);
        AttributeItem clientNow = global(AttributeItem.Global.CLIENTNOW);
        AttributeItem localNow = global(AttributeItem.Global.LOCALNOW);
        AttributeItem anonymous = global(AttributeItem.Global.ANONYMOUS);

        LogicalExpression formula = new LogicalExpression();

        formula.set$and(List.of(
                fn(claimAttribute, JOHN_DOE.get(claimAttribute.getClaim()), LogicalExpression::set$eq),
                fn(utcNow, "0:00", LogicalExpression::set$ge),
                fn(clientNow, "23:59:59", LogicalExpression::set$le),
                fn(localNow, "0:00", LogicalExpression::set$lt),
                fn(anonymous, "abc-test", LogicalExpression::set$gt)));

        var routeNoWildcard = objectRoute("/api/v3.1/shells/12345/submodels/67890/submodel-elements/Abc.Def.Ghi/invoke-async/$value");
        var routePrefixWildcard = objectRoute("*/12345/submodels/67890/submodel-elements/Abc.Def.Ghi/invoke-async/$value");
        var routeSuffixWildcard = objectRoute("/api/v3.1/shells/12345/submodels/*");

        List<ObjectItem> objects = List.of(routeNoWildcard, routePrefixWildcard, routeSuffixWildcard);

        return rule(false, List.of(ALL), List.of(claimAttribute, utcNow, clientNow, localNow), formula, objects);
    }


    protected static AccessPermissionRule rule(boolean disabled) {
        return rule(disabled, null, null, null, null);
    }


    protected static AccessPermissionRule rule(List<AttributeItem> attributes) {
        return rule(false, null, attributes, null, null);
    }


    protected static AccessPermissionRule rule(ObjectItem... objects) {
        return rule(false, null, null, null, Arrays.asList(objects));
    }


    protected static AccessPermissionRule rule(RightsEnum right) {
        return rule(false, List.of(right), null, null, null);
    }


    protected static AccessPermissionRule rule(boolean disabled, List<RightsEnum> rights, List<AttributeItem> attributes, LogicalExpression formula, List<ObjectItem> objects) {
        var rule = new AccessPermissionRule();
        var acl = new Acl();
        acl.setAccess(disabled ? Acl.Access.DISABLED : Acl.Access.ALLOW);
        acl.setRights(rights);

        acl.setAttributes(attributes);
        rule.setFormula(formula);
        rule.setAcl(acl);
        rule.setObjects(objects);
        return rule;
    }


    private static LogicalExpression fn(AttributeItem attribute, String string, BiConsumer<LogicalExpression, List<Value>> appliedFunction) {
        LogicalExpression eqFormula = new LogicalExpression();
        Value claimValue = new Value();
        claimValue.set$attribute(attribute);
        Value claimEqValue = new Value();
        claimEqValue.set$strVal(string);
        appliedFunction.accept(eqFormula, List.of(claimValue, claimEqValue));
        return eqFormula;
    }


    private static AttributeItem global(AttributeItem.Global global) {
        AttributeItem item = new AttributeItem();
        item.setGlobal(global);
        return item;
    }


    protected static ObjectItem objectRoute(String route) {
        ObjectItem item = new ObjectItem();
        item.setRoute(route);
        return item;
    }


    protected static HttpServletRequest mockRequestWith(List<AccessPermissionRule> rules) {
        return mockRequestWith(rules, HttpMethod.GET, "/");
    }


    protected static HttpServletRequest mockRequestWith(List<AccessPermissionRule> rules, HttpMethod method, String path) {
        return mockRequestWith(rules, method, path, JOHN_DOE);
    }


    protected static HttpServletRequest mockRequestWith(List<AccessPermissionRule> rules, HttpMethod method, String path, JwtTestHelper.JWTMock jwtMock) {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getAttribute(ACL.getName())).thenReturn(rules);
        when(mockRequest.getMethod()).thenReturn(method.name());
        when(mockRequest.getServletPath()).thenReturn(path);
        when(mockRequest.getHeader(AUTHORIZATION)).thenReturn(BEARER.concat(" ")
                .concat(jwtMock.getJwt()));

        return mockRequest;
    }
}
