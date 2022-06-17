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
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.deser.std.ObjectArrayDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.Deserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.AnnotatedRelationshipElementValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.ContextAwareElementValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.ElementValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.EntityValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.EnumDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.MultiLanguagePropertyValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.PropertyValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.RangeValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.ReferenceElementValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.RelationshipElementValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.SubmodelElementCollectionValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.TypedValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.ValueArrayDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.ValueCollectionDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.ValueMapDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.PropertyValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.RelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementCollectionValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ContainerTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReflectionHelper;
import io.adminshell.aas.v3.dataformat.json.modeltype.ModelTypeProcessor;
import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * JSON deserializer for FA³ST.
 */
public class JsonDeserializer implements Deserializer {

    private final DeserializerWrapper wrapper;
    private static final String ERROR_MSG_DESERIALIZATION_FAILED = "deserialization failed";
    private static final String ERROR_MSG_TYPEINFO_MUST_BE_CONATINERTYPEINFO = "typeInfo must be of type ContainerTypeInfo";
    private static final String ERROR_MSG_ROOT_TYPE_INFO_MUST_BE_NON_NULL = "root type information must be non-null";
    private static final String ERROR_MSG_CONTENT_TYPE_MUST_BE_NON_NULL = "content type must be non-null";
    private static final String ERROR_MSG_TYPE_INFO_MUST_BE_NON_NULL = "typeInfo must be non-null";

    public JsonDeserializer() {
        this.wrapper = new DeserializerWrapper(x -> modifyMapper(x));
    }


    @Override
    public <T> T read(String json, Class<T> type) throws DeserializationException {
        try {
            return wrapper.getMapper().treeToValue(ModelTypeProcessor.preprocess(json), type);
        }
        catch (JsonProcessingException e) {
            throw new DeserializationException(ERROR_MSG_DESERIALIZATION_FAILED, e);
        }
    }


    @Override
    public <T> List<T> readList(String json, Class<T> type) throws DeserializationException {
        try {
            return wrapper.getMapper().treeToValue(ModelTypeProcessor.preprocess(json), wrapper.getMapper().getTypeFactory().constructCollectionType(List.class, type));
        }
        catch (JsonProcessingException e) {
            throw new DeserializationException(ERROR_MSG_DESERIALIZATION_FAILED, e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if typeInfo is null
     * @throws DeserializationException if typeInfo does not contain type
     *             information
     */
    @Override
    public <T extends ElementValue> T readValue(String json, TypeInfo typeInfo) throws DeserializationException {
        Ensure.requireNonNull(typeInfo, ERROR_MSG_TYPE_INFO_MUST_BE_NON_NULL);
        if (typeInfo.getType() == null) {
            throw new DeserializationException("missing root type information");
        }
        try {
            return (T) wrapper.getMapper().reader()
                    .withAttribute(ContextAwareElementValueDeserializer.VALUE_TYPE_CONTEXT, typeInfo)
                    .treeToValue(ModelTypeProcessor.preprocess(json), typeInfo.getType());
        }
        catch (IOException e) {
            throw new DeserializationException(ERROR_MSG_DESERIALIZATION_FAILED, e);
        }
    }


    @Override
    public <T extends ElementValue> T readValue(String json, Class<T> type) throws DeserializationException {
        try {
            return wrapper.getMapper().treeToValue(ModelTypeProcessor.preprocess(json), type);
        }
        catch (JsonProcessingException e) {
            throw new DeserializationException(ERROR_MSG_DESERIALIZATION_FAILED, e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if typeInfo is null
     * @throws IllegalArgumentException if typeInfo is not a
     *             {@link de.fraunhofer.iosb.ilt.faaast.service.typing.ContainerTypeInfo}
     * @throws DeserializationException if typeInfo does not contain type
     *             information
     */
    @Override
    public ElementValue[] readValueArray(String json, TypeInfo typeInfo) throws DeserializationException {
        Ensure.requireNonNull(typeInfo, ERROR_MSG_TYPE_INFO_MUST_BE_NON_NULL);
        if (!ContainerTypeInfo.class.isAssignableFrom(typeInfo.getClass())) {
            throw new DeserializationException(ERROR_MSG_TYPEINFO_MUST_BE_CONATINERTYPEINFO);
        }
        if (typeInfo.getType() == null) {
            throw new DeserializationException(ERROR_MSG_ROOT_TYPE_INFO_MUST_BE_NON_NULL);
        }
        ContainerTypeInfo containerTypeInfo = (ContainerTypeInfo) typeInfo;
        if (containerTypeInfo.getContentType() == null) {
            throw new DeserializationException(ERROR_MSG_CONTENT_TYPE_MUST_BE_NON_NULL);
        }
        try {
            return (ElementValue[]) wrapper.getMapper().reader()
                    .withAttribute(ContextAwareElementValueDeserializer.VALUE_TYPE_CONTEXT, typeInfo)
                    .forType(wrapper.getMapper().getTypeFactory().constructArrayType(containerTypeInfo.getContentType()))
                    .treeToValue(ModelTypeProcessor.preprocess(json), wrapper.getMapper().getTypeFactory().constructArrayType(containerTypeInfo.getContentType()));
        }
        catch (IOException e) {
            throw new DeserializationException(ERROR_MSG_DESERIALIZATION_FAILED, e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if typeInfo is null
     * @throws IllegalArgumentException if typeInfo is not a
     *             {@link de.fraunhofer.iosb.ilt.faaast.service.typing.ContainerTypeInfo}
     * @throws DeserializationException if typeInfo does not contain type
     *             information
     * @throws DeserializationException if typeInfo does contain content type
     *             information
     */
    @Override
    public <T extends ElementValue> List<T> readValueList(String json, TypeInfo typeInfo) throws DeserializationException {
        Ensure.requireNonNull(typeInfo, ERROR_MSG_TYPE_INFO_MUST_BE_NON_NULL);
        if (!ContainerTypeInfo.class.isAssignableFrom(typeInfo.getClass())) {
            throw new DeserializationException(ERROR_MSG_TYPEINFO_MUST_BE_CONATINERTYPEINFO);
        }
        if (typeInfo.getType() == null) {
            throw new DeserializationException(ERROR_MSG_ROOT_TYPE_INFO_MUST_BE_NON_NULL);
        }
        ContainerTypeInfo containerTypeInfo = (ContainerTypeInfo) typeInfo;
        if (containerTypeInfo.getContentType() == null) {
            throw new DeserializationException(ERROR_MSG_CONTENT_TYPE_MUST_BE_NON_NULL);
        }
        try {
            return (List<T>) wrapper.getMapper().reader()
                    .withAttribute(ContextAwareElementValueDeserializer.VALUE_TYPE_CONTEXT, typeInfo)
                    .forType(wrapper.getMapper().getTypeFactory().constructCollectionType(List.class, containerTypeInfo.getContentType()))
                    .readValue(json);
        }
        catch (IOException e) {
            throw new DeserializationException(ERROR_MSG_DESERIALIZATION_FAILED, e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if typeInfo is null
     * @throws IllegalArgumentException if typeInfo is not a
     *             {@link de.fraunhofer.iosb.ilt.faaast.service.typing.ContainerTypeInfo}
     * @throws DeserializationException if typeInfo does not contain type
     *             information
     * @throws DeserializationException if typeInfo does contain content type
     *             information
     */
    @Override
    public <K, V extends ElementValue> Map<K, V> readValueMap(String json, TypeInfo typeInfo) throws DeserializationException {
        Ensure.requireNonNull(typeInfo, ERROR_MSG_TYPE_INFO_MUST_BE_NON_NULL);
        if (!ContainerTypeInfo.class.isAssignableFrom(typeInfo.getClass())) {
            throw new DeserializationException(ERROR_MSG_TYPEINFO_MUST_BE_CONATINERTYPEINFO);
        }
        if (typeInfo.getType() == null) {
            throw new DeserializationException(ERROR_MSG_ROOT_TYPE_INFO_MUST_BE_NON_NULL);
        }
        ContainerTypeInfo containerTypeInfo = (ContainerTypeInfo) typeInfo;
        if (containerTypeInfo.getContentType() == null) {
            throw new DeserializationException(ERROR_MSG_CONTENT_TYPE_MUST_BE_NON_NULL);
        }
        try {
            return (Map<K, V>) wrapper.getMapper().reader()
                    .withAttribute(ContextAwareElementValueDeserializer.VALUE_TYPE_CONTEXT, typeInfo)
                    .forType(wrapper.getMapper().getTypeFactory().constructMapType(Map.class, Object.class, Object.class))
                    .readValue(json);
        }
        catch (IOException e) {
            throw new DeserializationException(ERROR_MSG_DESERIALIZATION_FAILED, e);
        }
    }


    @Override
    public <T> void useImplementation(Class<T> interfaceType, Class<? extends T> implementationType) {
        wrapper.useImplementation(interfaceType, implementationType);
    }


    /**
     * Modifies Jackson JsonMapper
     *
     * @param mapper mapper to modify
     */
    protected void modifyMapper(JsonMapper mapper) {
        mapper.addMixIn(PropertyValue.class, PropertyValueMixin.class);
        SimpleModule module = new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                context.addBeanDeserializerModifier(new BeanDeserializerModifier() {
                    @Override
                    public com.fasterxml.jackson.databind.JsonDeserializer<?> modifyMapDeserializer(DeserializationConfig config,
                                                                                                    MapType type, BeanDescription beanDesc,
                                                                                                    com.fasterxml.jackson.databind.JsonDeserializer<?> deserializer) {
                        if (deserializer instanceof MapDeserializer) {
                            return new ValueMapDeserializer((MapDeserializer) deserializer);
                        }
                        return super.modifyMapDeserializer(config, type, beanDesc, deserializer);
                    }


                    @Override
                    public com.fasterxml.jackson.databind.JsonDeserializer<?> modifyArrayDeserializer(DeserializationConfig config,
                                                                                                      ArrayType valueType, BeanDescription beanDesc,
                                                                                                      com.fasterxml.jackson.databind.JsonDeserializer<?> deserializer) {
                        if (deserializer instanceof ObjectArrayDeserializer) {
                            return new ValueArrayDeserializer((ObjectArrayDeserializer) deserializer);
                        }
                        return super.modifyArrayDeserializer(config, valueType, beanDesc, deserializer);
                    }


                    @Override
                    public com.fasterxml.jackson.databind.JsonDeserializer<?> modifyCollectionDeserializer(DeserializationConfig config,
                                                                                                           CollectionType type, BeanDescription beanDesc,
                                                                                                           com.fasterxml.jackson.databind.JsonDeserializer<?> deserializer) {
                        if (deserializer instanceof CollectionDeserializer) {
                            return new ValueCollectionDeserializer((CollectionDeserializer) deserializer);
                        }
                        return super.modifyCollectionDeserializer(config, type, beanDesc, deserializer);
                    }
                });
            }
        };
        module.addDeserializer(TypedValue.class, new TypedValueDeserializer());
        module.addDeserializer(PropertyValue.class, new PropertyValueDeserializer());
        module.addDeserializer(AnnotatedRelationshipElementValue.class, new AnnotatedRelationshipElementValueDeserializer());
        module.addDeserializer(RelationshipElementValue.class, new RelationshipElementValueDeserializer());
        module.addDeserializer(SubmodelElementCollectionValue.class, new SubmodelElementCollectionValueDeserializer());
        module.addDeserializer(MultiLanguagePropertyValue.class, new MultiLanguagePropertyValueDeserializer());
        module.addDeserializer(ReferenceElementValue.class, new ReferenceElementValueDeserializer());
        module.addDeserializer(EntityValue.class, new EntityValueDeserializer());
        module.addDeserializer(ElementValue.class, new ElementValueDeserializer());
        module.addDeserializer(RangeValue.class, new RangeValueDeserializer());
        ReflectionHelper.ENUMS.forEach(x -> module.addDeserializer(x, new EnumDeserializer(x)));
        mapper.registerModule(module);
    }

}
