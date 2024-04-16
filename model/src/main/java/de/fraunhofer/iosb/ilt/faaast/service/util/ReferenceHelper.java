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

import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.deserialization.EnumDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.serialization.EnumSerializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.util.ReflectionHelper;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
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
    public static Reference fromKeys(Key... keys) {
        return new DefaultReference.Builder()
                .type((Objects.nonNull(keys) && keys.length > 0)
                        ? determineReferenceType(keys[0])
                        : null)
                .keys(new ArrayList<>(Arrays.asList(keys)))
                .build();
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
                .referredSemanticId(clone(reference.getReferredSemanticId()))
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
        if (!equals(ref1.getReferredSemanticId(), ref2.getReferredSemanticId())) {
            return false;
        }
        if (ref1.getKeys().size() != ref2.getKeys().size()) {
            return false;
        }
        return IntStream.range(0, ref1.getKeys().size())
                .allMatch(x -> equals(ref1.getKeys().get(x), ref2.getKeys().get(x)));
    }


    /**
     * Checks if a given key is compatible with given AAS class.
     *
     * @param key the key to check
     * @param type the expected AAS type
     * @return true if key is compatible with given type, otherwise false
     */
    public static boolean isKeyType(Key key, Class<?> type) {
        if (Objects.isNull(key) || Objects.isNull(type)) {
            return false;
        }
        return type.isAssignableFrom(keyTypeToClass(key.getType()));
    }


    /**
     * Gets a Java interface representing the type provided by key.
     *
     * @param key The KeyElements type
     * @return a Java interface representing the provided KeyElements type or null if no matching Class/interface could
     *         be found. It also returns abstract types like SUBMODEL_ELEMENT or DATA_ELEMENT
     */
    private static Class<?> keyTypeToClass(KeyTypes key) {
        return Stream.concat(ReflectionHelper.INTERFACES.stream(), ReflectionHelper.INTERFACES_WITHOUT_DEFAULT_IMPLEMENTATION.stream())
                .filter(x -> x.getSimpleName().equals(EnumSerializer.serializeEnumName(key.name())))
                .findAny()
                .orElse(null);
    }


    /**
     * Ensures that a given key is compatible to a given AAS class.
     *
     * @param key the key to check
     * @param type the expected AAS type
     * @throws IllegalArgumentException if key is not compatible with given type
     */
    public static void ensureKeyType(Key key, Class<?> type) {
        Ensure.requireNonNull(key, "key must be non-null");
        Class<?> keyClass = keyTypeToClass(key.getType());
        Ensure.requireNonNull(keyClass, String.format("unsupported key type '%s'", key.getType()));
        Ensure.require(
                type.isAssignableFrom(keyClass),
                String.format("key must be compatible to %s (found: %s)", type, keyClass));
    }


    /**
     * Finds the first occuring key with given keyType and returns its value. If no key of given keyType is found null
     * is return.
     *
     * @param reference the reference
     * @param keyType the key type to find
     * @return the value of the key with the given keyType or null if no key with given keyType is found
     */
    public static String findFirstKeyType(Reference reference, KeyTypes keyType) {
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(keyType, "keyType must be non-null");
        return reference.getKeys().stream()
                .filter(x -> Objects.equals(x.getType(), keyType))
                .map(Key::getValue)
                .findFirst()
                .orElse(null);
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
        Reference result = clone(reference);
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
                .referredSemanticId(referredSemanticId)
                .keys(keys)
                .build();
    }


    /**
     * Parses a given string as Reference. If the given string is not a valid reference, null is returned.
     *
     * @param value String representation of the reference
     * @return parsed Reference or null is given value is not a valid Reference
     */
    public static Reference parseReference(String value) {
        return parseReference(value, ReflectionHelper.getDefaultImplementation(Reference.class));
    }


    /**
     * Parses a given string as Reference using the provided implementation of Reference and Key interface. If the given
     * string is not a valid reference, null is returned.
     *
     * @param value String representation of the reference
     * @param referenceType implementation type of Reference interface
     * @return parsed Reference or null is given value is not a valid Reference
     */
    public static Reference parseReference(String value, Class<? extends Reference> referenceType) {
        String reference = value;
        if (reference == null || reference.isBlank()) {
            return null;
        }

        try {
            Reference result = referenceType.getConstructor().newInstance();
            // check if optional [<ReferenceTypes>] is present, if so, check for consistency
            if (reference.startsWith(SQUARE_BRACKET_LEFT)) {
                reference = reference.substring(reference.indexOf(SQUARE_BRACKET_RIGHT) + 1);
            }
            result.setKeys(Stream.of(reference.split(KEY_SEPARATOR))
                    .map(ReferenceHelper::parseKey)
                    .collect(Collectors.toList()));
            if (!result.getKeys().isEmpty()) {
                if (result.getType() == null) {
                    // deduct from first element
                    result.setType(result.getKeys().get(0).getType() == KeyTypes.GLOBAL_REFERENCE
                            ? ReferenceTypes.EXTERNAL_REFERENCE
                            : ReferenceTypes.MODEL_REFERENCE);
                }
                else {
                    // validate against first element
                    if (!isCompatible(result.getKeys().get(0).getType(), result.getType())) {
                        throw new IllegalArgumentException(String.format("invalid reference - reference type '%s' is not compatible with type of first key elemenet '%s'",
                                result.getType(), result.getKeys().get(0)));
                    }
                }
            }
            // check that keys following SubmodelElementList have valid index (i.e. are number >= 0)
            validateSubmodelElementListKeyValues(result.getKeys());
            return result;
        }
        catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new IllegalArgumentException("error parsing reference - could not instantiate reference type", ex);
        }
    }


    private static boolean isCompatible(KeyTypes keyType, ReferenceTypes referenceType) {
        if (keyType == null && referenceType == null) {
            return true;
        }
        if (keyType == null ^ referenceType == null) {
            return false;
        }
        return referenceType == ReferenceTypes.EXTERNAL_REFERENCE
                ? keyType == KeyTypes.GLOBAL_REFERENCE
                : keyType != KeyTypes.GLOBAL_REFERENCE;
    }


    private static void validateSubmodelElementListKeyValues(List<Key> keys) {
        if (keys == null || keys.size() <= 1) {
            return;
        }
        for (int i = 0; i < keys.size() - 1; i++) {
            if (keys.get(i).getType() == KeyTypes.SUBMODEL_ELEMENT_LIST) {
                try {
                    if (Integer.parseInt(keys.get(i + 1).getValue()) < 0) {
                        throw new IllegalArgumentException(String.format("invalid value for key with index %d, expected integer values >= 0, but found '%s'",
                                i, keys.get(i + 1).getValue()));
                    }
                }
                catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(String.format("invalid value for key with index %d, expected integer values >= 0, but found '%s'",
                            i, keys.get(i + 1).getValue()));
                }
            }
        }
    }


    /**
     * Checks if a reference starts with another reference/prefix.Returns true if th prefix is null or empty.
     *
     * @param reference the reference the test if it starts with the prfix
     * @param prefix the prefix the reference has to start with
     * @return true if the reference starts with the prefix
     */
    public static boolean startsWith(Reference reference, Reference prefix) {
        boolean referenceEmpty = Objects.isNull(reference) || Objects.isNull(reference.getKeys()) || reference.getKeys().isEmpty();
        boolean prefixEmpty = Objects.isNull(prefix) || Objects.isNull(prefix.getKeys()) || prefix.getKeys().isEmpty();
        if (prefixEmpty) {
            return true;
        }
        if (referenceEmpty) {
            return false;
        }
        if (reference.getKeys().size() < prefix.getKeys().size()) {
            return false;
        }
        for (int i = 0; i < prefix.getKeys().size(); i++) {
            if (!equals(reference.getKeys().get(i), prefix.getKeys().get(i))) {
                return false;
            }
        }
        return true;
    }


    /**
     * Get the corresponding {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes} to the given class.
     *
     * @param clazz to convert to a KeyTypes
     * @return the corresponding KeyTypes of the class
     */
    public static KeyTypes toKeyType(Class<?> clazz) {
        Class<?> aasInterface = ReflectionHelper.getAasInterface(clazz);
        return aasInterface != null ? KeyTypes.valueOf(EnumDeserializer.deserializeEnumName(aasInterface.getSimpleName())) : null;
    }


    /**
     * Formats a Reference as string.
     *
     * @param reference Reference to serialize
     * @return string representation of the reference for serialization, null if reference is null
     */
    public static String asString(Reference reference) {
        if (reference == null) {
            return null;
        }
        return String.format("[%s]%s", reference.getType(),
                reference.getKeys().stream().map(x -> String.format("(%s)%s", EnumSerializer.serializeEnumName(x.getType().name()), x.getValue()))
                        .collect(Collectors.joining(KEY_SEPARATOR)));
    }


    /**
     * Create an element path out of a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} to a
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}.
     *
     * @param reference reference to the submodel element
     * @return values of the keys of the reference separated by a "."
     */
    public static String toPath(Reference reference) {
        if (reference == null || reference.getKeys().isEmpty()) {
            return "";
        }
        return IdShortPath.fromReference(reference).toString();
    }


    /**
     * Combine a parent reference and a child reference to one reference.
     *
     * @param parent reference of the parent
     * @param child reference of the child
     * @return the combined reference
     */
    public static Reference combine(Reference parent, Reference child) {
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
                    ? toString(reference.getReferredSemanticId(), includeReferenceType, false)
                    : "";
            result = String.format("[%s%s]",
                    toString(reference.getType()),
                    !StringHelper.isBlank(referredSemanticId) ? String.format("- %s -", referredSemanticId)
                            : "");
        }
        result += reference.getKeys().stream()
                .map(x -> String.format("(%s)%s",
                        EnumSerializer.serializeEnumName(x.getType().name()),
                        x.getValue()))
                .collect(Collectors.joining(", "));
        return result;
    }


    private static Key clone(Key key) {
        return newKey(key.getType(), key.getValue());
    }


    /**
     * Tries to automatically determine the reference type of a reference. If reference type is set, this is returned.
     * If reference type is null it is automatically determinde if possible, otherwise is null is returned.
     *
     * @param reference the reference
     * @return the determined reference type
     */
    public static ReferenceTypes determineReferenceType(Reference reference) {
        if (Objects.isNull(reference)) {
            return null;
        }
        if (Objects.nonNull(reference.getType())) {
            return reference.getType();
        }
        if (Objects.isNull(reference.getKeys()) || reference.getKeys().isEmpty() || Objects.isNull(reference.getKeys().get(0))) {
            return null;
        }
        return determineReferenceType(reference.getKeys().get(0));
    }


    private static ReferenceTypes determineReferenceType(Key key) {
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


    static Key newKey(KeyTypes type, String value) {
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
        return KeyTypes.valueOf(EnumDeserializer.deserializeEnumName(keyType));
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
