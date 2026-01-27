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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Translator that converts query expressions to SQL queries for PostgreSQL persistence.
 */
public class QueryToSqlTranslator {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryToSqlTranslator.class);

    private static final String PREFIX_AAS = "$aas#";
    private static final String PREFIX_SM = "$sm#";
    private static final String PREFIX_SME = "$sme";
    private static final String PREFIX_CD = "$cd#";
    private static final String RECURSIVE_CTE_TEMPLATE = """
            WITH RECURSIVE search_path AS (
                SELECT current_elem
                FROM jsonb_array_elements(%s.content->'submodelElements') as current_elem
                UNION ALL
                SELECT child_elem
                FROM search_path
                CROSS JOIN jsonb_array_elements(current_elem->'value') as child_elem
                WHERE current_elem->'value' IS NOT NULL
            )
            SELECT 1 FROM search_path
            """;

    private final List<Object> parameters = new ArrayList<>();

    /**
     * Translates a logical expression to SQL.
     *
     * @param expr the logical expression to translate
     * @param tableName the table name to use in the query
     * @return the translation result containing SQL and parameters
     */
    public TranslationResult translate(LogicalExpression expr, String tableName) {
        parameters.clear();

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
            List<String> sqlParts = new ArrayList<>();
            List<MatchCondition> smeNoPathConditions = new ArrayList<>();

            for (LogicalExpression e: expr.get$and()) {
                if (e == null) {
                    continue;
                }

                String translated = translateExpression(e, tableName);
                if (translated.isEmpty() || translated.equals("TRUE")) {
                    continue;
                }

                if (isSimpleSmeComparison(e)) {
                    MatchCondition cond = extractSmeCondition(e);
                    if (cond != null) {
                        smeNoPathConditions.add(cond);
                        continue;
                    }
                }

                sqlParts.add("(" + translated + ")");
            }

            if (!smeNoPathConditions.isEmpty()) {
                String smeSql = translateSmeMatch(smeNoPathConditions, tableName);
                if (!smeSql.isEmpty() && !smeSql.equals("TRUE")) {
                    sqlParts.add("(" + smeSql + ")");
                }
            }

            if (sqlParts.isEmpty()) {
                return "TRUE";
            }

            return String.join(" AND ", sqlParts);
        }

        if (expr.get$or() != null && !expr.get$or().isEmpty()) {
            return expr.get$or().stream()
                    .map(e -> translateExpression(e, tableName))
                    .filter(c -> !c.isEmpty())
                    .map(c -> "(" + c + ")")
                    .reduce((a, b) -> a + " OR " + b)
                    .orElse("FALSE");
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


    private boolean isSimpleSmeComparison(LogicalExpression expr) {
        if (expr.get$eq() != null && !expr.get$eq().isEmpty()) {
            Value left = expr.get$eq().get(0);
            if (left != null && left.get$field() != null) {
                String field = left.get$field();
                return field.equals("$sme#semanticId") || field.equals("$sme#value") ||
                        field.equals("$sme#idShort") || field.equals("$sme#valueType") ||
                        field.equals("$sme#category") || field.equals("$sme#language");
            }
        }
        if (expr.get$lt() != null && !expr.get$lt().isEmpty()) {
            Value left = expr.get$lt().get(0);
            if (left != null && left.get$field() != null) {
                String field = left.get$field();
                return field.equals("$sme#semanticId") || field.equals("$sme#value") ||
                        field.equals("$sme#idShort") || field.equals("$sme#valueType") ||
                        field.equals("$sme#category") || field.equals("$sme#language");
            }
        }
        if (expr.get$le() != null && !expr.get$le().isEmpty()) {
            Value left = expr.get$le().get(0);
            if (left != null && left.get$field() != null) {
                String field = left.get$field();
                return field.equals("$sme#semanticId") || field.equals("$sme#value") ||
                        field.equals("$sme#idShort") || field.equals("$sme#valueType") ||
                        field.equals("$sme#category") || field.equals("$sme#language");
            }
        }
        if (expr.get$gt() != null && !expr.get$gt().isEmpty()) {
            Value left = expr.get$gt().get(0);
            if (left != null && left.get$field() != null) {
                String field = left.get$field();
                return field.equals("$sme#semanticId") || field.equals("$sme#value") ||
                        field.equals("$sme#idShort") || field.equals("$sme#valueType") ||
                        field.equals("$sme#category") || field.equals("$sme#language");
            }
        }
        if (expr.get$ge() != null && !expr.get$ge().isEmpty()) {
            Value left = expr.get$ge().get(0);
            if (left != null && left.get$field() != null) {
                String field = left.get$field();
                return field.equals("$sme#semanticId") || field.equals("$sme#value") ||
                        field.equals("$sme#idShort") || field.equals("$sme#valueType") ||
                        field.equals("$sme#category") || field.equals("$sme#language");
            }
        }
        if (expr.get$ne() != null && !expr.get$ne().isEmpty()) {
            Value left = expr.get$ne().get(0);
            if (left != null && left.get$field() != null) {
                String field = left.get$field();
                return field.equals("$sme#semanticId") || field.equals("$sme#value") ||
                        field.equals("$sme#idShort") || field.equals("$sme#valueType") ||
                        field.equals("$sme#category") || field.equals("$sme#language");
            }
        }
        return false;
    }


    private MatchCondition extractSmeCondition(LogicalExpression expr) {
        String operator = null;
        List<? extends Value> args = null;

        if (expr.get$eq() != null && !expr.get$eq().isEmpty()) {
            operator = "=";
            args = expr.get$eq();
        }
        else if (expr.get$lt() != null && !expr.get$lt().isEmpty()) {
            operator = "<";
            args = expr.get$lt();
        }
        else if (expr.get$le() != null && !expr.get$le().isEmpty()) {
            operator = "<=";
            args = expr.get$le();
        }
        else if (expr.get$gt() != null && !expr.get$gt().isEmpty()) {
            operator = ">";
            args = expr.get$gt();
        }
        else if (expr.get$ge() != null && !expr.get$ge().isEmpty()) {
            operator = ">=";
            args = expr.get$ge();
        }
        else if (expr.get$ne() != null && !expr.get$ne().isEmpty()) {
            operator = "<>";
            args = expr.get$ne();
        }

        if (args == null || args.size() < 2) {
            return null;
        }

        Value left = args.get(0);
        Value right = args.get(1);

        if (left == null || left.get$field() == null) {
            return null;
        }

        String field = left.get$field();
        String rightValue = evaluateRightValue(right);

        if (field.equals("$sme#semanticId") || field.equals("$sme#value") ||
                field.equals("$sme#idShort") || field.equals("$sme#valueType") ||
                field.equals("$sme#category") || field.equals("$sme#language")) {
            String suffix = field.substring(5);
            return new MatchCondition(suffix, operator, rightValue);
        }

        return null;
    }


    private String translateMatch(List<MatchExpression> matches, String tableName) {
        if (matches == null || matches.isEmpty()) {
            return "TRUE";
        }

        String commonPrefix = null;
        List<MatchCondition> conditions = new ArrayList<>();
        List<MatchExpression> nonArrayMatches = new ArrayList<>();

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
                nonArrayMatches.add(m);
            }
        }

        List<String> sqlParts = new ArrayList<>();

        if (commonPrefix != null) {
            String prefixSql = translateMatchWithPrefix(commonPrefix, conditions, tableName);
            if (!prefixSql.isEmpty() && !prefixSql.equals("TRUE")) {
                sqlParts.add(prefixSql);
            }
        }

        if (!nonArrayMatches.isEmpty()) {
            String nonArraySql = translateNonArrayMatchGrouped(nonArrayMatches, tableName);
            if (!nonArraySql.isEmpty() && !nonArraySql.equals("TRUE")) {
                sqlParts.add(nonArraySql);
            }
        }

        if (sqlParts.isEmpty()) {
            return "TRUE";
        }

        return sqlParts.stream()
                .map(s -> "(" + s + ")")
                .reduce((a, b) -> a + " AND " + b)
                .orElse("TRUE");
    }


    private String translateNonArrayMatchGrouped(List<MatchExpression> matches, String tableName) {
        if (matches == null || matches.isEmpty()) {
            return "TRUE";
        }

        Map<String, List<MatchCondition>> prefixToConditions = new LinkedHashMap<>();

        for (MatchExpression m: matches) {
            MatchOperation mo = getMatchOperation(m);
            if (mo == null) {
                continue;
            }

            Value left = mo.args.get(0);
            Value right = mo.args.get(1);

            if (left.get$field() == null) {
                continue;
            }

            String field = left.get$field();
            String rightValue = evaluateRightValue(right);

            String prefix = null;
            String suffix = "";
            if (field.startsWith("$sme")) {
                if (field.startsWith("$sme#")) {
                    prefix = "$sme";
                    suffix = field.substring(5);
                }
                else if (field.startsWith("$sme.")) {
                    int dotIdx = field.indexOf('.', 5);
                    if (dotIdx == -1) {
                        prefix = "$sme";
                        suffix = field.substring(5);
                    }
                    else {
                        prefix = "$sme";
                        suffix = field.substring(5);
                    }
                }
            }
            else if (field.startsWith("$sm#")) {
                prefix = "$sm#";
                suffix = field.substring(4);
            }
            else if (field.startsWith("$aas#")) {
                prefix = "$aas#";
                suffix = field.substring(5);
            }

            if (prefix != null) {
                prefixToConditions.computeIfAbsent(prefix, k -> new ArrayList<>())
                        .add(new MatchCondition(suffix, mo.operator, rightValue));
            }
        }

        List<String> sqlParts = new ArrayList<>();
        for (Map.Entry<String, List<MatchCondition>> entry: prefixToConditions.entrySet()) {
            String prefix = entry.getKey();
            List<MatchCondition> condList = entry.getValue();

            String sql;
            if (prefix.equals("$sme")) {
                sql = translateSmeMatch(condList, tableName);
            }
            else if (prefix.startsWith("$sme.")) {
                String path = prefix.substring("$sme.".length());
                sql = translateSmePathMatchSimple(path, condList, tableName);
            }
            else if (prefix.equals("$sm#")) {
                sql = translateSmMatch(condList, tableName);
            }
            else if (prefix.equals("$aas#")) {
                sql = translateAasMatch(condList, tableName);
            }
            else {
                continue;
            }

            if (!sql.isEmpty() && !sql.equals("TRUE")) {
                sqlParts.add(sql);
            }
        }

        if (sqlParts.isEmpty()) {
            return "TRUE";
        }

        return sqlParts.stream()
                .map(s -> "(" + s + ")")
                .reduce((a, b) -> a + " AND " + b)
                .orElse("TRUE");
    }


    private String translateAasMatch(List<MatchCondition> conditions, String tableName) {
        List<String> aasConditions = new ArrayList<>();
        for (MatchCondition cond: conditions) {
            String fieldName = cond.suffix.startsWith(".") ? cond.suffix.substring(1) : cond.suffix;
            String aasCondition = translateAasField(fieldName, cond.operator, cond.value, tableName);
            if (!aasCondition.isEmpty()) {
                aasConditions.add(aasCondition);
            }
        }

        if (aasConditions.isEmpty()) {
            return "TRUE";
        }

        return String.join(" AND ", aasConditions);
    }


    private String translateMatchWithPrefix(String prefix, List<MatchCondition> conditions, String tableName) {
        String effectivePrefix = prefix;
        if (effectivePrefix.endsWith("[]")) {
            effectivePrefix = effectivePrefix.substring(0, effectivePrefix.length() - 2);
        }

        switch (effectivePrefix) {
            case "$aas#assetInformation.specificAssetIds":
                return translateArrayMatch(
                        tableName,
                        "content->'assetInformation'->'specificAssetIds'",
                        conditions,
                        new String[] {
                                "name",
                                "value"
                        });

            case "$aas#extensions", "$sm#extensions":
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
                if (effectivePrefix.startsWith("$sme.")) {
                    String path = effectivePrefix.substring("$sme.".length());
                    return translateSmePathMatchSimple(path, conditions, tableName);
                }
                if (effectivePrefix.equals("$sm#")) {
                    return translateSmMatch(conditions, tableName);
                }
                LOGGER.warn("Unsupported match prefix: {}", effectivePrefix);
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
            String itemCondition = "item" + jsonFieldPath + " " + cond.operator + " '" + escapeString(cond.value) + "'";
            itemConditions.add(itemCondition);
        }

        sql.append(String.join(" AND ", itemConditions));
        sql.append(")");

        return sql.toString();
    }


    private String mapFieldNameToJsonPath(String fieldName, String[] mappings) {
        for (String mapping: mappings) {
            if (fieldName.equals(mapping)) {
                return "->>'" + mapping + "'";
            }
        }
        return "->>'" + fieldName + "'";
    }


    private String translateSmeMatch(List<MatchCondition> conditions, String tableName) {
        if (conditions.size() == 1) {
            // Use recursive CTE to handle arbitrary nesting depth
            MatchCondition cond = conditions.get(0);
            String fieldName = cond.suffix.startsWith(".") ? cond.suffix.substring(1) : cond.suffix;
            String jsonCondition = String.format("{\"%s\": \"%s\"}", escapeJsonPathString(fieldName), escapeJsonPathString(cond.value));

            return String.format(
                    "EXISTS (" + RECURSIVE_CTE_TEMPLATE +
                            "WHERE current_elem @> '%s')",
                    tableName, jsonCondition);
        }
        else {
            // For multiple conditions, use simple jsonb_array_elements
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
    }


    private String translateSmMatch(List<MatchCondition> conditions, String tableName) {
        List<String> smConditions = new ArrayList<>();
        for (MatchCondition cond: conditions) {
            String fieldName = cond.suffix.startsWith(".") ? cond.suffix.substring(1) : cond.suffix;
            String smCondition = translateSmField(fieldName, cond.operator, cond.value, tableName);
            if (!smCondition.isEmpty()) {
                smConditions.add(smCondition);
            }
        }

        if (smConditions.isEmpty()) {
            return "TRUE";
        }

        return String.join(" AND ", smConditions);
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
                if (fieldName.indexOf('#') != -1) {
                    return translateNestedSmeJsonConditionSimple(fieldName, operator, value);
                }
                LOGGER.warn("Unsupported SME JSON field: {}", fieldName);
                return "TRUE";
        }
    }


    private String translateNestedSmeJsonConditionSimple(String fieldName, String operator, String value) {
        LOGGER.debug("translateNestedSmeJsonConditionSimple called with: {}", fieldName);
        int hashIndex = fieldName.indexOf('#');
        String actualField = fieldName.substring(hashIndex + 1);
        String path = fieldName.substring(0, hashIndex);
        LOGGER.debug("  path = {}, actualField = {}", path, actualField);

        String jsonOp = operator.equals("=") ? "==" : operator;

        StringBuilder jsonPath = new StringBuilder("$.submodelElements[*]");

        for (String cleanPart: cleanPathParts(path)) {
            jsonPath.append(".value[*] ? (@.idShort == \"").append(escapeJsonPathString(cleanPart)).append("\")");
        }

        jsonPath.append(" ? (@.").append(escapeJsonPathString(actualField)).append(" ").append(jsonOp).append(" \"").append(escapeJsonPathString(value)).append("\")");

        String result = "jsonb_path_exists(submodels.content, '" + jsonPath.toString() + "')";
        LOGGER.debug("  result = {}", result);
        return result;
    }


    private List<String> cleanPathParts(String path) {
        List<String> cleanPathParts = new ArrayList<>();
        for (String part: path.split("\\.")) {
            if (part.endsWith("[]")) {
                cleanPathParts.add(part.substring(0, part.length() - 2));
            }
            else {
                cleanPathParts.add(part);
            }
        }
        LOGGER.debug("  cleanPathParts = {}", cleanPathParts);
        return cleanPathParts;
    }


    private String translateSmePathMatchSimple(String path, List<MatchCondition> conditions, String tableName) {
        LOGGER.debug("translateSmePathMatchSimple called with path: {}, tableName: {}", path, tableName);
        List<String> cleanPathParts = cleanPathParts(path);
        StringBuilder jsonPath = new StringBuilder("$.submodelElements[*] ? (@.idShort == \"").append(escapeJsonPathString(cleanPathParts.get(0))).append("\")");
        for (int i = 1; i < cleanPathParts.size(); i++) {
            jsonPath.append(".value[*] ? (@.idShort == \"").append(escapeJsonPathString(cleanPathParts.get(i))).append("\")");
        }

        for (MatchCondition cond: conditions) {
            String jsonOp = cond.operator.equals("=") ? "==" : cond.operator;
            String fieldName = cond.suffix.startsWith(".") ? cond.suffix.substring(1) : cond.suffix;
            int hashIdx = fieldName.indexOf('#');
            if (hashIdx != -1) {
                String actualField = fieldName.substring(hashIdx + 1);
                String condPath = fieldName.substring(0, hashIdx);

                List<String> cleanCondPathParts = new ArrayList<>();
                for (String part: condPath.split("\\.")) {
                    if (part.endsWith("[]")) {
                        cleanCondPathParts.add(part.substring(0, part.length() - 2));
                    }
                    else {
                        cleanCondPathParts.add(part);
                    }
                }

                for (String cleanPart: cleanCondPathParts) {
                    jsonPath.append(".value[*] ? (@.idShort == \"").append(escapeJsonPathString(cleanPart)).append("\")");
                }
                jsonPath.append(" ? (@.").append(escapeJsonPathString(actualField)).append(" ").append(jsonOp).append(" \"").append(escapeJsonPathString(cond.value)).append("\")");
            }
            else {
                jsonPath.append(" ? (@.").append(escapeJsonPathString(fieldName)).append(" ").append(jsonOp).append(" \"").append(escapeJsonPathString(cond.value)).append("\")");
            }
        }

        String result = "jsonb_path_exists(" + tableName + ".content, '" + jsonPath.toString() + "')";
        LOGGER.debug("  result = {}", result);
        return result;
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
            String attr = field.startsWith(PREFIX_SME + "#")
                    ? field.substring((PREFIX_SME + "#").length())
                    : field.substring(PREFIX_SME.length() + 1);
            return translateSmeDirectField(attr, operator, value, tableName);
        }

        LOGGER.warn("Unsupported field: {}", field);
        return "";
    }


    private String translateSmeDirectField(String fieldName, String operator, String value, String tableName) {
        LOGGER.debug("translateSmeDirectField called with: {}, operator: {}, value: {}, tableName: {}", fieldName, operator, value, tableName);
        StringBuilder sql = new StringBuilder();
        sql.append("EXISTS (SELECT 1 FROM jsonb_array_elements(").append(tableName).append(".content->'submodelElements') AS elem WHERE ");

        int hashIndex = fieldName.indexOf('#');
        String actualField;
        String path;

        if (hashIndex != -1) {
            path = fieldName.substring(0, hashIndex);
            actualField = fieldName.substring(hashIndex + 1);
        }
        else {
            path = "";
            actualField = fieldName;
        }
        LOGGER.debug("  path = {}, actualField = {}", path, actualField);

        if (!path.isEmpty()) {
            List<String> cleanPathParts = cleanPathParts(path);

            String jsonOp = operator.equals("=") ? "==" : operator;

            StringBuilder jsonPath = new StringBuilder("$.submodelElements[*] ? (@.idShort == \"").append(escapeJsonPathString(cleanPathParts.get(0))).append("\")");
            for (int i = 1; i < cleanPathParts.size(); i++) {
                jsonPath.append(".value[*] ? (@.idShort == \"").append(escapeJsonPathString(cleanPathParts.get(i))).append("\")");
            }
            jsonPath.append(" ? (@.").append(escapeJsonPathString(actualField)).append(" ").append(jsonOp).append(" \"").append(escapeJsonPathString(value)).append("\")");

            sql.append("jsonb_path_exists(").append(tableName).append(".content, '").append(jsonPath).append("')");
            LOGGER.debug("  jsonPath = {}", jsonPath);
        }
        else {
            sql.append("elem->>'").append(escapeString(actualField)).append("' ").append(operator).append(" '").append(escapeString(value)).append("'");
        }

        sql.append(")");

        String result = sql.toString();
        LOGGER.debug("  result = {}", result);
        return result;
    }


    private String translateAasField(String attr, String operator, String value, String tableName) {
        String jsonPath = aasAttrToJsonPath(attr);
        return tableName + "." + jsonPath + " " + operator + " '" + escapeString(value) + "'";
    }


    private String aasAttrToJsonPath(String attr) {
        switch (attr) {
            case "idShort" -> {
                return "content->>'idShort'";
            }
            case "id" -> {
                return "content->>'id'";
            }
            case "assetInformation.assetKind" -> {
                return "content->'assetInformation'->>'assetKind'";
            }
            case "assetInformation.assetType" -> {
                return "content->'assetInformation'->>'assetType'";
            }
            case "assetInformation.globalAssetId" -> {
                return "content->'assetInformation'->>'globalAssetId'";
            }
            case "description" -> {
                return "content->'description'->0->>'text'";
            }
            case "displayName" -> {
                return "content->'displayName'->0->>'text'";
            }
            case "administration.version" -> {
                return "content->'administration'->>'version'";
            }
            case "administration.revision" -> {
                return "content->'administration'->>'revision'";
            }
        }

        LOGGER.warn("Unsupported AAS attribute: {}", attr);
        return "content";
    }


    private String translateSmField(String attr, String operator, String value, String tableName) {
        String jsonPath = smAttrToJsonPath(attr);
        return tableName + "." + jsonPath + " " + operator + " '" + escapeString(value) + "'";
    }


    private String smAttrToJsonPath(String attr) {
        switch (attr) {
            case "idShort" -> {
                return "content->>'idShort'";
            }
            case "id" -> {
                return "content->>'id'";
            }
            case "semanticId" -> {
                return "content->'semanticId'->'keys'->0->>'value'";
            }
            case "semanticId.type" -> {
                return "content->'semanticId'->'keys'->0->>'type'";
            }
            case "kind" -> {
                return "content->>'kind'";
            }
            case "description" -> {
                return "content->'description'->0->>'text'";
            }
            case "displayName" -> {
                return "content->'displayName'->0->>'text'";
            }
            case "administration.version" -> {
                return "content->'administration'->>'version'";
            }
            case "administration.revision" -> {
                return "content->'administration'->>'revision'";
            }
        }

        LOGGER.warn("Unsupported SM attribute: {}", attr);
        return "content";
    }


    private String translateCdField(String attr, String operator, String value, String tableName) {
        String jsonPath = cdAttrToJsonPath(attr);
        return tableName + "." + jsonPath + " " + operator + " '" + escapeString(value) + "'";
    }


    private String cdAttrToJsonPath(String attr) {
        switch (attr) {
            case "idShort" -> {
                return "content->>'idShort'";
            }
            case "id" -> {
                return "content->>'id'";
            }
            case "description" -> {
                return "content->'description'->0->>'text'";
            }
            case "displayName" -> {
                return "content->'displayName'->0->>'text'";
            }
        }

        LOGGER.warn("Unsupported CD attribute: {}", attr);
        return "content";
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
                Value::get$field,
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
            return v.get$timeVal();
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


    private String escapeJsonPathString(String s) {
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

    /**
     * Result of translating a query expression to SQL.
     */
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
