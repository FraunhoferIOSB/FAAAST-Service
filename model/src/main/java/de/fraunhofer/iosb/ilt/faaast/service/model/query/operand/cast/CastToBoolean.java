package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.cast;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.BooleanLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.Literal;


public class CastToBoolean extends Cast<BooleanLiteral> {

    protected CastToBoolean(Operand operand) {
        super(operand);
    }


    @Override
    protected Cast<BooleanLiteral> withOperand(Operand evaluated) {
        return new CastToBoolean(evaluated);
    }


    @Override
    protected BooleanLiteral cast(Literal input) {
        return input.asBoolean();
    }
}
