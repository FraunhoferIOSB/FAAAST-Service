package de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.string;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.AbstractBinaryComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.Literal;

import java.util.List;


/**
 * $starts-with, $ends-with, $contains and $regex check, if the first given argument is part of the second argument or if the first argument matches with the given REGEX.
 */
public abstract class AbstractStringOperation extends AbstractBinaryComparison {

    @Override
    protected void validate(List<LogicalExpression> operands) throws IllegalArgumentException {
        super.validate(operands);
        if (!operands.stream().map(LogicalExpression::asValue).map(Literal::asValue).allMatch(Literal::isString)) {
            throw new IllegalArgumentException(String.format("operands to %s were not strings", this.getClass().getSimpleName()));
        }
    }
}
