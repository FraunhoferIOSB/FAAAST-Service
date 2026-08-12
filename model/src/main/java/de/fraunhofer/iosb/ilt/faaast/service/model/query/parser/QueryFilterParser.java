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

import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.filter.QueryFilter;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.SecurityQueryFilter;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.FieldIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.expression.logical.LogicalExpressionParser;


public class QueryFilterParser extends AbstractParser<SecurityQueryFilter, QueryFilter> {

    private final LogicalExpressionParser logicalExpressionParser = new LogicalExpressionParser();

    @Override
    public QueryFilter parse(de.fraunhofer.iosb.ilt.faaast.service.model.query.json.SecurityQueryFilter input) {
        FieldIdentifier fieldIdentifierAttribute = stringToFieldIdentifierParser.parse(input.getFragment());
        LogicalExpression condition = logicalExpressionParser.parse(input.getCondition());
        return new QueryFilter(fieldIdentifierAttribute, condition);
    }
}
