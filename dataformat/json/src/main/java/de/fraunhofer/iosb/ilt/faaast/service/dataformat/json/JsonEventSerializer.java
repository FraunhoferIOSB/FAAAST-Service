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
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.event.EventMessageMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.ReferenceElementValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.AnnotatedRelationshipElementValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.BlobValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.EnumSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.FileValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.MultiLanguagePropertyValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.event.ElementValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.event.TypedValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.BlobValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.FileValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReflectionHelper;


/**
 * JSON Serializer for {@link EventMessage}.
 */
public class JsonEventSerializer {

    private final SerializerWrapper wrapper;

    public JsonEventSerializer() {
        this.wrapper = new SerializerWrapper(this::modifyMapper);
    }


    /**
     * Modifies Jackson JsonMapper.
     *
     * @param mapper mapper to modify
     */
    protected void modifyMapper(JsonMapper mapper) {
        SimpleModule module = new SimpleModule();
        ReflectionHelper.ENUMS.forEach(x -> module.addSerializer(x, new EnumSerializer()));

        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                if (ElementValue.class.isAssignableFrom(beanDesc.getBeanClass())) {
                    return new ElementValueSerializer((JsonSerializer<Object>) serializer);
                }
                return serializer;
            }
        });
        module.addSerializer(BlobValue.class, new BlobValueSerializer());
        module.addSerializer(FileValue.class, new FileValueSerializer());
        module.addSerializer(MultiLanguagePropertyValue.class, new MultiLanguagePropertyValueSerializer());
        module.addSerializer(AnnotatedRelationshipElementValue.class, new AnnotatedRelationshipElementValueSerializer());
        module.addSerializer(TypedValue.class, new TypedValueSerializer());
        mapper.registerModule(module);
        mapper.addMixIn(EventMessage.class, EventMessageMixin.class);
        mapper.addMixIn(ReferenceElementValue.class, ReferenceElementValueMixin.class);
    }


    /**
     * Serializes a event message as JSON.
     *
     * @param msg the message to serialize
     * @return the JSON string representation of the message
     * @throws SerializationException if serialization fails
     */
    public String write(EventMessage msg) throws SerializationException {
        try {
            return wrapper.getMapper().writer().writeValueAsString(msg);
        }
        catch (JsonProcessingException e) {
            throw new SerializationException("serialization failed", e);
        }
    }
}
