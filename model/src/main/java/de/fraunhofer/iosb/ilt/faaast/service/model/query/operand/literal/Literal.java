package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;


public sealed interface Literal extends Operand permits BooleanLiteral, DateTimeLiteral, HexLiteral, NumberLiteral, StringLiteral, TimeLiteral {

    @Override
    default boolean isValue() {
        return true;
    }


    @Override
    default Literal asValue() {
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
