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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.BasicEventElementValue;
import java.io.IOException;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Deserializer for {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.BasicEventElementValue}.
 */
public class BasicEventElementValueDeserializer extends ContextAwareElementValueDeserializer<BasicEventElementValue> {

    public BasicEventElementValueDeserializer() {
        this(null);
    }


    public BasicEventElementValueDeserializer(Class<BasicEventElementValue> type) {
        super(type);
    }


    @Override
    public BasicEventElementValue deserializeValue(JsonNode node, DeserializationContext context) throws IOException, JacksonException {
        BasicEventElementValue.Builder builder = new BasicEventElementValue.Builder();
        if (node.has(JsonFieldNames.BASIC_EVENT_ELEMENT_OBSERVED)) {
            builder.observed(context.readTreeAsValue(node.get(JsonFieldNames.BASIC_EVENT_ELEMENT_OBSERVED), Reference.class));
        }
        return builder.build();
    }

}
