/*
 * Copyright (c) 2021 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.faaast.service.model.query.visitor;

import static de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.ClientNow.ISSUED_AT_CLAIM;
import static java.time.ZoneOffset.UTC;
import static java.util.Optional.ofNullable;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.AbstractBinaryComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.EqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.GreaterThanEqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.GreaterThanOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.LessThanEqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.LessThanOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.NotEqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.string.ContainsComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.string.EndsWithComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.string.RegexComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.string.StartsWithComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical.AbstractLogicalOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical.AndOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical.NotOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical.OrOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.match.MatchExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.match.QueryMatchElement;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.ClaimAttribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.FieldIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.Anonymous;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.ClientNow;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.LocalNow;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.UtcNow;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.cast.Cast;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.temporal.TemporalOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.BooleanValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DateTimeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.IntValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.StringValue;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;


/**
 * A {@link LogicalExpressionVisitor} that performs partial (bottom-up) evaluation of a logical expression against an
 * {@link EvaluationContext}.
 *
 * <p>Each node may return an expression, a function or a literal ({@link TypedValue}), depending on the evaluation of
 * its arguments. The evaluation may also return a {@link BooleanValue}, meaning it is evaluated completely.
 */
public class PartialEvaluationVisitor implements LogicalExpressionVisitor<LogicalExpression> {

    private final EvaluationContext evaluationContext;

    /**
     * Creates a new partial evaluation visitor with the given evaluation context.
     *
     * @param evaluationContext the context used to evaluate the expression
     */
    public PartialEvaluationVisitor(EvaluationContext evaluationContext) {
        this.evaluationContext = evaluationContext;
    }


    @Override
    public LogicalExpression visit(NotOperation expr) {
        LogicalExpression folded = expr.operand().accept(this);
        if (folded.isBoolean()) {
            return new BooleanValue(Boolean.FALSE.equals(folded.asBoolean()));
        }
        return new NotOperation(folded);
    }


    @Override
    public LogicalExpression visit(AndOperation expr) {
        return evaluateLogicalOperation(expr);
    }


    @Override
    public LogicalExpression visit(OrOperation expr) {
        return evaluateLogicalOperation(expr);
    }


    @Override
    public LogicalExpression visit(EqualsOperation expr) {
        return evaluateBinaryComparison(expr);
    }


    @Override
    public LogicalExpression visit(NotEqualsOperation expr) {
        return evaluateBinaryComparison(expr);
    }


    @Override
    public LogicalExpression visit(LessThanOperation expr) {
        return evaluateBinaryComparison(expr);
    }


    @Override
    public LogicalExpression visit(LessThanEqualsOperation expr) {
        return evaluateBinaryComparison(expr);
    }


    @Override
    public LogicalExpression visit(GreaterThanOperation expr) {
        return evaluateBinaryComparison(expr);
    }


    @Override
    public LogicalExpression visit(GreaterThanEqualsOperation expr) {
        return evaluateBinaryComparison(expr);
    }


    @Override
    public LogicalExpression visit(StartsWithComparison expr) {
        return evaluateBinaryComparison(expr);
    }


    @Override
    public LogicalExpression visit(EndsWithComparison expr) {
        return evaluateBinaryComparison(expr);
    }


    @Override
    public LogicalExpression visit(ContainsComparison expr) {
        return evaluateBinaryComparison(expr);
    }


    @Override
    public LogicalExpression visit(RegexComparison expr) {
        return evaluateBinaryComparison(expr);
    }


    @Override
    public LogicalExpression visit(MatchExpression expr) {
        List<QueryMatchElement> evaluated = new ArrayList<>();
        boolean changed = false;
        for (QueryMatchElement element: expr.elements()) {
            LogicalExpression folded = element.accept(this);
            if (folded != element) {
                changed = true;
            }
            if (folded.isBoolean()) {
                if (Boolean.FALSE.equals(folded.asBoolean())) {
                    return new BooleanValue(false);
                }
                changed = true;
                continue;
            }
            evaluated.add((QueryMatchElement) folded);
        }
        if (evaluated.isEmpty()) {
            return new BooleanValue(true);
        }
        return changed ? new MatchExpression(evaluated) : expr;
    }


    @Override
    public LogicalExpression visit(ClaimAttribute expr) {
        return ofNullable(evaluationContext.getClaim(expr.getClaim()))
                .map(StringValue::new)
                .orElseThrow(() -> new IllegalStateException(String.format("Claim %s not present in context", expr.getClaim())));
    }


    @Override
    public LogicalExpression visit(UtcNow expr) {
        return new DateTimeValue(LocalDateTime.now().atOffset(UTC));
    }


    @Override
    public LogicalExpression visit(ClientNow expr) {
        String iat = evaluationContext.getClaim(ISSUED_AT_CLAIM);
        if (iat == null) {
            return expr;
        }
        var dateTime = new DateTimeValue();
        try {
            dateTime.fromString(LocalDateTime.ofEpochSecond(Integer.parseInt(iat), 0, ZoneOffset.UTC).toString());
        }
        catch (ValueFormatException e) {
            throw new IllegalArgumentException(String.format("Could not parse claim 'iat': %s", iat), e);
        }
        return dateTime;
    }


    @Override
    public LogicalExpression visit(LocalNow expr) {
        return new DateTimeValue(LocalDateTime.now().atOffset(ZoneId.systemDefault().getRules().getOffset(Instant.now())));
    }


    @Override
    public LogicalExpression visit(Anonymous expr) {
        return new BooleanValue(evaluationContext.isAnonymous());
    }


    @Override
    public LogicalExpression visit(Cast<?> expr) {
        Operand evaluated = evalOperand(expr.getOperand());
        if (evaluated.isTypedValue()) {
            return expr.cast(evaluated.asTypedValue());
        }
        return evaluated == expr.getOperand() ? expr : expr.withOperand(evaluated);
    }


    @Override
    public LogicalExpression visit(TemporalOperation expr) {
        Operand evaluated = evalOperand(expr.getOperand());
        if (!evaluated.isTypedValue()) {
            return expr;
        }
        return new IntValue(expr.operation().apply((DateTimeValue) expr.getOperand().asTypedValue()));
    }


    @Override
    public LogicalExpression visit(FieldIdentifier expr) {
        // We cannot evaluate this part before looking into the persistence
        return expr;
    }


    @Override
    public LogicalExpression visit(TypedValue<?> value) {
        return value;
    }


    /**
     * Evaluate an operand via the visitor, casting the result back to {@link Operand}. This is safe because
     * evaluating an {@link Operand} always yields an {@link Operand}.
     *
     * @param operand the operand to evaluate
     * @return the evaluated operand
     */
    private Operand evalOperand(Operand operand) {
        return (Operand) operand.accept(this);
    }


    /**
     * Shared bottom-up fold for {@link AbstractLogicalOperation} ({@code $and} / {@code $or}).
     *
     * @param expr the logical operation
     * @return the folded result
     */
    private LogicalExpression evaluateLogicalOperation(AbstractLogicalOperation expr) {
        List<LogicalExpression> evaluated = new ArrayList<>();
        boolean changed = false;
        for (LogicalExpression operand: expr.getOperands()) {
            LogicalExpression folded = operand.accept(this);
            if (folded != operand) {
                changed = true;
            }
            if (folded.isBoolean()) {
                if (!Boolean.valueOf(expr.neutralElement()).equals(folded.asBoolean())) {
                    return folded;
                }
                changed = true;
                continue;
            }
            evaluated.add(folded);
        }
        return switch (evaluated.size()) {
            case 0 -> new BooleanValue(expr.neutralElement());
            case 1 -> evaluated.get(0);
            default -> changed ? expr.withOperands(List.copyOf(evaluated)) : expr;
        };
    }


    /**
     * Shared bottom-up fold for {@link AbstractBinaryComparison} (all binary comparison operators).
     *
     * @param expr the binary comparison
     * @return the folded result
     */
    private LogicalExpression evaluateBinaryComparison(AbstractBinaryComparison expr) {
        Operand leftEvaluated = evalOperand(expr.getLeft());
        Operand rightEvaluated = evalOperand(expr.getRight());

        if (leftEvaluated.isTypedValue() && rightEvaluated.isTypedValue()) {
            return new BooleanValue(expr.compute(leftEvaluated.asTypedValue(), rightEvaluated.asTypedValue()));
        }
        if (leftEvaluated != expr.getLeft() || rightEvaluated != expr.getRight()) {
            return expr.withOperands(leftEvaluated, rightEvaluated);
        }
        return expr;
    }
}
