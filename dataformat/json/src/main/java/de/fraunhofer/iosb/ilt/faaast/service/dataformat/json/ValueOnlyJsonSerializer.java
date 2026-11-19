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

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.AbstractRequestWithModifierMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.AbstractSubmodelInterfaceRequestMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.ImportResultMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.MessageMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.PageMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.ResponseMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.ResultMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.AbstractDateTimeValueMixIn;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.GetOperationAsyncResultResponseValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.InvokeOperationRequestValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.OperationResultValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.PropertyValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.ReferenceElementValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.SubmodelElementCollectionValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.SubmodelElementListValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.TypedValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.AnnotatedRelationshipElementValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.BasicEventElementValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.BlobValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.EntityValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.EnumSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.FileValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.ModifierAwareSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.MultiLanguagePropertyValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.PagingMetadataSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.SubmodelElementValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.SubmodelValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractRequestWithModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractSubmodelInterfaceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.proprietary.ImportResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetOperationAsyncResultResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.UnsupportedContentModifierException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.BasicEventElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.BlobValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.FileValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementCollectionValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementListValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.AbstractDateTimeValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReflectionHelper;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.Message;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Result;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;
import tools.jackson.databind.util.TokenBuffer;


/**
 * Serializer for content=value.
 */
public class ValueOnlyJsonSerializer {

    private final SerializerWrapper wrapper;

    private static boolean isJreType(Class<?> type) {
        if (type.getClassLoader() == null || type.getClassLoader().getParent() == null) {
            return true;
        }
        String pkg = type.getPackage().getName();
        return pkg.startsWith("java.") || pkg.startsWith("com.sun") || pkg.startsWith("sun.");
    }


    public ValueOnlyJsonSerializer() {
        //this.wrapper = new SerializerWrapper(x -> modifyMapper(x));
        this.wrapper = new SerializerWrapper(this::modifyMapper);
    }


    public JsonMapper getMapper() {
        return wrapper.getMapper();
    }


    /**
     * Serializes a given object as string using
     * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level#DEFAULT} and
     * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent#DEFAULT}.
     *
     * @param obj the object to serialize
     * @return the serialized object
     * @throws SerializationException if serialization fails
     * @throws UnsupportedContentModifierException if obj does not support valueOnly serialization
     */
    public String write(Object obj) throws SerializationException, UnsupportedContentModifierException {
        return write(obj, Level.DEFAULT, Extent.DEFAULT);
    }


    /**
     * Serializes a given object as string using provided level and
     * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent#DEFAULT}.
     *
     * @param obj the object to serialize
     * @param level the level to use for serialization
     * @return the serialized object
     * @throws SerializationException if serialization fails
     * @throws UnsupportedContentModifierException if obj does not support valueOnly serialization
     */
    public String write(Object obj, Level level) throws SerializationException, UnsupportedContentModifierException {
        return write(obj, level, Extent.DEFAULT);
    }


    /**
     * Serializes a given object as string using
     * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level#DEFAULT} and provided extent.
     *
     * @param obj the object to serialize
     * @param extent the extent to use for serialization
     * @return the serialized object
     * @throws SerializationException if serialization fails
     * @throws UnsupportedContentModifierException if obj does not support valueOnly serialization
     */
    public String write(Object obj, Extent extent) throws SerializationException, UnsupportedContentModifierException {
        return write(obj, Level.DEFAULT, extent);
    }


    /**
     * Serializes a given object as string using provided level and extent.
     *
     * @param obj the object to serialize
     * @param level the level to use for serialization
     * @param extend the extent to use for serialization
     * @return the serialized object
     * @throws SerializationException if serialization fails
     * @throws UnsupportedContentModifierException if obj does not support valueOnly serialization
     */
    public String write(Object obj, Level level, Extent extend) throws SerializationException, UnsupportedContentModifierException {
        if (Objects.nonNull(obj) &&
                !ElementValueHelper.isValueOnlySupported(obj) &&
                !isExplicitelyAcceptedType(obj.getClass())) {
            throw new UnsupportedContentModifierException(Content.VALUE, obj.getClass());
        }
        try {
            return wrapper.getMapper().writer()
                    .withAttribute(ModifierAwareSerializer.LEVEL, level)
                    .withAttribute(ModifierAwareSerializer.EXTEND, extend)
                    .writeValueAsString(obj);
        }
        catch (JacksonException | NullPointerException e) {
            throw new SerializationException("serialization failed", e);
        }
    }


    /**
     * Serializes a given object as JsonNode using
     * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level#DEFAULT} and
     * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent#DEFAULT}.
     *
     * @param obj the object to serialize
     * @return the serialized object
     * @throws SerializationException if serialization fails
     * @throws UnsupportedContentModifierException if obj does not support valueOnly serialization
     */
    public JsonNode writeAsNode(Object obj) throws SerializationException, UnsupportedContentModifierException {
        return writeAsNode(obj, Level.DEFAULT, Extent.DEFAULT);
    }


    /**
     * Serializes a given object as JsonNode using provided level and
     * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent#DEFAULT}.
     *
     * @param obj the object to serialize
     * @param level the level to use for serialization
     * @return the serialized object
     * @throws SerializationException if serialization fails
     * @throws UnsupportedContentModifierException if obj does not support valueOnly serialization
     */
    public JsonNode writeAsNode(Object obj, Level level) throws SerializationException, UnsupportedContentModifierException {
        return writeAsNode(obj, level, Extent.DEFAULT);
    }


    /**
     * Serializes a given object as JsonNode using
     * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level#DEFAULT} and provided extent.
     *
     * @param obj the object to serialize
     * @param extent the extent to use for serialization
     * @return the serialized object
     * @throws SerializationException if serialization fails
     * @throws UnsupportedContentModifierException if obj does not support valueOnly serialization
     */
    public JsonNode writeAsNode(Object obj, Extent extent) throws SerializationException, UnsupportedContentModifierException {
        return writeAsNode(obj, Level.DEFAULT, extent);
    }


    /**
     * Serializes a given object as JsonNode using provided level and extent.
     *
     * @param obj the object to serialize
     * @param level the level to use for serialization
     * @param extend the extent to use for serialization
     * @return the serialized object
     * @throws SerializationException if serialization fails
     * @throws UnsupportedContentModifierException if obj does not support valueOnly serialization
     */
    public JsonNode writeAsNode(Object obj, Level level, Extent extend) throws SerializationException, UnsupportedContentModifierException {
        if (Objects.nonNull(obj) &&
                !ElementValueHelper.isValueOnlySupported(obj) &&
                !isExplicitelyAcceptedType(obj.getClass())) {
            throw new UnsupportedContentModifierException(Content.VALUE, obj.getClass());
        }
        try {
            // TODO: must be checked
            //TokenBuffer buffer = new TokenBuffer(wrapper.getMapper(), false);
            //TokenBuffer buffer = new TokenBuffer(ObjectWriteContext.empty(), false);
            TokenBuffer buffer = wrapper.getMapper().writer()
                    .withAttribute(ModifierAwareSerializer.LEVEL, level)
                    .withAttribute(ModifierAwareSerializer.EXTEND, extend)
                    .writeValueIntoBuffer(obj);
            return wrapper.getMapper().readTree(buffer.asParser());
        }
        catch (JacksonException e) {
            throw new SerializationException("serialization failed", e);
        }
    }


    private static boolean isExplicitelyAcceptedType(Class<?> type) {
        return Key.class.equals(type)
                || OperationVariable.class.equals(type)
                || InvokeOperationRequest.class.isAssignableFrom(type)
                || GetOperationAsyncResultResponse.class.equals(type)
                || Result.class.isAssignableFrom(type)
                || Message.class.equals(type);
    }


    /**
     * Modifies the mapper by adding required mixins and De-/serializers.
     *
     * @param mapper the mapper to modify
     * @return the updated mapper
     */
    protected JsonMapper modifyMapper(JsonMapper mapper) {
        SimpleModule module = new SimpleModule();
        ReflectionHelper.ENUMS.forEach(x -> module.addSerializer(x, new EnumSerializer()));
        module.addSerializer(MultiLanguagePropertyValue.class, new MultiLanguagePropertyValueSerializer());
        module.addSerializer(FileValue.class, new FileValueSerializer());
        module.addSerializer(BlobValue.class, new BlobValueSerializer());
        module.addSerializer(BasicEventElementValue.class, new BasicEventElementValueSerializer());
        module.addSerializer(AnnotatedRelationshipElementValue.class, new AnnotatedRelationshipElementValueSerializer());
        module.addSerializer(EntityValue.class, new EntityValueSerializer());
        module.addSerializer(SubmodelElement.class, new SubmodelElementValueSerializer());
        module.addSerializer(Submodel.class, new SubmodelValueSerializer());
        module.addSerializer(PagingMetadata.class, new PagingMetadataSerializer());

        return mapper.rebuild()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .annotationIntrospector(new JacksonAnnotationIntrospector())
                .addMixIn(PropertyValue.class, PropertyValueMixin.class)
                .addMixIn(SubmodelElementCollectionValue.class, SubmodelElementCollectionValueMixin.class)
                .addMixIn(SubmodelElementListValue.class, SubmodelElementListValueMixin.class)
                .addMixIn(TypedValue.class, TypedValueMixin.class)
                .addMixIn(AbstractDateTimeValue.class, AbstractDateTimeValueMixIn.class)
                .addMixIn(ReferenceElementValue.class, ReferenceElementValueMixin.class)
                .addMixIn(Page.class, PageMixin.class)
                .addMixIn(Message.class, MessageMixin.class)
                .addMixIn(ImportResult.class, ImportResultMixin.class)
                .addMixIn(AbstractRequestWithModifier.class, AbstractRequestWithModifierMixin.class)
                .addMixIn(AbstractSubmodelInterfaceRequest.class, AbstractSubmodelInterfaceRequestMixin.class)
                .addMixIn(InvokeOperationRequest.class, InvokeOperationRequestValueMixin.class)
                .addMixIn(Response.class, ResponseMixin.class)
                .addMixIn(Result.class, ResultMixin.class)
                .addMixIn(GetOperationAsyncResultResponse.class, GetOperationAsyncResultResponseValueMixin.class)
                .addMixIn(OperationResult.class, OperationResultValueMixin.class)
                .addModule(module)
                .addModule((new SimpleModule() {
                    @Override
                    public void setupModule(JacksonModule.SetupContext context) {
                        super.setupModule(context);
                        context.addSerializerModifier(new ValueSerializerModifier() {
                            @Override
                            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                                             BeanDescription.Supplier beanDesc, List<BeanPropertyWriter> beanProperties) {
                                beanProperties.removeIf(property -> !evalContainerType(property.getType()).stream()
                                        .allMatch(
                                                x -> isJreType(x.getRawClass())
                                                        || ElementValueHelper.isValueOnlySupported(x.getRawClass())
                                                        || isExplicitelyAcceptedType(x.getRawClass())));
                                return beanProperties;
                            }
                        });
                    }
                }))
                .build();
    }


    private Set<JavaType> evalContainerType(JavaType type) {
        Set<JavaType> result = new HashSet<>();
        if (type.isContainerType()) {
            if (type.getContentType() != null) {
                result.add(type.getContentType());
            }
            if (type.getKeyType() != null) {
                result.add(type.getKeyType());
            }
            if (type.getBindings() != null) {
                result.addAll(type.getBindings().getTypeParameters());
            }
        }
        return result;
    }

}
