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
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.AccessRuleEntity;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.BooleanValue;

import javax.annotation.Nullable;


public interface LogicalExpression extends AccessRuleEntity<LogicalExpression> {

    LogicalExpression evaluatePartially(EvaluationContext evaluationContext);


    @Override
    default LogicalExpression getInstance() {
        return this;
    }


    default boolean isBoolean() {
        return this.isTypedValue() && ((TypedValue<?>) this).getDataType() == Datatype.BOOLEAN;
    }


    default @Nullable Boolean asBoolean() {
        return this.isBoolean() ? ((BooleanValue) this).getValue() : null;
    }


    default boolean isLogical() {
        return false;
    }


    default boolean isMatch() {
        return false;
    }


    default boolean isOperand() {
        return false;
    }


    default boolean isTypedValue() {
        return false;
    }


    default TypedValue<?> asTypedValue() {
        throw new UnsupportedOperationException(String.format("%s cannot be transformed to typed value", this.getClass().getSimpleName()));

    }

}
