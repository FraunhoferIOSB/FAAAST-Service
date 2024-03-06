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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonFieldNames;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue;
import java.io.IOException;
import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.serialization.EnumSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;


/**
 * Serializer for {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue}.
 */
public class EntityValueSerializer extends StdSerializer<EntityValue> {

    public EntityValueSerializer() {
        this(null);
    }


    public EntityValueSerializer(Class<EntityValue> type) {
        super(type);
    }


    @Override
    public void serialize(EntityValue value, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
        if (value != null) {
            generator.writeStartObject();
            generator.writeFieldName(JsonFieldNames.ENTITY_VALUE_STATEMENTS);
            generator.writeStartObject();
            for (Map.Entry<String, ElementValue> annotation: value.getStatements().entrySet()) {
                provider.defaultSerializeField(annotation.getKey(), annotation.getValue(), generator);
            }
            generator.writeEndObject();
            generator.writeStringField(JsonFieldNames.ENTITY_VALUE_ENTITY_TYPE, EnumSerializer.serializeEnumName(value.getEntityType().name()));
            provider.defaultSerializeField(JsonFieldNames.ENTITY_VALUE_GLOBAL_ASSET_ID, new DefaultReference.Builder()
                    .type(ReferenceTypes.EXTERNAL_REFERENCE)
                    .keys(new DefaultKey.Builder()
                            .type(KeyTypes.GLOBAL_REFERENCE)
                            .value(value.getGlobalAssetId())
                            .build())
                    .build(), generator);
            generator.writeEndObject();
        }
    }

}
