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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.expression.logical;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.AbstractBinaryComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.string.AbstractStringComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical.AndOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical.NotOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical.OrOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.IdtaLogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.IdtaMatchExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.BooleanLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.AbstractParser;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.expression.match.MatchExpressionParser;

import java.util.List;
import java.util.function.Function;


public class LogicalExpressionParser extends AbstractParser<IdtaLogicalExpression, LogicalExpression> {

    private final MatchExpressionParser matchExpressionParser = new MatchExpressionParser();
    private final LogicalBinaryComparisonParser binaryComparisonParser = new LogicalBinaryComparisonParser();
    private final LogicalStringComparisonParser stringComparisonParser = new LogicalStringComparisonParser();

    @Override
    public LogicalExpression parse(IdtaLogicalExpression expression) {
        if (!assertExpression(expression)) {
            throw new IllegalArgumentException("expression malformed: %s".formatted(expression));
        }

        if (notNullNorEmpty(expression.get$and())) {
            List<LogicalExpression> operands = expression.get$and().stream().map(this::parse).toList();
            return new AndOperation(operands);
        }
        if (notNullNorEmpty(expression.get$or())) {
            List<LogicalExpression> operands = expression.get$or().stream().map(this::parse).toList();
            return new OrOperation(operands);
        }
        if (expression.get$not() != null) {
            return new NotOperation(parse(expression.get$not()));
        }
        if (notNullNorEmpty(expression.get$match())) {
            IdtaMatchExpression idtaMatchExpression = new IdtaMatchExpression();
            idtaMatchExpression.set$match(expression.get$match());
            return matchExpressionParser.parse(idtaMatchExpression);
        }

        AbstractBinaryComparison binaryComparison = binaryComparisonParser.parse(expression);
        if (binaryComparison != null) {
            return binaryComparison;
        }
        AbstractStringComparison stringComparison = stringComparisonParser.parse(expression);
        if (stringComparison != null) {
            return stringComparison;
        }

        if (expression.get$boolean() != null) {
            return new BooleanLiteral(expression.get$boolean());
        }
        throw new IllegalArgumentException(String.format("Unsupported logical expression: %s", expression));
    }


    private boolean assertExpression(IdtaLogicalExpression expression) {
        if (expression == null) {
            return false;
        }

        short nonNullNonEmptyElements = 0;
        List<Function<IdtaLogicalExpression, List<?>>> listAccessors = List.of(
                IdtaLogicalExpression::get$and,
                IdtaLogicalExpression::get$match, IdtaLogicalExpression::get$or,
                IdtaLogicalExpression::get$eq,
                IdtaLogicalExpression::get$ne,
                IdtaLogicalExpression::get$gt,
                IdtaLogicalExpression::get$ge,
                IdtaLogicalExpression::get$lt,
                IdtaLogicalExpression::get$le,
                IdtaLogicalExpression::get$contains,
                IdtaLogicalExpression::get$startsWith,
                IdtaLogicalExpression::get$endsWith,
                IdtaLogicalExpression::get$regex);
        List<Function<IdtaLogicalExpression, Object>> objectAccessors = List.of(
                IdtaLogicalExpression::get$not,
                IdtaLogicalExpression::get$boolean);

        for (Function<IdtaLogicalExpression, List<?>> listAccessor: listAccessors) {
            if (listAccessor.apply(expression) != null && !listAccessor.apply(expression).isEmpty()) {
                nonNullNonEmptyElements++;
            }
        }

        for (Function<IdtaLogicalExpression, Object> objectAccessor: objectAccessors) {
            if (objectAccessor.apply(expression) != null) {
                nonNullNonEmptyElements++;
            }
        }

        return nonNullNonEmptyElements != 1;
    }

}
