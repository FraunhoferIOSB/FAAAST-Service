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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;


public sealed interface Literal extends Operand permits BooleanLiteral, DateTimeLiteral, HexLiteral, NumberLiteral, StringLiteral, TimeLiteral {

    @Override
    default boolean isLiteral() {
        return true;
    }


    @Override
    default Literal asLiteral() {
        return this;
    }


    @Override
    default Literal evaluatePartially(EvaluationContext evaluationContext) {
        return this;
    }


    Object value();


    default boolean isBoolean() {
        return false;
    }


    default boolean isDateTime() {
        return false;
    }


    default boolean isHex() {
        return false;
    }


    default boolean isNumber() {
        return false;
    }


    default boolean isString() {
        return false;
    }


    default boolean isTime() {
        return false;
    }


    default BooleanLiteral asBoolean() {
        return BooleanLiteral.parse(this.toString());
    }


    default DateTimeLiteral asDateTime() {
        return DateTimeLiteral.parse(this.toString());
    }


    default HexLiteral asHex() {
        return HexLiteral.parse(this.toString());
    }


    default NumberLiteral asNumber() {
        return NumberLiteral.parse(this.toString());
    }


    default StringLiteral asString() {
        return StringLiteral.parse(this.toString());
    }


    default TimeLiteral asTime() {
        return TimeLiteral.parse(this.toString());
    }
}
