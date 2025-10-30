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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Acl;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AttributeItem;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.ObjectItem;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.RightsEnum;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;


public class AccessRuleTest {

    @Test
    public void testAllowAnonymousReadConstruction() {
        // Create ACL
        Acl acl = new Acl();
        AttributeItem attr = new AttributeItem();
        attr.setGlobal(AttributeItem.Global.valueOf("ANONYMOUS"));
        acl.setAttributes(Arrays.asList(attr));
        acl.setRights(Arrays.asList(RightsEnum.valueOf("READ")));
        acl.setAccess(Acl.Access.valueOf("ALLOW"));

        // Create OBJECTS
        ObjectItem obj = new ObjectItem();
        obj.setRoute("*");

        // Create FORMULA with a simple (boolean) expression
        LogicalExpression formula = new LogicalExpression();
        formula.set$boolean(true);

        // Create a Rule
        AccessPermissionRule rule = new AccessPermissionRule();
        rule.setAcl(acl);
        rule.setObjects(Arrays.asList(obj));
        rule.setFormula(formula);

        // Wrap in AllAccessPermissionRules and the root
        AllAccessPermissionRules allRules = new AllAccessPermissionRules();
        allRules.setRules(Arrays.asList(rule));
        assertNotNull(allRules.getRules());
    }


    @Test
    public void testParse() throws IOException {
        InputStream inputStream = AccessRuleTest.class.getResourceAsStream("/ACLReadAccessAnonymous.json");
        if (inputStream == null) {
            System.out.println("ACL not found in resources!");
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(inputStream);
        AllAccessPermissionRules allRules = mapper.treeToValue(rootNode.get("AllAccessPermissionRules"), AllAccessPermissionRules.class);
        assertNotNull(allRules);
    }
}
