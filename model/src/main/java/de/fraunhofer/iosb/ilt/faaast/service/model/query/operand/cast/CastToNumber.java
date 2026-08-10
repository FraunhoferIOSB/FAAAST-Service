package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.cast;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.Literal;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.NumberLiteral;


public class CastToNumber extends Cast<NumberLiteral> {

    protected CastToNumber(Operand operand) {
        super(operand);
    }


    @Override
    protected Cast<NumberLiteral> withOperand(Operand evaluated) {
        return new CastToNumber(evaluated);
    }


    @Override
    protected NumberLiteral cast(Literal input) {
        return input.asNumber();
    }
}
