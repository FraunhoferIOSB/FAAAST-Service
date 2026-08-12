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
package de.fraunhofer.iosb.ilt.faaast.service.model.security.parser;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.filter.QueryFilter;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.Parser;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.QueryFilterParser;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.expression.logical.LogicalExpressionParser;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.AccessRuleEntity;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.UnresolvedAccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.UseFormula;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.AccessObject;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.UseObject;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule.Rule;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule.UseRule;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


public class AccessPermissionRuleParser implements Parser<AccessPermissionRule, UnresolvedAccessPermissionRule> {

    private final AclToRuleParser ruleParser = new AclToRuleParser();
    private final ObjectItemToAccessObjectParser objectParser = new ObjectItemToAccessObjectParser();
    private final LogicalExpressionParser formulaParser = new LogicalExpressionParser();
    private final QueryFilterParser filterParser = new QueryFilterParser();

    public UnresolvedAccessPermissionRule parse(AccessPermissionRule idtaAccessPermissionRule) {
        AccessRuleEntity<Rule> accessRule;
        if (idtaAccessPermissionRule.getAcl() != null) {
            accessRule = ruleParser.parse(idtaAccessPermissionRule.getAcl());
        }
        else {
            accessRule = new UseRule(idtaAccessPermissionRule.getUseacl());
        }

        List<AccessRuleEntity<AccessObject>> objects = new ArrayList<>();

        // Objects may be empty...
        if (idtaAccessPermissionRule.getObjects() != null) {
            idtaAccessPermissionRule.getObjects().stream().map(objectParser::parse).forEach(objects::add);
        }
        else {
            idtaAccessPermissionRule.getUseobjects().stream().map(UseObject::new).forEach(objects::add);
        }

        AccessRuleEntity<LogicalExpression> formula;
        if (idtaAccessPermissionRule.getFormula() != null) {
            formula = formulaParser.parse(idtaAccessPermissionRule.getFormula());
        }
        else {
            formula = new UseFormula(idtaAccessPermissionRule.getUseformula());
        }

        List<QueryFilter> filters = Stream.of(idtaAccessPermissionRule.getFilter()).map(filterParser::parse).toList();

        return new UnresolvedAccessPermissionRule(accessRule, objects, formula, filters);
    }
}
