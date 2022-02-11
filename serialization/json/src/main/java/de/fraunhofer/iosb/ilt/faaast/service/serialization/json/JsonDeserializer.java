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
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementCollectionValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.RelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.Deserializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.deserializer.AnnotatedRelationshipElementValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.deserializer.ContextAwareElementValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.deserializer.EntityValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.deserializer.MultiLanguagePropertyValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.deserializer.PropertyValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.deserializer.ReferenceElementValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.deserializer.RelationshipElementValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.deserializer.SubmodelElementCollectionValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.deserializer.TypedValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.mixins.PropertyValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeContext;
import io.adminshell.aas.v3.dataformat.json.modeltype.ModelTypeProcessor;
import java.io.IOException;
import java.util.List;


public class JsonDeserializer implements Deserializer {

    private final DeserializerWrapper wrapper;

    public JsonDeserializer() {
        this.wrapper = new DeserializerWrapper(x -> modifyMapper(x));
    }


    @Override
    public <T> T read(String json, Class<T> type) throws DeserializationException {
        try {
            String parsed = wrapper.getMapper().writeValueAsString(ModelTypeProcessor.preprocess(json));
            return wrapper.getMapper().readValue(parsed, type);
        }
        catch (JsonProcessingException ex) {
            throw new DeserializationException("deserialization failed", ex);
        }
    }


    @Override
    public <T> List<T> readList(String json, Class<T> type) throws DeserializationException {
        try {
            String parsed = wrapper.getMapper().writeValueAsString(ModelTypeProcessor.preprocess(json));
            return wrapper.getMapper().readValue(parsed, wrapper.getMapper().getTypeFactory().constructCollectionType(List.class, type));
        }
        catch (JsonProcessingException ex) {
            throw new DeserializationException("deserialization failed", ex);
        }
    }


    @Override
    public <T extends ElementValue> T readValue(String json, TypeContext context) throws DeserializationException {
        if (context == null) {
            throw new IllegalArgumentException("context must be non-null");
        }
        if (context.getRootInfo().getValueType() == null) {
            throw new DeserializationException("missing root type information");
        }
        try {
            return (T) wrapper.getMapper().reader()
                    .withAttribute(ContextAwareElementValueDeserializer.VALUE_TYPE_CONTEXT, context)
                    .readValue(json, context.getRootInfo().getValueType());
        }
        catch (IOException ex) {
            throw new DeserializationException("deserialization failed", ex);
        }
    }


    @Override
    public <T extends ElementValue> T readValue(String json, Class<T> type) throws DeserializationException {
        try {
            return wrapper.getMapper().readValue(json, type);
        }
        catch (JsonProcessingException ex) {
            throw new DeserializationException("deserialization failed", ex);
        }
    }


    @Override
    public <T> void useImplementation(Class<T> interfaceType, Class<? extends T> implementationType) {
        wrapper.useImplementation(interfaceType, implementationType);
    }


    protected void modifyMapper(JsonMapper mapper) {
        mapper.addMixIn(PropertyValue.class, PropertyValueMixin.class);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(TypedValue.class, new TypedValueDeserializer());
        module.addDeserializer(PropertyValue.class, new PropertyValueDeserializer());
        module.addDeserializer(AnnotatedRelationshipElementValue.class, new AnnotatedRelationshipElementValueDeserializer());
        module.addDeserializer(RelationshipElementValue.class, new RelationshipElementValueDeserializer());
        module.addDeserializer(ElementCollectionValue.class, new SubmodelElementCollectionValueDeserializer());
        module.addDeserializer(MultiLanguagePropertyValue.class, new MultiLanguagePropertyValueDeserializer());
        module.addDeserializer(ReferenceElementValue.class, new ReferenceElementValueDeserializer());
        module.addDeserializer(EntityValue.class, new EntityValueDeserializer());
        mapper.registerModule(module);
    }

}
