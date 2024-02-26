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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.EntityValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.EnumDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.MultiLanguagePropertyValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.PropertyValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.RangeValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.event.AnnotatedRelationshipElementValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.event.ElementValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.event.SubmodelElementCollectionValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.event.TypedValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.event.EventMessageMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.ReferenceElementValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementCollectionValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReflectionHelper;


/**
 * JSON Deserializer for {@link EventMessage}.
 */
public class JsonEventDeserializer {

    private final DeserializerWrapper wrapper;

    public JsonEventDeserializer() {
        this.wrapper = new DeserializerWrapper(this::modifyMapper);
    }


    /**
     * Modifies Jackson JsonMapper.
     *
     * @param mapper mapper to modify
     */
    protected void modifyMapper(JsonMapper mapper) {
        SimpleModule module = new SimpleModule();
        ReflectionHelper.ENUMS.forEach(x -> module.addDeserializer(x, new EnumDeserializer(x)));
        module.addDeserializer(TypedValue.class, new TypedValueDeserializer());
        module.addDeserializer(PropertyValue.class, new PropertyValueDeserializer());
        module.addDeserializer(AnnotatedRelationshipElementValue.class, new AnnotatedRelationshipElementValueDeserializer());
        module.addDeserializer(SubmodelElementCollectionValue.class, new SubmodelElementCollectionValueDeserializer());
        module.addDeserializer(MultiLanguagePropertyValue.class, new MultiLanguagePropertyValueDeserializer());
        module.addDeserializer(EntityValue.class, new EntityValueDeserializer());
        module.addDeserializer(RangeValue.class, new RangeValueDeserializer());
        module.addDeserializer(ElementValue.class, new ElementValueDeserializer());
        mapper.registerModule(module);
        mapper.addMixIn(EventMessage.class, EventMessageMixin.class);
        mapper.addMixIn(ReferenceElementValue.class, ReferenceElementValueMixin.class);
    }


    /**
     * Read an event message from string.
     *
     * @param <T> type of event message
     * @param json the JSON to parse
     * @param type type of event message to deserialize to
     * @return the parsed event message
     * @throws DeserializationException if deserialization fails
     */
    public <T extends EventMessage> T read(String json, Class<T> type) throws DeserializationException {
        try {
            return wrapper.getMapper().readValue(json, type);
        }
        catch (JsonProcessingException e) {
            throw new DeserializationException(
                    String.format("Deserializing event message failed (reason: %s)",
                            e.getMessage()),
                    e);
        }
    }


    /**
     * Read an event message from string.
     *
     * @param json the JSON to parse
     * @return the parsed event message
     * @throws DeserializationException if deserialization fails
     */
    public EventMessage read(String json) throws DeserializationException {
        return read(json, EventMessage.class);
    }

}
