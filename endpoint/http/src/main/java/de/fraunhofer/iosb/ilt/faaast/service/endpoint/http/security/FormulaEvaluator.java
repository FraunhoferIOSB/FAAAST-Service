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
     * @return True if the evaluation is successful, false othetwise.
     */
    public static boolean evaluate(Map<String, Object> formula,
                                   Map<String, Object> runtimeValues) {
        return eval(formula, runtimeValues);
    }


    private static boolean eval(Map<String, Object> node,
                                Map<String, Object> ctx) {
        String op = node.keySet().iterator().next();
        Object value = node.get(op);
        switch (op) {
            case "$and" -> {
                for (Object child: (List<Object>) value) {
                    if (!eval((Map<String, Object>) child, ctx)) {
                        return false;
                    }
                }
                return true;
            }
            case "$or" -> {
                for (Object child: (List<Object>) value) {
                    if (eval((Map<String, Object>) child, ctx)) {
                        return true;
                    }
                }
                return false;
            }
            case "$eq" -> {
                List<Object> ops = (List<Object>) value;
                Object left = resolve((Map<String, Object>) ops.get(0), ctx);
                Object right = resolve((Map<String, Object>) ops.get(1), ctx);
                return Objects.equals(left, right);
            }
            case "$regex" -> {
                List<Object> ops = (List<Object>) value;
                Object left = resolve((Map<String, Object>) ops.get(0), ctx);
                String regex = (String) resolve((Map<String, Object>) ops.get(1), ctx);
                return left != null && Pattern.matches(regex, left.toString());
            }
            case "$ge", "$le" -> {
                List<Object> ops = (List<Object>) value;
                Object lObj = resolve((Map<String, Object>) ops.get(0), ctx);
                Object rObj = resolve((Map<String, Object>) ops.get(1), ctx);

                if (!(lObj instanceof Comparable<?> left) || !(rObj instanceof Comparable<?>)) {
                    throw new IllegalArgumentException("Operands are not comparable: "
                            + lObj + ", " + rObj);
                }
                int cmp = ((Comparable<Object>) left).compareTo(rObj);
                return "$ge".equals(op) ? cmp >= 0 : cmp <= 0;
            }
            default -> throw new IllegalArgumentException("Unsupported operator " + op);
        }
    }


    private static Object resolve(Map<String, Object> operand,
                                  Map<String, Object> ctx) {
        if (operand.containsKey("$strVal"))
            return operand.get("$strVal");
        if (operand.containsKey("$timeVal"))
            return LocalTime.parse((String) operand.get("$timeVal"));
        if (operand.containsKey("$field"))
            return ctx.get(operand.get("$field"));
        if (operand.containsKey("$attribute")) {
            Map<String, String> path = (Map<String, String>) operand.get("$attribute");
            if (path.containsKey("CLAIM"))
                return ctx.get("CLAIM:" + path.get("CLAIM"));
            if (path.containsKey("REFERENCE"))
                return ctx.get("REF:" + path.get("REFERENCE"));
            if (path.containsKey("GLOBAL") && "UTCNOW".equals(path.get("GLOBAL")))
                return ctx.get("UTCNOW");
        }
        throw new IllegalArgumentException("Unresolvable operand " + operand);
    }
}
