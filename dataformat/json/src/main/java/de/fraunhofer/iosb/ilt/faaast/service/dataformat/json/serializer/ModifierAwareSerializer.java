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
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import java.io.IOException;


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
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        serialize(value, gen, provider,
                provider.getAttribute(LEVEL) != null && Level.class.isAssignableFrom(provider.getAttribute(LEVEL).getClass())
                        ? (Level) provider.getAttribute(LEVEL)
                        : Level.DEFAULT,
                provider.getAttribute(EXTEND) != null && Extent.class.isAssignableFrom(provider.getAttribute(EXTEND).getClass())
                        ? (Extent) provider.getAttribute(EXTEND)
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
     * @throws IOException is serialization fails
     */
    public abstract void serialize(T value, JsonGenerator generator, SerializerProvider provider, Level level, Extent extend) throws IOException;

}
