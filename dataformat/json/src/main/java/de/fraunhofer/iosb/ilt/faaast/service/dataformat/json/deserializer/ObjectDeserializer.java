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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer;

import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;


/**
 * Generic deserializer determining target type from current typeInfo context and forwarding deserialization.
 */
public class ObjectDeserializer extends StdDeserializer<Object> {

    public ObjectDeserializer() {
        this(Object.class);
    }


    public ObjectDeserializer(Class<Object> type) {
        super(type);
    }


    @Override
    public Object deserialize(JsonParser parser, DeserializationContext context) {
        TypeInfo typeInfo = ContextAwareElementValueDeserializer.getTypeInfo(context);
        return context.readValue(parser, typeInfo.getType());
    }

}
