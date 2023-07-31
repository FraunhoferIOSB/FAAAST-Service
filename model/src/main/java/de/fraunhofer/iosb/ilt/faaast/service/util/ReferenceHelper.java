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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import static org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils.keyTypeToClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.ReflectionHelper;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to work with AAS references.
 */
public class ReferenceHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceHelper.class);
    private static final String BRACKET_LEFT = "(";
    private static final String BRACKET_RIGHT = ")";
    private static final String KEY_SEPARATOR = ", ";
    private static final String REFERRED_SEMANTIC_ID_END = " -";
    private static final String REFERRED_SEMANTIC_ID_START = "- ";
    private static final String SQUARE_BRACKET_LEFT = "[";
    private static final String SQUARE_BRACKET_RIGHT = "]";
    private static final Map<ReferenceTypes, String> REFERENCE_TYPE_REPRESENTATION = Map.of(
            ReferenceTypes.EXTERNAL_REFERENCE, "ExternalRef",
            ReferenceTypes.MODEL_REFERENCE, "ModelRef");

    private ReferenceHelper() {}


    /**
     * Converst a list of key to a reference. If possible, the key type is automatically determined or set to null if
     * this not possible.
     *
     * @param keys which are converted to a reference
     * @return the reference with the keys
     */
    public static Reference build(List<Key> keys) {
        return new DefaultReference.Builder()
                .type((Objects.nonNull(keys) && !keys.isEmpty())
                        ? determineReferenceType(keys.get(0))
                        : null)
                .keys(keys)
                .build();
    }


    /**
     * Converst a list of key to a reference. If possible, the key type is automatically determined or set to null if
     * this not possible.
     *
     * @param keys which are converted to a reference
     * @return the reference with the keys
     */
    public static Reference build(Key... keys) {
        return build(Arrays.asList(keys));
    }


    /**
     * Create a reference for an {@link Identifiable}.
     *
     * @param id of the identifiable
     * @param clazz of the identifiable
     * @return reference of the identifiable
     */
    public static Reference build(String id, Class<?> clazz) {
        return build(id, toKeyType(clazz));
    }


    /**
     * Create a reference for an {@link Identifiable}.
     *
     * @param id of the identifiable
     * @param keyType the key type
     * @return reference of the identifiable
     */
    public static Reference build(String id, KeyTypes keyType) {
        return build(List.of(newKey(keyType, id)));
    }


    /**
     * Builds a reference identifying a submodel within an AAS.
     *
     * @param aasIdentifier the AAS identifier, set to null if not AAS should be included
     * @param submodelIdentifier the submodel identifier
     * @return a reference to the submodel element
     */
    public static Reference build(String aasIdentifier, String submodelIdentifier) {
        List<Key> keys = new ArrayList<>();
        if (!StringHelper.isEmpty(aasIdentifier)) {
            keys.add(newKey(KeyTypes.ASSET_ADMINISTRATION_SHELL, aasIdentifier));
        }
        if (!StringHelper.isEmpty(submodelIdentifier)) {
            keys.add(newKey(KeyTypes.SUBMODEL, submodelIdentifier));
        }
        return build(keys);
    }


    /**
     * Builds a reference identifying a submodel element.
     *
     * @param aasIdentifier the AAS identifier, set to null if not AAS should be included
     * @param submodelIdentifier the submodel identifier
     * @param submodelElements the types and idShort of the submodel elements
     * @return a reference to the submodel element
     */
    public static Reference build(String aasIdentifier, String submodelIdentifier, List<Key> submodelElements) {
        List<Key> keys = new ArrayList<>();
        if (!StringHelper.isEmpty(aasIdentifier)) {
            keys.add(newKey(KeyTypes.ASSET_ADMINISTRATION_SHELL, aasIdentifier));
        }
        if (!StringHelper.isEmpty(submodelIdentifier)) {
            keys.add(newKey(KeyTypes.SUBMODEL, submodelIdentifier));
        }
        keys.addAll(submodelElements);
        return build(keys);
    }


    /**
     * Builds a reference identifying a submodel element. The retured reference will use
     * {@link KeyTypes#SUBMODEL_ELEMENT} for all elements.
     *
     * <p>ATTENTION! This might build incorrect references as elements within a {@link SubmodelElementList} require the
     * key.value to be set to the index instead of idShort. However, this method is not able to do this as it does not
     * know the concrete types of the submodel elements. Therefore, it is up to the user to provide the index instead of
     * idShort in these cases.
     *
     * @param aasIdentifier the AAS identifier
     * @param submodelIdentifier the submodel identifier
     * @param submodelElementIdshorts the submodel element idShort path
     * @return a reference to the submodel element
     */
    public static Reference build(String aasIdentifier, String submodelIdentifier, String... submodelElementIdshorts) {
        return ReferenceHelper.build(aasIdentifier, submodelIdentifier,
                Stream.of(submodelElementIdshorts)
                        .map(x -> newKey(KeyTypes.SUBMODEL_ELEMENT, x))
                        .collect(Collectors.toList()));
    }


    /**
     * TODO.
     *
     * @param reference TODO
     * @return TODO
     */
    public static Reference normalize(Reference reference) {
        if (Objects.isNull(reference)
                || Objects.isNull(reference.getKeys())
                || reference.getKeys().size() < 2) {
            return reference;
        }
        // AAS -> Submodel      2
        // SUbmodel             1
        // Property             0
        int i = 1;
        for (; i < reference.getKeys().size(); i++) {
            Class<?> type = keyTypeToClass(reference.getKeys().get(i).getType());
            if (Objects.isNull(type) || !Identifiable.class.isAssignableFrom(type)) {
                break;
            }
        }
        Reference result = clone(reference);
        result.getKeys().subList(0, i - 1).clear();
        return result;
    }


    /**
     * Creates a deep copy of a reference.
     *
     * @param reference the reference to clone
     * @return the cloned reference or nulll if the reference is null
     */
    public static Reference clone(Reference reference) {
        if (Objects.isNull(reference)) {
            return null;
        }
        return new DefaultReference.Builder()
                .referredSemanticID(clone(reference.getReferredSemanticID()))
                .type(reference.getType())
                .keys(reference.getKeys().stream()
                        .map(ReferenceHelper::clone)
                        .collect(Collectors.toList()))
                .build();
    }


    /**
     * Compares if two {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} are equal. This also works when keys
     * have different but compatibale {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes}, e.g.
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes#SUBMODEL_ELEMENT} and
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes#PROPERTY}.
     *
     * @param ref1 the first reference
     * @param ref2 the second reference
     * @return true if the two reference are considered equals, false otherwise
     */
    public static boolean equals(Reference ref1, Reference ref2) {
        boolean ref1Empty = Objects.isNull(ref1) || Objects.isNull(ref1.getKeys()) || ref1.getKeys().isEmpty();
        boolean ref2Empty = Objects.isNull(ref2) || Objects.isNull(ref2.getKeys()) || ref2.getKeys().isEmpty();
        if (ref1Empty && ref2Empty) {
            return true;
        }
        if (ref1Empty != ref2Empty) {
            return false;
        }
        if (!equals(ref1.getReferredSemanticID(), ref2.getReferredSemanticID())) {
            return false;
        }
        if (ref1.getKeys().size() != ref2.getKeys().size()) {
            return false;
        }
        return IntStream.range(0, ref1.getKeys().size())
                .allMatch(x -> equals(ref1.getKeys().get(x), ref2.getKeys().get(x)));
    }


    /**
     * Gets the reference to the parent element of the element addressed by given reference.
     *
     * @param reference the reference to the element to find the parent for
     * @return The reference to the parent element of the element addressed by given reference. If no parent exists null
     *         is returned.
     * @throws IllegalArgumentException if reference is null
     */
    public static Reference getParent(Reference reference) {
        Ensure.requireNonNull(reference, "referenec must be non-null");
        if (Objects.isNull(reference.getKeys()) || reference.getKeys().size() < 2) {
            return null;
        }
        Reference result = AasUtils.clone(reference);
        result.getKeys().remove(result.getKeys().size() - 1);
        return result;
    }


    /**
     * Gets the root key of the reference.
     *
     * @param reference the reference to the get the root key for
     * @return The root key of the reference or null is reference does not contain at least one key
     */
    public static Key getRoot(Reference reference) {
        if (Objects.isNull(reference) || Objects.isNull(reference.getKeys()) || reference.getKeys().isEmpty()) {
            return null;
        }
        return reference.getKeys().get(0);
    }


    /**
     * Checks if a given reference is null or empty.
     *
     * @param reference the reference to check
     * @return true if reference is null or empty, otherwise false
     */
    public static boolean isNullOrEmpty(Reference reference) {
        return Objects.isNull(reference)
                || Objects.isNull(reference.getKeys())
                || reference.getKeys().isEmpty();
    }


    /**
     * Parses a reference from string.
     *
     * @param reference the string representation of the reference to parse or null is the input is null or blank
     * @return the parsed reference
     * @throws IllegalArgumentException if the reference does not contain at least one key
     */
    public static Reference parse(String reference) {
        if (StringHelper.isBlank(reference)) {
            return null;
        }
        ReferenceTypes referenceType = null;
        Reference referredSemanticId = null;
        int position = 0;
        if (reference.startsWith(SQUARE_BRACKET_LEFT)) {
            String temp = reference.substring(1, reference.lastIndexOf(SQUARE_BRACKET_RIGHT));
            if (temp.contains(REFERRED_SEMANTIC_ID_START) && temp.contains(REFERRED_SEMANTIC_ID_END)) {
                referenceType = parseReferenceType(temp.substring(0, temp.indexOf(REFERRED_SEMANTIC_ID_START)));
                referredSemanticId = parse(temp.substring(
                        temp.indexOf(REFERRED_SEMANTIC_ID_START) + REFERRED_SEMANTIC_ID_START.length(),
                        temp.indexOf(REFERRED_SEMANTIC_ID_END)));
            }
            else {
                referenceType = parseReferenceType(temp);
            }
            position = reference.lastIndexOf(SQUARE_BRACKET_RIGHT) + 1;
        }

        List<Key> keys = Stream.of(reference.substring(position).split(KEY_SEPARATOR))
                .map(ReferenceHelper::parseKey)
                .collect(Collectors.toList());
        if (keys.isEmpty()) {
            throw new IllegalArgumentException("reference must contain at least one key");
        }
        if (Objects.isNull(referenceType)) {
            referenceType = determineReferenceType(keys.get(0));
        }
        return new DefaultReference.Builder()
                .type(referenceType)
                .referredSemanticID(referredSemanticId)
                .keys(keys)
                .build();
    }


    /**
     * Get the corresponding {@link KeyElements} to the given class.
     *
     * @param clazz to convert to a KeyElement
     * @return the corresponding KeyElement of the class
     */
    public static KeyTypes toKeyType(Class<?> clazz) {
        Class<?> aasInterface = ReflectionHelper.getAasInterface(clazz);
        return aasInterface != null ? KeyTypes.valueOf(AasUtils.deserializeEnumName(aasInterface.getSimpleName())) : null;
    }


    /**
     * Combines a list of keys of a child element with a parent to a reference.
     *
     * @param keys of the child
     * @param parentId of the parent
     * @param parentClass type of the parent
     * @return the full reference to the child element
     */
    public static Reference toReference(List<Key> keys, String parentId, Class<?> parentClass) {
        Reference parentReference = ReferenceHelper.build(parentId, parentClass);
        Reference childReference = new DefaultReference.Builder()
                .keys(keys)
                .build();
        return toReference(parentReference, childReference);
    }


    /**
     * Combine a parent reference and a child reference to one reference.
     *
     * @param parent reference of the parent
     * @param child reference of the child
     * @return the combined reference
     */
    public static Reference toReference(Reference parent, Reference child) {
        List<Key> keys = new ArrayList<>();
        ReferenceTypes referenceType = null;
        if (Objects.nonNull(parent) && Objects.nonNull(parent.getKeys())) {
            keys.addAll(parent.getKeys());
            referenceType = parent.getType();
        }
        if (Objects.nonNull(child) && Objects.nonNull(child.getKeys())) {
            keys.addAll(child.getKeys());
        }
        if (Objects.isNull(referenceType)) {
            referenceType = determineReferenceType(keys.get(0));
        }
        return new DefaultReference.Builder()
                .type(referenceType)
                .keys(keys)
                .build();
    }


    /**
     * Serializes a {@link Reference} to string including reference type information and referredSemanticId.
     *
     * @param reference the reference to serialize
     * @return the serialized reference or null if reference is null, reference.keys is null or reference does not
     *         contain any keys
     */
    public static String toString(Reference reference) {
        return toString(reference, true, true);
    }


    /**
     * Serializes a {@link Reference} to string.
     *
     * @param reference the reference to serialize
     * @param includeReferenceType if reference type information should be included
     * @param includeReferredSemanticId if referred semanticId should be included
     * @return the serialized reference or null if reference is null, reference.keys is null or reference does not
     *         contain any keys
     */
    public static String toString(Reference reference, boolean includeReferenceType, boolean includeReferredSemanticId) {
        if (Objects.isNull(reference) || Objects.isNull(reference.getKeys()) || reference.getKeys().isEmpty()) {
            return null;
        }
        String result = "";
        if (includeReferenceType) {
            String referredSemanticId = includeReferredSemanticId
                    ? toString(reference.getReferredSemanticID(), includeReferenceType, false)
                    : "";
            result = String.format("[%s%s]",
                    toString(reference.getType()),
                    !StringHelper.isBlank(referredSemanticId) ? String.format("- %s -", referredSemanticId)
                            : "");
        }
        result += reference.getKeys().stream()
                .map(x -> String.format("(%s)%s",
                        AasUtils.serializeEnumName(x.getType().name()),
                        x.getValue()))
                .collect(Collectors.joining(", "));
        return result;
    }


    private static Key clone(Key key) {
        return newKey(key.getType(), key.getValue());
    }


    private static ReferenceTypes determineReferenceType(Key key) {
        if (Objects.isNull(key)) {
            return null;
        }
        switch (key.getType()) {
            case FRAGMENT_REFERENCE:
            case GLOBAL_REFERENCE:
                return ReferenceTypes.EXTERNAL_REFERENCE;
            default:
                return ReferenceTypes.MODEL_REFERENCE;
        }
    }


    private static boolean equals(Key key1, Key key2) {
        if (Objects.isNull(key1) != Objects.isNull(key2)) {
            return false;
        }
        if (Objects.isNull(key1)) {
            return true;
        }
        if (!Objects.equals(key1.getValue(), key2.getValue())) {
            return false;
        }
        if (Objects.equals(key1.getType(), key2.getType())) {
            return true;
        }
        Class<?> type1 = keyTypeToClass(key1.getType());
        Class<?> type2 = keyTypeToClass(key2.getType());
        if (Objects.isNull(type1) != Objects.isNull(type2)
                || Objects.isNull(type1)
                || (!(type1.isAssignableFrom(type2) || type2.isAssignableFrom(type1)))) {
            LOGGER.warn(String.format(
                    "encountered reference keys with same value but incompatible types (key value: %s, key type 1: %s, key type 2: %s)",
                    key1.getValue(),
                    type1,
                    type2));
        }
        return true;
    }


    private static Key newKey(KeyTypes type, String value) {
        return new DefaultKey.Builder()
                .type(type)
                .value(value)
                .build();
    }


    private static Key parseKey(String key) {
        Ensure.requireNonNull(key, "key must be non-null");
        if (!key.startsWith(BRACKET_LEFT) || !key.contains(BRACKET_RIGHT)) {
            throw new IllegalArgumentException("key is missing type information");
        }
        String type = key.substring(1, key.indexOf(BRACKET_RIGHT));
        String value = key.substring(key.indexOf(BRACKET_RIGHT) + 1);
        return newKey(parseKeyType(type), value);
    }


    private static KeyTypes parseKeyType(String keyType) {
        return KeyTypes.valueOf(AasUtils.deserializeEnumName(keyType));
    }


    private static ReferenceTypes parseReferenceType(String referenceType) {
        return REFERENCE_TYPE_REPRESENTATION.entrySet().stream()
                .filter(x -> Objects.equals(x.getValue(), referenceType))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Unsupported reference type '%s'", referenceType)));
    }


    private static String toString(ReferenceTypes referenceType) {
        if (!REFERENCE_TYPE_REPRESENTATION.containsKey(referenceType)) {
            throw new IllegalArgumentException(String.format("Unsupported reference type '%s'", referenceType));
        }
        return REFERENCE_TYPE_REPRESENTATION.get(referenceType);
    }

}
