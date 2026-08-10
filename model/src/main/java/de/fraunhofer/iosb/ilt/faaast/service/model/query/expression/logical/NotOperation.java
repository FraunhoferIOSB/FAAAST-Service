package de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.BooleanLiteral;


public class NotOperation implements LogicalExpression {

    private final LogicalExpression operand;


    public NotOperation(LogicalExpression operand) {this.operand = operand;}


    @Override
    public LogicalExpression evaluatePartially(EvaluationContext evaluationContext) {
        LogicalExpression evaluated = operand.evaluatePartially(evaluationContext);

        if (evaluated.isLiteral() && evaluated.asLiteral().isBoolean()) {
            return new BooleanLiteral(!evaluated.asLiteral().asBoolean().value());
        }
        return withOperand(evaluated);
    }


    private NotOperation withOperand(LogicalExpression evaluated) {
        return new NotOperation(evaluated);
    }
}
