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
package de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.RelationshipElementValue;
import java.io.IOException;
import java.util.Map;

public class AnnotatedRelationshipElementValueSerializer extends OutputModifierAwareSerializer<AnnotatedRelationshipElementValue> {

    public AnnotatedRelationshipElementValueSerializer() {
        this(null);
    }

    public AnnotatedRelationshipElementValueSerializer(Class<AnnotatedRelationshipElementValue> type) {
        super(type);
    }

    @Override
    public void serialize(AnnotatedRelationshipElementValue value, JsonGenerator generator, SerializerProvider provider, OutputModifier modifier) throws IOException, JsonProcessingException {
        if (value != null) {
            generator.writeStartObject();
            provider.defaultSerializeField("first", ReferenceElementValue.builder()
                    .keys(value.getFirst())
                    .build(), generator);
            provider.defaultSerializeField("second", ReferenceElementValue.builder()
                    .keys(value.getSecond())
                    .build(), generator);
            generator.writeFieldName("annotation");
            generator.writeStartArray();
            for (Map.Entry<String, DataElementValue> annotation : value.getAnnotations().entrySet()) {
                generator.writeStartObject();
                provider.defaultSerializeField(annotation.getKey(), annotation.getValue(), generator);
                generator.writeEndObject();
            }
            generator.writeEndArray();
            generator.writeEndObject();
        }
    }
}
