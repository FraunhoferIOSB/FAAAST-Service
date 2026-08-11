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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.match.MatchElement;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.BooleanLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.DateTimeLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.HexLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.Literal;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.NumberLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.StringLiteral;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.TimeLiteral;

import java.util.List;
import java.util.function.BiFunction;


public abstract class AbstractBinaryComparison implements LogicalExpression, MatchElement {

    private final Operand left;
    private final Operand right;

    protected AbstractBinaryComparison(Operand left, Operand right) {
        this.left = left;
        this.right = right;
    }


    public Operand getLeft() {
        return left;
    }


    public Operand getRight() {
        return right;
    }


    public List<Operand> getOperands() {
        return List.of(left, right);
    }


    @Override
    public LogicalExpression evaluatePartially(EvaluationContext evaluationContext) {
        Operand leftEvaluated = left.evaluatePartially(evaluationContext);
        Operand rightEvaluated = right.evaluatePartially(evaluationContext);

        if (leftEvaluated.isLiteral() && rightEvaluated.isLiteral()) {
            validate(List.of(leftEvaluated, rightEvaluated));
            Literal leftLiteral = leftEvaluated.asLiteral();
            Literal rightLiteral = rightEvaluated.asLiteral();

            if (leftLiteral.isString()) {
                return stringOperation().apply(leftLiteral.asString(), rightLiteral.asString());
            }
            else if (leftLiteral.isNumber()) {
                return numberOperation().apply(leftLiteral.asNumber(), rightLiteral.asNumber());
            }
            else if (leftLiteral.isHex()) {
                return hexOperation().apply(leftLiteral.asHex(), rightLiteral.asHex());
            }
            else if (leftLiteral.isBoolean()) {
                return booleanOperation().apply(leftLiteral.asBoolean(), rightLiteral.asBoolean());
            }
            else if (leftLiteral.isDateTime()) {
                return dateTimeOperation().apply(leftLiteral.asDateTime(), rightLiteral.asDateTime());
            }
            else {
                return timeOperation().apply(leftLiteral.asTime(), rightLiteral.asTime());
            }
        }

        if (leftEvaluated != left || rightEvaluated != right) {
            return withOperands(leftEvaluated, rightEvaluated);
        }
        return this;
    }


    protected void validate(List<? extends LogicalExpression> operands) throws IllegalArgumentException {
        if (!operands.stream().allMatch(LogicalExpression::isLiteral)
                || operands.stream().map(Object::getClass).distinct().count() != 1) {
            throw new IllegalArgumentException(String.format("operands to %s are not of same type", this.getClass().getSimpleName()));
        }
        if (operands.size() != 2) {
            throw new IllegalArgumentException(String.format("%s can not handle %d operands", this.getClass().getSimpleName(), operands.size()));
        }
    }


    protected abstract AbstractBinaryComparison withOperands(Operand left, Operand right);


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


    protected <T extends Literal> BiFunction<T, T, BooleanLiteral> defaultOperation() {
        return (x, y) -> {
            throw new IllegalArgumentException(String.format("%s not possible for input %s", this.getClass().getSimpleName(), x.getClass().getSimpleName()));
        };
    }

}
