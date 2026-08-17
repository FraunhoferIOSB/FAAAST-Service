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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.expression;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.AccessRuleEntity;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.BooleanValue;

import javax.annotation.Nullable;


public interface LogicalExpression extends AccessRuleEntity<LogicalExpression> {

    /**
     * Evaluate this logical expression partially given the context. A bottom up approach of folding this expression will
     * take place. May throw unchecked exceptions for invalid
     * input arguments to operations. Each node of the expression may return an expression, a function or a literal
     * (TypedValue), depending on the evaluation of its arguments.
     *
     * The evaluation may also return a BooleanValue, meaning it is evaluated completely.
     *
     * @param evaluationContext The context used to evaluate the expression.
     * @return A (partially) evaluated expression.
     */
    LogicalExpression evaluatePartially(EvaluationContext evaluationContext);


    /**
     * Helper function to get this expression.
     *
     * @return This expression.
     */
    @Override
    default LogicalExpression getInstance() {
        return this;
    }


    /**
     * Returns whether this expression is a boolean represented by a BooleanValue.
     *
     * @return Whether this expression is a boolean represented by a BooleanValue.
     */
    default boolean isBoolean() {
        return this instanceof TypedValue<?> && ((TypedValue<?>) this).getDataType() == Datatype.BOOLEAN;
    }


    /**
     * Returns this expression as a boolean, if it is represented by a BooleanValue.
     *
     * @return the boolean represented by this expression if it is a BooleanValue, else null.
     */
    default @Nullable Boolean asBoolean() {
        return this.isBoolean() ? ((BooleanValue) this).getValue() : null;
    }


    /**
     * Returns whether this expression is an operand.
     *
     * @return Whether this expression is an operand.
     */
    default boolean isOperand() {
        return false;
    }


    /**
     * Returns this expression as an operand.
     *
     * @return the operand represented by this expression.
     */
    default Operand asOperand() {
        throw new UnsupportedOperationException(String.format("%s cannot be transformed to operand", this.getClass().getSimpleName()));

    }

}
