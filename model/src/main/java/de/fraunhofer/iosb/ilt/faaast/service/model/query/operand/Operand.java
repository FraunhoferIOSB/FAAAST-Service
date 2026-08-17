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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;


public interface Operand extends LogicalExpression {
    @Override
    default boolean isOperand() {
        return true;
    }


    /**
     * Returns whether this operand is really a literal.
     * 
     * @return whether this operand is really a literal.
     */
    default boolean isTypedValue() {
        return false;
    }


    /**
     * Returns this expression as a typed value.
     * 
     * @return this expression as a typed value.
     */
    default TypedValue<?> asTypedValue() {
        throw new UnsupportedOperationException(String.format("%s cannot be transformed to typed value", this.getClass().getSimpleName()));
    }


    @Override
    Operand evaluatePartially(EvaluationContext evaluationContext);
}
