package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.temporal;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.DateTimeLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.NumberLiteral;

import java.util.function.Function;


public class DayOfMonthOperation extends TemporalOperation {

    protected DayOfMonthOperation(Operand operand) {
        super(operand);
    }


    @Override
    protected Function<DateTimeLiteral, NumberLiteral> operation() {
        return dtv -> new NumberLiteral(dtv.value().getDayOfMonth());
    }


    @Override
    protected DayOfMonthOperation withOperand(Operand operand) {
        return new DayOfMonthOperation(operand);
    }
}
