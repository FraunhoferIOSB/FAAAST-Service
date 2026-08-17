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

import java.util.List;
import java.util.Objects;


/**
 * An access rule describing whether access is enabled and the associated rights and attributes.
 *
 * @param enabled whether the rule is enabled
 * @param rights the list of rights
 * @param attributes the list of attributes
 */
public record AccessRule(boolean enabled, List<Right> rights, List<Attribute> attributes) implements Rule {
    public AccessRule(boolean enabled, Right right, List<Attribute> attributes) {
        this(enabled, List.of(right), attributes);
    }


    public AccessRule(boolean enabled, List<Right> rights, Attribute attribute) {
        this(enabled, rights, List.of(attribute));
    }


    public AccessRule(boolean enabled, Right right, Attribute attribute) {
        this(enabled, List.of(right), List.of(attribute));
    }


    @Override
    public AccessRule getInstance() {
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        AccessRule that = (AccessRule) o;
        return enabled == that.enabled && Objects.equals(rights, that.rights) && Objects.equals(attributes, that.attributes);
    }


    @Override
    public int hashCode() {
        return Objects.hash(enabled, rights, attributes);
    }
}
