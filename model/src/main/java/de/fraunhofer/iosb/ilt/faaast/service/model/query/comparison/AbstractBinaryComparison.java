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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.match.QueryMatchElement;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.BooleanValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DateTimeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DoubleValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.HexBinaryValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.IntValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.StringValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TimeValue;

import java.util.function.BiFunction;


public abstract class AbstractBinaryComparison implements LogicalExpression, QueryMatchElement {

    private final Operand left;
    private final Operand right;

    protected AbstractBinaryComparison(Operand left, Operand right) {
        this.left = left;
        this.right = right;
    }


    public Operand getLeft() {
        return left;
    }


    public Operand getRight() {
        return right;
    }


    @Override
    public LogicalExpression evaluatePartially(EvaluationContext evaluationContext) {
        Operand leftEvaluated = left.evaluatePartially(evaluationContext);
        Operand rightEvaluated = right.evaluatePartially(evaluationContext);

        if (leftEvaluated.isTypedValue() && rightEvaluated.isTypedValue()) {
            TypedValue<?> leftValue = leftEvaluated.asTypedValue();
            TypedValue<?> rightValue = rightEvaluated.asTypedValue();
            validate(leftValue, rightValue);

            return new BooleanValue(
                    switch (leftValue.getDataType()) {
                        case STRING -> stringOperation().apply((StringValue) leftValue, (StringValue) rightValue);
                        case DOUBLE -> doubleOperation().apply((DoubleValue) leftValue, (DoubleValue) rightValue);
                        case INT -> intOperation().apply((IntValue) leftEvaluated, (IntValue) rightValue);
                        case HEX_BINARY -> hexOperation().apply((HexBinaryValue) leftValue, (HexBinaryValue) rightValue);
                        case BOOLEAN -> booleanOperation().apply((BooleanValue) leftEvaluated, (BooleanValue) rightEvaluated);
                        case DATE_TIME -> dateTimeOperation().apply((DateTimeValue) leftValue, (DateTimeValue) rightValue);
                        case TIME -> timeOperation().apply((TimeValue) leftValue, (TimeValue) rightValue);
                        default -> throw new IllegalArgumentException(String.format("Cannot compare %s", leftValue.getDataType()));
                    });
        }

        if (leftEvaluated != left || rightEvaluated != right) {
            return withOperands(leftEvaluated, rightEvaluated);
        }
        return this;
    }


    protected void validate(TypedValue<?> left, TypedValue<?> right) throws IllegalArgumentException {
        if (!left.getDataType().equals(right.getDataType())) {
            throw new IllegalArgumentException(String.format("operands to %s are not of same type", this.getClass().getSimpleName()));
        }
    }


    protected abstract AbstractBinaryComparison withOperands(Operand left, Operand right);


    protected BiFunction<StringValue, StringValue, Boolean> stringOperation() {
        return defaultOperation();
    }


    protected BiFunction<DoubleValue, DoubleValue, Boolean> doubleOperation() {
        return defaultOperation();
    }


    protected BiFunction<IntValue, IntValue, Boolean> intOperation() {
        return defaultOperation();
    }


    protected BiFunction<HexBinaryValue, HexBinaryValue, Boolean> hexOperation() {
        return defaultOperation();
    }


    protected BiFunction<DateTimeValue, DateTimeValue, Boolean> dateTimeOperation() {
        return defaultOperation();
    }


    protected BiFunction<TimeValue, TimeValue, Boolean> timeOperation() {
        return defaultOperation();
    }


    protected BiFunction<BooleanValue, BooleanValue, Boolean> booleanOperation() {
        return defaultOperation();
    }


    protected <T extends TypedValue<?>> BiFunction<T, T, Boolean> defaultOperation() {
        return (x, y) -> {
            throw new IllegalArgumentException(String.format("%s not possible for input %s", this.getClass().getSimpleName(), x.getClass().getSimpleName()));
        };
    }

}
