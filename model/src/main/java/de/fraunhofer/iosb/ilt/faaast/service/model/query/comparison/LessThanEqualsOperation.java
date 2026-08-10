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
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.BooleanLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.DateTimeLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.HexLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.NumberLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.StringLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.TimeLiteral;

import java.util.function.BiFunction;


public class LessThanEqualsOperation extends AbstractBinaryComparison {

    public LessThanEqualsOperation(Operand left, Operand right) {
        super(left, right);
    }


    @Override
    protected BiFunction<NumberLiteral, NumberLiteral, BooleanLiteral> numberOperation() {
        return (x, y) -> new BooleanLiteral(x.value().doubleValue() <= y.value().doubleValue());
    }


    @Override
    protected BiFunction<StringLiteral, StringLiteral, BooleanLiteral> stringOperation() {
        return (x, y) -> new BooleanLiteral(x.value().compareTo(y.value()) <= 0);
    }


    @Override
    protected BiFunction<HexLiteral, HexLiteral, BooleanLiteral> hexOperation() {
        return (x, y) -> new BooleanLiteral(x.value() <= y.value());
    }


    @Override
    protected BiFunction<DateTimeLiteral, DateTimeLiteral, BooleanLiteral> dateTimeOperation() {
        return (x, y) -> new BooleanLiteral(!x.value().isAfter(y.value()));
    }


    @Override
    protected BiFunction<TimeLiteral, TimeLiteral, BooleanLiteral> timeOperation() {
        return (x, y) -> new BooleanLiteral(!x.value().isAfter(y.value()));
    }


    @Override
    protected AbstractBinaryComparison withOperands(Operand left, Operand right) {
        return new LessThanEqualsOperation(left, right);
    }
}
