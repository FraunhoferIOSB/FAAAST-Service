package de.fraunhofer.iosb.ilt.faaast.service.security.expressions;

import de.fraunhofer.iosb.ilt.faaast.service.security.utils.EvaluationContext;

public interface LogicalExpression {
    boolean evaluate(EvaluationContext context);
}
