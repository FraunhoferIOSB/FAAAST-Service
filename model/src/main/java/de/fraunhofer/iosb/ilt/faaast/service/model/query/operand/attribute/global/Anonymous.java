package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.BooleanLiteral;


public class Anonymous extends GlobalAttribute {
    @Override
    public BooleanLiteral evaluatePartially(EvaluationContext evaluationContext) {
        return new BooleanLiteral(evaluationContext.isAnonymous());
    }
}
