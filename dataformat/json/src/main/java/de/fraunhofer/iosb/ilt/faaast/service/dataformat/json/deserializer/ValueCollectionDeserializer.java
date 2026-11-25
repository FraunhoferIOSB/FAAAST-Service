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

import de.fraunhofer.iosb.ilt.faaast.service.typing.ContainerTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import java.util.ArrayList;
import java.util.Collection;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.jdk.CollectionDeserializer;


/**
 * Deserializer for collection of {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue}.
 */
public class ValueCollectionDeserializer extends CollectionDeserializer {

    public ValueCollectionDeserializer(CollectionDeserializer src) {
        super(src);
    }


    @Override
    public CollectionDeserializer createContextual(DeserializationContext context,
                                                   BeanProperty property) {
        return new ValueCollectionDeserializer(super.createContextual(context, property));
    }


    @Override
    public Collection<Object> deserialize(JsonParser p, DeserializationContext ctxt) {
        return deserialize(p, ctxt, new ArrayList<>());
    }


    @Override
    public Collection<Object> deserialize(JsonParser parser, DeserializationContext context, Collection<Object> result) {
        TypeInfo typeInfo = ContextAwareElementValueDeserializer.getTypeInfo(context);
        if (typeInfo == null || !ContainerTypeInfo.class.isAssignableFrom(typeInfo.getClass())) {
            return super.deserialize(parser, context, result);
        }
        JsonNode node = context.readTree(parser);
        if (!node.isArray()) {
            return context.reportBadDefinition(Collection.class, "expected array");
        }
        if (node.size() != typeInfo.getElements().size()) {
            return context.reportBadDefinition(Collection.class,
                    String.format("number of elements mismatch (expected: %d, actual: %d)", typeInfo.getElements().size(), node.size()));
        }
        for (int i = 0; i < node.size(); i++) {
            context.setAttribute(ContextAwareElementValueDeserializer.CONTEXT_TYPE_INFO, typeInfo.getElements().get(i));
            Class<?> type = ((TypeInfo) typeInfo.getElements().get(i)).getType();
            Object element = context.readTreeAsValue(node.get(i), type);
            result.add(element);
        }
        return result;
    }

}
