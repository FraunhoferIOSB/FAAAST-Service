package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.temporal;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.DateTimeLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.NumberLiteral;

import java.util.function.Function;


public class MonthOperation extends TemporalOperation {

    protected MonthOperation(Operand operand) {
        super(operand);
    }


    @Override
    protected Function<DateTimeLiteral, NumberLiteral> operation() {
        return dtv -> new NumberLiteral(dtv.value().getMonthValue());
    }


    @Override
    protected MonthOperation withOperand(Operand operand) {
        return new MonthOperation(operand);
    }

}
