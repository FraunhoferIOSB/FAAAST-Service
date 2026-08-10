package de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.NAryExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.BooleanLiteral;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractLogicalOperation extends NAryExpression {

    protected AbstractLogicalOperation(List<LogicalExpression> operands) {
        super(operands);
    }


    @Override
    public LogicalExpression evaluatePartially(EvaluationContext evaluationContext) {
        List<LogicalExpression> evaluated = new ArrayList<>();
        boolean changed = false;

        for (LogicalExpression operand: getOperands()) {
            LogicalExpression folded = operand.evaluatePartially(evaluationContext);
            if (folded != operand) {
                changed = true;
            }

            if (folded.isLiteral() && folded.asLiteral().isBoolean()) {
                // E.g., AND (..., FALSE, ...) = FALSE
                if (folded.asLiteral().asBoolean().value() != neutralElement()) {
                    return folded.asLiteral().asBoolean();
                }
                changed = true;
                continue;
            }

            evaluated.add(folded);
        }

        return switch (evaluated.size()) {
            case 0 -> new BooleanLiteral(neutralElement());
            case 1 -> evaluated.get(0);
            default -> changed ? withOperands(List.copyOf(evaluated)) : this;
        };
    }


    protected abstract boolean neutralElement();


    protected abstract AbstractLogicalOperation withOperands(List<LogicalExpression> newOperands);
}
