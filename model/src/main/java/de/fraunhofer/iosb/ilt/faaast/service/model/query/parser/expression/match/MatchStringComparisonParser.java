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

import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.string.AbstractStringComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.string.ContainsComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.string.EndsWithComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.string.RegexComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.string.StartsWithComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.IdtaMatchExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.expression.AbstractStringComparisonParser;

import java.util.ArrayList;
import java.util.List;


public class MatchStringComparisonParser extends AbstractStringComparisonParser<IdtaMatchExpression, List<AbstractStringComparison>> {
    public MatchStringComparisonParser() {
        super();
    }


    @Override
    public List<AbstractStringComparison> parse(IdtaMatchExpression expression) {

        List<AbstractStringComparison> comparisons = new ArrayList<>();

        if (notNullNorEmpty(expression.get$contains())) {
            comparisons.add(buildComparison(ContainsComparison::new, expression.get$contains()));
        }
        if (notNullNorEmpty(expression.get$startsWith())) {
            comparisons.add(buildComparison(StartsWithComparison::new, expression.get$startsWith()));
        }
        if (notNullNorEmpty(expression.get$endsWith())) {
            comparisons.add(buildComparison(EndsWithComparison::new, expression.get$endsWith()));
        }
        if (notNullNorEmpty(expression.get$regex())) {
            comparisons.add(buildComparison(RegexComparison::new, expression.get$regex()));
        }
        return comparisons;
    }
}
