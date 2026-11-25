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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;


/**
 * Abstract base serializer providing context information while serializing.
 *
 * @param <T> type that should be serialized
 */
public abstract class ModifierAwareSerializer<T> extends StdSerializer<T> {

    public static final String LEVEL = "level";
    public static final String EXTEND = "extend";

    protected ModifierAwareSerializer() {
        this(null);
    }


    protected ModifierAwareSerializer(Class<T> type) {
        super(type);
    }


    @Override
    public void serialize(T value, JsonGenerator gen, SerializationContext context) {
        serialize(value, gen, context,
                context.getAttribute(LEVEL) != null && Level.class.isAssignableFrom(context.getAttribute(LEVEL).getClass())
                        ? (Level) context.getAttribute(LEVEL)
                        : Level.DEFAULT,
                context.getAttribute(EXTEND) != null && Extent.class.isAssignableFrom(context.getAttribute(EXTEND).getClass())
                        ? (Extent) context.getAttribute(EXTEND)
                        : Extent.DEFAULT);

    }


    /**
     * Serializes given value using provided context information level and extent.
     *
     * @param value value to serialize
     * @param generator generator used to generate JSON output
     * @param provider provider for accessing other serializers
     * @param level detail level of serialization
     * @param extend detail extent of serialization
     * @is serialization fails
     */
    public abstract void serialize(T value, JsonGenerator generator, SerializationContext provider, Level level, Extent extend);

}
