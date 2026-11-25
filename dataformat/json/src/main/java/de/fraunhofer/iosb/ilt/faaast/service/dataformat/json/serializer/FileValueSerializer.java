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

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonFieldNames;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.FileValue;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;


/**
 * Serializer for {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.FileValue}.
 */
public class FileValueSerializer extends StdSerializer<FileValue> {

    public FileValueSerializer() {
        this(null);
    }


    public FileValueSerializer(Class<FileValue> type) {
        super(type);
    }


    @Override
    public void serialize(FileValue value, JsonGenerator generator, SerializationContext context) {
        if (value != null) {
            generator.writeStartObject();
            if (value.getContentType() != null) {
                generator.writeStringProperty(JsonFieldNames.FILE_VALUE_CONTENT_TYPE, value.getContentType());
            }
            if (value.getValue() != null) {
                generator.writeStringProperty(JsonFieldNames.FILE_VALUE_VALUE, value.getValue());
            }
            generator.writeEndObject();
        }
    }
}
