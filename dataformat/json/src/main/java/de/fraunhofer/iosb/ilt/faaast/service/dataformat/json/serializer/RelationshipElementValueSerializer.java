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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.RelationshipElementValue;
import java.io.IOException;


/**
 * Serializer for
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.RelationshipElementValue}
 */
public class RelationshipElementValueSerializer extends StdSerializer<RelationshipElementValue> {

    public RelationshipElementValueSerializer() {
        this(null);
    }


    public RelationshipElementValueSerializer(Class<RelationshipElementValue> type) {
        super(type);
    }


    @Override
    public void serialize(RelationshipElementValue value, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
        if (value != null) {
            generator.writeStartObject();
            generator.writeFieldName(JsonFieldNames.RELATIONSHIP_ELEMENT_FIRST);
            provider.defaultSerializeValue(ReferenceElementValue.builder()
                    .keys(value.getFirst())
                    .build(), generator);
            generator.writeFieldName(JsonFieldNames.RELATIONSHIP_ELEMENT_SECOND);
            provider.defaultSerializeValue(ReferenceElementValue.builder()
                    .keys(value.getSecond())
                    .build(), generator);
            generator.writeEndObject();
        }
    }
}
