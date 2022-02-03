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

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Objects;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeContext;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;


public abstract class ContextAwareElementValueDeserializer<T extends ElementValue> extends StdDeserializer<T> {

    public static final String CURRENT_ELEMENT_PATH = "currentElementPath";
    public static final String VALUE_TYPE_CONTEXT = "valueDeserializationContext";

    protected static Stack<String> getCurrentElementPath(DeserializationContext context) {
        if (context.getAttribute(CURRENT_ELEMENT_PATH) == null || !Stack.class.isAssignableFrom(context.getAttribute(CURRENT_ELEMENT_PATH).getClass())) {
            context.setAttribute(CURRENT_ELEMENT_PATH, new Stack<String>());
        }
        return (Stack<String>) context.getAttribute(CURRENT_ELEMENT_PATH);
    }


    protected static TypeInfo getCurrentTypeInfo(DeserializationContext context) {
        TypeContext valueContext = getValueContext(context);
        Stack<String> currentElementPath = getCurrentElementPath(context);
        if (currentElementPath.isEmpty()) {
            return valueContext.getRootInfo();
        }
        List<String> path = new ArrayList<>(currentElementPath);
        Optional<TypeInfo> elementType = valueContext.getTypeInfos().stream()
                .filter(x -> Objects.equal(x.getIdShortPath(), path))
                .findFirst();
        if (elementType.isPresent()) {
            return elementType.get();
        }
        throw new IllegalStateException(String.format("missing type information for element path '%s'", String.join(".", path)));
    }


    protected static TypeContext getValueContext(DeserializationContext context) {
        return context.getAttribute(VALUE_TYPE_CONTEXT) != null && TypeContext.class.isAssignableFrom(context.getAttribute(VALUE_TYPE_CONTEXT).getClass())
                ? (TypeContext) context.getAttribute(VALUE_TYPE_CONTEXT)
                : TypeContext.builder().build();
    }


    public ContextAwareElementValueDeserializer() {
        this(null);
    }


    public ContextAwareElementValueDeserializer(Class<T> type) {
        super(type);
    }


    protected <T extends ElementValue> Map<String, T> deserializeChildren(JsonNode node, DeserializationContext context, Class<T> type) throws IOException {
        Map<String, T> result = new HashMap<>();
        if (node == null) {
            return result;
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
        TypeContext valueContext = getValueContext(context);
        for (Map.Entry<String, JsonNode> childNode: childNodes.entrySet()) {
            Stack<String> currentElementPath = getCurrentElementPath(context);
            currentElementPath.push(childNode.getKey());
            TypeInfo childTypeInfo = valueContext.getTypeInfoByPath(currentElementPath);
            if (childTypeInfo != null && ElementValue.class.isAssignableFrom(childTypeInfo.getValueType())) {
                result.put(childNode.getKey(), (T) context.readTreeAsValue(childNode.getValue(), childTypeInfo.getValueType()));
            }
            else {
                result.put(childNode.getKey(), context.readTreeAsValue(childNode.getValue(), type));
            }
            currentElementPath.pop();
        }
        return result;
    }
}
