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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Evaluates queries sent to /query endpoints
 */
public class QueryEvaluator {

    private final Environment environment;
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryEvaluator.class);

    public QueryEvaluator(Environment environment) {
        this.environment = environment;
    }


    /**
     * Used to decice whether to filter out the Identifiable
     *
     * @param expr LogicalExpression of the query
     * @param identifiable like AAS, Submodel or Concept-Description
     * @return true if expression matches
     */
    public boolean matches(LogicalExpression expr, Identifiable identifiable) {
        if (!expr.get$and().isEmpty()) {
            return expr.get$and().stream().allMatch(e -> matches(e, identifiable));
        }
        if (!expr.get$or().isEmpty()) {
            return expr.get$or().stream().anyMatch(e -> matches(e, identifiable));
        }
        if (expr.get$not() != null) {
            return !matches(expr.get$not(), identifiable);
        }
        if (!expr.get$match().isEmpty()) {
            return evaluateMatch(expr.get$match(), identifiable);
        }
        if (!expr.get$eq().isEmpty()) {
            List<Value> args = expr.get$eq();
            List<Object> left = evaluateValue(args.get(0), identifiable);
            List<Object> right = evaluateValue(args.get(1), identifiable);
            return left.stream().anyMatch(l -> right.stream().anyMatch(r -> compareObjects(l, r, "eq")));
        }
        if (!expr.get$ne().isEmpty()) {
            List<Value> args = expr.get$ne();
            List<Object> left = evaluateValue(args.get(0), identifiable);
            List<Object> right = evaluateValue(args.get(1), identifiable);
            return left.stream().anyMatch(l -> right.stream().anyMatch(r -> compareObjects(l, r, "ne")));
        }
        if (!expr.get$gt().isEmpty()) {
            List<Value> args = expr.get$gt();
            List<Object> left = evaluateValue(args.get(0), identifiable);
            List<Object> right = evaluateValue(args.get(1), identifiable);
            return left.stream().anyMatch(l -> right.stream().anyMatch(r -> compareObjects(l, r, "gt")));
        }
        if (!expr.get$ge().isEmpty()) {
            List<Value> args = expr.get$ge();
            List<Object> left = evaluateValue(args.get(0), identifiable);
            List<Object> right = evaluateValue(args.get(1), identifiable);
            return left.stream().anyMatch(l -> right.stream().anyMatch(r -> compareObjects(l, r, "ge")));
        }
        if (!expr.get$lt().isEmpty()) {
            List<Value> args = expr.get$lt();
            List<Object> left = evaluateValue(args.get(0), identifiable);
            List<Object> right = evaluateValue(args.get(1), identifiable);
            return left.stream().anyMatch(l -> right.stream().anyMatch(r -> compareObjects(l, r, "lt")));
        }
        if (!expr.get$le().isEmpty()) {
            List<Value> args = expr.get$le();
            List<Object> left = evaluateValue(args.get(0), identifiable);
            List<Object> right = evaluateValue(args.get(1), identifiable);
            return left.stream().anyMatch(l -> right.stream().anyMatch(r -> compareObjects(l, r, "le")));
        }
        if (!expr.get$contains().isEmpty()) {
            List<StringValue> args = expr.get$contains();
            List<Object> left = evaluateStringValue(args.get(0), identifiable);
            List<Object> right = evaluateStringValue(args.get(1), identifiable);
            return left.stream().anyMatch(l -> right.stream().anyMatch(r -> stringCompareObjects(l, r, "contains")));
        }
        if (!expr.get$startsWith().isEmpty()) {
            List<StringValue> args = expr.get$startsWith();
            List<Object> left = evaluateStringValue(args.get(0), identifiable);
            List<Object> right = evaluateStringValue(args.get(1), identifiable);
            return left.stream().anyMatch(l -> right.stream().anyMatch(r -> stringCompareObjects(l, r, "starts-with")));
        }
        if (!expr.get$endsWith().isEmpty()) {
            List<StringValue> args = expr.get$endsWith();
            List<Object> left = evaluateStringValue(args.get(0), identifiable);
            List<Object> right = evaluateStringValue(args.get(1), identifiable);
            return left.stream().anyMatch(l -> right.stream().anyMatch(r -> stringCompareObjects(l, r, "ends-with")));
        }
        if (!expr.get$regex().isEmpty()) {
            List<StringValue> args = expr.get$regex();
            List<Object> left = evaluateStringValue(args.get(0), identifiable);
            List<Object> right = evaluateStringValue(args.get(1), identifiable);
            return left.stream().anyMatch(l -> right.stream().anyMatch(r -> stringCompareObjects(l, r, "regex")));
        }
        if (expr.get$boolean() != null) {
            return expr.get$boolean();
        }
        return false;
    }


    private List<Object> evaluateValue(Value v, Identifiable identifiable) {
        if (v.get$field() != null) {
            return getFieldValues(v.get$field(), identifiable);
        }
        if (v.get$strVal() != null) {
            return Collections.singletonList(v.get$strVal());
        }
        if (v.get$numVal() != null) {
            return Collections.singletonList(v.get$numVal());
        }
        if (v.get$hexVal() != null) {
            return Collections.singletonList(v.get$hexVal());
        }
        if (v.get$dateTimeVal() != null) {
            return Collections.singletonList(v.get$dateTimeVal());
        }
        if (v.get$timeVal() != null) {
            return Collections.singletonList(v.get$timeVal());
        }
        if (v.get$boolean() != null) {
            return Collections.singletonList(v.get$boolean());
        }
        if (v.get$strCast() != null) {
            List<Object> inner = evaluateValue(v.get$strCast(), identifiable);
            return inner.stream().map(Object::toString).collect(Collectors.toList());
        }
        if (v.get$numCast() != null) {
            List<Object> inner = evaluateValue(v.get$numCast(), identifiable);
            return inner.stream().map(i -> {
                try {
                    return Double.parseDouble(i.toString());
                }
                catch (NumberFormatException e) {
                    return 0.0;
                }
            }).collect(Collectors.toList());
        }
        return null;
    }


    private List<Object> evaluateStringValue(StringValue sv, Identifiable identifiable) {
        if (sv.get$field() != null) {
            return getFieldValues(sv.get$field(), identifiable);
        }
        if (sv.get$strVal() != null) {
            return Collections.singletonList(sv.get$strVal());
        }
        if (sv.get$strCast() != null) {
            List<Object> inner = evaluateValue(sv.get$strCast(), identifiable);
            return inner.stream().map(Object::toString).collect(Collectors.toList());
        }
        // Attribute not supported
        LOGGER.error("Invalid string value.");
        return null;
    }


    private boolean evaluateMatch(List<MatchExpression> matches, Identifiable identifiable) {
        String commonPrefix = null;
        Map<String, String> suffixes = new HashMap<>();
        Map<String, Object> constants = new HashMap<>();
        String op = "eq"; // Assume eq for all
        for (MatchExpression m: matches) {
            if (m.get$eq() == null || m.get$eq().isEmpty()) {
                LOGGER.error("Only $eq supported in match for now");
                return false;
            }
            Value left = m.get$eq().get(0);
            Value right = m.get$eq().get(1);
            if (left.get$field() == null) {
                LOGGER.error("Left not field in match");
                return false;
            }
            String f = left.get$field();
            Object c = evaluateValue(right, identifiable).get(0);
            int pos = f.indexOf("[]");
            if (pos == -1) {
                LOGGER.error("No [] in field for match");
                return false;
            }
            String prefix = f.substring(0, pos);
            if (commonPrefix == null) {
                commonPrefix = prefix;
            }
            else if (!commonPrefix.equals(prefix)) {
                LOGGER.error("Non-common prefix in match");
            }
            String suffix = f.substring(pos + 2);
            suffixes.put(f, suffix);
            constants.put(f, c);
        }
        if (commonPrefix == null) {
            return true;
        }
        // Handle different prefixes
        if (commonPrefix.equals("$aas#assetInformation.specificAssetIds")) {
            if (!(identifiable instanceof AssetAdministrationShell))
                return false;
            AssetAdministrationShell aas = (AssetAdministrationShell) identifiable;
            List<SpecificAssetId> list = aas.getAssetInformation().getSpecificAssetIds();
            for (SpecificAssetId item: list) {
                boolean all = true;
                for (String f: suffixes.keySet()) {
                    String suffix = suffixes.get(f);
                    Object value = getPropertyFromObject(item, suffix);
                    Object c = constants.get(f);
                    if (!compareObjects(value, c, op)) {
                        all = false;
                        break;
                    }
                }
                if (all) {
                    return true;
                }
            }
            return false;
        }
        else if (commonPrefix.startsWith("$sme.")) {
            // Basic support for $sme paths with []
            List<Submodel> sms = environment.getSubmodels();
            for (Submodel sm: sms) {
                String path = commonPrefix.substring(5); // e.g., "ProductClassifications"
                SubmodelElement listElem = getSubmodelElementByPath(sm, path);
                if (listElem == null || !(listElem instanceof SubmodelElementList))
                    continue;
                SubmodelElementList smeList = (SubmodelElementList) listElem;
                for (SubmodelElement item: smeList.getValue()) {
                    boolean all = true;
                    for (String f: suffixes.keySet()) {
                        String suffix = suffixes.get(f);
                        Object value = getPropertyFromSuffix(item, suffix);
                        Object c = constants.get(f);
                        if (!compareObjects(value, c, op)) {
                            all = false;
                            break;
                        }
                    }
                    if (all) {
                        return true;
                    }
                }
            }
            return false;
        }
        LOGGER.error("Unsupported prefix for match: " + commonPrefix);
        return false;
    }


    private List<Object> getFieldValues(String field, Identifiable identifiable) {
        if (field.startsWith("$aas#")) {
            if (!(identifiable instanceof AssetAdministrationShell))
                return Collections.emptyList();
            AssetAdministrationShell aas = (AssetAdministrationShell) identifiable;
            String attr = field.substring(5);
            // Handle AAS attributes
            if (attr.equals("idShort")) {
                return Collections.singletonList(aas.getIdShort());
            }
            else if (attr.equals("id")) {
                return Collections.singletonList(aas.getId());
            }
            else if (attr.equals("assetInformation.assetKind")) {
                return Collections.singletonList(aas.getAssetInformation().getAssetKind().name());
            }
            else if (attr.equals("assetInformation.assetType")) {
                return Collections.singletonList(aas.getAssetInformation().getAssetType());
            }
            else if (attr.equals("assetInformation.globalAssetId")) {
                String globalAssetId = aas.getAssetInformation().getGlobalAssetId();
                if (globalAssetId == null)
                    return Collections.emptyList();
                return Collections.singletonList(globalAssetId);
            }
            else if (attr.startsWith("assetInformation.specificAssetIds")) {
                String remaining = attr.substring("assetInformation.specificAssetIds".length());
                boolean any = remaining.startsWith("[]");
                Integer index = null;
                if (remaining.startsWith("[")) {
                    int end = remaining.indexOf("]");
                    String idxStr = remaining.substring(1, end);
                    if (idxStr.isEmpty())
                        any = true;
                    else
                        index = Integer.parseInt(idxStr);
                    remaining = remaining.substring(end + 1);
                }
                List<SpecificAssetId> sais = aas.getAssetInformation().getSpecificAssetIds();
                List<Object> values = new ArrayList<>();
                List<SpecificAssetId> targets = any || index == null ? sais
                        : (index < sais.size() && index >= 0 ? Collections.singletonList(sais.get(index)) : Collections.emptyList());
                for (SpecificAssetId sai: targets) {
                    values.add(getPropertyFromObject(sai, remaining));
                }
                return values;
            }
            LOGGER.error("Unsupported AAS attribute: " + attr);
        }
        else if (field.startsWith("$sm#")) {
            String attr = field.substring(4);
            List<Submodel> sms = environment.getSubmodels();
            List<Object> values = new ArrayList<>();
            for (Submodel sm: sms) {
                values.addAll(getSmAttrValues(sm, attr));
            }
            return values;
        }
        else if (field.startsWith("$sme")) {
            String pathPart = "";
            String attr = "";
            if (field.contains(".")) {
                int hashPos = field.indexOf("#");
                pathPart = field.substring(field.indexOf(".") + 1, hashPos);
                attr = field.substring(hashPos + 1);
            }
            else {
                attr = field.substring(5);
            }
            if (!pathPart.isEmpty()) {
                // Basic path support, no [] for now
                List<Submodel> sms = environment.getSubmodels();
                List<Object> values = new ArrayList<>();
                for (Submodel sm: sms) {
                    SubmodelElement sme = getSubmodelElementByPath(sm, pathPart);
                    if (sme != null) {
                        values.addAll(getSmeAttrValues(sme, attr));
                    }
                }
                return values;
            }
            else {
                // Recursive all SME
                Submodel sm = (Submodel) identifiable;
                List<SubmodelElement> smes = sm.getSubmodelElements();
                List<Object> values = new ArrayList<>();
                for (SubmodelElement sme: smes) {
                    values.addAll(getSmeAttrValues(sme, attr));
                }
                return values;
            }
        }
        else if (field.startsWith("$cd#")) {
            if (!(identifiable instanceof ConceptDescription))
                return Collections.emptyList();
            ConceptDescription cd = (ConceptDescription) identifiable;
            String attr = field.substring(4);
            if (attr.equals("idShort")) {
                return Collections.singletonList(cd.getIdShort());
            }
            else if (attr.equals("id")) {
                return Collections.singletonList(cd.getId());
            }
            LOGGER.error("Unsupported CD attribute: " + attr);
        }
        LOGGER.error("Unsupported field: " + field);
        return null;
    }


    private List<Object> getSmAttrValues(Submodel sm, String attr) {
        if (attr.equals("idShort")) {
            return Collections.singletonList(sm.getIdShort());
        }
        else if (attr.equals("id")) {
            return Collections.singletonList(sm.getId());
        }
        else if (attr.equals("semanticId")) {
            Reference ref = sm.getSemanticId();
            if (ref == null || ref.getKeys().isEmpty())
                return Collections.emptyList();
            return Collections.singletonList(ref.getKeys().get(0).getValue());
        }
        else if (attr.startsWith("semanticId.keys")) {
            Reference ref = sm.getSemanticId();
            if (ref == null)
                return Collections.emptyList();
            //cut prefix
            String remaining = attr.substring("semanticId.keys".length());
            //check if there is an index like [1]
            boolean any = remaining.startsWith("[]");
            //set index
            Integer index = null;
            if (remaining.startsWith("[")) {
                int end = remaining.indexOf("]");
                String idxStr = remaining.substring(1, end);
                if (idxStr.isEmpty())
                    any = true;
                else
                    index = Integer.parseInt(idxStr);
                remaining = remaining.substring(end + 1);
            }
            List<Key> keys = ref.getKeys();
            List<Object> values = new ArrayList<>();
            // set targets depending if any is true otherwise target index
            List<Key> targets = any || index == null ? keys : (index < keys.size() && index >= 0 ? Collections.singletonList(keys.get(index)) : Collections.emptyList());
            for (Key key: targets) {
                if (remaining.equals(".type")) {
                    values.add(key.getType().name());
                }
                else if (remaining.equals(".value")) {
                    values.add(key.getValue());
                }
            }
            return values;
        }
        LOGGER.error("Unsupported SM attribute: " + attr);
        return null;
    }


    private List<Object> getSmeAttrValues(SubmodelElement sme, String attr) {
        if (attr.equals("idShort")) {
            return Collections.singletonList(sme.getIdShort());
        }
        else if (attr.equals("value")) {
            if (sme instanceof Property) {
                return Collections.singletonList(((Property) sme).getValue());
            }
            return Collections.emptyList();
        }
        else if (attr.equals("valueType")) {
            if (sme instanceof Property) {
                return Collections.singletonList(((Property) sme).getValueType().name());
            }
            return Collections.emptyList();
        }
        else if (attr.equals("language")) {
            if (sme instanceof MultiLanguageProperty) {
                return ((MultiLanguageProperty) sme).getValue().stream().map(LangStringTextType::getLanguage).collect(Collectors.toList());
            }
            return Collections.emptyList();
        }
        else if (attr.equals("semanticId")) {
            Reference ref = sme.getSemanticId();
            if (ref == null || ref.getKeys().isEmpty())
                return Collections.emptyList();
            return Collections.singletonList(ref.getKeys().get(0).getValue());
        }
        else if (attr.startsWith("semanticId.keys")) {
            Reference ref = sme.getSemanticId();
            if (ref == null)
                return Collections.emptyList();
            String remaining = attr.substring("semanticId.keys".length());
            boolean any = remaining.startsWith("[]");
            Integer index = null;
            if (remaining.startsWith("[")) {
                int end = remaining.indexOf("]");
                String idxStr = remaining.substring(1, end);
                if (idxStr.isEmpty())
                    any = true;
                else
                    index = Integer.parseInt(idxStr);
                remaining = remaining.substring(end + 1);
            }
            List<Key> keys = ref.getKeys();
            List<Object> values = new ArrayList<>();
            List<Key> targets = any || index == null ? keys : (index < keys.size() && index >= 0 ? Collections.singletonList(keys.get(index)) : Collections.emptyList());
            for (Key key: targets) {
                if (remaining.equals(".type")) {
                    values.add(key.getType().name());
                }
                else if (remaining.equals(".value")) {
                    values.add(key.getValue());
                }
            }
            return values;
        }
        LOGGER.error("Unsupported SME attribute: " + attr);
        return null;
    }


    private SubmodelElement getSubmodelElementByPath(Submodel sm, String path) {
        SubmodelElement current = null;
        for (String token: path.split("\\.")) {
            current = sm.getSubmodelElements().stream()
                    .filter(e -> e.getIdShort().equals(token))
                    .findFirst().orElse(null);
        }
        return current;
    }


    private Object getPropertyFromObject(Object item, String path) {
        if (item instanceof SpecificAssetId) {
            if (path.equals(".name")) {
                return ((SpecificAssetId) item).getName();
            }
            else if (path.equals(".value")) {
                return ((SpecificAssetId) item).getValue();
            }
            else if (path.startsWith(".externalSubjectId")) {
                return ((SpecificAssetId) item).getExternalSubjectId();
            }
        }
        LOGGER.error("Unsupported property: " + path);
        return null;
    }


    private Object getPropertyFromSuffix(SubmodelElement item, String suffix) {
        // For suffixes like .ProductClassId#value
        int hashPos = suffix.indexOf("#");
        if (hashPos == -1) {
            LOGGER.error("No # in suffix for match");
            return null;
        }
        String subPath = suffix.substring(0, hashPos);
        String attr = suffix.substring(hashPos + 1);
        SubmodelElement subElem = getSubmodelElementByPathForItem(item, subPath);
        if (subElem == null)
            return null;
        List<Object> values = getSmeAttrValues(subElem, attr);
        return values.isEmpty() ? null : values.get(0);
    }


    private SubmodelElement getSubmodelElementByPathForItem(SubmodelElement item, String path) {
        // Similar to getSubmodelElementByPath, but start from item
        SubmodelElement current = null;
        Submodel container = null;
        if (path.startsWith("."))
            path = path.substring(1);
        for (String token: path.split("\\.")) {
            if (container == null) {
                if (item.getIdShort().equals(token)) {
                    current = item;
                }
                else {
                    return null;
                }
            }
            else {
                current = container.getSubmodelElements().stream()
                        .filter(e -> e.getIdShort().equals(token))
                        .findFirst().orElse(null);
            }
            if (current == null)
                return null;
        }
        return current;
    }


    private boolean compareObjects(Object a, Object b, String op) {
        if (a == null || b == null) {
            switch (op) {
                case "eq":
                    return a == b;
                case "ne":
                    return a != b;
                default:
                    return false;
            }
        }
        try {
            double d1 = a instanceof Number ? ((Number) a).doubleValue() : Double.parseDouble(a.toString());
            double d2 = b instanceof Number ? ((Number) b).doubleValue() : Double.parseDouble(b.toString());
            switch (op) {
                case "eq":
                    return d1 == d2;
                case "ne":
                    return d1 != d2;
                case "gt":
                    return d1 > d2;
                case "ge":
                    return d1 >= d2;
                case "lt":
                    return d1 < d2;
                case "le":
                    return d1 <= d2;
            }
        }
        catch (NumberFormatException e) {}
        boolean bool1, bool2;
        try {
            bool1 = a instanceof Boolean ? (Boolean) a : Boolean.parseBoolean(a.toString());
            bool2 = b instanceof Boolean ? (Boolean) b : Boolean.parseBoolean(b.toString());
            switch (op) {
                case "eq":
                    return bool1 == bool2;
                case "ne":
                    return bool1 != bool2;
                default:
                    return false;
            }
        }
        catch (Exception e) {}
        String s1 = a.toString();
        String s2 = b.toString();
        int cmp = s1.compareTo(s2);
        switch (op) {
            case "eq":
                return cmp == 0;
            case "ne":
                return cmp != 0;
            case "gt":
                return cmp > 0;
            case "ge":
                return cmp >= 0;
            case "lt":
                return cmp < 0;
            case "le":
                return cmp <= 0;
        }
        return false;
    }


    private boolean stringCompareObjects(Object a, Object b, String op) {
        if (a == null || b == null)
            return false;
        String s1 = a.toString();
        String s2 = b.toString();
        switch (op) {
            case "contains":
                return s1.contains(s2);
            case "starts-with":
                return s1.startsWith(s2);
            case "ends-with":
                return s1.endsWith(s2);
            case "regex":
                return Pattern.compile(s2).matcher(s1).matches();
        }
        return false;
    }
}
