package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.temporal;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.DateTimeLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.NumberLiteral;

import java.util.function.Function;


public class YearOperation extends TemporalOperation {

    protected YearOperation(Operand operand) {
        super(operand);
    }


    @Override
    protected Function<DateTimeLiteral, NumberLiteral> operation() {
        return dtv -> new NumberLiteral(dtv.value().getYear());
    }


    @Override
    protected YearOperation withOperand(Operand operand) {
        return new YearOperation(operand);
    }
}
