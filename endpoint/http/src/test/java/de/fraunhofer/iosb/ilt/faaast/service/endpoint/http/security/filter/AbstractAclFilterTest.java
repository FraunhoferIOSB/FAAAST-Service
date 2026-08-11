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
import static de.fraunhofer.iosb.ilt.faaast.service.model.query.json.RightsEnum.ALL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Acl;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AttributeItem;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.IdtaLogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.IdtaValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.ObjectItem;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.RightsEnum;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
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
        assertEquals(expected, actual);
    }


    protected static AccessPermissionRule rule() {
        AttributeItem claimAttribute = new AttributeItem();
        claimAttribute.setClaim("name");
        AttributeItem utcNow = global(AttributeItem.Global.UTCNOW);
        AttributeItem clientNow = global(AttributeItem.Global.CLIENTNOW);
        AttributeItem localNow = global(AttributeItem.Global.LOCALNOW);
        AttributeItem anonymous = global(AttributeItem.Global.ANONYMOUS);

        IdtaLogicalExpression formula = new IdtaLogicalExpression();

        formula.set$and(List.of(
                fn(claimAttribute, JOHN_DOE.get(claimAttribute.getClaim()), IdtaLogicalExpression::set$eq),
                fn(utcNow, "0:00", IdtaLogicalExpression::set$ge),
                fn(clientNow, "23:59:59", IdtaLogicalExpression::set$le),
                fn(localNow, "0:00", IdtaLogicalExpression::set$lt),
                fn(anonymous, "abc-test", IdtaLogicalExpression::set$gt)));

        var routeNoWildcard = objectRoute("/shells/12345/submodels/67890/submodel-elements/Abc.Def.Ghi/invoke-async/$value");
        var routePrefixWildcard = objectRoute("*/12345/submodels/67890/submodel-elements/Abc.Def.Ghi/invoke-async/$value");
        var routeSuffixWildcard = objectRoute("/shells/12345/submodels/*");

        List<ObjectItem> objects = List.of(routeNoWildcard, routePrefixWildcard, routeSuffixWildcard);

        return rule(false, List.of(ALL), List.of(claimAttribute, utcNow, clientNow, localNow), formula, objects);
    }


    protected static AccessPermissionRule rule(boolean disabled) {
        return rule(disabled, null, null, null, null);
    }


    protected static AccessPermissionRule rule(RightsEnum right) {
        return rule(false, List.of(right), null, null, null);
    }


    protected static AccessPermissionRule rule(List<AttributeItem> attributes) {
        return rule(false, null, attributes, null, null);
    }


    protected static AccessPermissionRule rule(IdtaLogicalExpression formula) {
        return rule(false, null, null, formula, null);
    }


    protected static AccessPermissionRule rule(ObjectItem... objects) {
        return rule(false, null, null, null, Arrays.asList(objects));
    }


    protected static AccessPermissionRule rule(boolean disabled, List<RightsEnum> rights, List<AttributeItem> attributes, IdtaLogicalExpression formula, List<ObjectItem> objects) {
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


    protected static IdtaLogicalExpression fn(AttributeItem attribute, String string, BiConsumer<IdtaLogicalExpression, List<IdtaValue>> appliedFunction) {
        IdtaLogicalExpression eqFormula = new IdtaLogicalExpression();
        IdtaValue claimValue = new IdtaValue();
        claimValue.set$attribute(attribute);
        IdtaValue claimEqValue = new IdtaValue();
        claimEqValue.set$strVal(string);
        appliedFunction.accept(eqFormula, List.of(claimValue, claimEqValue));
        return eqFormula;
    }


    protected static AttributeItem global(AttributeItem.Global global) {
        AttributeItem item = new AttributeItem();
        item.setGlobal(global);
        return item;
    }


    protected static ObjectItem objectRoute(String route) {
        ObjectItem item = new ObjectItem();
        item.setRoute(route);
        return item;
    }

}
