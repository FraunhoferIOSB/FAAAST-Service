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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.postgres;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.StringValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class QueryToSqlTranslator {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryToSqlTranslator.class);

    private static final String PREFIX_AAS = "$aas#";
    private static final String PREFIX_SM = "$sm#";
    private static final String PREFIX_SME = "$sme";
    private static final String PREFIX_CD = "$cd#";

    private final List<Object> parameters = new ArrayList<>();
    private int paramCounter = 0;

    public TranslationResult translate(LogicalExpression expr, String tableName) {
        parameters.clear();
        paramCounter = 0;

        if (expr == null) {
            return new TranslationResult("TRUE", new ArrayList<>());
        }

        String sql = translateExpression(expr, tableName);
        return new TranslationResult(sql, new ArrayList<>(parameters));
    }


    private String translateExpression(LogicalExpression expr, String tableName) {
        if (expr == null) {
            return "TRUE";
        }

        if (expr.get$boolean() != null) {
            return expr.get$boolean() ? "TRUE" : "FALSE";
        }

        if (expr.get$and() != null && !expr.get$and().isEmpty()) {
            String conditions = expr.get$and().stream()
                    .map(e -> translateExpression(e, tableName))
                    .filter(c -> !c.isEmpty())
                    .map(c -> "(" + c + ")")
                    .reduce((a, b) -> a + " AND " + b)
                    .orElse("TRUE");
            return conditions;
        }

        if (expr.get$or() != null && !expr.get$or().isEmpty()) {
            String conditions = expr.get$or().stream()
                    .map(e -> translateExpression(e, tableName))
                    .filter(c -> !c.isEmpty())
                    .map(c -> "(" + c + ")")
                    .reduce((a, b) -> a + " OR " + b)
                    .orElse("FALSE");
            return conditions;
        }

        if (expr.get$not() != null) {
            String inner = translateExpression(expr.get$not(), tableName);
            return inner.isEmpty() ? "TRUE" : "NOT (" + inner + ")";
        }

        if (expr.get$match() != null && !expr.get$match().isEmpty()) {
            return translateMatch(expr.get$match(), tableName);
        }

        return translateComparison(expr, tableName);
    }


    private String translateMatch(List<MatchExpression> matches, String tableName) {
        if (matches == null || matches.isEmpty()) {
            return "TRUE";
        }

        String commonPrefix = null;
        List<MatchCondition> conditions = new ArrayList<>();

        for (MatchExpression m: matches) {
            MatchOperation mo = getMatchOperation(m);
            if (mo == null) {
                continue;
            }

            if (mo.args.size() < 2) {
                continue;
            }

            Value left = mo.args.get(0);
            Value right = mo.args.get(1);

            if (left.get$field() == null) {
                continue;
            }

            String field = left.get$field();
            String rightValue = evaluateRightValue(right);

            int listMarker = field.indexOf("[]");
            if (listMarker != -1) {
                String prefix = field.substring(0, listMarker);
                String suffix = field.substring(listMarker + 2);

                if (commonPrefix == null) {
                    commonPrefix = prefix;
                }
                else if (!commonPrefix.equals(prefix)) {
                    LOGGER.warn("Non-common prefix in match: {} vs {}", commonPrefix, prefix);
                    return "TRUE";
                }

                conditions.add(new MatchCondition(suffix, mo.operator, rightValue));
            }
            else {
                String condition = translateFieldComparison(field, mo.operator, rightValue, tableName);
                if (!condition.isEmpty()) {
                    conditions.add(new MatchCondition(field, mo.operator, rightValue));
                }
            }
        }

        if (commonPrefix == null) {
            return conditions.stream()
                    .map(c -> translateFieldComparison(c.suffix, c.operator, c.value, tableName))
                    .filter(s -> !s.isEmpty())
                    .map(s -> "(" + s + ")")
                    .reduce((a, b) -> a + " AND " + b)
                    .orElse("TRUE");
        }

        return translateMatchWithPrefix(commonPrefix, conditions, tableName);
    }


    private String translateMatchWithPrefix(String prefix, List<MatchCondition> conditions, String tableName) {
        switch (prefix) {
            case "$aas#assetInformation.specificAssetIds":
                return translateArrayMatch(
                        tableName,
                        "content->'assetInformation'->'specificAssetIds'",
                        conditions,
                        new String[] {
                                "name",
                                "value"
                        });

            case "$aas#extensions":
                return translateArrayMatch(
                        tableName,
                        "content->'extensions'",
                        conditions,
                        new String[] {
                                "name",
                                "value",
                                "valueType",
                                "semanticId"
                        });

            case "$sm#extensions":
                return translateArrayMatch(
                        tableName,
                        "content->'extensions'",
                        conditions,
                        new String[] {
                                "name",
                                "value",
                                "valueType",
                                "semanticId"
                        });

            case "$sm#semanticId.keys":
                return translateArrayMatch(
                        tableName,
                        "content->'semanticId'->'keys'",
                        conditions,
                        new String[] {
                                "type",
                                "value"
                        });

            case "$sme":
                return translateSmeMatch(conditions, tableName);

            default:
                if (prefix.startsWith("$sme.")) {
                    String path = prefix.substring("$sme.".length());
                    return translateSmePathMatch(path, conditions, tableName);
                }
                LOGGER.warn("Unsupported match prefix: {}", prefix);
                return "TRUE";
        }
    }


    private String translateArrayMatch(String tableName, String jsonPath, List<MatchCondition> conditions, String[] fieldMappings) {
        StringBuilder sql = new StringBuilder();
        sql.append("EXISTS (SELECT 1 FROM jsonb_array_elements(").append(tableName).append(".").append(jsonPath).append(") AS item WHERE ");

        List<String> itemConditions = new ArrayList<>();
        for (MatchCondition cond: conditions) {
            String fieldName = cond.suffix.startsWith(".") ? cond.suffix.substring(1) : cond.suffix;
            String jsonFieldPath = mapFieldNameToJsonPath(fieldName, fieldMappings);

            String itemCondition = translateConditionToJsonPath("item", jsonFieldPath, cond.operator, cond.value);
            itemConditions.add(itemCondition);
        }

        sql.append(String.join(" AND ", itemConditions));
        sql.append(")");

        return sql.toString();
    }


    private String mapFieldNameToJsonPath(String fieldName, String[] mappings) {
        for (int i = 0; i < mappings.length; i++) {
            if (fieldName.equals(mappings[i])) {
                return "->>'" + mappings[i] + "'";
            }
        }
        return "->>'" + fieldName + "'";
    }


    private String translateSmeMatch(List<MatchCondition> conditions, String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append("EXISTS (SELECT 1 FROM jsonb_array_elements(").append(tableName).append(".content->'submodelElements') AS elem WHERE ");

        List<String> elemConditions = new ArrayList<>();
        for (MatchCondition cond: conditions) {
            String fieldName = cond.suffix.startsWith(".") ? cond.suffix.substring(1) : cond.suffix;
            String elemCondition = translateSmeJsonCondition(fieldName, cond.operator, cond.value);
            elemConditions.add(elemCondition);
        }

        sql.append(String.join(" AND ", elemConditions));
        sql.append(")");

        return sql.toString();
    }


    private String translateSmeJsonCondition(String fieldName, String operator, String value) {
        switch (fieldName) {
            case "idShort":
                return "elem->>'idShort' " + operator + " '" + escapeString(value) + "'";
            case "semanticId":
                return "elem->'semanticId'->'keys'->0->>'value' " + operator + " '" + escapeString(value) + "'";
            case "value":
                return "elem->>'value' " + operator + " '" + escapeString(value) + "'";
            case "valueType":
                return "elem->>'valueType' " + operator + " '" + escapeString(value) + "'";
            case "category":
                return "elem->>'category' " + operator + " '" + escapeString(value) + "'";
            case "language":
                return "elem->>'language' " + operator + " '" + escapeString(value) + "'";
            default:
                LOGGER.warn("Unsupported SME JSON field: {}", fieldName);
                return "TRUE";
        }
    }


    private String translateSmePathMatch(String path, List<MatchCondition> conditions, String tableName) {
        StringBuilder sql = new StringBuilder();

        String[] pathParts = path.split("\\.");
        String firstElement = pathParts[0];
        String remainingPath = pathParts.length > 1 ? String.join(".", Arrays.copyOfRange(pathParts, 1, pathParts.length)) : null;

        sql.append("EXISTS (SELECT 1 FROM jsonb_array_elements(").append(tableName).append(".content->'submodelElements') AS elem ");
        sql.append("WHERE elem->>'idShort' = '").append(escapeString(firstElement)).append("'");

        if (remainingPath != null && !remainingPath.isEmpty()) {
            sql.append(" AND EXISTS (SELECT 1 FROM jsonb_array_elements(elem->'value') AS child ");
            sql.append("WHERE child->>'idShort' = '").append(escapeString(remainingPath)).append("'");

            List<String> childConditions = new ArrayList<>();
            for (MatchCondition cond: conditions) {
                String fieldName = cond.suffix.startsWith(".") ? cond.suffix.substring(1) : cond.suffix;
                String childCondition = translateSmeJsonCondition(fieldName, cond.operator, cond.value);
                childConditions.add("child." + childCondition);
            }

            if (!childConditions.isEmpty()) {
                sql.append(" AND ").append(String.join(" AND ", childConditions));
            }
            sql.append(")");
        }
        else {
            List<String> elemConditions = new ArrayList<>();
            for (MatchCondition cond: conditions) {
                String fieldName = cond.suffix.startsWith(".") ? cond.suffix.substring(1) : cond.suffix;
                String elemCondition = translateSmeJsonCondition(fieldName, cond.operator, cond.value);
                elemConditions.add(elemCondition);
            }

            if (!elemConditions.isEmpty()) {
                sql.append(" AND ").append(String.join(" AND ", elemConditions));
            }
        }

        sql.append(")");

        return sql.toString();
    }


    private String translateConditionToJsonPath(String tableAlias, String jsonPath, String operator, String value) {
        String escapedValue = escapeString(value);
        return tableAlias + jsonPath + " " + operator + " '" + escapedValue + "'";
    }


    private String translateFieldComparison(String field, String operator, String value, String tableName) {
        if (field.startsWith(PREFIX_AAS)) {
            String attr = field.substring(PREFIX_AAS.length());
            return translateAasField(attr, operator, value, tableName);
        }
        else if (field.startsWith(PREFIX_SM)) {
            String attr = field.substring(PREFIX_SM.length());
            return translateSmField(attr, operator, value, tableName);
        }
        else if (field.startsWith(PREFIX_CD)) {
            String attr = field.substring(PREFIX_CD.length());
            return translateCdField(attr, operator, value, tableName);
        }
        else if (field.startsWith(PREFIX_SME)) {
            return translateSmeField(field, operator, value, tableName);
        }

        LOGGER.warn("Unsupported field: {}", field);
        return "";
    }


    private String translateAasField(String attr, String operator, String value, String tableName) {
        String jsonPath = aasAttrToJsonPath(attr);
        return tableName + "." + jsonPath + " " + operator + " '" + escapeString(value) + "'";
    }


    private String aasAttrToJsonPath(String attr) {
        if (attr.equals("idShort")) {
            return "content->>'idShort'";
        }
        else if (attr.equals("id")) {
            return "content->>'id'";
        }
        else if (attr.equals("assetInformation.assetKind")) {
            return "content->'assetInformation'->>'assetKind'";
        }
        else if (attr.equals("assetInformation.assetType")) {
            return "content->'assetInformation'->>'assetType'";
        }
        else if (attr.equals("assetInformation.globalAssetId")) {
            return "content->'assetInformation'->>'globalAssetId'";
        }
        else if (attr.equals("description")) {
            return "content->'description'->0->>'text'";
        }
        else if (attr.equals("displayName")) {
            return "content->'displayName'->0->>'text'";
        }
        else if (attr.equals("administration.version")) {
            return "content->'administration'->>'version'";
        }
        else if (attr.equals("administration.revision")) {
            return "content->'administration'->>'revision'";
        }

        LOGGER.warn("Unsupported AAS attribute: {}", attr);
        return "content";
    }


    private String translateSmField(String attr, String operator, String value, String tableName) {
        String jsonPath = smAttrToJsonPath(attr);
        return tableName + "." + jsonPath + " " + operator + " '" + escapeString(value) + "'";
    }


    private String smAttrToJsonPath(String attr) {
        if (attr.equals("idShort")) {
            return "content->>'idShort'";
        }
        else if (attr.equals("id")) {
            return "content->>'id'";
        }
        else if (attr.equals("semanticId")) {
            return "content->'semanticId'->'keys'->0->>'value'";
        }
        else if (attr.equals("semanticId.type")) {
            return "content->'semanticId'->'keys'->0->>'type'";
        }
        else if (attr.equals("kind")) {
            return "content->>'kind'";
        }
        else if (attr.equals("description")) {
            return "content->'description'->0->>'text'";
        }
        else if (attr.equals("displayName")) {
            return "content->'displayName'->0->>'text'";
        }
        else if (attr.equals("administration.version")) {
            return "content->'administration'->>'version'";
        }
        else if (attr.equals("administration.revision")) {
            return "content->'administration'->>'revision'";
        }

        LOGGER.warn("Unsupported SM attribute: {}", attr);
        return "content";
    }


    private String translateCdField(String attr, String operator, String value, String tableName) {
        String jsonPath = cdAttrToJsonPath(attr);
        return tableName + "." + jsonPath + " " + operator + " '" + escapeString(value) + "'";
    }


    private String cdAttrToJsonPath(String attr) {
        if (attr.equals("idShort")) {
            return "content->>'idShort'";
        }
        else if (attr.equals("id")) {
            return "content->>'id'";
        }
        else if (attr.equals("description")) {
            return "content->'description'->0->>'text'";
        }
        else if (attr.equals("displayName")) {
            return "content->'displayName'->0->>'text'";
        }

        LOGGER.warn("Unsupported CD attribute: {}", attr);
        return "content";
    }


    private String translateSmeField(String field, String operator, String value, String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append("EXISTS (SELECT 1 FROM jsonb_array_elements(").append(tableName).append(".content->'submodelElements') AS elem WHERE ");

        String fieldName = field.startsWith(PREFIX_SME + "#")
                ? field.substring((PREFIX_SME + "#").length())
                : field.substring(PREFIX_SME.length() + 1);

        String condition = translateSmeJsonCondition(fieldName, operator, value);
        sql.append(condition);

        sql.append(")");

        return sql.toString();
    }


    private String translateComparison(LogicalExpression expr, String tableName) {
        List<ComparisonSpec<Value>> valueComparisons = Arrays.asList(
                new ComparisonSpec<>("=", expr::get$eq),
                new ComparisonSpec<>("<>", expr::get$ne),
                new ComparisonSpec<>(">", expr::get$gt),
                new ComparisonSpec<>(">=", expr::get$ge),
                new ComparisonSpec<>("<", expr::get$lt),
                new ComparisonSpec<>("<=", expr::get$le));

        for (ComparisonSpec<Value> spec: valueComparisons) {
            List<Value> args = spec.argumentProvider.get();
            if (args != null && !args.isEmpty()) {
                return translateValueComparison(args, spec.operator, tableName);
            }
        }

        List<ComparisonSpec<StringValue>> stringComparisons = Arrays.asList(
                new ComparisonSpec<>("LIKE", expr::get$contains),
                new ComparisonSpec<>("LIKE", expr::get$startsWith),
                new ComparisonSpec<>("LIKE", expr::get$endsWith),
                new ComparisonSpec<>("~", expr::get$regex));

        for (ComparisonSpec<StringValue> spec: stringComparisons) {
            List<StringValue> args = spec.argumentProvider.get();
            if (args != null && !args.isEmpty()) {
                return translateStringComparison(args, spec.operator, tableName);
            }
        }

        return "";
    }


    private <T> String translateComparisonArgs(List<T> args, String operator, java.util.function.Function<T, String> valueExtractor,
                                               java.util.function.Function<T, String> fieldExtractor, String tableName) {
        if (args.size() < 2) {
            return "";
        }

        T left = args.get(0);
        T right = args.get(1);

        String leftField = fieldExtractor.apply(left);
        String rightValue = valueExtractor.apply(right);

        if (leftField != null) {
            return translateFieldComparison(leftField, operator, rightValue, tableName);
        }

        return "";
    }


    private String translateValueComparison(List<Value> args, String operator, String tableName) {
        return translateComparisonArgs(
                args,
                operator,
                this::evaluateRightValue,
                arg -> arg.get$field(),
                tableName);
    }


    private String translateStringComparison(List<StringValue> args, String operator, String tableName) {
        if (args.size() < 2) {
            return "";
        }

        StringValue left = args.get(0);
        StringValue right = args.get(1);

        String leftField = left.get$field();
        String rightValue = evaluateStringRightValue(right);

        if (leftField != null) {
            String condition = translateFieldComparison(leftField, operator, rightValue, tableName);
            if (operator.equals("LIKE")) {
                if (rightValue.startsWith("%") && rightValue.endsWith("%")) {
                    condition = condition.replace("LIKE", "ILIKE");
                }
            }
            return condition;
        }

        return "";
    }


    private String evaluateRightValue(Value v) {
        if (v == null) {
            return "";
        }
        if (v.get$strVal() != null) {
            return v.get$strVal();
        }
        if (v.get$numVal() != null) {
            return String.valueOf(v.get$numVal());
        }
        if (v.get$hexVal() != null) {
            return v.get$hexVal();
        }
        if (v.get$dateTimeVal() != null) {
            return v.get$dateTimeVal().toString();
        }
        if (v.get$timeVal() != null) {
            return v.get$timeVal().toString();
        }
        if (v.get$boolean() != null) {
            return v.get$boolean().toString();
        }

        return "";
    }


    private String evaluateStringRightValue(StringValue sv) {
        if (sv == null) {
            return "";
        }
        if (sv.get$strVal() != null) {
            return sv.get$strVal();
        }

        return "";
    }


    private MatchOperation getMatchOperation(MatchExpression m) {
        if (m.get$eq() != null && !m.get$eq().isEmpty()) {
            return new MatchOperation("=", m.get$eq());
        }
        if (m.get$ne() != null && !m.get$ne().isEmpty()) {
            return new MatchOperation("<>", m.get$ne());
        }
        if (m.get$gt() != null && !m.get$gt().isEmpty()) {
            return new MatchOperation(">", m.get$gt());
        }
        if (m.get$ge() != null && !m.get$ge().isEmpty()) {
            return new MatchOperation(">=", m.get$ge());
        }
        if (m.get$lt() != null && !m.get$lt().isEmpty()) {
            return new MatchOperation("<", m.get$lt());
        }
        if (m.get$le() != null && !m.get$le().isEmpty()) {
            return new MatchOperation("<=", m.get$le());
        }
        return null;
    }


    private String escapeString(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("'", "''");
    }

    private static class ComparisonSpec<T> {
        final String operator;
        final java.util.function.Supplier<List<T>> argumentProvider;

        ComparisonSpec(String operator, java.util.function.Supplier<List<T>> argumentProvider) {
            this.operator = operator;
            this.argumentProvider = argumentProvider;
        }
    }

    private static class MatchOperation {
        final String operator;
        final List<? extends Value> args;

        MatchOperation(String operator, List<? extends Value> args) {
            this.operator = operator;
            this.args = args;
        }
    }

    private static class MatchCondition {
        final String suffix;
        final String operator;
        final String value;

        MatchCondition(String suffix, String operator, String value) {
            this.suffix = suffix;
            this.operator = operator;
            this.value = value;
        }
    }

    public static class TranslationResult {
        private final String sql;
        private final List<Object> parameters;

        public TranslationResult(String sql, List<Object> parameters) {
            this.sql = sql;
            this.parameters = parameters;
        }


        public String getSql() {
            return sql;
        }


        public List<Object> getParameters() {
            return parameters;
        }


        public boolean isEmpty() {
            return sql == null || sql.isEmpty() || sql.equals("TRUE") || sql.equals("FALSE");
        }
    }
}
