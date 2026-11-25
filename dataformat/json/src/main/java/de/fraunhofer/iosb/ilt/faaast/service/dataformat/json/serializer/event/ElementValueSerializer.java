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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.event;

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonFieldNames;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.ser.std.StdSerializer;


/**
 * Mixin for {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue} when used in message bus events. As
 * in this case no context informatino is available it needs to be embedded when serialising.
 */
public class ElementValueSerializer extends StdSerializer<ElementValue> {

    private final ValueSerializer<Object> defaultSerializer;

    public ElementValueSerializer(ValueSerializer<Object> defaultSerializer) {
        this(ElementValue.class, defaultSerializer);
    }


    public ElementValueSerializer(Class<ElementValue> type, ValueSerializer<Object> defaultSerializer) {
        super(type);
        this.defaultSerializer = defaultSerializer;
    }


    @Override
    public void serialize(ElementValue value, JsonGenerator generator, SerializationContext provider) {
        if (value != null) {
            generator.writeStartObject();
            generator.writeStringProperty(JsonFieldNames.EVENT_MODELTYPE, ElementValueMapper.getElementClass(value.getClass()).getSimpleName());
            StringWriter valueJson = new StringWriter();
            JsonGenerator valueGenerator = generator.objectWriteContext().createGenerator(valueJson);
            defaultSerializer.serialize(value, valueGenerator, provider);
            valueGenerator.flush();
            JsonNode contentNode = new ObjectMapper().readTree(valueJson.toString());
            if (Objects.nonNull(contentNode) && contentNode.isArray()) {
                provider.defaultSerializeProperty(JsonFieldNames.EVENT_VALUE, contentNode, generator);
            }
            if (Objects.nonNull(contentNode)
                    && contentNode.size() == 1
                    && contentNode.has(JsonFieldNames.EVENT_VALUE)
                    && contentNode.properties().iterator().next().getValue().isObject()) {
                for (Iterator<Map.Entry<String, JsonNode>> i = ((ObjectNode) contentNode.properties().iterator().next().getValue()).properties().iterator(); i.hasNext();) {
                    Map.Entry<String, JsonNode> entry = i.next();
                    provider.defaultSerializeProperty(entry.getKey(), entry.getValue(), generator);
                }
            }
            else if (Objects.nonNull(contentNode)) {
                for (Iterator<Map.Entry<String, JsonNode>> i = contentNode.properties().iterator(); i.hasNext();) {
                    Map.Entry<String, JsonNode> entry = i.next();
                    provider.defaultSerializeProperty(entry.getKey(), entry.getValue(), generator);
                }
            }
            generator.writeEndObject();
        }
    }

}
