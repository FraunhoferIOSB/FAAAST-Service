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

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.util.JwtTestHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Acl;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AttributeItem;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.ObjectItem;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.RightsEnum;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Value;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.JwtAuthorizationFilter.AUTHORIZATION;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.JwtAuthorizationFilter.BEARER;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.SharedAttributes.ACL;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.util.JwtTestHelper.JOHN_DOE;
import static de.fraunhofer.iosb.ilt.faaast.service.model.query.json.RightsEnum.ALL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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
    public void testEmptyListRemovesNone() throws ServletException, IOException {
        List<AccessPermissionRule> rules = List.of();

        ServletRequest mockRequest = mockRequestWith(rules);
        HttpServletResponse mockResponse = mockResponse();

        filter.doFilter(mockRequest, mockResponse, mock(FilterChain.class));

        verifyEmptyRules(mockRequest, mockResponse);
    }


    @Test
    public void testRemovesNoValidRules() throws ServletException, IOException {
        AccessPermissionRule unfilteredRule = rule();
        List<AccessPermissionRule> rules = List.of(unfilteredRule, unfilteredRule);

        List<AccessPermissionRule> expected = List.of(unfilteredRule, unfilteredRule);

        ServletRequest mockRequest = mockRequestWith(rules, HttpMethod.GET, "/");

        filter.doFilter(mockRequest, mock(HttpServletResponse.class), mock(FilterChain.class));

        verifyReturn(mockRequest, expected);
    }


    protected void verifyReturn(ServletRequest mockRequest, List<AccessPermissionRule> expected) {
        verify(mockRequest, times(1)).setAttribute(ACL.getName(), expected);
    }


    protected void verifyEmptyRules(ServletRequest mockRequest, HttpServletResponse mockResponse) throws IOException {
        verify(mockRequest, times(0)).setAttribute(any(), any());
        verify(mockResponse, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(mockResponse, times(1)).getWriter();
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
                fn(anonymous, "abc-test", LogicalExpression::set$gt)
        ));

        ObjectItem routeNoWildcard = new ObjectItem();
        routeNoWildcard.setRoute("/api/v3.1/shells/12345/submodels/67890/submodel-elements/Abc.Def.Ghi/invoke-async/$value");
        ObjectItem routeWildcard = new ObjectItem();
        routeWildcard.setRoute("*/12345/submodels/67890/submodel-elements/Abc.Def.Ghi/invoke-async/$value");



        List<ObjectItem> objects = List.of();

        return rule(false, List.of(ALL), List.of(claimAttribute, utcNow, clientNow, localNow), formula, objects);
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


    protected static AccessPermissionRule rule(boolean disabled) {
        return rule(disabled, null, null, null, null);
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


    protected static HttpServletResponse mockResponse() throws IOException {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStream() {
            @Override
            public void write(int b) {
                // intentionally empty
            }
        }));
        return mockResponse;
    }
}
