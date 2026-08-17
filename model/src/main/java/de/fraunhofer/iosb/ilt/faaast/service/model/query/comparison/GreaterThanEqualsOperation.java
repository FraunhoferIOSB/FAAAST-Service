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

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DateTimeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DoubleValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.HexBinaryValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.StringValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TimeValue;

import java.util.Arrays;
import java.util.function.BiFunction;


/**
 * The AAS Query Language {@code $ge} operator, checking whether the left operand is greater than or equal to the right
 * operand.
 *
 * <p>This is an overloaded operator, i.e. it can deal with several input types (e.g. string and numeric values).
 */
public class GreaterThanEqualsOperation extends AbstractBinaryComparison {

    public GreaterThanEqualsOperation(Operand left, Operand right) {
        super(left, right);
    }


    @Override
    protected BiFunction<DoubleValue, DoubleValue, Boolean> doubleOperation() {
        return (x, y) -> x.getValue() >= y.getValue();
    }


    @Override
    protected BiFunction<StringValue, StringValue, Boolean> stringOperation() {
        return (x, y) -> x.getValue().compareTo(y.getValue()) >= 0;
    }


    @Override
    protected BiFunction<HexBinaryValue, HexBinaryValue, Boolean> hexOperation() {
        return (x, y) -> Arrays.compare(x.getValue(), y.getValue()) >= 0;
    }


    @Override
    protected BiFunction<DateTimeValue, DateTimeValue, Boolean> dateTimeOperation() {
        return (x, y) -> !x.getValue().isBefore(y.getValue());
    }


    @Override
    protected BiFunction<TimeValue, TimeValue, Boolean> timeOperation() {
        return (x, y) -> !x.getValue().isBefore(y.getValue());
    }


    @Override
    protected AbstractBinaryComparison withOperands(Operand left, Operand right) {
        return new GreaterThanEqualsOperation(left, right);
    }
}
