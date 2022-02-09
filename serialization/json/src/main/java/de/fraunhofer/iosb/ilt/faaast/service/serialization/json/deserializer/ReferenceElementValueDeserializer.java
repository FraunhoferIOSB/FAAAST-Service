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
package de.fraunhofer.iosb.ilt.faaast.service.serialization.json.deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.JsonFieldNames;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class ReferenceElementValueDeserializer extends StdDeserializer<ReferenceElementValue> {

    public ReferenceElementValueDeserializer() {
        this(null);
    }


    public ReferenceElementValueDeserializer(Class<ReferenceElementValue> type) {
        super(type);
    }


    @Override
    public ReferenceElementValue deserialize(JsonParser parser, DeserializationContext context) throws IOException, JacksonException {
        JsonNode root = parser.readValueAsTree();
        if (root == null || !root.isArray()) {
            return null;
        }
        ReferenceElementValue.Builder builder = ReferenceElementValue.builder();
        if (!root.elements().hasNext()) {
            return builder.build();
        }
        Iterable<JsonNode> iterable = () -> root.elements();
        for (JsonNode element: StreamSupport
                .stream(iterable.spliterator(), false)
                .collect(Collectors.toList())) {
            if (element.isObject()) {
                if (!element.has(JsonFieldNames.REFERENCE_ELEMENT_VALUE_ID_TYPE)) {
                    throw new RuntimeException(String.format("missing property '%s'", JsonFieldNames.REFERENCE_ELEMENT_VALUE_ID_TYPE));
                }
                if (!element.has(JsonFieldNames.REFERENCE_ELEMENT_VALUE_TYPE)) {
                    throw new RuntimeException(String.format("missing property '%s'", JsonFieldNames.REFERENCE_ELEMENT_VALUE_TYPE));
                }
                if (!element.has(JsonFieldNames.REFERENCE_ELEMENT_VALUE_VALUE)) {
                    throw new RuntimeException(String.format("missing property '%s'", JsonFieldNames.REFERENCE_ELEMENT_VALUE_VALUE));
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
                context.handleUnexpectedToken(ReferenceElementValue.class, parser);
            }
        }
        return builder.build();
    }

}
