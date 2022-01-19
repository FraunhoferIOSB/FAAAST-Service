/*
 * Copyright 2022 Fraunhofer IOSB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.OutputModifier;
import java.io.IOException;

public abstract class OutputModifierAwareSerializer<T> extends StdSerializer<T> {
    
    public static final String OUTPUT_MODIFIER_ATTRIBUTE = "outputModifier";
    
    protected OutputModifierAwareSerializer() {
        this(null);
    }
    
    protected OutputModifierAwareSerializer(Class<T> type) {
        super(type);
    }
    
    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        serialize(value, gen, provider,
                provider.getAttribute(OUTPUT_MODIFIER_ATTRIBUTE) != null && OutputModifier.class.isAssignableFrom(provider.getAttribute(OUTPUT_MODIFIER_ATTRIBUTE).getClass())
                ? (OutputModifier) provider.getAttribute(OUTPUT_MODIFIER_ATTRIBUTE)
                : OutputModifier.DEFAULT);
        
    }
    
    public abstract void serialize(T value, JsonGenerator gen, SerializerProvider provider, OutputModifier modifier) throws IOException;
    
}
