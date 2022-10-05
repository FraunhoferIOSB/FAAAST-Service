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
package de.fraunhofer.iosb.ilt.faaast.service.config.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Utility class for serializing Java Map instances as key-value pairs in JSON using Jackson framework.
 */
public class MapToCoupleArraySerializer extends JsonSerializer<Map<?, ?>> {

    public static final String DEFAULT_TAG_KEY = "key";
    public static final String DEFAULT_TAG_VALUE = "value";

    private final String tagKey;
    private final String tagValue;

    public MapToCoupleArraySerializer() {
        this.tagKey = DEFAULT_TAG_KEY;
        this.tagValue = DEFAULT_TAG_VALUE;
    }


    public MapToCoupleArraySerializer(String tagKey, String tagValue) {
        this.tagKey = tagKey;
        this.tagValue = tagValue;
    }


    @Override
    public void serialize(Map<?, ?> value, JsonGenerator generator, SerializerProvider serializers) throws IOException, JsonProcessingException {
        generator.writeStartArray();
        for (Entry<?, ?> entry: value.entrySet()) {
            generator.writeStartObject();
            generator.writeObjectField(tagKey, entry.getKey());
            generator.writeObjectField(tagValue, entry.getValue());
            generator.writeEndObject();
        }
        generator.writeEndArray();
    }
}
