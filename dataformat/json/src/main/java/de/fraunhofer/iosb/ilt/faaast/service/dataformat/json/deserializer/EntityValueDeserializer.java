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

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonFieldNames;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue;
import java.io.IOException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.deserialization.EnumDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.EntityType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Deserializer for {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue}.
 */
public class EntityValueDeserializer extends ContextAwareElementValueDeserializer<EntityValue> {

    public EntityValueDeserializer() {
        this(null);
    }


    public EntityValueDeserializer(Class<EntityValue> type) {
        super(type);
    }


    @Override
    public EntityValue deserializeValue(JsonNode node, DeserializationContext context) throws IOException, JacksonException {
        EntityValue.Builder builder = new EntityValue.Builder();
        if (node.has(JsonFieldNames.ENTITY_VALUE_ENTITY_TYPE)) {
            builder.entityType(EntityType.valueOf(EnumDeserializer.deserializeEnumName(node.get(JsonFieldNames.ENTITY_VALUE_ENTITY_TYPE).asText())));
        }
        if (node.has(JsonFieldNames.ENTITY_VALUE_GLOBAL_ASSET_ID)) {
            builder.globalAssetId(context.readTreeAsValue(node.get(JsonFieldNames.ENTITY_VALUE_GLOBAL_ASSET_ID), Reference.class)
                    .getKeys().get(0).getValue());
        }
        if (node.has(JsonFieldNames.ENTITY_VALUE_STATEMENTS)) {
            builder.statements(deserializeChildren(node.get(JsonFieldNames.ENTITY_VALUE_STATEMENTS), context, ElementValue.class));
        }
        return builder.build();
    }

}
