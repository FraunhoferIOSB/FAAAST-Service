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
package de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ReferenceElementValue;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import java.io.IOException;
import java.util.Map;

public class EntityValueSerializer extends OutputModifierAwareSerializer<EntityValue> {

    public EntityValueSerializer() {
        this(null);
    }

    public EntityValueSerializer(Class<EntityValue> type) {
        super(type);
    }

    @Override
    public void serialize(EntityValue value, JsonGenerator generator, SerializerProvider provider, OutputModifier modifier) throws IOException, JsonProcessingException {
        if (value != null) {
            generator.writeStartObject();
            generator.writeFieldName("statements");
            generator.writeStartObject();
            for (Map.Entry<String, ElementValue> annotation : value.getStatements().entrySet()) {
                provider.defaultSerializeField(annotation.getKey(), annotation.getValue(), generator);
            }
            generator.writeEndObject();
            generator.writeStringField("entityType", AasUtils.serializeEnumName(value.getEntityType().name()));
            provider.defaultSerializeField("globalAssetId", ReferenceElementValue.builder()
                    .keys(value.getGlobalAssetId())
                    .build(), generator);
            generator.writeEndObject();
        }
    }

}
