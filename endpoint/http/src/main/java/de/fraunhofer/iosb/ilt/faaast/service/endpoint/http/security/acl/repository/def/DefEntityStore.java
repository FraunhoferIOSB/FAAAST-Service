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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.acl.repository.def;

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.acl.repository.def.entity.DefAccessObject;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.acl.repository.def.entity.DefAttribute;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.acl.repository.def.entity.DefFormula;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.acl.repository.def.entity.DefRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Defacl;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Defattribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Defformula;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Defobject;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.Attribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.expression.logical.LogicalExpressionParser;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.operand.AttributeItemToAttributeParser;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.AccessObject;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule.Rule;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.parser.AclToRuleParser;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.parser.ObjectItemToAccessObjectParser;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class DefEntityStore<T> {
    private final Map<T, DefAttribute> defAttributes;
    private final Map<T, DefRule> defAccessRules;
    private final Map<T, DefAccessObject> defAccessObjects;
    private final Map<T, DefFormula> defFormulas;

    private final AclToRuleParser aclParser = new AclToRuleParser();
    private final AttributeItemToAttributeParser attributeParser = new AttributeItemToAttributeParser();
    private final LogicalExpressionParser formulaParser = new LogicalExpressionParser();
    private final ObjectItemToAccessObjectParser objectParser = new ObjectItemToAccessObjectParser();

    public DefEntityStore() {
        defAttributes = new ConcurrentHashMap<>();
        defAccessObjects = new ConcurrentHashMap<>();
        defAccessRules = new ConcurrentHashMap<>();
        defFormulas = new ConcurrentHashMap<>();
    }


    public void addDefAttributes(T identifier, List<Defattribute> values) {
        Map<String, List<Attribute>> parsed = values.stream()
                .map(defAttribute -> Map.entry(defAttribute.getName(), defAttribute.getAttributes().stream().map(attributeParser::parse).toList()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        defAttributes.put(identifier, new DefAttribute(parsed));
    }


    public void addDefAcls(T identifier, List<Defacl> values) {
        Map<String, Rule> parsed = values.stream()
                .map(defacl -> Map.entry(defacl.getName(), aclParser.parse(defacl.getAcl())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        defAccessRules.put(identifier, new DefRule(parsed));
    }


    public void addDefObjects(T identifier, List<Defobject> values) {
        Map<String, List<AccessObject>> parsed = values.stream()
                .map(defObject -> Map.entry(defObject.getName(), defObject.getObjects().stream().map(objectParser::parse).toList()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        defAccessObjects.put(identifier, new DefAccessObject(parsed));
    }


    public void addDefFormulas(T identifier, List<Defformula> values) {
        Map<String, LogicalExpression> parsed = values.stream()
                .map(defFormula -> Map.entry(defFormula.getName(), formulaParser.parse(defFormula.getFormula())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        defFormulas.put(identifier, new DefFormula(parsed));
    }


    public void delete(T identifier) {
        defAttributes.remove(identifier);
        defAccessRules.remove(identifier);
        defAccessObjects.remove(identifier);
        defFormulas.remove(identifier);
    }


    public List<Attribute> getAttribute(String name) {
        return defAttributes
                .values().stream()
                .flatMap(x -> x.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .get(name);
    }


    public Rule getAccessRule(String name) {
        return defAccessRules
                .values().stream()
                .flatMap(x -> x.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .get(name);
    }


    public List<AccessObject> getObjects(String name) {
        return defAccessObjects
                .values().stream()
                .flatMap(x -> x.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .get(name);
    }


    public LogicalExpression getFormula(String name) {
        return defFormulas
                .values().stream()
                .flatMap(x -> x.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .get(name);
    }
}
