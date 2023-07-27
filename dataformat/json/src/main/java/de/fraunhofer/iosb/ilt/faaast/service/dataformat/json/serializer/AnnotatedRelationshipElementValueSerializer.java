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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonFieldNames;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import java.io.IOException;
import java.util.Map;


/**
 * Serializer for {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.AnnotatedRelationshipElementValue}.
 */
public class AnnotatedRelationshipElementValueSerializer extends StdSerializer<AnnotatedRelationshipElementValue> {

    public AnnotatedRelationshipElementValueSerializer() {
        this(null);
    }


    public AnnotatedRelationshipElementValueSerializer(Class<AnnotatedRelationshipElementValue> type) {
        super(type);
    }


    @Override
    public void serialize(AnnotatedRelationshipElementValue value, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
        if (value != null) {
            generator.writeStartObject();
            provider.defaultSerializeField(JsonFieldNames.ANNOTATED_RELATIONSHIP_ELEMENT_VALUE_FIRST, value.getFirst(), generator);
            provider.defaultSerializeField(JsonFieldNames.ANNOTATED_RELATIONSHIP_ELEMENT_VALUE_SECOND, value.getSecond(), generator);
            generator.writeFieldName(JsonFieldNames.ANNOTATED_RELATIONSHIP_ELEMENT_VALUE_ANNOTATION);
            generator.writeStartArray();
            for (Map.Entry<String, DataElementValue> annotation: value.getAnnotations().entrySet()) {
                generator.writeStartObject();
                generator.writeObjectField(annotation.getKey(), annotation.getValue());
                generator.writeEndObject();
            }
            generator.writeEndArray();
            generator.writeEndObject();
        }
    }
}
