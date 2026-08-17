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

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Acl;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.Attribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.Parser;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.operand.AttributeItemToAttributeParser;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.AccessRuleEntity;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.UseAttribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule.AccessRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule.Right;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule.Rule;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule.UnresolvedAccessRule;

import java.util.List;


/**
 * Parser that converts an ACL into a rule.
 */
public class AclToRuleParser implements Parser<Acl, Rule> {
    private final RightsEnumToRightParser rightParser = new RightsEnumToRightParser();
    private final AttributeItemToAttributeParser attributeParser = new AttributeItemToAttributeParser();

    @Override
    public Rule parse(Acl acl) {
        List<Right> rights = acl.getRights().stream().map(rightParser::parse).toList();

        if (acl.getAttributes() != null) {
            List<Attribute> attributes = acl.getAttributes().stream()
                    .map(attributeParser::parse)
                    .toList();

            return new AccessRule(acl.getAccess() == Acl.Access.ALLOW, rights, attributes);
        }
        List<AccessRuleEntity<Attribute>> useAttributes = List.of(new UseAttribute(acl.getUseattributes()));
        return new UnresolvedAccessRule(acl.getAccess() == Acl.Access.ALLOW, rights, useAttributes);
    }

}
