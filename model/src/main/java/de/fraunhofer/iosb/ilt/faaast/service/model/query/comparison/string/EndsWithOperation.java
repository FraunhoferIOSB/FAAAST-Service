package de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.string;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.BooleanLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.StringLiteral;

import java.util.function.BiFunction;


public class EndsWithOperation extends AbstractStringOperation {

    @Override
    protected BiFunction<StringLiteral, StringLiteral, BooleanLiteral> stringOperation() {
        return (x, y) -> new BooleanLiteral(y.getValue().endsWith(x.getValue()));
    }

}
