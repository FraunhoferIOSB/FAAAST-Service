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


/**
 * Abstract class containing common logic for all binary comparisons within a query.
 */
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


    /**
     * Computes the comparison result for two fully-evaluated typed value operands.
     *
     * @param leftValue the left operand as a typed value
     * @param rightValue the right operand as a typed value
     * @return the comparison result
     * @throws IllegalArgumentException if the operands are not valid for this comparison
     */
    public boolean compute(TypedValue<?> leftValue, TypedValue<?> rightValue) {
        validate(leftValue, rightValue);
        return switch (leftValue.getDataType()) {
            case STRING -> stringOperation().apply((StringValue) leftValue, (StringValue) rightValue);
            case DOUBLE -> doubleOperation().apply((DoubleValue) leftValue, (DoubleValue) rightValue);
            case INT -> intOperation().apply((IntValue) leftValue, (IntValue) rightValue);
            case HEX_BINARY -> hexOperation().apply((HexBinaryValue) leftValue, (HexBinaryValue) rightValue);
            case BOOLEAN -> booleanOperation().apply((BooleanValue) leftValue, (BooleanValue) rightValue);
            case DATE_TIME -> dateTimeOperation().apply((DateTimeValue) leftValue, (DateTimeValue) rightValue);
            case TIME -> timeOperation().apply((TimeValue) leftValue, (TimeValue) rightValue);
            default -> throw new IllegalArgumentException(String.format("Cannot compare %s", leftValue.getDataType()));
        };
    }


    /**
     * Validates both arguments of this operation.
     *
     * @param left The left operand.
     * @param right The right operand.
     * @throws IllegalArgumentException If either or both arguments are not valid for this operation
     */
    protected void validate(TypedValue<?> left, TypedValue<?> right) throws IllegalArgumentException {
        if (!left.getDataType().equals(right.getDataType())) {
            throw new IllegalArgumentException(
                    String.format("operands to %s are not of same type: [%s, %s]", this.getClass().getSimpleName(), left.getDataType(), right.getDataType()));
        }
    }


    /**
     * Return a new instance of this comparison with the given operands.
     *
     * @param left The left operand.
     * @param right The right operand.
     * @return A new instance of this comparison.
     */
    public abstract AbstractBinaryComparison withOperands(Operand left, Operand right);


    /**
     * Get a comparator for string operands.
     *
     * @return A comparison function taking two string values, producing a boolean
     */
    protected BiFunction<StringValue, StringValue, Boolean> stringOperation() {
        return defaultOperation();
    }


    /**
     * Get a comparator for double operands.
     *
     * @return A comparison function taking two double values, producing a boolean
     */
    protected BiFunction<DoubleValue, DoubleValue, Boolean> doubleOperation() {
        return defaultOperation();
    }


    /**
     * Get a comparator for int operands.
     *
     * @return A comparison function taking two int values, producing a boolean
     */
    protected BiFunction<IntValue, IntValue, Boolean> intOperation() {
        return defaultOperation();
    }


    /**
     * Get a comparator for hex operands.
     *
     * @return A comparison function taking two hex values, producing a boolean
     */
    protected BiFunction<HexBinaryValue, HexBinaryValue, Boolean> hexOperation() {
        return defaultOperation();
    }


    /**
     * Get a comparator for date time operands.
     *
     * @return A comparison function taking two date time values, producing a boolean
     */
    protected BiFunction<DateTimeValue, DateTimeValue, Boolean> dateTimeOperation() {
        return defaultOperation();
    }


    /**
     * Get a comparator for time operands.
     *
     * @return A comparison function taking two time values, producing a boolean
     */
    protected BiFunction<TimeValue, TimeValue, Boolean> timeOperation() {
        return defaultOperation();
    }


    /**
     * Get a comparator for boolean operands.
     *
     * @return A comparison function taking two boolean values, producing a boolean
     */
    protected BiFunction<BooleanValue, BooleanValue, Boolean> booleanOperation() {
        return defaultOperation();
    }


    /**
     * Returns the default operation for this comparison function. Defaults to throwing an unsupported operation exception.
     *
     * @param <T> Type of the input values.
     * @return operation that applies to all operand types not explicitly defined using *Operation().
     */
    protected <T extends TypedValue<?>> BiFunction<T, T, Boolean> defaultOperation() {
        return (x, y) -> {
            throw new UnsupportedOperationException(String.format("%s not possible for input %s", this.getClass().getSimpleName(), x.getClass().getSimpleName()));
        };
    }
}
