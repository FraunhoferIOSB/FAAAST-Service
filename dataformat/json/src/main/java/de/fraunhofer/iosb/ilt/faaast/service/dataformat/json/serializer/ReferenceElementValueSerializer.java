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
import io.adminshell.aas.v3.dataformat.core.ReflectionHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import java.io.IOException;


/**
 * Serializer for
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue}
 */
public class ReferenceElementValueSerializer extends StdSerializer<ReferenceElementValue> {

    public ReferenceElementValueSerializer() {
        this(null);
    }


    public ReferenceElementValueSerializer(Class<ReferenceElementValue> type) {
        super(type);
    }


    @Override
    public void serialize(ReferenceElementValue value, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
        if (value != null) {
            if (value.getKeys() == null) {
                generator.writeStartArray();
                generator.writeEndArray();
            }
            else if (value.getKeys().stream().allMatch(x -> x.getType() == KeyElements.GLOBAL_REFERENCE)) {
                // global reference
                generator.writeArray(value.getKeys().stream().map(x -> x.getValue()).toArray(String[]::new), 0, value.getKeys().size());
            }
            else {
                // model reference
                generator.writeStartArray();
                for (Key key: value.getKeys()) {
                    generator.writeStartObject();
                    generator.writeFieldName(JsonFieldNames.REFERENCE_ELEMENT_VALUE_ID_TYPE);
                    if (ReflectionHelper.ENUMS.contains(key.getIdType().getClass())) {
                        generator.writeString(AasUtils.serializeEnumName(key.getIdType().name()));
                    }
                    else {
                        provider.findValueSerializer(Enum.class).serialize(key.getIdType(), generator, provider);
                    }
                    generator.writeFieldName(JsonFieldNames.REFERENCE_ELEMENT_VALUE_TYPE);
                    if (ReflectionHelper.ENUMS.contains(key.getType().getClass())) {
                        generator.writeString(AasUtils.serializeEnumName(key.getType().name()));
                    }
                    else {
                        provider.findValueSerializer(Enum.class).serialize(key.getType(), generator, provider);
                    }
                    generator.writeStringField(JsonFieldNames.REFERENCE_ELEMENT_VALUE_VALUE, key.getValue());
                    generator.writeEndObject();
                }
                generator.writeEndArray();
            }

        }
    }
}
