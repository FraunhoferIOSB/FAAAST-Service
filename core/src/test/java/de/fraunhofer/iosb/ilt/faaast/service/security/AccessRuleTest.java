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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.security.json.ACL;
import de.fraunhofer.iosb.ilt.faaast.service.security.json.AllAccessPermissionRules;
import de.fraunhofer.iosb.ilt.faaast.service.security.json.AllAccessPermissionRulesRoot;
import de.fraunhofer.iosb.ilt.faaast.service.security.json.Attribute;
import de.fraunhofer.iosb.ilt.faaast.service.security.json.Objects;
import de.fraunhofer.iosb.ilt.faaast.service.security.json.Rule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;


public class AccessRuleTest {

    @Before
    public void init() {}

    private String rule1 = "{\"AllAccessPermissionRules\":{\"rules\":[{\"ACL\":{\"ATTRIBUTES\":[{\"CLAIM\":\"__Albert__Alb\"}],\"RIGHTS\":[\"READ\"],\"ACCESS\":\"ALLOW\"},\"OBJECTS\":[{\"ROUTE\":\"*\"}],\"FORMULA\":{\"$boolean\":true}},{\"ACL\":{\"ATTRIBUTES\":[{\"CLAIM\":\"isNotAuthenticated\"}],\"RIGHTS\":[\"READ\"],\"ACCESS\":\"ALLOW\"},\"OBJECTS\":[{\"ROUTE\":\"/submodels/*\"},{\"ROUTE\":\"/shells/aHR0cHM6Ly96dmVpLm9yZy9kZW1vL2Fhcy9Db250cm9sQ2FiaW5ldA\"}],\"FORMULA\":{\"$or\":[{\"$and\":[{\"$eq\":[{\"$field\":\"$sm#idShort\"},{\"$strVal\":\"Nameplate\"}]},{\"$starts_with\":[{\"$field\":\"$sme#idShort\"},{\"$strVal\":\"Manufacturer\"}]}]},{\"$eq\":[{\"$field\":\"$sm#idShort\"},{\"$strVal\":\"TechnicalData\"}]}]},\"FRAGMENT\":\"sme#\",\"FILTER\":{\"$or\":[{\"$starts_with\":[{\"$field\":\"$sme#idShort\"},{\"$strVal\":\"Manufacturer\"}]},{\"$starts_with\":[{\"$field\":\"$sme#idShort\"},{\"$strVal\":\"General\"}]}]}}]}}";
    private String rule2 = "{\"AllAccessPermissionRules\":{\"rules\":[{\"ACL\":{\"ATTRIBUTES\":[{\"GLOBAL\":\"ANONYMOUS\"}],\"RIGHTS\":[\"READ\"],\"ACCESS\":\"ALLOW\"},\"OBJECTS\":[{\"ROUTE\":\"*\"}],\"FORMULA\":{\"$or\":[{\"$eq\":[{\"$field\":\"$sm#idShort\"},{\"$strVal\":\"Nameplate\"}]},{\"$eq\":[{\"$field\":\"$sm#idShort\"},{\"$strVal\":\"TechnicalData\"}]}]}}]}}";

    private String ruleWithFilter = "{\"AllAccessPermissionRules\":{\"rules\":[{\"ACL\":{\"ATTRIBUTES\":[{\"CLAIM\":\"BusinessPartnerNumber\"}],\"RIGHTS\":[\"READ\"],\"ACCESS\":\"ALLOW\"},\"OBJECTS\":[{\"DESCRIPTOR\":\"(aasdesc)*\"}],\"FORMULA\":{\"$and\":[{\"$eq\":[{\"$attribute\":{\"CLAIM\":\"BusinessPartnerNumber\"}},{\"$strVal\":\"BPNL00000000000A\"}]},{\"$match\":[{\"$eq\":[{\"$field\":\"$aasdesc#specificAssetIds[].name\"},{\"$strVal\":\"manufacturerPartId\"}]},{\"$eq\":[{\"$field\":\"$aasdesc#specificAssetIds[].value\"},{\"$strVal\":\"99991\"}]},{\"$eq\":[{\"$field\":\"$aasdesc#specificAssetIds[].externalSubjectId\"},{\"$strVal\":\"PUBLIC_READABLE\"}]}]},{\"$match\":[{\"$eq\":[{\"$field\":\"$aasdesc#specificAssetIds[].name\"},{\"$strVal\":\"customerPartId\"}]},{\"$eq\":[{\"$field\":\"$aasdesc#specificAssetIds[].value\"},{\"$strVal\":\"ACME001\"}]}]}]},\"FILTER\":{\"FRAGMENT\":\"$aasdesc#assetInformation.specificAssetIds[]\",\"CONDITION\":{\"$or\":[{\"$match\":[{\"$eq\":[{\"$field\":\"$aasdesc#specificAssetIds[].name\"},{\"$strVal\":\"manufacturerPartId\"}]},{\"$eq\":[{\"$field\":\"$aasdesc#specificAssetIds[].value\"},{\"$strVal\":\"99991\"}]}]},{\"$match\":[{\"$eq\":[{\"$field\":\"$aasdesc#specificAssetIds[].name\"},{\"$strVal\":\"customerPartId\"}]},{\"$eq\":[{\"$field\":\"$aasdesc#specificAssetIds[].value\"},{\"$strVal\":\"ACME001\"}]}]},{\"$eq\":[{\"$field\":\"$aasdesc#specificAssetIds[].name\"},{\"$strVal\":\"partInstanceId\"}]},{\"$eq\":[{\"$field\":\"$aasdesc#specificAssetIds[].externalSubjectId\"},{\"$attribute\":{\"CLAIM\":\"BusinessPartnerNumber\"}}]},{\"$eq\":[{\"$field\":\"$aasdesc#specificAssetIds[].externalSubjectId\"},{\"$strVal\":\"PUBLIC_READABLE\"}]}]}}}]}}";
    @Test
    public void testAllowAnonymousRead() {
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
    public void testParse1() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        AllAccessPermissionRulesRoot allRules = mapper.readValue(rule1, AllAccessPermissionRulesRoot.class);
    }


    @Test
    public void testParse2() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        AllAccessPermissionRulesRoot allRules = mapper.readValue(rule2, AllAccessPermissionRulesRoot.class);

    }

    @Test
    public void testParse3() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        AllAccessPermissionRulesRoot allRules = mapper.readValue(ruleWithFilter, AllAccessPermissionRulesRoot.class);
    }
}
