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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.util;

import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.lang.reflect.Array;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;


/**
 * Utility class for working with OPC UA array values.
 */
public class ArrayHelper {

    private static final String REGEX_ARRAY_INDEX = "\\[(\\d+)\\]";
    private static final String REGEX_ARRAY_EXPRESSION = String.format("^(?>%s)+$", REGEX_ARRAY_INDEX);
    private static final Pattern REGEX_PATTERN_ARRAY_INDEX = Pattern.compile(REGEX_ARRAY_INDEX);

    private ArrayHelper() {}


    /**
     * Parses an array index expression of the form [x][y]...[z].
     *
     * @param indexExpression the expression to parse
     * @return the parse index or null if the expression if null or empty
     * @throws IllegalArgumentException if indexExpression is not valid
     */
    public static int[] parseArrayIndex(String indexExpression) throws IllegalArgumentException {
        if (Objects.isNull(indexExpression) || indexExpression.isBlank()) {
            return new int[0];
        }
        if (!indexExpression.matches(REGEX_ARRAY_EXPRESSION)) {
            throw new IllegalArgumentException(String.format("invalid array index expression (expression: %s, expected format: %s)",
                    indexExpression,
                    REGEX_ARRAY_EXPRESSION));
        }
        return REGEX_PATTERN_ARRAY_INDEX.matcher(indexExpression).results()
                .mapToInt(x -> Integer.parseInt(x.group(1)))
                .toArray();
    }


    /**
     * Converts an index to its string representation of the form [x][y]...[z].
     *
     * @param index the index to convert to string
     * @return the string representation of the index
     */
    public static String indexToString(int... index) {
        return indexToString(index.length, index);
    }


    /**
     * Converts an index to its string representation of the form [x][y]...[z] until a given depth, e.g.depth 1 would
     * return [x], depth 2 [x][y], etc.
     *
     * @param depth the depth/dimension until which to convert to string
     * @param index the index to convert to string
     * @return the string representation of the index
     */
    public static String indexToString(int depth, int... index) {
        Ensure.requireNonNull(depth, "depth must be non-null");
        Ensure.requireNonNull(index, "index must be non-null");
        return Stream.of(index)
                .limit(depth)
                .map(Object::toString)
                .collect(Collectors.joining("", "[", "]"));
    }


    /**
     * Navigates to a given index in a (multidimensional) array.
     *
     * @param obj the initial object
     * @param index the index within the object to navigate to
     * @return the element at given index
     * @throws NullPointerException if any intermediate element on the path is null so that the index cannot fully be
     *             resolved
     * @throws ClassCastException if any intermediate element on the path is not an array so that the index cannot fully
     *             be resolved
     * @throws ArrayIndexOutOfBoundsException if index is out of bounds
     */
    public static Object navigateToIndex(Object obj, int... index) {
        if (Objects.isNull(index)) {
            return obj;
        }
        Object result = obj;
        for (int i = 0; i < index.length - 1; i++) {
            if (Objects.isNull(result)) {
                throw new NullPointerException(String.format(
                        "error accessing array at given index - intermediate element is null (requested index: %s, index with object being null: %s)",
                        indexToString(index),
                        indexToString(i + 1, index)));
            }
            if (!result.getClass().isArray()) {
                throw new ClassCastException(String.format(
                        "error accessing array at given index - intermediate element not an array (requested index: %s, index with non-array object: %s)",
                        indexToString(index),
                        indexToString(i + 1, index)));
            }
            result = Array.get(result, index[i]);
        }
        if (!result.getClass().isArray()) {
            throw new ClassCastException(String.format(
                    "error accessing array at given index - intermediate element not an array (requested index: %s, index with non-array object: %s)",
                    indexToString(index),
                    indexToString(index.length - 1, index)));
        }
        return result;
    }


    /**
     * Sets the given value in the desired element of the given array.
     *
     * @param array The original array.
     * @param newValue The desired value.
     * @param index index to set the value on
     * @throws NullPointerException if an intermediate element is null
     * @throws ClassCastException if an intermediate element is not an array
     * @throws ArrayIndexOutOfBoundsException if index is out of bounds
     */
    public static void setArrayElement(Object array, Object newValue, int... index) {
        if (isValidArrayIndex(index)) {
            Array.set(navigateToIndex(array, index), index[index.length - 1], newValue);
        }
    }


    /**
     * Gets the given value at array index of the given array. If no index is given the array itself is returned.
     *
     * @param array The original array.
     * @param index the index to get the element at
     * @return element at the index
     * @throws NullPointerException if an intermediate element is null
     * @throws ClassCastException if an intermediate element is not an array
     * @throws ArrayIndexOutOfBoundsException if index is out of bounds
     */
    public static Object getArrayElement(Object array, int... index) {
        if (!isValidArrayIndex(index)) {
            return array;
        }
        return Array.get(navigateToIndex(array, index), index[index.length - 1]);
    }


    /**
     * Unwraps value in terms of returning the sub-value based on index if set, otherwise returns unmodified value.
     *
     * @param dataValue the value to process
     * @param index the index to use
     * @return the unwrapped value
     */
    public static Variant unwrapValue(DataValue dataValue, int... index) {
        return isValidArrayIndex(index)
                ? new Variant(getArrayElement(dataValue.getValue().getValue(), index))
                : dataValue.getValue();
    }


    /**
     * Unwraps value in terms of returning the sub-value based on index if set, otherwise returns unmodified value.
     *
     * @param dataValue the value to process
     * @param indexExpression the index expression
     * @return the unwrapped value
     */
    public static Variant unwrapValue(DataValue dataValue, String indexExpression) {
        return unwrapValue(dataValue, parseArrayIndex(indexExpression));
    }


    /**
     * Wraps the elementVaue within its array value is required, otherwise does nothing.
     *
     * @param arrayValue the array value, not required if index is null
     * @param elementValue the value of the element
     * @param index the index to use
     * @return the arrayValue with updated elementValue if index is set, otherwise the unchanged elementValue.
     */
    public static Variant wrapValue(DataValue arrayValue, Variant elementValue, int... index) {
        if (isValidArrayIndex(index)) {
            setArrayElement(arrayValue.getValue().getValue(), elementValue.getValue(), index);
            return arrayValue.getValue();
        }
        return elementValue;
    }


    /**
     * Return wether a valid array index has been defined for this provider.
     *
     * @param index the index to check
     * @return true if there is a valid array index, false otherwise
     */
    public static boolean isValidArrayIndex(int... index) {
        return Objects.nonNull(index) && index.length > 0;
    }


    /**
     * Return wether a valid array index has been defined for this provider.
     *
     * @param indexExpression the index expression to check
     * @return true if there is a valid array index, false otherwise
     */
    public static boolean isValidArrayIndex(String indexExpression) {
        return isValidArrayIndex(parseArrayIndex(indexExpression));
    }
}
