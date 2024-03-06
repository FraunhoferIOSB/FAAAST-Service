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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.deser.std.ObjectArrayDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ContainerTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import java.io.IOException;
import java.util.Collection;


/**
 * Deserializer for array of {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue}.
 */
public class ValueArrayDeserializer extends ContainerDeserializerBase<Object[]> {

    public ValueArrayDeserializer(ObjectArrayDeserializer src) {
        super(src);
    }


    @Override
    public ElementValue[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return deserialize(p, ctxt, null);
    }


    @Override
    public ElementValue[] deserialize(JsonParser parser, DeserializationContext context, Object[] temp) throws IOException {
        TypeInfo typeInfo = ContextAwareElementValueDeserializer.getTypeInfo(context);
        if (typeInfo == null) {
            return context.reportBadDefinition(Collection.class, "missing type information");
        }
        if (!ContainerTypeInfo.class.isAssignableFrom(typeInfo.getClass())) {
            return context.reportBadDefinition(Collection.class, "type information mismatch - must be of type ContainerTypeInfo");
        }
        JsonNode node = context.readTree(parser);
        if (!node.isArray()) {
            return context.reportBadDefinition(Collection.class, "expected array");
        }
        if (node.size() != typeInfo.getElements().size()) {
            return context.reportBadDefinition(Collection.class,
                    String.format("number of elements mismatch (expected: %d, actual: %d)", typeInfo.getElements().size(), node.size()));
        }
        ElementValue[] result = new ElementValue[node.size()];
        for (int i = 0; i < node.size(); i++) {
            context.setAttribute(ContextAwareElementValueDeserializer.VALUE_TYPE_CONTEXT, typeInfo.getElements().get(i));
            Class<?> type = ((TypeInfo) typeInfo.getElements().get(i)).getType();
            Object element = context.readTreeAsValue(node.get(i), type);
            result[i] = (ElementValue) element;
        }
        return result;
    }


    @Override
    public JsonDeserializer<Object> getContentDeserializer() {
        return null;
    }
}
