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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.BasicEventElementValue;
import java.util.Objects;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;


/**
 * Serializer for {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.BasicEventElementValue}.
 */
public class BasicEventElementValueSerializer extends StdSerializer<BasicEventElementValue> {

    public BasicEventElementValueSerializer() {
        this(null);
    }


    public BasicEventElementValueSerializer(Class<BasicEventElementValue> type) {
        super(type);
    }


    @Override
    public void serialize(BasicEventElementValue value, JsonGenerator generator, SerializationContext context) throws JacksonException {
        if (Objects.nonNull(value)) {
            generator.writeStartObject();
            if (Objects.nonNull(value.getObserved())) {
                context.defaultSerializeProperty(JsonFieldNames.BASIC_EVENT_ELEMENT_OBSERVED, value.getObserved(), generator);
            }
            generator.writeEndObject();
        }
    }

}
