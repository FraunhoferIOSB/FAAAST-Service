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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.event;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonFieldNames;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.*;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import java.io.IOException;


/**
 * Deserializer for {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue}.
 */
public class ReferenceElementValueDeserializer extends ContextAwareElementValueDeserializer<ReferenceElementValue> {

    public ReferenceElementValueDeserializer() {
        this(null);
    }


    public ReferenceElementValueDeserializer(Class<ReferenceElementValue> type) {
        super(type);
    }


    @Override
    public ReferenceElementValue deserializeValue(JsonNode node, DeserializationContext context) throws IOException, JacksonException {
        final String MISSING_PROPERTY = "missing property '%s'";
        if (node == null) {
            return null;
        }
        JsonNode root = node;
        if (!node.isArray() && node.hasNonNull(JsonFieldNames.EVENT_VALUE) && node.get(JsonFieldNames.EVENT_VALUE).isArray()) {
            root = node.get(JsonFieldNames.EVENT_VALUE);
        }
        ReferenceElementValue.Builder builder = ReferenceElementValue.builder();
        if (!root.elements().hasNext()) {
            return builder.build();
        }
        for (JsonNode element: root) {
            if (element.isObject()) {
                if (!element.has(JsonFieldNames.REFERENCE_ELEMENT_VALUE_ID_TYPE)) {
                    throw new IllegalArgumentException(String.format(MISSING_PROPERTY, JsonFieldNames.REFERENCE_ELEMENT_VALUE_ID_TYPE));
                }
                if (!element.has(JsonFieldNames.REFERENCE_ELEMENT_VALUE_TYPE)) {
                    throw new IllegalArgumentException(String.format(MISSING_PROPERTY, JsonFieldNames.REFERENCE_ELEMENT_VALUE_TYPE));
                }
                if (!element.has(JsonFieldNames.REFERENCE_ELEMENT_VALUE_VALUE)) {
                    throw new IllegalArgumentException(String.format(MISSING_PROPERTY, JsonFieldNames.REFERENCE_ELEMENT_VALUE_VALUE));
                }
                builder.key(context.readTreeAsValue(
                        element.get(JsonFieldNames.REFERENCE_ELEMENT_VALUE_ID_TYPE), KeyType.class),
                        context.readTreeAsValue(element.get(JsonFieldNames.REFERENCE_ELEMENT_VALUE_TYPE), KeyElements.class),
                        element.get(JsonFieldNames.REFERENCE_ELEMENT_VALUE_VALUE).textValue());
            }
            else if (element.isTextual()) {
                builder.key(KeyType.IRI, KeyElements.GLOBAL_REFERENCE, element.textValue());
            }
            else {
                context.reportBadDefinition(ReferenceElementValue.class, "unknown format of reference element");
            }
        }
        return builder.build();
    }

}
