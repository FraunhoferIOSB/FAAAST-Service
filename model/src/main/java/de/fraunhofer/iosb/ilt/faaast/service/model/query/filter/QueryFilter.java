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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.filter;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.FieldIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.AccessRuleEntity;


/**
 * A single filter block ({@code $filters} entry) consisting of a field fragment ({@code $fragment}) and a condition
 * ({@code $condition}) evaluated against it.
 *
 * <p>Filters reduce the amount of data returned. A fragment ending with {@code []} applies the condition row-wise to
 * each array entry; the corresponding fragment without {@code []} applies to the entire array-valued object.
 */
public record QueryFilter(FieldIdentifier fragment, LogicalExpression condition) implements AccessRuleEntity<QueryFilter> {
    public static QueryFilter EMPTY = new QueryFilter(null, null);
}
