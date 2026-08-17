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
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.NAryExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.BooleanValue;

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract class containing common logic for all logical comparisons within a query.
 */
public abstract class AbstractLogicalOperation extends NAryExpression {

    protected AbstractLogicalOperation(List<LogicalExpression> operands) {
        super(operands);
    }


    @Override
    public LogicalExpression evaluatePartially(EvaluationContext evaluationContext) {
        List<LogicalExpression> evaluated = new ArrayList<>();
        boolean changed = false;

        for (LogicalExpression operand: getOperands()) {
            LogicalExpression folded = operand.evaluatePartially(evaluationContext);
            if (folded != operand) {
                changed = true;
            }

            if (folded.isBoolean()) {
                // E.g., AND (..., FALSE, ...) = FALSE
                if (Boolean.valueOf(neutralElement()).equals(folded.asBoolean())) {
                    return folded;
                }
                changed = true;
                continue;
            }

            evaluated.add(folded);
        }

        return switch (evaluated.size()) {
            case 0 -> new BooleanValue(neutralElement());
            case 1 -> evaluated.get(0);
            default -> changed ? withOperands(List.copyOf(evaluated)) : this;
        };
    }


    /**
     * Return the neutral element of this operation. For example, the neutral element for AND is true -> 'x AND y == x AND y
     * AND true'.
     * 
     * @return The neutral element of this operation.
     */
    protected abstract boolean neutralElement();


    /**
     * Return a new instance of this operation with the given operands.
     *
     * @param newOperands The new operands.
     * @return A new instance of this operation.
     */
    protected abstract AbstractLogicalOperation withOperands(List<LogicalExpression> newOperands);
}
