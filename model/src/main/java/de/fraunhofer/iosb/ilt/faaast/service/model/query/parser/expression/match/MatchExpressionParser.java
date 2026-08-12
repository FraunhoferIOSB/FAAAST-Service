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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.expression.match;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.match.MatchElement;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.match.MatchExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.IdtaMatchExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.BooleanLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.AbstractParser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class MatchExpressionParser extends AbstractParser<IdtaMatchExpression, MatchExpression> {
    private final MatchBinaryComparisonParser binaryComparisonParser = new MatchBinaryComparisonParser();
    private final MatchStringComparisonParser stringComparisonParser = new MatchStringComparisonParser();

    @Override
    public MatchExpression parse(IdtaMatchExpression expression) {
        if (!assertExpression(expression)) {
            throw new IllegalArgumentException("expression malformed: %s".formatted(expression));
        }

        List<MatchElement> matchElements = new ArrayList<>();

        if (notNullNorEmpty(expression.get$match())) {
            List<MatchExpression> elements = expression.get$match().stream()
                    .map(this::parse)
                    .toList();
            matchElements.addAll(elements);
        }

        matchElements.addAll(binaryComparisonParser.parse(expression));
        matchElements.addAll(stringComparisonParser.parse(expression));

        if (expression.get$boolean() != null) {
            matchElements.add(new BooleanLiteral(expression.get$boolean()));
        }

        return new MatchExpression(matchElements);
    }


    private boolean assertExpression(IdtaMatchExpression expression) {
        if (expression == null) {
            return false;
        }

        short nonNullNonEmptyElements = 0;
        List<Function<IdtaMatchExpression, List<?>>> listAccessors = List.of(
                IdtaMatchExpression::get$match,
                IdtaMatchExpression::get$eq,
                IdtaMatchExpression::get$ne,
                IdtaMatchExpression::get$gt,
                IdtaMatchExpression::get$ge,
                IdtaMatchExpression::get$lt,
                IdtaMatchExpression::get$le,
                IdtaMatchExpression::get$contains,
                IdtaMatchExpression::get$startsWith,
                IdtaMatchExpression::get$endsWith,
                IdtaMatchExpression::get$regex);
        List<Function<IdtaMatchExpression, Object>> objectAccessors = List.of(
                IdtaMatchExpression::get$boolean);

        for (Function<IdtaMatchExpression, List<?>> listAccessor: listAccessors) {
            if (listAccessor.apply(expression) != null && !listAccessor.apply(expression).isEmpty()) {
                nonNullNonEmptyElements++;
            }
        }

        for (Function<IdtaMatchExpression, Object> objectAccessor: objectAccessors) {
            if (objectAccessor.apply(expression) != null) {
                nonNullNonEmptyElements++;
            }
        }

        return nonNullNonEmptyElements >= 1;
    }
}
