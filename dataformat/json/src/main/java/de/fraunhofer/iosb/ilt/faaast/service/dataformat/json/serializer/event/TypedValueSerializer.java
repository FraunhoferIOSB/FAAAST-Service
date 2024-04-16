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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonFieldNames;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import java.io.IOException;


/**
 * Mixin for {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue} when used in message bus events. As
 * in this case no context informatino is available it needs to be embedded when serialising.
 */
public class TypedValueSerializer extends StdSerializer<TypedValue> {

    public TypedValueSerializer() {
        this(TypedValue.class);
    }


    public TypedValueSerializer(Class<TypedValue> type) {
        super(type);
    }


    @Override
    public void serialize(TypedValue value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeStartObject();
        generator.writeStringField(JsonFieldNames.EVENT_DATATYPE, value.getDataType().getName());
        provider.defaultSerializeField(JsonFieldNames.EVENT_VALUE, value.getValue(), generator);
        generator.writeEndObject();
    }

}
