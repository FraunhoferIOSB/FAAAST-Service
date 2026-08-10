package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.cast;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.Literal;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.StringLiteral;


public class CastToString extends Cast<StringLiteral> {

    protected CastToString(Operand operand) {
        super(operand);
    }


    @Override
    protected Cast<StringLiteral> withOperand(Operand evaluated) {
        return new CastToString(evaluated);
    }


    @Override
    protected StringLiteral cast(Literal input) {
        return input.asString();
    }
}
