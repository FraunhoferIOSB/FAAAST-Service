package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.cast;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.Literal;


public abstract class Cast<O extends Literal> implements Operand {

    private final Operand operand;


    protected Cast(Operand operand) {this.operand = operand;}


    @Override
    public Operand evaluatePartially(EvaluationContext evaluationContext) {
        Operand evaluated = operand.evaluatePartially(evaluationContext);
        if (evaluated.isLiteral()) {
            try {
                return cast(evaluated.asLiteral());
            }
            catch (Exception e) {
                // TODO error handling?
                throw new RuntimeException(e);
            }
        }
        return evaluated == operand ? this : withOperand(evaluated);
    }


    protected abstract Cast<O> withOperand(Operand evaluated);


    protected abstract O cast(Literal input) throws Exception;
}
