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

import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;


/**
 * Utility class supporting event deserialization.
 */
public class EventDeserializationHelper {

    /**
     * Recursively deserialize children elements.
     *
     * @param <T> type of value to deserialize
     * @param node current JSON node
     * @param context deserialization context
     * @param type target type
     * @return deserialized children
     */
    public static <T extends ElementValue> Map<String, T> deserializeChildren(JsonNode node, DeserializationContext context, Class<T> type) {
        Map<String, T> result = new HashMap<>();
        if (node == null) {
            return result;
        }

        Map<String, JsonNode> childNodes = new HashMap<>();
        if (node.isObject()) {
            node.properties().iterator().forEachRemaining(x -> childNodes.put(x.getKey(), x.getValue()));
        }
        else if (node.isArray()) {
            Iterator<JsonNode> iterator = node.values().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> child = iterator.next().properties().iterator().next();
                childNodes.put(child.getKey(), child.getValue());
            }
        }
        for (Map.Entry<String, JsonNode> childNode: childNodes.entrySet()) {
            result.put(childNode.getKey(), type.cast(context.readTreeAsValue(childNode.getValue(), ElementValue.class)));
        }
        return result;
    }


    private EventDeserializationHelper() {}
}
