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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.ACL;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.AllAccessPermissionRules;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.AllAccessPermissionRulesRoot;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.Attribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.Objects;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.Rule;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;


public class AccessRuleTest {

    @Before
    public void init() {}


    @Test
    public void testAllowAnonymousReadConstruction() {
        // Create ACL
        ACL acl = new ACL();
        Attribute attr = new Attribute();
        attr.setGLOBAL("ANONYMOUS");
        acl.setATTRIBUTES(Arrays.asList(attr));
        acl.setRIGHTS(Arrays.asList("READ"));
        acl.setACCESS("ALLOW");

        // Create OBJECTS
        Objects obj = new Objects();
        obj.setROUTE("*");

        // Create FORMULA with a simple (boolean) expression
        Map<String, Object> formula = new HashMap<>();
        formula.put("$boolean", true);

        // Create a Rule
        Rule rule = new Rule();
        rule.setACL(acl);
        rule.setOBJECTS(Arrays.asList(obj));
        rule.setFORMULA(formula);

        // Wrap in AllAccessPermissionRules and the root
        AllAccessPermissionRules allRules = new AllAccessPermissionRules();
        allRules.setRules(Arrays.asList(rule));
        AllAccessPermissionRulesRoot root = new AllAccessPermissionRulesRoot();
        root.setAllAccessPermissionRules(allRules);
    }


    @Test
    public void testParse() throws IOException {
        InputStream inputStream = AccessRuleTest.class.getResourceAsStream("/" + "ACLReadAccessAnonymous.json");
        if (inputStream == null) {
            System.out.println("ACL not found in resources!");
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        AllAccessPermissionRulesRoot allRules = mapper.readValue(
                new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), AllAccessPermissionRulesRoot.class);
    }
}
