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
package de.fraunhofer.iosb.ilt.faaast.service.serialization.json.deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public abstract class ContextAwareElementValueDeserializer<T extends ElementValue> extends StdDeserializer<T> {

    public static final String VALUE_TYPE_CONTEXT = "typeInfoContext";

    protected static TypeInfo getTypeInfo(DeserializationContext context) {
        return context.getAttribute(VALUE_TYPE_CONTEXT) != null && TypeInfo.class.isAssignableFrom(context.getAttribute(VALUE_TYPE_CONTEXT).getClass())
                ? (TypeInfo) context.getAttribute(VALUE_TYPE_CONTEXT)
                : null;
    }


    public ContextAwareElementValueDeserializer() {
        this(null);
    }


    public ContextAwareElementValueDeserializer(Class<T> type) {
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


    protected boolean isWrapped(JsonNode node) {
        return node != null
                && node.isObject()
                && node.size() == 1;
    }


    protected JsonNode unwrap(JsonNode node) {
        return node.iterator().next();
    }


    public abstract T deserializeValue(JsonNode node, DeserializationContext context) throws IOException, JacksonException;


    protected <T extends ElementValue> Map<String, T> deserializeChildren(JsonNode node, DeserializationContext context, Class<T> type) throws IOException {
        Map<String, T> result = new HashMap<>();
        if (node == null) {
            return result;
        }
        TypeInfo typeInfo = getTypeInfo(context);
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
                throw new RuntimeException(String.format("no type information found for element (idShort: %s)", childNode.getKey()));
            }
            result.put(childNode.getKey(), (T) context.setAttribute(VALUE_TYPE_CONTEXT, childTypeInfo)
                    .readTreeAsValue(childNode.getValue(), childTypeInfo.getType()));

        }
        return result;
    }
}
