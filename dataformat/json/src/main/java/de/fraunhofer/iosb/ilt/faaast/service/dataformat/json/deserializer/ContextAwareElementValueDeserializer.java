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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Abstract base deserializer providing context information while deserializing.
 *
 * @param <T> type of ElementValue that should be deserialized
 */
public abstract class ContextAwareElementValueDeserializer<T extends ElementValue> extends StdDeserializer<T> {

    public static final String VALUE_TYPE_CONTEXT = "typeInfoContext";

    /**
     * Fetches type information from {@link com.fasterxml.jackson.databind.DeserializationContext}. If no type
     * information is present, null is returned.
     *
     * @param context deserialization context holding the type information
     * @return type information if present, else null
     */
    protected static TypeInfo getTypeInfo(DeserializationContext context) {
        return context.getAttribute(VALUE_TYPE_CONTEXT) != null && TypeInfo.class.isAssignableFrom(context.getAttribute(VALUE_TYPE_CONTEXT).getClass())
                ? (TypeInfo) context.getAttribute(VALUE_TYPE_CONTEXT)
                : null;
    }


    protected ContextAwareElementValueDeserializer() {
        this(null);
    }


    protected ContextAwareElementValueDeserializer(Class<T> type) {
        super(type);
    }


    @Override
    public T deserialize(JsonParser parser, DeserializationContext context) throws IOException, JacksonException {
        JsonNode node = context.readTree(parser);
        if (isWrapped(node)) {
            return deserializeValue(unwrap(node), context);
        }
        return deserializeValue(node, context);
    }


    /**
     * Check if node is a wrapper node for a value.
     *
     * @param node node to check
     * @return true if the node is a wrapper {@code (defined as node.isObject() && node.size() == 1)}, otherwise false
     */
    protected boolean isWrapped(JsonNode node) {
        return node != null
                && node.isObject()
                && node.size() == 1;
    }


    /**
     * Unwraps a given node, i.e. returns the first child element
     *
     * @param node node to unwrap
     * @return first child node
     */
    protected JsonNode unwrap(JsonNode node) {
        return node.iterator().next();
    }


    /**
     * Deserializes a value from node given current context.
     *
     * @param node node to deserialize
     * @param context deserialization context
     * @return deserialized values
     * @throws IOException if reading node fails
     * @throws JacksonException is deserialization fails
     */
    public abstract T deserializeValue(JsonNode node, DeserializationContext context) throws IOException, JacksonException;


    /**
     * Deserialize children as a map of element values with their idShort as key.
     *
     * @param <T> element value type
     * @param node node to deserialize
     * @param context deserialization context
     * @param type target type
     * @return map of sub-values identified by their idShort as key
     * @throws IOException as reading node fails
     * @throws IllegalArgumentException if no type information can be found
     */
    protected <T extends ElementValue> Map<String, T> deserializeChildren(JsonNode node, DeserializationContext context, Class<T> type) throws IOException {
        Map<String, T> result = new HashMap<>();
        if (node == null) {
            return result;
        }
        TypeInfo typeInfo = getTypeInfo(context);
        if (typeInfo == null) {
            throw new IllegalArgumentException("no type information given in deserialization context");
        }
        Map<String, JsonNode> childNodes = new HashMap<>();
        if (node.isObject()) {
            node.fields().forEachRemaining(x -> childNodes.put(x.getKey(), x.getValue()));
        }
        else if (node.isArray()) {
            Iterator<JsonNode> iterator = node.elements();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> child = iterator.next().fields().next();
                childNodes.put(child.getKey(), child.getValue());
            }
        }
        for (Map.Entry<String, JsonNode> childNode: childNodes.entrySet()) {
            TypeInfo childTypeInfo = (TypeInfo) typeInfo.getElements().get(childNode.getKey());
            if (childTypeInfo == null || childTypeInfo.getType() == null) {
                throw new IllegalArgumentException(String.format("no type information found for element (idShort: %s)", childNode.getKey()));
            }
            result.put(childNode.getKey(), (T) context.setAttribute(VALUE_TYPE_CONTEXT, childTypeInfo)
                    .readTreeAsValue(childNode.getValue(), childTypeInfo.getType()));

        }
        return result;
    }
}
