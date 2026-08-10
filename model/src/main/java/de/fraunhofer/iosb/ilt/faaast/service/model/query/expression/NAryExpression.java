package de.fraunhofer.iosb.ilt.faaast.service.model.query.expression;

import java.util.List;


public abstract class NAryExpression implements LogicalExpression {
    private final List<LogicalExpression> operands;


    protected NAryExpression(List<LogicalExpression> operands) {this.operands = operands;}


    public List<LogicalExpression> getOperands() {
        return operands;
    }
}
