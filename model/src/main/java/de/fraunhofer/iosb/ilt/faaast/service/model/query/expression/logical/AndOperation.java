package de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;

import java.util.List;


public class AndOperation extends AbstractLogicalOperation {
    protected AndOperation(List<LogicalExpression> operands) {
        super(operands);
    }


    @Override
    protected boolean neutralElement() {
        return true;
    }


    @Override
    protected AbstractLogicalOperation withOperands(List<LogicalExpression> operands) {
        return new AndOperation(operands);
    }

}
