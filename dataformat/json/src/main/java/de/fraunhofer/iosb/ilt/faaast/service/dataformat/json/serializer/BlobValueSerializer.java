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
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonFieldNames;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.BlobValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import java.io.IOException;


/**
 * Serializer for {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.BlobValue}.
 */
public class BlobValueSerializer extends ModifierAwareSerializer<BlobValue> {

    public BlobValueSerializer() {
        this(null);
    }


    public BlobValueSerializer(Class<BlobValue> type) {
        super(type);
    }


    @Override
    public void serialize(BlobValue value, JsonGenerator generator, SerializerProvider provider, Level level, Extent extend) throws IOException, JsonProcessingException {
        if (value != null) {
            generator.writeStartObject();
            generator.writeStringField(JsonFieldNames.BLOB_VALUE_CONTENT_TYPE, value.getContentType());
            if (extend == Extent.WITH_BLOB_VALUE) {
                generator.writeStringField(JsonFieldNames.BLOB_VALUE_VALUE, new String(EncodingHelper.base64Encode(value.getValue())));
            }
            generator.writeEndObject();
        }
    }
}
