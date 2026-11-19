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

import de.fraunhofer.iosb.ilt.faaast.service.util.ReflectionHelper;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;


/**
 * Enum Serializer applying the AAS naming strategy.
 */
public class EnumSerializer extends ValueSerializer<Enum> {

    @Override
    public void serialize(Enum value, JsonGenerator gen, SerializationContext context) throws JacksonException {
        if (value != null) {
            if (ReflectionHelper.ENUMS.contains(value.getClass())) {
                gen.writeString(org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.serialization.EnumSerializer.serializeEnumName(value.name()));
            }
            else {
                context.findValueSerializer(Enum.class).serialize(value, gen, context);
            }
        }
    }
}
