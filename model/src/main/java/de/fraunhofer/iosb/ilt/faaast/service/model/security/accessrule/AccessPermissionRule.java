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
package de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.filter.QueryFilter;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.AccessObject;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule.AccessRule;

import java.util.List;
import java.util.Objects;


/**
 * An access permission rule combining a rule, the objects it applies to, an optional formula and filters.
 *
 * @param rule the access rule
 * @param objects the objects the rule applies to
 * @param formula the optional formula
 * @param filters the list of filters
 */
public record AccessPermissionRule(AccessRule rule, List<AccessObject> objects, LogicalExpression formula, List<QueryFilter> filters) {

    public AccessPermissionRule(AccessRule rule, List<AccessObject> objects, LogicalExpression formula) {
        this(rule, objects, formula, List.of());
    }


    public AccessPermissionRule(AccessRule rule, AccessObject object, LogicalExpression formula) {
        this(rule, List.of(object), formula, List.of());
    }


    /**
     * Returns whether the rule is enabled.
     *
     * @return true if the rule is enabled, otherwise false
     */
    public boolean isEnabled() {
        return rule.enabled();
    }


    /**
     * Returns a copy of this rule with the given formula.
     *
     * @param formula the formula to use
     * @return the modified copy
     */
    public AccessPermissionRule with(LogicalExpression formula) {
        return new AccessPermissionRule(rule, objects, formula, filters);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        AccessPermissionRule that = (AccessPermissionRule) o;
        return Objects.equals(rule, that.rule) && Objects.equals(formula, that.formula) && Objects.equals(filters, that.filters)
                && Objects.equals(objects, that.objects);
    }


    @Override
    public int hashCode() {
        return Objects.hash(rule, objects, formula, filters);
    }
}
