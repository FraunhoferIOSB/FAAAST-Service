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
package de.fraunhofer.iosb.ilt.faaast.service.query;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.StringValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Evaluates queries sent to /query endpoints.
 */
public class QueryEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryEvaluator.class);
    private static final String PREFIX_AAS = "$aas#";
    private static final String PREFIX_SM = "$sm#";
    private static final String PREFIX_SME = "$sme";
    private static final String PREFIX_CD = "$cd#";

    private enum ComparisonOperator {
        EQ,
        NE,
        GT,
        GE,
        LT,
        LE,
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH,
        REGEX;

        boolean isStringOperator() {
            return this == CONTAINS || this == STARTS_WITH || this == ENDS_WITH || this == REGEX;
        }
    }

    private enum ValueKind {
        NONE,
        FIELD,
        STR,
        NUM,
        HEX,
        DATETIME,
        TIME,
        BOOL,
        STR_CAST,
        NUM_CAST
    }

    private enum StringValueKind {
        NONE,
        FIELD,
        STR,
        STR_CAST
    }

    private static final List<ValueKindCheck> VALUE_KIND_CHECKS = Arrays.asList(
            new ValueKindCheck(ValueKind.FIELD, v -> v.get$field() != null),
            new ValueKindCheck(ValueKind.STR, v -> v.get$strVal() != null),
            new ValueKindCheck(ValueKind.NUM, v -> v.get$numVal() != null),
            new ValueKindCheck(ValueKind.HEX, v -> v.get$hexVal() != null),
            new ValueKindCheck(ValueKind.DATETIME, v -> v.get$dateTimeVal() != null),
            new ValueKindCheck(ValueKind.TIME, v -> v.get$timeVal() != null),
            new ValueKindCheck(ValueKind.BOOL, v -> v.get$boolean() != null),
            new ValueKindCheck(ValueKind.STR_CAST, v -> v.get$strCast() != null),
            new ValueKindCheck(ValueKind.NUM_CAST, v -> v.get$numCast() != null));

    private static final List<StringValueKindCheck> STRING_VALUE_KIND_CHECKS = Arrays.asList(
            new StringValueKindCheck(StringValueKind.FIELD, v -> v.get$field() != null),
            new StringValueKindCheck(StringValueKind.STR, v -> v.get$strVal() != null),
            new StringValueKindCheck(StringValueKind.STR_CAST, v -> v.get$strCast() != null));

    /**
     * Used to decide whether to filter out the Identifiable.
     *
     * @param expr logical expression (tree)
     * @param identifiable AAS | Submodel | ConceptDescription
     * @return true if expression matches the identifiable
     * 
     */
    public boolean matches(LogicalExpression expr, Identifiable identifiable) {
        if (expr == null || identifiable == null) {
            return false;
        }

        Boolean directBoolean = evaluateBooleanLiteral(expr);
        if (directBoolean != null) {
            return directBoolean;
        }

        Boolean logical = evaluateLogicalExpression(expr, identifiable);
        if (logical != null) {
            return logical;
        }

        if (hasMatchExpression(expr)) {
            return evaluateMatch(expr.get$match(), identifiable);
        }

        if (evaluateFirstValueOperator(expr, identifiable)) {
            return true;
        }

        return evaluateFirstStringOperator(expr, identifiable);
    }


    private Boolean evaluateBooleanLiteral(LogicalExpression expr) {
        return expr.get$boolean();
    }


    private Boolean evaluateLogicalExpression(LogicalExpression expr, Identifiable identifiable) {
        if (expr.get$and() != null && !expr.get$and().isEmpty()) {
            return expr.get$and().stream().allMatch(e -> matches(e, identifiable));
        }
        if (expr.get$or() != null && !expr.get$or().isEmpty()) {
            return expr.get$or().stream().anyMatch(e -> matches(e, identifiable));
        }
        if (expr.get$not() != null) {
            return !matches(expr.get$not(), identifiable);
        }
        return null;
    }


    private boolean hasMatchExpression(LogicalExpression expr) {
        return expr.get$match() != null && !expr.get$match().isEmpty();
    }


    private boolean evaluateFirstValueOperator(LogicalExpression expr, Identifiable identifiable) {
        List<OperationSpec<Value>> operations = Arrays.asList(
                new OperationSpec<>(ComparisonOperator.EQ, expr::get$eq),
                new OperationSpec<>(ComparisonOperator.NE, expr::get$ne),
                new OperationSpec<>(ComparisonOperator.GT, expr::get$gt),
                new OperationSpec<>(ComparisonOperator.GE, expr::get$ge),
                new OperationSpec<>(ComparisonOperator.LT, expr::get$lt),
                new OperationSpec<>(ComparisonOperator.LE, expr::get$le));
        for (OperationSpec<Value> spec: operations) {
            List<Value> args = spec.argumentProvider.get();
            if (args != null && !args.isEmpty()) {
                return evaluateBinaryComparison(args, identifiable, spec.operator);
            }
        }
        return false;
    }


    private boolean evaluateFirstStringOperator(LogicalExpression expr, Identifiable identifiable) {
        List<OperationSpec<StringValue>> operations = Arrays.asList(
                new OperationSpec<>(ComparisonOperator.CONTAINS, expr::get$contains),
                new OperationSpec<>(ComparisonOperator.STARTS_WITH, expr::get$startsWith),
                new OperationSpec<>(ComparisonOperator.ENDS_WITH, expr::get$endsWith),
                new OperationSpec<>(ComparisonOperator.REGEX, expr::get$regex));
        for (OperationSpec<StringValue> spec: operations) {
            List<StringValue> args = spec.argumentProvider.get();
            if (args != null && !args.isEmpty()) {
                return evaluateBinaryStringOperator(args, identifiable, spec.operator);
            }
        }
        return false;
    }

    /**
     * @param argumentProvider provides arguments for this operator
     */
    private record OperationSpec<T>(ComparisonOperator operator, Supplier<List<T>> argumentProvider) {}

    private boolean evaluateBinaryComparison(List<Value> args, Identifiable identifiable, ComparisonOperator operator) {
        if (args.size() < 2) {
            LOGGER.error("Operator {} requires two arguments", operator);
            return false;
        }
        List<Object> left = evaluateValue(args.get(0), identifiable);
        List<Object> right = evaluateValue(args.get(1), identifiable);
        return anyPairSatisfies(left, right, operator);
    }


    private boolean evaluateBinaryStringOperator(List<StringValue> args, Identifiable identifiable, ComparisonOperator operator) {
        if (args.size() < 2) {
            LOGGER.error("String operator {} requires two arguments", operator);
            return false;
        }
        List<Object> left = evaluateStringValue(args.get(0), identifiable);
        List<Object> right = evaluateStringValue(args.get(1), identifiable);
        return anyPairSatisfies(left, right, operator);
    }


    private boolean anyPairSatisfies(List<Object> left, List<Object> right, ComparisonOperator operator) {
        if (left == null || right == null) {
            return false;
        }
        for (Object l: left) {
            for (Object r: right) {
                if (compareValues(l, r, operator)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param predicate checks if a value kind applies
     */
    private record ValueKindCheck(ValueKind kind, Predicate<Value> predicate) {}

    /**
     * @param predicate checks if a string value kind applies
     */
    private record StringValueKindCheck(StringValueKind kind, Predicate<StringValue> predicate) {}

    private ValueKind determineValueKind(Value v) {
        if (v == null) {
            return ValueKind.NONE;
        }
        for (ValueKindCheck check: VALUE_KIND_CHECKS) {
            if (check.predicate.test(v)) {
                return check.kind;
            }
        }
        return ValueKind.NONE;
    }


    private StringValueKind determineStringValueKind(StringValue sv) {
        if (sv == null) {
            return StringValueKind.NONE;
        }
        for (StringValueKindCheck check: STRING_VALUE_KIND_CHECKS) {
            if (check.predicate.test(sv)) {
                return check.kind;
            }
        }
        return StringValueKind.NONE;
    }


    private List<Object> evaluateValue(Value v, Identifiable identifiable) {
        return switch (determineValueKind(v)) {
            case FIELD -> nonNull(getFieldValues(v.get$field(), identifiable));
            case STR -> Collections.singletonList(v.get$strVal());
            case NUM -> Collections.singletonList(v.get$numVal());
            case HEX -> Collections.singletonList(v.get$hexVal());
            case DATETIME -> Collections.singletonList(v.get$dateTimeVal());
            case TIME -> Collections.singletonList(v.get$timeVal());
            case BOOL -> Collections.singletonList(v.get$boolean());
            case STR_CAST -> evaluateValue(v.get$strCast(), identifiable).stream()
                    .map(String::valueOf).collect(Collectors.toList());
            case NUM_CAST -> evaluateValue(v.get$numCast(), identifiable).stream()
                    .map(String::valueOf)
                    .map(this::parseDoubleOrNull)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            default -> Collections.emptyList();
        };
    }


    private List<Object> evaluateStringValue(StringValue sv, Identifiable identifiable) {
        return switch (determineStringValueKind(sv)) {
            case FIELD -> nonNull(getFieldValues(sv.get$field(), identifiable));
            case STR -> Collections.singletonList(sv.get$strVal());
            case STR_CAST -> evaluateValue(sv.get$strCast(), identifiable).stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
            default -> {
                LOGGER.error("Invalid string value: {}", sv);
                yield Collections.emptyList();
            }
        };
    }


    private Double parseDoubleOrNull(String s) {
        try {
            return Double.valueOf(s);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }


    private Double toDouble(Object o) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        return parseDoubleOrNull(String.valueOf(o));
    }

    /**
     * @param suffix e.g., ".name", "Sub.Path#value"
     */
    private record Condition(String suffix, ComparisonOperator operator, List<Object> rightVals) {
        private Condition(String suffix, ComparisonOperator operator, List<Object> rightVals) {
            this.suffix = suffix;
            this.operator = operator;
            this.rightVals = rightVals != null ? rightVals : Collections.emptyList();
        }
    }

    private record MatchOperation(ComparisonOperator operator, List<Value> args) {}

    private record MatchEvaluationContext(String commonPrefix, List<Condition> itemConditions, boolean directMismatch) {}

    private boolean evaluateMatch(List<MatchExpression> matches, Identifiable identifiable) {
        if (matches == null || matches.isEmpty()) {
            return true;
        }
        MatchEvaluationContext ctx = buildMatchEvaluationContext(matches, identifiable);
        if (ctx.directMismatch) {
            return false;
        }
        if (ctx.commonPrefix == null) {
            return true;
        }
        return evaluateListMatch(ctx.commonPrefix, ctx.itemConditions, identifiable);
    }


    private MatchEvaluationContext buildMatchEvaluationContext(List<MatchExpression> matches, Identifiable identifiable) {
        String commonPrefix = null;
        List<Condition> itemConditions = new ArrayList<>();
        boolean directMismatch = false;

        for (MatchExpression m: matches) {
            MatchOperation mo = getMatchOperation(m);
            if (mo == null) {
                LOGGER.error("Unsupported operator in match");
                directMismatch = true;
                break;
            }
            if (mo.args.size() < 2) {
                LOGGER.error("$match operator {} requires two arguments", mo.operator);
                directMismatch = true;
                break;
            }

            Value left = mo.args.get(0);
            Value right = mo.args.get(1);
            if (left.get$field() == null) {
                LOGGER.error("Left side in $match must be a field: {}", left);
                directMismatch = true;
                break;
            }

            String field = left.get$field();
            List<Object> rightVals = evaluateValue(right, identifiable);

            int listMarker = field.indexOf("[]");
            if (listMarker == -1) {
                if (field.startsWith(PREFIX_SME + "#")) {
                    String prefix = PREFIX_SME;
                    if (commonPrefix != null && !commonPrefix.equals(prefix)) {
                        LOGGER.error("Non-common prefix in match: {} vs {}", commonPrefix, prefix);
                        directMismatch = true;
                        break;
                    }
                    commonPrefix = prefix;
                    String suffix = field.substring((PREFIX_SME + "#").length());
                    itemConditions.add(new Condition(suffix, mo.operator, rightVals));
                }
                else {
                    // evaluate parent condition immediately
                    List<Object> leftVals = evaluateValue(left, identifiable);
                    if (!anyPairSatisfies(leftVals, rightVals, mo.operator)) {
                        directMismatch = true;
                        break;
                    }
                }
            }
            else {
                String prefix = field.substring(0, listMarker);
                if (commonPrefix != null && !commonPrefix.equals(prefix)) {
                    LOGGER.error("Non-common prefix in match: {} vs {}", commonPrefix, prefix);
                    directMismatch = true;
                    break;
                }
                commonPrefix = prefix;
                String suffix = field.substring(listMarker + 2);
                itemConditions.add(new Condition(suffix, mo.operator, rightVals));
            }
        }

        return new MatchEvaluationContext(commonPrefix, itemConditions, directMismatch);
    }


    private boolean evaluateListMatch(String commonPrefix, List<Condition> itemConditions, Identifiable identifiable) {
        switch (commonPrefix) {
            case "$aas#assetInformation.specificAssetIds":
                if (!(identifiable instanceof AssetAdministrationShell aas))
                    return false;
                if (aas.getAssetInformation() == null || aas.getAssetInformation().getSpecificAssetIds() == null)
                    return false;

                for (SpecificAssetId item: aas.getAssetInformation().getSpecificAssetIds()) {
                    if (doAllItemConditionsMatch(itemConditions, cond -> {
                        String s = getSpecificAssetIdAttribute(item, cond.suffix);
                        return s == null ? Collections.emptyList() : Collections.singletonList(s);
                    })) {
                        return true;
                    }
                }
                return false;

            case PREFIX_SME:
                if (!(identifiable instanceof Submodel sm))
                    return false;
                List<SubmodelElement> topLevel = sm.getSubmodelElements();
                if (topLevel == null)
                    return false;

                for (SubmodelElement item: topLevel) {
                    if (doAllItemConditionsMatch(itemConditions, cond -> getPropertyValuesFromSuffix(item, cond.suffix))) {
                        return true;
                    }
                }
                return false;

            default:
                if (commonPrefix.startsWith(PREFIX_SME + ".")) {
                    if (!(identifiable instanceof Submodel sm2))
                        return false;
                    String path = commonPrefix.substring((PREFIX_SME + ".").length());
                    SubmodelElement listElem = getSubmodelElementByPath(sm2, path);
                    if (!(listElem instanceof SubmodelElementList))
                        return false;

                    List<SubmodelElement> items = ((SubmodelElementList) listElem).getValue();
                    if (items == null)
                        return false;

                    for (SubmodelElement item: items) {
                        if (doAllItemConditionsMatch(itemConditions, cond -> getPropertyValuesFromSuffix(item, cond.suffix))) {
                            return true;
                        }
                    }
                    return false;
                }
                LOGGER.error("Unsupported prefix for $match: {}", commonPrefix);
                return false;
        }
    }


    private MatchOperation getMatchOperation(MatchExpression m) {
        List<MatchOperation> candidates = Arrays.asList(
                new MatchOperation(ComparisonOperator.EQ, m.get$eq()),
                new MatchOperation(ComparisonOperator.NE, m.get$ne()),
                new MatchOperation(ComparisonOperator.GT, m.get$gt()),
                new MatchOperation(ComparisonOperator.GE, m.get$ge()),
                new MatchOperation(ComparisonOperator.LT, m.get$lt()),
                new MatchOperation(ComparisonOperator.LE, m.get$le()));
        for (MatchOperation mo: candidates) {
            if (mo.args != null && !mo.args.isEmpty()) {
                return mo;
            }
        }
        return null;
    }


    private boolean doAllItemConditionsMatch(List<Condition> conditions,
                                             java.util.function.Function<Condition, List<Object>> leftValueExtractor) {
        for (Condition cond: conditions) {
            List<Object> leftVals = nonNull(leftValueExtractor.apply(cond));
            if (!anyPairSatisfies(leftVals, cond.rightVals, cond.operator)) {
                return false;
            }
        }
        return true;
    }


    private List<Object> getFieldValues(String field, Identifiable identifiable) {
        if (field == null || identifiable == null) {
            return Collections.emptyList();
        }

        List<Object> values = resolveValuesForPrefix(field, PREFIX_AAS, identifiable, AssetAdministrationShell.class,
                (aas, attr) -> getAasFieldValues(aas, attr));
        if (values != null) {
            return values;
        }

        values = resolveValuesForPrefix(field, PREFIX_SM, identifiable, Submodel.class,
                (sm, attr) -> new ArrayList<>(getSubmodelAttributeValues(sm, attr)));
        if (values != null) {
            return values;
        }

        values = resolveSmeFieldValues(field, identifiable);
        if (values != null) {
            return values;
        }

        values = resolveValuesForPrefix(field, PREFIX_CD, identifiable, ConceptDescription.class,
                (cd, attr) -> getConceptDescriptionValues(cd, attr));
        if (values != null) {
            return values;
        }

        LOGGER.error("Unsupported field: {}", field);
        return Collections.emptyList();
    }


    private <T extends Identifiable> List<Object> resolveValuesForPrefix(String field,
                                                                         String prefix,
                                                                         Identifiable identifiable,
                                                                         Class<T> type,
                                                                         BiFunction<T, String, List<Object>> extractor) {
        if (!field.startsWith(prefix)) {
            return null;
        }
        if (!type.isInstance(identifiable)) {
            return Collections.emptyList();
        }
        return extractor.apply(type.cast(identifiable), field.substring(prefix.length()));
    }


    private List<Object> resolveSmeFieldValues(String field, Identifiable identifiable) {
        if (!field.startsWith(PREFIX_SME)) {
            return null;
        }
        if (!(identifiable instanceof Submodel sm)) {
            return Collections.emptyList();
        }

        String pathPart = "";
        String attr;
        if (field.contains(".")) {
            int hashPos = field.indexOf("#", field.indexOf("."));
            pathPart = field.substring(field.indexOf(".") + 1, hashPos);
            attr = field.substring(hashPos + 1);
        }
        else {
            attr = field.substring((PREFIX_SME + "#").length());
        }

        List<Object> values = new ArrayList<>();
        if (!pathPart.isEmpty()) {
            SubmodelElement sme = getSubmodelElementByPath(sm, pathPart);
            if (sme != null) {
                values.addAll(getSubmodelElementAttributeValues(sme, attr));
            }
            return values;
        }

        List<SubmodelElement> smes = sm.getSubmodelElements();
        if (smes != null) {
            for (SubmodelElement sme: smes) {
                values.addAll(getSubmodelElementAttributeValues(sme, attr));
            }
        }
        return values;
    }


    private List<Object> getConceptDescriptionValues(ConceptDescription cd, String attr) {
        return switch (attr) {
            case "idShort" -> Collections.singletonList(cd.getIdShort());
            case "id" -> Collections.singletonList(cd.getId());
            default -> {
                LOGGER.error("Unsupported CD attribute: {}", attr);
                yield Collections.emptyList();
            }
        };
    }


    private List<Object> getAasFieldValues(AssetAdministrationShell aas, String attr) {
        switch (attr) {
            case "idShort":
                return Collections.singletonList(aas.getIdShort());
            case "id":
                return Collections.singletonList(aas.getId());
            case "assetInformation.assetKind":
                return (aas.getAssetInformation() == null || aas.getAssetInformation().getAssetKind() == null)
                        ? Collections.emptyList()
                        : Collections.singletonList(aas.getAssetInformation().getAssetKind().name());
            case "assetInformation.assetType":
                return (aas.getAssetInformation() == null)
                        ? Collections.emptyList()
                        : Collections.singletonList(aas.getAssetInformation().getAssetType());
            case "assetInformation.globalAssetId":
                if (aas.getAssetInformation() == null)
                    return Collections.emptyList();
                String globalAssetId = aas.getAssetInformation().getGlobalAssetId();
                return globalAssetId == null ? Collections.emptyList() : Collections.singletonList(globalAssetId);
            default:
                if (attr.startsWith("assetInformation.specificAssetIds")) {
                    if (aas.getAssetInformation() == null || aas.getAssetInformation().getSpecificAssetIds() == null) {
                        return Collections.emptyList();
                    }
                    String remaining = attr.substring("assetInformation.specificAssetIds".length());
                    IndexSelection indexSelection = parseIndexSelection(remaining);
                    List<SpecificAssetId> sais = aas.getAssetInformation().getSpecificAssetIds();
                    List<SpecificAssetId> selectedItems = selectByIndex(sais, indexSelection);
                    List<Object> values = new ArrayList<>();
                    for (SpecificAssetId sai: selectedItems) {
                        values.add(getSpecificAssetIdAttribute(sai, indexSelection.remainingSuffix));
                    }
                    return values;
                }
                LOGGER.error("Unsupported AAS attribute: {}", attr);
                return Collections.emptyList();
        }
    }


    private List<String> getSubmodelAttributeValues(Submodel sm, String attr) {
        switch (attr) {
            case "idShort":
                return Collections.singletonList(sm.getIdShort());
            case "id":
                return Collections.singletonList(sm.getId());
            case "semanticId": {
                Reference ref = sm.getSemanticId();
                if (ref == null || ref.getKeys() == null || ref.getKeys().isEmpty())
                    return Collections.emptyList();
                return Collections.singletonList(ref.getKeys().get(0).getValue());
            }
            default:
                if (attr.startsWith("semanticId.keys")) {
                    Reference ref = sm.getSemanticId();
                    if (ref == null || ref.getKeys() == null)
                        return Collections.emptyList();

                    String remaining = attr.substring("semanticId.keys".length());
                    IndexSelection indexSelection = parseIndexSelection(remaining);

                    List<Key> selectedItems = selectByIndex(ref.getKeys(), indexSelection);
                    return extractKeyAttributeValues(selectedItems, indexSelection);
                }
                LOGGER.error("Unsupported SM attribute: {}", attr);
                return Collections.emptyList();
        }
    }


    private List<String> getSubmodelElementAttributeValues(SubmodelElement sme, String attr) {
        if (sme == null || attr == null)
            return Collections.emptyList();

        switch (attr) {
            case "idShort":
                return Collections.singletonList(sme.getIdShort());
            case "value":
                if (sme instanceof Property) {
                    return Collections.singletonList(((Property) sme).getValue());
                }
                return Collections.emptyList();
            case "valueType":
                if (sme instanceof Property && ((Property) sme).getValueType() != null) {
                    return Collections.singletonList(((Property) sme).getValueType().name());
                }
                return Collections.emptyList();
            case "language":
                if (sme instanceof MultiLanguageProperty) {
                    List<LangStringTextType> values = ((MultiLanguageProperty) sme).getValue();
                    if (values == null)
                        return Collections.emptyList();
                    return values.stream()
                            .filter(Objects::nonNull)
                            .map(LangStringTextType::getLanguage)
                            .collect(Collectors.toList());
                }
                return Collections.emptyList();
            case "semanticId": {
                Reference ref = sme.getSemanticId();
                if (ref == null || ref.getKeys() == null || ref.getKeys().isEmpty())
                    return Collections.emptyList();
                return Collections.singletonList(ref.getKeys().get(0).getValue());
            }
            default:
                if (attr.startsWith("semanticId.keys")) {
                    Reference ref = sme.getSemanticId();
                    if (ref == null || ref.getKeys() == null)
                        return Collections.emptyList();

                    String remaining = attr.substring("semanticId.keys".length());
                    IndexSelection indexSelection = parseIndexSelection(remaining);

                    List<Key> selectedItems = selectByIndex(ref.getKeys(), indexSelection);
                    return extractKeyAttributeValues(selectedItems, indexSelection);
                }
                LOGGER.error("Unsupported SME attribute: {}", attr);
                return Collections.emptyList();
        }
    }


    private static List<String> extractKeyAttributeValues(List<Key> selectedItems, IndexSelection selector) {
        List<String> results = new ArrayList<>();
        for (Key key: selectedItems) {
            switch (selector.remainingSuffix) {
                case ".type":
                    results.add(key.getType().name());
                    break;
                case ".value":
                    results.add(key.getValue());
                    break;
                default:
                    break;
            }
        }
        return results;
    }


    /**
     * Resolve a submodel element by dot-separated path.
     */
    private SubmodelElement getSubmodelElementByPath(Submodel sm, String path) {
        if (sm == null || path == null || path.isEmpty())
            return null;

        String[] tokens = path.split("\\.");
        SubmodelElement current = null;

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (i == 0) {
                current = findByIdShort(sm.getSubmodelElements(), token);
            }
            else {
                if (current instanceof SubmodelElementCollection) {
                    current = findByIdShort(((SubmodelElementCollection) current).getValue(), token);
                }
                else if (current instanceof SubmodelElementList) {
                    current = findByIdShort(((SubmodelElementList) current).getValue(), token);
                }
                else {
                    return null;
                }
            }
            if (current == null)
                return null;
        }
        return current;
    }


    private SubmodelElement findByIdShort(List<SubmodelElement> elements, String idShort) {
        if (elements == null || idShort == null)
            return null;
        return elements.stream().filter(e -> idShort.equals(e.getIdShort())).findFirst().orElse(null);
    }


    private String getSpecificAssetIdAttribute(Object item, String path) {
        if (!(item instanceof SpecificAssetId sai) || path == null) {
            LOGGER.error("Unsupported property {} for object {}", path, item);
            return null;
        }
        switch (path) {
            case ".name":
                return sai.getName();
            case ".value":
                return sai.getValue();
            default:
                if (path.startsWith(".externalSubjectId") && sai.getExternalSubjectId() != null) {
                    return String.valueOf(sai.getExternalSubjectId());
                }
        }
        LOGGER.error("Unsupported property: {}", path);
        return null;
    }


    private List<Object> getPropertyValuesFromSuffix(SubmodelElement item, String suffix) {
        if (item == null || suffix == null)
            return Collections.emptyList();

        String normalized = suffix.startsWith(".") ? suffix.substring(1) : suffix;
        String subPath = "";
        String attr = normalized;
        int hashPos = normalized.indexOf('#');
        if (hashPos != -1) {
            subPath = normalized.substring(0, hashPos);
            attr = normalized.substring(hashPos + 1);
        }
        SubmodelElement target = resolveRelativeSubmodelElementPath(item, subPath);
        if (target == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(getSubmodelElementAttributeValues(target, attr));
    }


    private SubmodelElement resolveRelativeSubmodelElementPath(SubmodelElement item, String path) {
        if (item == null || path == null || path.isEmpty())
            return item;

        List<String> tokens = Arrays.asList(path.split("\\."));
        SubmodelElement current = item;
        if (!tokens.isEmpty() && tokens.get(0).equals(current.getIdShort())) {
            tokens = tokens.subList(1, tokens.size());
        }
        for (String token: tokens) {
            if (!(current instanceof SubmodelElementCollection)) {
                return null;
            }
            current = findByIdShort(((SubmodelElementCollection) current).getValue(), token);
            if (current == null)
                return null;
        }
        return current;
    }


    private boolean compareValues(Object a, Object b, ComparisonOperator operator) {
        if (operator == null)
            return false;

        if (operator.isStringOperator()) {
            return compareUsingStringOperator(a, b, operator);
        }
        return compareUsingGeneralComparison(a, b, operator);
    }


    private boolean compareUsingStringOperator(Object a, Object b, ComparisonOperator operator) {
        if (a == null || b == null)
            return false;
        String left = String.valueOf(a);
        String right = String.valueOf(b);

        return switch (operator) {
            case CONTAINS -> left.contains(right);
            case STARTS_WITH -> left.startsWith(right);
            case ENDS_WITH -> left.endsWith(right);
            case REGEX -> Pattern.compile(right).matcher(left).matches();
            default -> false;
        };
    }


    private boolean compareUsingGeneralComparison(Object a, Object b, ComparisonOperator operator) {
        Boolean nullResult = compareNulls(a, b, operator);
        if (nullResult != null) {
            return nullResult;
        }

        Boolean numericResult = compareNumbers(a, b, operator);
        if (numericResult != null) {
            return numericResult;
        }

        String sa = String.valueOf(a).trim();
        String sb = String.valueOf(b).trim();
        Boolean booleanResult = compareBooleans(sa, sb, operator);
        if (booleanResult != null) {
            return booleanResult;
        }

        return compareStrings(sa, sb, operator);
    }


    private Boolean compareNulls(Object a, Object b, ComparisonOperator operator) {
        if (a != null && b != null) {
            return null;
        }
        if (operator == ComparisonOperator.EQ) {
            return Objects.equals(a, b);
        }
        if (operator == ComparisonOperator.NE) {
            return !Objects.equals(a, b);
        }
        return false;
    }


    private Boolean compareNumbers(Object a, Object b, ComparisonOperator operator) {
        Double d1 = toDouble(a);
        Double d2 = toDouble(b);
        if (d1 == null || d2 == null) {
            return null;
        }
        return switch (operator) {
            case EQ -> Double.compare(d1, d2) == 0;
            case NE -> Double.compare(d1, d2) != 0;
            case GT -> d1 > d2;
            case GE -> d1 >= d2;
            case LT -> d1 < d2;
            case LE -> d1 <= d2;
            default -> false;
        };
    }


    private Boolean compareBooleans(String sa, String sb, ComparisonOperator operator) {
        Boolean ba = parseBooleanStrict(sa);
        Boolean bb = parseBooleanStrict(sb);
        if (ba == null || bb == null) {
            return null;
        }
        return switch (operator) {
            case EQ -> Objects.equals(ba, bb);
            case NE -> !Objects.equals(ba, bb);
            default -> false;
        };
    }


    private boolean compareStrings(String sa, String sb, ComparisonOperator operator) {
        int cmp = sa.compareTo(sb);
        return switch (operator) {
            case EQ -> cmp == 0;
            case NE -> cmp != 0;
            case GT -> cmp > 0;
            case GE -> cmp >= 0;
            case LT -> cmp < 0;
            case LE -> cmp <= 0;
            default -> false;
        };
    }


    private static Boolean parseBooleanStrict(String s) {
        if ("true".equalsIgnoreCase(s))
            return Boolean.TRUE;
        if ("false".equalsIgnoreCase(s))
            return Boolean.FALSE;
        return null;
    }


    private static <T> List<T> nonNull(List<T> in) {
        return in != null ? in : Collections.emptyList();
    }

    /**
     * @param remainingSuffix remaining suffix (e.g., ".name")
     */
    private record IndexSelection(boolean selectAll, Integer index, String remainingSuffix) {}

    private IndexSelection parseIndexSelection(String s) {
        if (s == null || s.isEmpty()) {
            return new IndexSelection(true, null, "");
        }
        String rem = s;
        boolean selectAll = false;
        Integer idx = null;

        if (rem.startsWith("[]")) {
            selectAll = true;
            rem = rem.substring(2);
        }
        else if (rem.startsWith("[")) {
            int end = rem.indexOf(']');
            if (end > 1) {
                String idxStr = rem.substring(1, end);
                try {
                    idx = Integer.parseInt(idxStr);
                }
                catch (NumberFormatException e) {
                    LOGGER.error("Invalid index in path: {}", s);
                    return new IndexSelection(true, null, rem.substring(end + 1));
                }
                rem = rem.substring(end + 1);
            }
        }
        return new IndexSelection(selectAll, idx, rem);
    }


    private <T> List<T> selectByIndex(List<T> list, IndexSelection selector) {
        if (list == null || list.isEmpty())
            return Collections.emptyList();
        if (selector.selectAll || selector.index == null)
            return list;
        int i = selector.index;
        return (i >= 0 && i < list.size()) ? Collections.singletonList(list.get(i)) : Collections.emptyList();
    }
}
