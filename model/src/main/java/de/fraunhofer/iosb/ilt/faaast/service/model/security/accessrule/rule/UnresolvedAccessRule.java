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
package de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.Attribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.AccessRuleEntity;

import java.util.List;


/**
 * An access rule whose attributes may still be unresolved references.
 *
 * @param enabled whether the rule is enabled
 * @param rights the list of rights
 * @param attributes the list of attributes, possibly unresolved
 */
public record UnresolvedAccessRule(boolean enabled, List<Right> rights, List<AccessRuleEntity<Attribute>> attributes) implements Rule {

    /**
     * Creates a resolved access rule using the given attributes.
     *
     * @param resolvedAttributes the resolved attributes
     * @return the resolved access rule
     */
    public AccessRule from(List<Attribute> resolvedAttributes) {
        return new AccessRule(enabled, rights, resolvedAttributes);
    }
}
