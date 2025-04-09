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
package de.fraunhofer.iosb.ilt.faaast.service.security;

import de.fraunhofer.iosb.ilt.faaast.service.security.attributes.ClaimAttribute;
import de.fraunhofer.iosb.ilt.faaast.service.security.expressions.ComparisonExpression;
import de.fraunhofer.iosb.ilt.faaast.service.security.expressions.ComparisonOperator;
import de.fraunhofer.iosb.ilt.faaast.service.security.objects.IdentifiableObject;
import de.fraunhofer.iosb.ilt.faaast.service.security.operands.StringOperand;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;


public class AccessRuleTest {

    @Before
    public void init() {}


    @Test
    public void testConstruction() {
        ClaimAttribute roleAttribute = new ClaimAttribute("role=admin");

        List<Right> rights = Arrays.asList(Right.READ, Right.UPDATE);

        ACL acl = new ACL();
        acl.setAttributes(Arrays.asList(roleAttribute));
        acl.setRights(rights);
        acl.setAccessType(AccessType.ALLOW);

        IdentifiableObject identifiableObject = new IdentifiableObject("Submodel123");

        ComparisonExpression conditionExpression = new ComparisonExpression(
                new StringOperand("$subject#role"),
                ComparisonOperator.EQ,
                new StringOperand("admin"));

        Condition condition = new Condition();
        condition.setExpression(conditionExpression);

        AccessPermissionRule rule = new AccessPermissionRule();
        rule.setAcl(acl);
        rule.setObjects(Arrays.asList(identifiableObject));
        rule.setFormula(condition);
    }
}
