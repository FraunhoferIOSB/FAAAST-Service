package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;


public interface Operand extends LogicalExpression {

    @Override
    Operand evaluatePartially(EvaluationContext evaluationContext);
}
