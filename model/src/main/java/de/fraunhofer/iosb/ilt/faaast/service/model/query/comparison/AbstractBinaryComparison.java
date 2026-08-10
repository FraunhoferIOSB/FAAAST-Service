package de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.operator.ComparisonOperator;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.BooleanLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.DateTimeLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.HexLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.Literal;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.NumberLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.StringLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.TimeLiteral;

import java.util.List;
import java.util.function.BiFunction;


public abstract class AbstractBinaryComparison implements LogicalExpression {

    private ComparisonOperator comparisonOperator;
    private Literal<?> left;
    private Literal<?> right;


    @Override
    public LogicalExpression evaluatePartially(EvaluationContext evaluationContext) {
        LogicalExpression leftEvaluated = left.evaluatePartially(evaluationContext);
        LogicalExpression rightEvaluated = right.evaluatePartially(evaluationContext);

        if (leftEvaluated.isValue()) {
            left = leftEvaluated.asValue();
        }

        List<LogicalExpression> operands = getOperands().stream()
                .map(e -> e.evaluatePartially(evaluationContext))
                .toList();

        if (!operands.stream().allMatch(LogicalExpression::isValue)) {
            return this;
        }

        validate(operands);

        List<Literal> operandStrings = operands.stream()
                .map(LogicalExpression::asValue)
                .map(Literal::asValue)
                .toList();

        Literal<?> left = operandStrings.get(0);
        Literal<?> right = operandStrings.get(1);

        if (left.isString()) {
            return stringOperation().apply(left.asString(), right.asString());
        }
        else if (left.isNumber()) {
            return numberOperation().apply(left.asNumber(), right.asNumber());
        }
        else if (left.isHex()) {
            return hexOperation().apply(left.asHex(), right.asHex());
        }
        else if (left.isBoolean()) {
            return booleanOperation().apply(left.asBoolean(), right.asBoolean());
        }
        else if (left.isDateTime()) {
            return dateTimeOperation().apply(left.asDateTime(), right.asDateTime());
        }
        else {
            return timeOperation().apply(left.asTime(), right.asTime());
        }
    }


    @Override
    protected void validate(List<LogicalExpression> operands) throws IllegalArgumentException {
        if (!operands.stream().map(LogicalExpression::asValue).allMatch(Literal::isValue)
                || operands.stream().map(Object::getClass).distinct().count() != 1) {
            throw new IllegalArgumentException(String.format("operands to %s are not of same type", this.getClass().getSimpleName()));
        }
        if (operands.size() != 2) {
            throw new IllegalArgumentException(String.format("%s can not handle %d operands", this.getClass().getSimpleName(), operands.size()));
        }
    }


    protected BiFunction<StringLiteral, StringLiteral, BooleanLiteral> stringOperation() {
        return defaultOperation();
    }


    protected BiFunction<NumberLiteral, NumberLiteral, BooleanLiteral> numberOperation() {
        return defaultOperation();
    }


    protected BiFunction<HexLiteral, HexLiteral, BooleanLiteral> hexOperation() {
        return defaultOperation();
    }


    protected BiFunction<DateTimeLiteral, DateTimeLiteral, BooleanLiteral> dateTimeOperation() {
        return defaultOperation();
    }


    protected BiFunction<TimeLiteral, TimeLiteral, BooleanLiteral> timeOperation() {
        return defaultOperation();
    }


    protected BiFunction<BooleanLiteral, BooleanLiteral, BooleanLiteral> booleanOperation() {
        return defaultOperation();
    }


    protected <T extends Literal<?>> BiFunction<T, T, BooleanLiteral> defaultOperation() {
        return (x, y) -> {
            throw new IllegalArgumentException(String.format("%s not possible for input %s", this.getClass().getSimpleName(), x.getClass().getSimpleName()));
        };
    }


    protected BooleanLiteral defaultOperation(T left, T right) {
        throw new IllegalArgumentException(String.format("%s not possible for input %s", this.getClass().getSimpleName(), left.getClass().getSimpleName()));
    }

}
