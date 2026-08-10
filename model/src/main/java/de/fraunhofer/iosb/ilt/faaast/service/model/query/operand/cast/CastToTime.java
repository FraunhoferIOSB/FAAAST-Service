package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.cast;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.Literal;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.TimeLiteral;


public class CastToTime extends Cast<TimeLiteral> {

    protected CastToTime(Operand operand) {
        super(operand);
    }


    @Override
    protected Cast<TimeLiteral> withOperand(Operand evaluated) {
        return new CastToTime(evaluated);
    }


    @Override
    protected TimeLiteral cast(Literal input) {
        return input.asTime();
    }
}
