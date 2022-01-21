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
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Extend;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.BlobValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementCollectionValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.FileValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.RelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.mixins.ElementCollectionValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.mixins.PropertyValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.AnnotatedRelationshipElementValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.BlobValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.EntityValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.FileValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.ModifierAwareSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.MultiLanguagePropertyValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.ReferenceElementValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.RelationshipElementValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.SubmodelElementValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer.SubmodelValueSerializer;
import io.adminshell.aas.v3.model.DataElement;
import io.adminshell.aas.v3.model.Entity;
import io.adminshell.aas.v3.model.ReferenceElement;
import io.adminshell.aas.v3.model.RelationshipElement;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.util.ArrayList;
import java.util.List;


public class ValueOnlyJsonSerializer {

    private final SerializerWrapper wrapper;

    public JsonMapper getMapper() {
        return wrapper.getMapper();
    }


    public ValueOnlyJsonSerializer() {
        this.wrapper = new SerializerWrapper(x -> modifyMapper(x));
    }


    public String write(Object obj) throws SerializationException {
        return write(obj, Level.DEFAULT, Extend.DEFAULT);
    }


    public String write(Object obj, Level level) throws SerializationException {
        return write(obj, level, Extend.DEFAULT);
    }


    public String write(Object obj, Extend extend) throws SerializationException {
        return write(obj, Level.DEFAULT, extend);
    }


    public String write(Object obj, Level level, Extend extend) throws SerializationException {
        try {
            return wrapper.getMapper().writer()
                    .withAttribute(ModifierAwareSerializer.LEVEL, level)
                    .withAttribute(ModifierAwareSerializer.EXTEND, extend)
                    .writeValueAsString(obj);
        }
        catch (JsonProcessingException ex) {
            throw new SerializationException("serialization failed", ex);
        }
    }


    protected void modifyMapper(JsonMapper mapper) {
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
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
        module.addSerializer(SubmodelElement.class, new SubmodelElementValueSerializer());
        module.addSerializer(Submodel.class, new SubmodelValueSerializer());
        mapper.registerModule(module);
        mapper.registerModule(new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                context.addBeanSerializerModifier(new BeanSerializerModifier() {
                    @Override
                    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                                     BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                        beanProperties.removeIf(property -> {
                            JavaType type = property.getType();
                            List<JavaType> usedTypes = new ArrayList<>();
                            if (type.isContainerType()) {
                                if (type.getContentType() != null) {
                                    usedTypes.add(type.getContentType());
                                }
                                if (type.getKeyType() != null) {
                                    usedTypes.add(type.getKeyType());
                                }
                                if (type.getBindings() != null) {
                                    usedTypes.addAll(type.getBindings().getTypeParameters());
                                }
                            }
                            return !usedTypes.stream().allMatch(x -> isJreType(x.getRawClass()) || isValueType(x.getRawClass()));
                        });
                        return beanProperties;
                    }
                });
            }
        });
    }


    private static boolean isValueType(Class<?> type) {
        return DataElement.class.isAssignableFrom(type)
                || Submodel.class.isAssignableFrom(type)
                || SubmodelElementCollection.class.isAssignableFrom(type)
                || ReferenceElement.class.isAssignableFrom(type)
                || RelationshipElement.class.isAssignableFrom(type)
                || Entity.class.isAssignableFrom(type)
                || ElementValue.class.isAssignableFrom(type);
    }


    public static boolean isJreType(Class<?> type) {
        if (type.getClassLoader() == null || type.getClassLoader().getParent() == null) {
            return true;
        }
        String pkg = type.getPackage().getName();
        return pkg.startsWith("java.") || pkg.startsWith("com.sun") || pkg.startsWith("sun.");
    }
}
