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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.expression;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.AbstractParser;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.Parser;

import java.util.List;
import java.util.function.BiFunction;


/**
 * Base class for parsers of comparison expressions.
 *
 * @param <I> the input type
 * @param <O> the output type
 * @param <T> the value type to parse
 * @param <U> the resulting comparison type
 */
public abstract class AbstractComparisonParser<I, O, T, U> extends AbstractParser<I, O> {
    private final Parser<T, Operand> parser;

    protected AbstractComparisonParser(Parser<T, Operand> parser) {
        this.parser = parser;
    }


    /**
     * Builds a comparison from the given operands.
     *
     * @param factory the factory to create the comparison
     * @param values the list of values to compare
     * @return the built comparison
     */
    protected U buildComparison(BiFunction<Operand, Operand, U> factory, List<T> values) {
        if (values.size() != 2) {
            throw new IllegalArgumentException(String.format("Comparison requires exactly 2 operands but got %d", values.size()));
        }
        Operand left = parser.parse(values.get(0));
        Operand right = parser.parse(values.get(1));
        return factory.apply(left, right);
    }
}
