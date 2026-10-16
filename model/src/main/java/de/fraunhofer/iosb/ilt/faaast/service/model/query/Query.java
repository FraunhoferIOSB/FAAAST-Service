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
package de.fraunhofer.iosb.ilt.faaast.service.model.query;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.filter.QueryFilter;

import java.util.List;


/**
 * Represents the root object of a parsed AAS Query, consisting of an optional {@code $select} statement, a
 * {@code $condition} and an optional list of {@code $filters}.
 *
 * <p>By default, a query returns a list of the respective AAS objects. If {@code selectId} is set, only the list of
 * identifiers is returned ({@code $select id}).
 *
 * @param selectId whether only the identifiers ({@code $select id}) should be returned
 * @param condition the logical condition
 * @param filters the list of query filters
 */
public record Query(boolean selectId, LogicalExpression condition, List<QueryFilter> filters) {

}
