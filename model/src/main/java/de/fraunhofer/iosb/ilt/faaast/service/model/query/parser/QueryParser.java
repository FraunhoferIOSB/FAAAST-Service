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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.parser;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.Query;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.filter.QueryFilter;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.expression.logical.LogicalExpressionParser;

import java.util.List;


/**
 * Parser for {@link Query} objects.
 */
public class QueryParser implements Parser<de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Query, Query> {

    /**
     * Parses the given IDTA query into a {@link Query}.
     *
     * @param idtaQuery the input query
     * @return the parsed query
     */
    public Query parse(de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Query idtaQuery) {
        boolean id = idtaQuery.get$select() != null;
        LogicalExpression expression = new LogicalExpressionParser().parse(idtaQuery.get$condition());
        List<QueryFilter> filter = null; // TODO new QueryFilterParser().parse(idtaQuery.get());

        return new Query(id, expression, filter);
    }
}
