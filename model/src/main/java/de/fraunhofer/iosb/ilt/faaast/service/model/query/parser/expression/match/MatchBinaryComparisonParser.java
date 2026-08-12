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

import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.AbstractBinaryComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.EqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.GreaterThanEqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.GreaterThanOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.LessThanEqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.LessThanOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.NotEqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.IdtaMatchExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.expression.AbstractBinaryComparisonParser;

import java.util.ArrayList;
import java.util.List;


public class MatchBinaryComparisonParser extends AbstractBinaryComparisonParser<IdtaMatchExpression, List<AbstractBinaryComparison>> {
    @Override
    public List<AbstractBinaryComparison> parse(IdtaMatchExpression expression) {
        List<AbstractBinaryComparison> comparisons = new ArrayList<>();
        if (notNullNorEmpty(expression.get$eq())) {
            comparisons.add(buildComparison(EqualsOperation::new, expression.get$eq()));
        }
        if (notNullNorEmpty(expression.get$ne())) {
            comparisons.add(buildComparison(NotEqualsOperation::new, expression.get$ne()));
        }
        if (notNullNorEmpty(expression.get$gt())) {
            comparisons.add(buildComparison(GreaterThanOperation::new, expression.get$gt()));
        }
        if (notNullNorEmpty(expression.get$ge())) {
            comparisons.add(buildComparison(GreaterThanEqualsOperation::new, expression.get$ge()));
        }
        if (notNullNorEmpty(expression.get$lt())) {
            comparisons.add(buildComparison(LessThanOperation::new, expression.get$lt()));
        }
        if (notNullNorEmpty(expression.get$le())) {
            comparisons.add(buildComparison(LessThanEqualsOperation::new, expression.get$le()));
        }
        return comparisons;
    }
}
