package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.temporal;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.DateTimeLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.NumberLiteral;

import java.util.function.Function;


public abstract class TemporalOperation implements Operand {

    private final Operand operand;


    protected TemporalOperation(Operand operand) {this.operand = operand;}


    @Override
    public Operand evaluatePartially(EvaluationContext evaluationContext) {
        Operand evaluated = operand.evaluatePartially(evaluationContext);

        if (!evaluated.isLiteral()) {
            return this;
        }

        return operation().apply(operand.asLiteral().asDateTime());
    }


    protected abstract Function<DateTimeLiteral, NumberLiteral> operation();


    protected abstract TemporalOperation withOperand(Operand operand);
}
