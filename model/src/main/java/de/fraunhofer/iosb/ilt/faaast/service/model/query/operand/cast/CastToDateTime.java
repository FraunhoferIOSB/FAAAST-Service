package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.cast;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.DateTimeLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.Literal;


public class CastToDateTime extends Cast<DateTimeLiteral> {

    protected CastToDateTime(Operand operand) {
        super(operand);
    }


    @Override
    protected Cast<DateTimeLiteral> withOperand(Operand evaluated) {
        return new CastToDateTime(evaluated);
    }


    @Override
    protected DateTimeLiteral cast(Literal input) {
        return input.asDateTime();
    }
}
