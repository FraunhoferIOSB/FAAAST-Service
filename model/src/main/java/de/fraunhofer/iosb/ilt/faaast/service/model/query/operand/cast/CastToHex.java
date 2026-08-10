package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.cast;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.HexLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.Literal;


public class CastToHex extends Cast<HexLiteral> {

    protected CastToHex(Operand operand) {
        super(operand);
    }


    @Override
    protected Cast<HexLiteral> withOperand(Operand evaluated) {
        return new CastToHex(evaluated);
    }


    @Override
    protected HexLiteral cast(Literal input) {
        return input.asHex();
    }
}
