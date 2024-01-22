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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ContainerTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Deserializer for map with value type {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue}.
 */
public class ValueMapDeserializer extends MapDeserializer {

    public ValueMapDeserializer(MapDeserializer src) {
        super(src);
    }


    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext context,
                                                BeanProperty property)
            throws JsonMappingException {
        return new ValueMapDeserializer((MapDeserializer) super.createContextual(context, property));
    }


    @Override
    public Map<Object, Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return deserialize(p, ctxt, new HashMap<>());
    }


    @Override
    public Map<Object, Object> deserialize(JsonParser parser, DeserializationContext context, Map<Object, Object> result) throws IOException {
        TypeInfo typeInfo = ContextAwareElementValueDeserializer.getTypeInfo(context);
        if (typeInfo == null || !ContainerTypeInfo.class.isAssignableFrom(typeInfo.getClass())) {
            return super.deserialize(parser, context);
        }
        JsonNode node = context.readTree(parser);
        if (!node.isObject()) {
            return context.reportBadDefinition(Collection.class, "expected array");
        }
        if (node.size() != typeInfo.getElements().size()) {
            return context.reportBadDefinition(Collection.class,
                    String.format("number of elements mismatch (expected: %d, actual: %d)", typeInfo.getElements().size(), node.size()));
        }
        Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> element = iterator.next();
            if (!typeInfo.getElements().containsKey(element.getKey())) {
                throw new JsonMappingException(parser, String.format(
                        "found element '%s' during valueOnly deserialization that is not defined by type information",
                        element.getKey()));
            }
            context.setAttribute(ContextAwareElementValueDeserializer.VALUE_TYPE_CONTEXT, typeInfo.getElements().get(element.getKey()));
            Class<?> type = ((TypeInfo) typeInfo.getElements().get(element.getKey())).getType();
            result.put(element.getKey(), context.readTreeAsValue(element.getValue(), type));
        }
        return result;
    }

}
