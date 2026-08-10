package de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;

import java.util.List;


public class OrOperation extends AbstractLogicalOperation {
    protected OrOperation(List<LogicalExpression> operands) {
        super(operands);
    }


    @Override
    protected boolean neutralElement() {
        return false;
    }


    @Override
    protected AbstractLogicalOperation withOperands(List<LogicalExpression> operands) {
        return new OrOperation(operands);
    }
}
