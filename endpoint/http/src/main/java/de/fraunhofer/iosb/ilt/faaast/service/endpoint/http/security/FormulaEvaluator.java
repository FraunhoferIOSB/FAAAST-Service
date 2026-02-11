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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AttributeItem;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.StringValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Value;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;


/**
 * Class to evaluate formulas.
 */
public final class FormulaEvaluator {

    @FunctionalInterface
    private interface ValueComparator {
        boolean compare(Object left, Object right);
    }

    @FunctionalInterface
    private interface StringComparator {
        boolean compare(String left, String right);
    }

    private record ValueOperationSpec(String name, Supplier<List<Value>> operands, ValueComparator comparator) {}

    private record StringOperationSpec(String name, Supplier<List<StringValue>> operands, StringComparator comparator) {}

    /**
     * Evaluates the given formula.
     *
     * @param formula The formula.
     * @param runtimeValues The runtime values.
     * @return True if the evaluation is successful, false otherwise.
     */
    public static boolean evaluate(LogicalExpression formula,
                                   Map<String, Object> runtimeValues) {
        return eval(formula, runtimeValues);
    }


    private static boolean eval(LogicalExpression node,
                                Map<String, Object> ctx) {
        Boolean logical = evaluateLogical(node, ctx);
        if (logical != null) {
            return logical;
        }

        Boolean valueComparison = evaluateValueComparison(node, ctx);
        if (valueComparison != null) {
            return valueComparison;
        }

        Boolean stringComparison = evaluateStringComparison(node, ctx);
        if (stringComparison != null) {
            return stringComparison;
        }

        if (node.get$boolean() != null) {
            return node.get$boolean();
        }
        if (!node.get$match().isEmpty()) {
            throw new UnsupportedOperationException("Operator $match not supported");
        }
        throw new IllegalArgumentException("No supported operator found in node");
    }


    private static Boolean evaluateLogical(LogicalExpression node,
                                           Map<String, Object> ctx) {
        if (!node.get$and().isEmpty()) {
            for (LogicalExpression child: node.get$and()) {
                if (!eval(child, ctx)) {
                    return false;
                }
            }
            return true;
        }
        if (!node.get$or().isEmpty()) {
            for (LogicalExpression child: node.get$or()) {
                if (eval(child, ctx)) {
                    return true;
                }
            }
            return false;
        }
        if (node.get$not() != null) {
            return !eval(node.get$not(), ctx);
        }
        return null;
    }


    private static Boolean evaluateValueComparison(LogicalExpression node,
                                                   Map<String, Object> ctx) {
        List<ValueOperationSpec> operations = List.of(
                new ValueOperationSpec("$eq", node::get$eq, Objects::equals),
                new ValueOperationSpec("$ne", node::get$ne, (left, right) -> !Objects.equals(left, right)),
                new ValueOperationSpec("$gt", node::get$gt, (left, right) -> compareComparable(left, right, v -> v > 0)),
                new ValueOperationSpec("$ge", node::get$ge, (left, right) -> compareComparable(left, right, v -> v >= 0)),
                new ValueOperationSpec("$lt", node::get$lt, (left, right) -> compareComparable(left, right, v -> v < 0)),
                new ValueOperationSpec("$le", node::get$le, (left, right) -> compareComparable(left, right, v -> v <= 0)));

        for (ValueOperationSpec spec: operations) {
            List<Value> ops = spec.operands.get();
            if (!ops.isEmpty()) {
                validateOperandCount(ops.size(), spec.name);
                Object left = resolve(ops.get(0), ctx);
                Object right = resolve(ops.get(1), ctx);
                return spec.comparator.compare(left, right);
            }
        }
        return null;
    }


    private static Boolean evaluateStringComparison(LogicalExpression node,
                                                    Map<String, Object> ctx) {
        List<StringOperationSpec> operations = List.of(
                new StringOperationSpec("$contains", node::get$contains, (left, right) -> left != null && left.contains(right)),
                new StringOperationSpec("$starts-with", node::get$startsWith, (left, right) -> left != null && left.startsWith(right)),
                new StringOperationSpec("$ends-with", node::get$endsWith, (left, right) -> left != null && left.endsWith(right)),
                new StringOperationSpec("$regex", node::get$regex, (left, right) -> left != null && Pattern.matches(right, left)));

        for (StringOperationSpec spec: operations) {
            List<StringValue> ops = spec.operands.get();
            if (!ops.isEmpty()) {
                validateOperandCount(ops.size(), spec.name);
                String left = resolveString(ops.get(0), ctx);
                String right = resolveString(ops.get(1), ctx);
                return spec.comparator.compare(left, right);
            }
        }
        return null;
    }


    private static void validateOperandCount(int count,
                                             String operator) {
        if (count != 2) {
            throw new IllegalArgumentException(operator + " requires exactly 2 operands");
        }
    }


    private static boolean compareComparable(Object left,
                                             Object right,
                                             IntPredicate predicate) {
        if (!(left instanceof Comparable<?>) || !(right instanceof Comparable<?>)) {
            throw new IllegalArgumentException("Operands are not comparable: "
                    + left + ", " + right);
        }
        int cmp = ((Comparable<Object>) left).compareTo(right);
        return predicate.test(cmp);
    }


    private static Object resolve(Value operand,
                                  Map<String, Object> ctx) {
        if (operand.get$strVal() != null) {
            return operand.get$strVal();
        }
        if (operand.get$timeVal() != null) {
            return LocalTime.parse(operand.get$timeVal());
        }
        if (operand.get$field() != null) {
            return ctx.get(operand.get$field());
        }
        if (operand.get$attribute() != null) {
            AttributeItem path = operand.get$attribute();
            if (!Objects.isNull(path.getClaim())) {
                return ctx.get("CLAIM:" + path.getClaim());
            }
            if (!Objects.isNull(path.getReference())) {
                return ctx.get("REF:" + path.getReference());
            }
            return ctx.get("UTCNOW");
        }
        throw new IllegalArgumentException("Unresolvable operand " + operand);
    }


    private static String resolveString(StringValue operand,
                                        Map<String, Object> ctx) {
        if (operand.get$strVal() != null) {
            return operand.get$strVal();
        }
        if (operand.get$field() != null) {
            Object val = ctx.get(operand.get$field());
            return val != null ? val.toString() : null;
        }
        if (operand.get$attribute() != null) {
            AttributeItem path = operand.get$attribute();
            String key = null;
            if (!Objects.isNull(path.getClaim())) {
                key = "CLAIM:" + path.getClaim();
            }
            else if (!Objects.isNull(path.getReference())) {
                key = "REF:" + path.getReference();
            }
            else {
                key = "UTCNOW";
            }
            if (key != null) {
                Object val = ctx.get(key);
                return val != null ? val.toString() : null;
            }
        }
        throw new IllegalArgumentException("Unresolvable operand " + operand);
    }
}
