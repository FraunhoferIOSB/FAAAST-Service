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
import java.util.regex.Pattern;


/**
 * Class to evaluate formulas.
 */
public final class FormulaEvaluator {

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
        if (!node.get$eq().isEmpty()) {
            List<Value> ops = node.get$eq();
            if (ops.size() != 2) {
                throw new IllegalArgumentException("$eq requires exactly 2 operands");
            }
            Object left = resolve(ops.get(0), ctx);
            Object right = resolve(ops.get(1), ctx);
            return Objects.equals(left, right);
        }
        if (!node.get$ne().isEmpty()) {
            List<Value> ops = node.get$ne();
            if (ops.size() != 2) {
                throw new IllegalArgumentException("$ne requires exactly 2 operands");
            }
            Object left = resolve(ops.get(0), ctx);
            Object right = resolve(ops.get(1), ctx);
            return !Objects.equals(left, right);
        }
        if (!node.get$gt().isEmpty()) {
            List<Value> ops = node.get$gt();
            if (ops.size() != 2) {
                throw new IllegalArgumentException("$gt requires exactly 2 operands");
            }
            Object lObj = resolve(ops.get(0), ctx);
            Object rObj = resolve(ops.get(1), ctx);
            if (!(lObj instanceof Comparable<?>) || !(rObj instanceof Comparable<?>)) {
                throw new IllegalArgumentException("Operands are not comparable: "
                        + lObj + ", " + rObj);
            }
            int cmp = ((Comparable<Object>) lObj).compareTo(rObj);
            return cmp > 0;
        }
        if (!node.get$ge().isEmpty()) {
            List<Value> ops = node.get$ge();
            if (ops.size() != 2) {
                throw new IllegalArgumentException("$ge requires exactly 2 operands");
            }
            Object lObj = resolve(ops.get(0), ctx);
            Object rObj = resolve(ops.get(1), ctx);
            if (!(lObj instanceof Comparable<?>) || !(rObj instanceof Comparable<?>)) {
                throw new IllegalArgumentException("Operands are not comparable: "
                        + lObj + ", " + rObj);
            }
            int cmp = ((Comparable<Object>) lObj).compareTo(rObj);
            return cmp >= 0;
        }
        if (!node.get$lt().isEmpty()) {
            List<Value> ops = node.get$lt();
            if (ops.size() != 2) {
                throw new IllegalArgumentException("$lt requires exactly 2 operands");
            }
            Object lObj = resolve(ops.get(0), ctx);
            Object rObj = resolve(ops.get(1), ctx);
            if (!(lObj instanceof Comparable<?>) || !(rObj instanceof Comparable<?>)) {
                throw new IllegalArgumentException("Operands are not comparable: "
                        + lObj + ", " + rObj);
            }
            int cmp = ((Comparable<Object>) lObj).compareTo(rObj);
            return cmp < 0;
        }
        if (!node.get$le().isEmpty()) {
            List<Value> ops = node.get$le();
            if (ops.size() != 2) {
                throw new IllegalArgumentException("$le requires exactly 2 operands");
            }
            Object lObj = resolve(ops.get(0), ctx);
            Object rObj = resolve(ops.get(1), ctx);
            if (!(lObj instanceof Comparable<?>) || !(rObj instanceof Comparable<?>)) {
                throw new IllegalArgumentException("Operands are not comparable: "
                        + lObj + ", " + rObj);
            }
            int cmp = ((Comparable<Object>) lObj).compareTo(rObj);
            return cmp <= 0;
        }
        if (!node.get$contains().isEmpty()) {
            List<StringValue> ops = node.get$contains();
            if (ops.size() != 2) {
                throw new IllegalArgumentException("$contains requires exactly 2 operands");
            }
            String left = resolveString(ops.get(0), ctx);
            String right = resolveString(ops.get(1), ctx);
            return left != null && left.contains(right);
        }
        if (!node.get$startsWith().isEmpty()) {
            List<StringValue> ops = node.get$startsWith();
            if (ops.size() != 2) {
                throw new IllegalArgumentException("$starts-with requires exactly 2 operands");
            }
            String left = resolveString(ops.get(0), ctx);
            String right = resolveString(ops.get(1), ctx);
            return left != null && left.startsWith(right);
        }
        if (!node.get$endsWith().isEmpty()) {
            List<StringValue> ops = node.get$endsWith();
            if (ops.size() != 2) {
                throw new IllegalArgumentException("$ends-with requires exactly 2 operands");
            }
            String left = resolveString(ops.get(0), ctx);
            String right = resolveString(ops.get(1), ctx);
            return left != null && left.endsWith(right);
        }
        if (!node.get$regex().isEmpty()) {
            List<StringValue> ops = node.get$regex();
            if (ops.size() != 2) {
                throw new IllegalArgumentException("$regex requires exactly 2 operands");
            }
            String left = resolveString(ops.get(0), ctx);
            String regex = resolveString(ops.get(1), ctx);
            return left != null && Pattern.matches(regex, left);
        }
        if (node.get$boolean() != null) {
            return node.get$boolean();
        }
        if (!node.get$match().isEmpty()) {
            throw new UnsupportedOperationException("Operator $match not supported");
        }
        throw new IllegalArgumentException("No supported operator found in node");
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
