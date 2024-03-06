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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.MultiLanguagePropertyValue;
import java.io.IOException;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;


/**
 * Serializer for {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.MultiLanguagePropertyValue}.
 */
public class MultiLanguagePropertyValueSerializer extends StdSerializer<MultiLanguagePropertyValue> {

    public MultiLanguagePropertyValueSerializer() {
        this(null);
    }


    public MultiLanguagePropertyValueSerializer(Class<MultiLanguagePropertyValue> type) {
        super(type);
    }


    @Override
    public void serialize(MultiLanguagePropertyValue value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        if (value != null) {
            generator.writeStartArray();
            for (LangStringTextType langString: value.getLangStringSet()) {
                generator.writeStartObject();
                generator.writeStringField(langString.getLanguage(), langString.getText());
                generator.writeEndObject();
            }
            generator.writeEndArray();
        }
    }

}
