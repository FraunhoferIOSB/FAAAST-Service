package de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.BooleanLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.Literal;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.NumberLiteral;

import java.util.function.BiFunction;


public class EqualsOperation extends AbstractBinaryComparison {

    @Override
    protected BiFunction<NumberLiteral, NumberLiteral, BooleanLiteral> numberOperation() {
        return (x, y) -> new BooleanLiteral(x.getValue().doubleValue() == y.getValue().doubleValue());
    }


    @Override
    protected <T extends Literal<?>> BiFunction<T, T, BooleanLiteral> defaultOperation() {
        return (x, y) -> new BooleanLiteral(x.getValue().equals(y.getValue()));
    }
}
