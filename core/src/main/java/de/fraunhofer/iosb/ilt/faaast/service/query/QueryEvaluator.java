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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
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
    private final Environment environment;

    public QueryEvaluator(Environment environment) {
        this.environment = environment;
    }

    private enum Operator {
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

        boolean isStringOp() {
            return this == CONTAINS || this == STARTS_WITH || this == ENDS_WITH || this == REGEX;
        }
    }

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
        if (expr.get$boolean() != null) {
            return expr.get$boolean();
        }

        if (expr.get$and() != null && !expr.get$and().isEmpty()) {
            return expr.get$and().stream().allMatch(e -> matches(e, identifiable));
        }
        if (expr.get$or() != null && !expr.get$or().isEmpty()) {
            return expr.get$or().stream().anyMatch(e -> matches(e, identifiable));
        }
        if (expr.get$not() != null) {
            return !matches(expr.get$not(), identifiable);
        }

        // $match
        if (expr.get$match() != null && !expr.get$match().isEmpty()) {
            return evaluateMatch(expr.get$match(), identifiable);
        }

        // Binary
        if (expr.get$eq() != null && !expr.get$eq().isEmpty())
            return evaluateBinary(expr.get$eq(), identifiable, Operator.EQ);
        if (expr.get$ne() != null && !expr.get$ne().isEmpty())
            return evaluateBinary(expr.get$ne(), identifiable, Operator.NE);
        if (expr.get$gt() != null && !expr.get$gt().isEmpty())
            return evaluateBinary(expr.get$gt(), identifiable, Operator.GT);
        if (expr.get$ge() != null && !expr.get$ge().isEmpty())
            return evaluateBinary(expr.get$ge(), identifiable, Operator.GE);
        if (expr.get$lt() != null && !expr.get$lt().isEmpty())
            return evaluateBinary(expr.get$lt(), identifiable, Operator.LT);
        if (expr.get$le() != null && !expr.get$le().isEmpty())
            return evaluateBinary(expr.get$le(), identifiable, Operator.LE);

        // String binary operators
        if (expr.get$contains() != null && !expr.get$contains().isEmpty())
            return evaluateStringBinary(expr.get$contains(), identifiable, Operator.CONTAINS);
        if (expr.get$startsWith() != null && !expr.get$startsWith().isEmpty())
            return evaluateStringBinary(expr.get$startsWith(), identifiable, Operator.STARTS_WITH);
        if (expr.get$endsWith() != null && !expr.get$endsWith().isEmpty())
            return evaluateStringBinary(expr.get$endsWith(), identifiable, Operator.ENDS_WITH);
        if (expr.get$regex() != null && !expr.get$regex().isEmpty())
            return evaluateStringBinary(expr.get$regex(), identifiable, Operator.REGEX);

        return false;
    }


    private boolean evaluateBinary(List<Value> args, Identifiable identifiable, Operator op) {
        if (args.size() < 2) {
            LOGGER.error("Operator {} requires two arguments", op);
            return false;
        }
        List<Object> left = evaluateValue(args.get(0), identifiable);
        List<Object> right = evaluateValue(args.get(1), identifiable);
        return anyPairMatches(left, right, op);
    }


    private boolean evaluateStringBinary(List<StringValue> args, Identifiable identifiable, Operator op) {
        if (args.size() < 2) {
            LOGGER.error("String operator {} requires two arguments", op);
            return false;
        }
        List<Object> left = evaluateStringValue(args.get(0), identifiable);
        List<Object> right = evaluateStringValue(args.get(1), identifiable);
        return anyPairMatches(left, right, op);
    }


    private boolean anyPairMatches(List<Object> left, List<Object> right, Operator op) {
        if (left == null || right == null) {
            return false;
        }
        for (Object l: left) {
            for (Object r: right) {
                if (compare(l, r, op)) {
                    return true;
                }
            }
        }
        return false;
    }


    private List<Object> evaluateValue(Value v, Identifiable identifiable) {
        if (v == null)
            return Collections.emptyList();

        if (v.get$field() != null) {
            return nonNull(getFieldValues(v.get$field(), identifiable));
        }
        if (v.get$strVal() != null)
            return Collections.singletonList(v.get$strVal());
        if (v.get$numVal() != null)
            return Collections.singletonList(v.get$numVal());
        if (v.get$hexVal() != null)
            return Collections.singletonList(v.get$hexVal());
        if (v.get$dateTimeVal() != null)
            return Collections.singletonList(v.get$dateTimeVal());
        if (v.get$timeVal() != null)
            return Collections.singletonList(v.get$timeVal());
        if (v.get$boolean() != null)
            return Collections.singletonList(v.get$boolean());
        if (v.get$strCast() != null) {
            return evaluateValue(v.get$strCast(), identifiable).stream().map(String::valueOf).collect(Collectors.toList());
        }
        if (v.get$numCast() != null) {
            return evaluateValue(v.get$numCast(), identifiable).stream().map(String::valueOf).map(this::parseDoubleOrNull).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }


    private List<Object> evaluateStringValue(StringValue sv, Identifiable identifiable) {
        if (sv == null)
            return Collections.emptyList();
        if (sv.get$field() != null) {
            return nonNull(getFieldValues(sv.get$field(), identifiable));
        }
        if (sv.get$strVal() != null) {
            return Collections.singletonList(sv.get$strVal());
        }
        if (sv.get$strCast() != null) {
            return evaluateValue(sv.get$strCast(), identifiable).stream().map(String::valueOf).collect(Collectors.toList());
        }
        LOGGER.error("Invalid string value: {}", sv);
        return Collections.emptyList();
    }


    private Double parseDoubleOrNull(String s) {
        try {
            return Double.valueOf(s);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    private static final class Condition {
        final String suffix; // e.g., ".name", "Sub.Path#value"
        final Operator op;
        final List<Object> rightVals;

        Condition(String suffix, Operator op, List<Object> rightVals) {
            this.suffix = suffix;
            this.op = op;
            this.rightVals = rightVals != null ? rightVals : Collections.emptyList();
        }
    }

    private static final class MatchOp {
        final Operator op;
        final List<Value> args;

        MatchOp(Operator op, List<Value> args) {
            this.op = op;
            this.args = args;
        }
    }

    private boolean evaluateMatch(List<MatchExpression> matches, Identifiable identifiable) {
        if (matches == null || matches.isEmpty()) {
            return true;
        }

        String commonPrefix = null;
        List<Condition> itemConditions = new ArrayList<>();

        // Evaluate/collect each match clause
        for (MatchExpression m: matches) {
            MatchOp mo = getMatchOp(m);
            if (mo == null) {
                LOGGER.error("Unsupported operator in match");
                return false;
            }

            Value left = mo.args.get(0);
            Value right = mo.args.get(1);
            if (left.get$field() == null) {
                LOGGER.error("Left side in $match must be a field: {}", left);
                return false;
            }

            String field = left.get$field();
            List<Object> rightVals = evaluateValue(right, identifiable);

            int listMarker = field.indexOf("[]");
            if (listMarker == -1) {
                if (field.startsWith(PREFIX_SME + "#")) {
                    String prefix = PREFIX_SME;
                    if (commonPrefix != null && !commonPrefix.equals(prefix)) {
                        LOGGER.error("Non-common prefix in match: {} vs {}", commonPrefix, prefix);
                        return false;
                    }
                    commonPrefix = prefix;
                    String suffix = field.substring((PREFIX_SME + "#").length());
                    itemConditions.add(new Condition(suffix, mo.op, rightVals));
                }
                else {
                    List<Object> leftVals = evaluateValue(left, identifiable);
                    if (!anyPairMatches(leftVals, rightVals, mo.op)) {
                        return false;
                    }
                }
            }
            else {
                String prefix = field.substring(0, listMarker);
                if (commonPrefix != null && !commonPrefix.equals(prefix)) {
                    LOGGER.error("Non-common prefix in match: {} vs {}", commonPrefix, prefix);
                    return false;
                }
                commonPrefix = prefix;
                String suffix = field.substring(listMarker + 2);
                itemConditions.add(new Condition(suffix, mo.op, rightVals));
            }
        }

        // parent conditions: all matched
        if (commonPrefix == null) {
            return true;
        }

        // Evaluate list items depending on prefix
        switch (commonPrefix) {
            case "$aas#assetInformation.specificAssetIds":
                if (!(identifiable instanceof AssetAdministrationShell))
                    return false;
                AssetAdministrationShell aas = (AssetAdministrationShell) identifiable;
                if (aas.getAssetInformation() == null || aas.getAssetInformation().getSpecificAssetIds() == null)
                    return false;

                for (SpecificAssetId item: aas.getAssetInformation().getSpecificAssetIds()) {
                    if (allItemConditionsMatch(itemConditions, cond -> {
                        String s = getPropertyFromObject(item, cond.suffix);
                        return s == null ? Collections.emptyList() : Collections.singletonList(s);
                    })) {
                        return true;
                    }
                }
                return false;

            case PREFIX_SME:
                if (!(identifiable instanceof Submodel))
                    return false;
                Submodel sm = (Submodel) identifiable;
                List<SubmodelElement> topLevel = sm.getSubmodelElements();
                if (topLevel == null)
                    return false;

                for (SubmodelElement item: topLevel) {
                    if (allItemConditionsMatch(itemConditions, cond -> getPropertyFromSuffix(item, cond.suffix))) {
                        return true;
                    }
                }
                return false;

            default:
                if (commonPrefix.startsWith(PREFIX_SME + ".")) {
                    if (!(identifiable instanceof Submodel))
                        return false;
                    Submodel sm2 = (Submodel) identifiable;
                    String path = commonPrefix.substring((PREFIX_SME + ".").length());
                    SubmodelElement listElem = getSubmodelElementByPath(sm2, path);
                    if (!(listElem instanceof SubmodelElementList))
                        return false;

                    List<SubmodelElement> items = ((SubmodelElementList) listElem).getValue();
                    if (items == null)
                        return false;

                    for (SubmodelElement item: items) {
                        if (allItemConditionsMatch(itemConditions, cond -> getPropertyFromSuffix(item, cond.suffix))) {
                            return true;
                        }
                    }
                    return false;
                }
                LOGGER.error("Unsupported prefix for $match: {}", commonPrefix);
                return false;
        }
    }


    private MatchOp getMatchOp(MatchExpression m) {
        if (m.get$eq() != null && !m.get$eq().isEmpty())
            return new MatchOp(Operator.EQ, m.get$eq());
        if (m.get$ne() != null && !m.get$ne().isEmpty())
            return new MatchOp(Operator.NE, m.get$ne());
        if (m.get$gt() != null && !m.get$gt().isEmpty())
            return new MatchOp(Operator.GT, m.get$gt());
        if (m.get$ge() != null && !m.get$ge().isEmpty())
            return new MatchOp(Operator.GE, m.get$ge());
        if (m.get$lt() != null && !m.get$lt().isEmpty())
            return new MatchOp(Operator.LT, m.get$lt());
        if (m.get$le() != null && !m.get$le().isEmpty())
            return new MatchOp(Operator.LE, m.get$le());
        return null;
    }


    private boolean allItemConditionsMatch(List<Condition> conditions,
                                           java.util.function.Function<Condition, List<Object>> leftExtractor) {
        for (Condition cond: conditions) {
            List<Object> leftVals = nonNull(leftExtractor.apply(cond));
            if (!anyPairMatches(leftVals, cond.rightVals, cond.op)) {
                return false;
            }
        }
        return true;
    }


    private List<Object> getFieldValues(String field, Identifiable identifiable) {
        if (field == null || identifiable == null)
            return Collections.emptyList();

        if (field.startsWith(PREFIX_AAS)) {
            if (!(identifiable instanceof AssetAdministrationShell))
                return Collections.emptyList();
            return getAasFieldValues((AssetAdministrationShell) identifiable, field.substring(PREFIX_AAS.length()));
        }

        if (field.startsWith(PREFIX_SM)) {
            if (!(identifiable instanceof Submodel))
                return Collections.emptyList();
            return new ArrayList<>(getSmAttrValues((Submodel) identifiable, field.substring(PREFIX_SM.length())));
        }

        if (field.startsWith(PREFIX_SME)) {
            if (!(identifiable instanceof Submodel))
                return Collections.emptyList();
            Submodel sm = (Submodel) identifiable;

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
                    values.addAll(getSmeAttrValues(sme, attr));
                }
            }
            else {
                List<SubmodelElement> smes = sm.getSubmodelElements();
                if (smes != null) {
                    for (SubmodelElement sme: smes) {
                        values.addAll(getSmeAttrValues(sme, attr));
                    }
                }
            }
            return values;
        }

        if (field.startsWith(PREFIX_CD)) {
            if (!(identifiable instanceof ConceptDescription))
                return Collections.emptyList();
            ConceptDescription cd = (ConceptDescription) identifiable;
            String attr = field.substring(PREFIX_CD.length());
            if ("idShort".equals(attr))
                return Collections.singletonList(cd.getIdShort());
            if ("id".equals(attr))
                return Collections.singletonList(cd.getId());
            LOGGER.error("Unsupported CD attribute: {}", attr);
            return Collections.emptyList();
        }

        LOGGER.error("Unsupported field: {}", field);
        return Collections.emptyList();
    }


    private List<Object> getAasFieldValues(AssetAdministrationShell aas, String attr) {
        if ("idShort".equals(attr))
            return Collections.singletonList(aas.getIdShort());
        if ("id".equals(attr))
            return Collections.singletonList(aas.getId());

        if ("assetInformation.assetKind".equals(attr)) {
            return aas.getAssetInformation() == null || aas.getAssetInformation().getAssetKind() == null
                    ? Collections.emptyList()
                    : Collections.singletonList(aas.getAssetInformation().getAssetKind().name());
        }
        if ("assetInformation.assetType".equals(attr)) {
            return aas.getAssetInformation() == null
                    ? Collections.emptyList()
                    : Collections.singletonList(aas.getAssetInformation().getAssetType());
        }
        if ("assetInformation.globalAssetId".equals(attr)) {
            if (aas.getAssetInformation() == null)
                return Collections.emptyList();
            String globalAssetId = aas.getAssetInformation().getGlobalAssetId();
            return globalAssetId == null ? Collections.emptyList() : Collections.singletonList(globalAssetId);
        }

        if (attr.startsWith("assetInformation.specificAssetIds")) {
            if (aas.getAssetInformation() == null || aas.getAssetInformation().getSpecificAssetIds() == null) {
                return Collections.emptyList();
            }
            String remaining = attr.substring("assetInformation.specificAssetIds".length());
            IndexSpec spec = parseIndexSuffix(remaining);

            List<SpecificAssetId> sais = aas.getAssetInformation().getSpecificAssetIds();
            List<SpecificAssetId> targets = selectTargets(sais, spec);

            List<Object> values = new ArrayList<>();
            for (SpecificAssetId sai: targets) {
                values.add(getPropertyFromObject(sai, spec.remaining));
            }
            return values;
        }

        LOGGER.error("Unsupported AAS attribute: {}", attr);
        return Collections.emptyList();
    }


    private List<String> getSmAttrValues(Submodel sm, String attr) {
        if ("idShort".equals(attr))
            return Collections.singletonList(sm.getIdShort());
        if ("id".equals(attr))
            return Collections.singletonList(sm.getId());

        if ("semanticId".equals(attr)) {
            Reference ref = sm.getSemanticId();
            if (ref == null || ref.getKeys() == null || ref.getKeys().isEmpty())
                return Collections.emptyList();
            return Collections.singletonList(ref.getKeys().get(0).getValue());
        }

        if (attr.startsWith("semanticId.keys")) {
            Reference ref = sm.getSemanticId();
            if (ref == null || ref.getKeys() == null)
                return Collections.emptyList();

            String remaining = attr.substring("semanticId.keys".length());
            IndexSpec spec = parseIndexSuffix(remaining);

            List<Key> targets = selectTargets(ref.getKeys(), spec);
            List<String> out = new ArrayList<>();
            for (Key key: targets) {
                if (".type".equals(spec.remaining))
                    out.add(key.getType().name());
                else if (".value".equals(spec.remaining))
                    out.add(key.getValue());
            }
            return out;
        }

        LOGGER.error("Unsupported SM attribute: {}", attr);
        return Collections.emptyList();
    }


    private List<String> getSmeAttrValues(SubmodelElement sme, String attr) {
        if (sme == null || attr == null)
            return Collections.emptyList();

        if ("idShort".equals(attr)) {
            return Collections.singletonList(sme.getIdShort());
        }
        if ("value".equals(attr)) {
            if (sme instanceof Property) {
                return Collections.singletonList(((Property) sme).getValue());
            }
            return Collections.emptyList();
        }
        if ("valueType".equals(attr)) {
            if (sme instanceof Property && ((Property) sme).getValueType() != null) {
                return Collections.singletonList(((Property) sme).getValueType().name());
            }
            return Collections.emptyList();
        }
        if ("language".equals(attr)) {
            if (sme instanceof MultiLanguageProperty) {
                List<LangStringTextType> values = ((MultiLanguageProperty) sme).getValue();
                if (values == null)
                    return Collections.emptyList();
                return values.stream().filter(Objects::nonNull).map(LangStringTextType::getLanguage).collect(Collectors.toList());
            }
            return Collections.emptyList();
        }
        if ("semanticId".equals(attr)) {
            Reference ref = sme.getSemanticId();
            if (ref == null || ref.getKeys() == null || ref.getKeys().isEmpty())
                return Collections.emptyList();
            return Collections.singletonList(ref.getKeys().get(0).getValue());
        }
        if (attr.startsWith("semanticId.keys")) {
            Reference ref = sme.getSemanticId();
            if (ref == null || ref.getKeys() == null)
                return Collections.emptyList();

            String remaining = attr.substring("semanticId.keys".length());
            IndexSpec spec = parseIndexSuffix(remaining);

            List<Key> targets = selectTargets(ref.getKeys(), spec);
            List<String> out = new ArrayList<>();
            for (Key key: targets) {
                if (".type".equals(spec.remaining))
                    out.add(key.getType().name());
                else if (".value".equals(spec.remaining))
                    out.add(key.getValue());
            }
            return out;
        }

        LOGGER.error("Unsupported SME attribute: {}", attr);
        return Collections.emptyList();
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


    private String getPropertyFromObject(Object item, String path) {
        if (!(item instanceof SpecificAssetId) || path == null) {
            LOGGER.error("Unsupported property {} for object {}", path, item);
            return null;
        }
        SpecificAssetId sai = (SpecificAssetId) item;
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


    private List<Object> getPropertyFromSuffix(SubmodelElement item, String suffix) {
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
        SubmodelElement target = getSubmodelElementByPathForItem(item, subPath);
        if (target == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(getSmeAttrValues(target, attr));
    }


    private SubmodelElement getSubmodelElementByPathForItem(SubmodelElement item, String path) {
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


    private boolean compare(Object a, Object b, Operator op) {
        if (op == null)
            return false;

        if (op.isStringOp()) {
            return compareStrings(a, b, op);
        }
        return compareGeneral(a, b, op);
    }


    private boolean compareStrings(Object a, Object b, Operator op) {
        if (a == null || b == null)
            return false;
        String left = String.valueOf(a);
        String right = String.valueOf(b);

        switch (op) {
            case CONTAINS:
                return left.contains(right);
            case STARTS_WITH:
                return left.startsWith(right);
            case ENDS_WITH:
                return left.endsWith(right);
            case REGEX:
                return Pattern.compile(right).matcher(left).matches();
            default:
                return false;
        }
    }


    private boolean compareGeneral(Object a, Object b, Operator op) {
        if (a == null || b == null) {
            return (op == Operator.EQ) ? a == b
                    : (op == Operator.NE) && a != b;
        }

        try {
            double d1 = (a instanceof Number) ? ((Number) a).doubleValue() : Double.parseDouble(String.valueOf(a));
            double d2 = (b instanceof Number) ? ((Number) b).doubleValue() : Double.parseDouble(String.valueOf(b));
            switch (op) {
                case EQ:
                    return d1 == d2;
                case NE:
                    return d1 != d2;
                case GT:
                    return d1 > d2;
                case GE:
                    return d1 >= d2;
                case LT:
                    return d1 < d2;
                case LE:
                    return d1 <= d2;
                default:
                    return false;
            }
        }
        catch (NumberFormatException ignored) {
            // left blank intentionally
        }

        // boolean
        String sa = String.valueOf(a).trim();
        String sb = String.valueOf(b).trim();
        boolean ba = "true".equalsIgnoreCase(sa) || "false".equalsIgnoreCase(sa);
        boolean bb = "true".equalsIgnoreCase(sb) || "false".equalsIgnoreCase(sb);
        if (ba && bb) {
            boolean b1 = Boolean.parseBoolean(sa);
            boolean b2 = Boolean.parseBoolean(sb);
            return (op == Operator.EQ) ? (b1 == b2)
                    : (op == Operator.NE) && (b1 != b2);
        }

        // string
        int cmp = sa.compareTo(sb);
        switch (op) {
            case EQ:
                return cmp == 0;
            case NE:
                return cmp != 0;
            case GT:
                return cmp > 0;
            case GE:
                return cmp >= 0;
            case LT:
                return cmp < 0;
            case LE:
                return cmp <= 0;
            default:
                return false;
        }
    }


    private static <T> List<T> nonNull(List<T> in) {
        return in != null ? in : Collections.emptyList();
    }

    /**
     * @param remaining remaining suffix (e.g., ".name")
     */
    private record IndexSpec(boolean any, Integer index, String remaining) {}

    private IndexSpec parseIndexSuffix(String s) {
        if (s == null || s.isEmpty()) {
            return new IndexSpec(true, null, "");
        }
        String rem = s;
        boolean any = false;
        Integer idx = null;

        if (rem.startsWith("[]")) {
            any = true;
            rem = rem.substring(2);
        }
        else if (rem.startsWith("[")) {
            int end = rem.indexOf(']');
            if (end > 1) {
                String idxStr = rem.substring(1, end);
                if (idxStr.isEmpty()) {
                    any = true;
                }
                else {
                    try {
                        idx = Integer.parseInt(idxStr);
                    }
                    catch (NumberFormatException e) {
                        LOGGER.error("Invalid index in path: {}", s);
                        return new IndexSpec(true, null, rem.substring(end + 1));
                    }
                }
                rem = rem.substring(end + 1);
            }
        }
        return new IndexSpec(any, idx, rem);
    }


    private <T> List<T> selectTargets(List<T> list, IndexSpec spec) {
        if (list == null || list.isEmpty())
            return Collections.emptyList();
        if (spec.any || spec.index == null)
            return list;
        int i = spec.index;
        return (i >= 0 && i < list.size()) ? Collections.singletonList(list.get(i)) : Collections.emptyList();
    }
}
