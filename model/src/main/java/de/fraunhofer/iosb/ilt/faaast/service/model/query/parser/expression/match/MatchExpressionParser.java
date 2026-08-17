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

import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.match.MatchExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.match.QueryMatchElement;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.AbstractParser;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.BooleanValue;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class MatchExpressionParser extends AbstractParser<de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression, MatchExpression> {
    private final MatchBinaryComparisonParser binaryComparisonParser = new MatchBinaryComparisonParser();
    private final MatchStringComparisonParser stringComparisonParser = new MatchStringComparisonParser();

    @Override
    public MatchExpression parse(de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression expression) {
        if (!assertExpression(expression)) {
            throw new IllegalArgumentException("expression malformed: %s".formatted(expression));
        }

        List<QueryMatchElement> matchElements = new ArrayList<>();

        if (notNullNorEmpty(expression.get$match())) {
            List<MatchExpression> elements = expression.get$match().stream()
                    .map(this::parse)
                    .toList();
            matchElements.addAll(elements);
        }

        matchElements.addAll(binaryComparisonParser.parse(expression));
        matchElements.addAll(stringComparisonParser.parse(expression));

        if (expression.get$boolean() != null) {
            matchElements.add(new BooleanValue(expression.get$boolean()));
        }

        return new MatchExpression(matchElements);
    }


    private boolean assertExpression(de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression expression) {
        if (expression == null) {
            return false;
        }

        short nonNullNonEmptyElements = 0;
        List<Function<de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression, List<?>>> listAccessors = List.of(
                de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression::get$match,
                de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression::get$eq,
                de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression::get$ne,
                de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression::get$gt,
                de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression::get$ge,
                de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression::get$lt,
                de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression::get$le,
                de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression::get$contains,
                de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression::get$startsWith,
                de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression::get$endsWith,
                de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression::get$regex);
        List<Function<de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression, Object>> objectAccessors = List.of(
                de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression::get$boolean);

        for (Function<de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression, List<?>> listAccessor: listAccessors) {
            if (listAccessor.apply(expression) != null && !listAccessor.apply(expression).isEmpty()) {
                nonNullNonEmptyElements++;
            }
        }

        for (Function<de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression, Object> objectAccessor: objectAccessors) {
            if (objectAccessor.apply(expression) != null) {
                nonNullNonEmptyElements++;
            }
        }

        return nonNullNonEmptyElements >= 1;
    }
}
