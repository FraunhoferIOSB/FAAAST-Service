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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.BooleanValue;


/**
 * The AAS Query Language {@code $not} operator, negating a single logical expression. If the operand evaluates to true
 * the result is false, and vice versa.
 */
public record NotOperation(LogicalExpression operand) implements LogicalExpression {

    @Override
    public LogicalExpression evaluatePartially(EvaluationContext evaluationContext) {
        LogicalExpression evaluated = operand.evaluatePartially(evaluationContext);

        if (evaluated.isBoolean()) {
            return new BooleanValue(Boolean.FALSE.equals(evaluated.asBoolean()));
        }
        return withOperand(evaluated);
    }


    private NotOperation withOperand(LogicalExpression evaluated) {
        return new NotOperation(evaluated);
    }
}
