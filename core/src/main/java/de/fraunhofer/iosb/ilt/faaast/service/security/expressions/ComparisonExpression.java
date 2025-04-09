package de.fraunhofer.iosb.ilt.faaast.service.security.expressions;

import de.fraunhofer.iosb.ilt.faaast.service.security.operands.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.security.operands.StringOperand;
import de.fraunhofer.iosb.ilt.faaast.service.security.utils.EvaluationContext;

public class ComparisonExpression implements LogicalExpression {
    private Operand leftOperand;
    private ComparisonOperator operator;
    private Operand rightOperand;

    public ComparisonExpression(StringOperand stringOperand, ComparisonOperator comparisonOperator, StringOperand admin) {
    }

    @Override
    public boolean evaluate(EvaluationContext context) {
        Object leftValue = leftOperand.getValue(context);
        Object rightValue = rightOperand.getValue(context);
        // TODO Implement comparison based on operator
        return true;
    }

    // Constructors, getters, and setters
}

