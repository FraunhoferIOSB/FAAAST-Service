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
package de.fraunhofer.iosb.ilt.faaast.service.serialization.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.BlobValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementCollectionValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.FileValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.RelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.Serializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.mixins.ElementCollectionValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.mixins.PropertyValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.AnnotatedRelationshipElementValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.BlobValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.EntityValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.FileValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.MultiLanguagePropertyValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.OutputModifierAwareSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.ReferenceElementValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.RelationshipElementValueSerializer;

/**
 * @author jab
 */
public class JsonSerializer implements Serializer {

    private final SerializerWrapper wrapper;

    public JsonSerializer() {
        this.wrapper = new SerializerWrapper();
    }

    @Override
    public String write(Object obj, OutputModifier modifier) throws SerializationException {
        try {
            return wrapper.getMapper().writer()
                    .withAttribute(OutputModifierAwareSerializer.OUTPUT_MODIFIER_ATTRIBUTE, modifier)
                    .writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            throw new SerializationException("serialization failed", ex);
        }
    }

    protected void modifyMapper(JsonMapper mapper) {
        mapper.addMixIn(PropertyValue.class, PropertyValueMixin.class);
        mapper.addMixIn(ElementCollectionValue.class, ElementCollectionValueMixin.class);
        SimpleModule module = new SimpleModule();
        module.addSerializer(MultiLanguagePropertyValue.class, new MultiLanguagePropertyValueSerializer());
        module.addSerializer(ReferenceElementValue.class, new ReferenceElementValueSerializer());
        module.addSerializer(FileValue.class, new FileValueSerializer());
        module.addSerializer(BlobValue.class, new BlobValueSerializer());
        module.addSerializer(RelationshipElementValue.class, new RelationshipElementValueSerializer());
        module.addSerializer(AnnotatedRelationshipElementValue.class, new AnnotatedRelationshipElementValueSerializer());
        module.addSerializer(EntityValue.class, new EntityValueSerializer());
        mapper.registerModule(module);
    }

    private class SerializerWrapper extends io.adminshell.aas.v3.dataformat.json.JsonSerializer {

        @Override
        protected void buildMapper() {
            super.buildMapper();
            modifyMapper(mapper);
        }

        protected JsonMapper getMapper() {
            return mapper;
        }
    }

}
