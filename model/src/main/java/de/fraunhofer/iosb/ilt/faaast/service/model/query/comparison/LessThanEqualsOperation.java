package de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.BooleanLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.DateTimeLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.HexLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.NumberLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.StringLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.TimeLiteral;

import java.util.HexFormat;
import java.util.function.BiFunction;


public class LessThanEqualsOperation extends AbstractBinaryComparison {

    @Override
    protected BiFunction<NumberLiteral, NumberLiteral, BooleanLiteral> numberOperation() {
        return (x, y) -> new BooleanLiteral(x.getValue().doubleValue() <= y.getValue().doubleValue());
    }


    @Override
    protected BiFunction<StringLiteral, StringLiteral, BooleanLiteral> stringOperation() {
        return (x, y) -> new BooleanLiteral(x.getValue().compareTo(y.getValue()) <= 0);
    }


    @Override
    protected BiFunction<HexLiteral, HexLiteral, BooleanLiteral> hexOperation() {
        return (x, y) -> new BooleanLiteral(HexFormat.fromHexDigits(x.getValue()) <= HexFormat.fromHexDigits(y.getValue()));
    }


    @Override
    protected BiFunction<DateTimeLiteral, DateTimeLiteral, BooleanLiteral> dateTimeOperation() {
        return (x, y) -> new BooleanLiteral(!x.getValue().isAfter(y.getValue()));
    }


    @Override
    protected BiFunction<TimeLiteral, TimeLiteral, BooleanLiteral> timeOperation() {
        return (x, y) -> new BooleanLiteral(!x.getValue().isAfter(y.getValue()));
    }
}
