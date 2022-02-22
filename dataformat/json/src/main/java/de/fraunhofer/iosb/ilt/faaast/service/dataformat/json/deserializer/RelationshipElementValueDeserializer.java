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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.RelationshipElementValue;
import java.io.IOException;


/**
 * Deserializer for
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.RelationshipElementValue}
 */
public class RelationshipElementValueDeserializer extends ContextAwareElementValueDeserializer<RelationshipElementValue> {

    public RelationshipElementValueDeserializer() {
        this(null);
    }


    public RelationshipElementValueDeserializer(Class<RelationshipElementValue> type) {
        super(type);
    }


    @Override
    public RelationshipElementValue deserializeValue(JsonNode node, DeserializationContext context) throws IOException, JacksonException {
        RelationshipElementValue.Builder builder = new RelationshipElementValue.Builder();
        if (node.has(JsonFieldNames.ANNOTATED_RELATIONSHIP_ELEMENT_VALUE_FIRST)) {
            builder.first(context.readTreeAsValue(node.get(JsonFieldNames.ANNOTATED_RELATIONSHIP_ELEMENT_VALUE_FIRST), ReferenceElementValue.class).getKeys());
        }
        if (node.has(JsonFieldNames.ANNOTATED_RELATIONSHIP_ELEMENT_VALUE_SECOND)) {
            builder.second(context.readTreeAsValue(node.get(JsonFieldNames.ANNOTATED_RELATIONSHIP_ELEMENT_VALUE_SECOND), ReferenceElementValue.class).getKeys());
        }
        return builder.build();
    }

}
