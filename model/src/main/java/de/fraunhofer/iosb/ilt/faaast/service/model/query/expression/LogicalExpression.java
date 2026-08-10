package de.fraunhofer.iosb.ilt.faaast.service.model.query.expression;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.Literal;


public interface LogicalExpression {

    LogicalExpression evaluatePartially(EvaluationContext evaluationContext);


    default boolean isLiteral() {
        return false;
    }


    default Literal asLiteral() {
        throw new UnsupportedOperationException(String.format("%s cannot be transformed to literal", this.getClass().getSimpleName()));
    }

}
