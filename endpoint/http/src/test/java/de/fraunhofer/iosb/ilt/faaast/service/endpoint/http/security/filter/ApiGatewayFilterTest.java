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

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.auth.SharedAttributes.ACL;
import static de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AttributeItem.Global.ANONYMOUS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Acl;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AttributeItem;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.ObjectItem;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.RightsEnum;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.Test;


public class ApiGatewayFilterTest extends JwtAuthorizationFilterTest {

    private ApiGateway apiGateway;

    private static HttpServletRequest req(String method, String uri) {
        HttpServletRequest r = mock(HttpServletRequest.class);
        when(r.getMethod()).thenReturn(method);
        when(r.getRequestURI()).thenReturn(uri);
        return r;
    }


    @Test
    public void anonymousAccessDependsOnAclFile() {
        apiGateway = new ApiGateway();

        HttpServletRequest request = req("GET", "/api/v3.0/submodels");
        when(request.getAttribute(ACL.getName())).thenReturn(List.of());

        assertFalse(apiGateway.isAuthorized(request));

        AllAccessPermissionRules env = mockEnvironment();

        when(request.getAttribute(ACL.getName())).thenReturn(env.getRules());
        await().atMost(5, SECONDS).pollInterval(100, MILLISECONDS).untilAsserted(() -> assertTrue(apiGateway.isAuthorized(request)));

        when(request.getAttribute(ACL.getName())).thenReturn(List.of());
        await().atMost(5, SECONDS).pollInterval(100, MILLISECONDS).untilAsserted(() -> assertFalse(apiGateway.isAuthorized(request)));
    }


    private AllAccessPermissionRules mockEnvironment() {
        var env = new AllAccessPermissionRules();
        AccessPermissionRule rule = new AccessPermissionRule();
        Acl acl = new Acl();
        AttributeItem attributeItem = new AttributeItem();
        attributeItem.setGlobal(ANONYMOUS);
        acl.setAttributes(List.of(attributeItem));
        acl.setRights(List.of(RightsEnum.READ));
        acl.setAccess(Acl.Access.ALLOW);
        rule.setAcl(acl);
        ObjectItem object = new ObjectItem();
        object.setRoute("*");
        rule.setObjects(List.of(object));
        LogicalExpression formula = new LogicalExpression();
        formula.set$boolean(true);
        rule.setFormula(formula);
        env.setRules(List.of(rule));
        return env;
    }
}
