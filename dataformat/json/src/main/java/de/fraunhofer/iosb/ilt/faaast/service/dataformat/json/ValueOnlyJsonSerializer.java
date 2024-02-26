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
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.AbstractRequestWithModifierMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.AbstractSubmodelInterfaceRequestMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.PageMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.ResponseMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.ResultMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.GetOperationAsyncResultResponseValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.InvokeOperationRequestValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.OperationResultValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.PropertyValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.ReferenceElementValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.SubmodelElementCollectionValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.SubmodelElementListValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.TypedValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.AnnotatedRelationshipElementValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.BlobValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.EntityValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.EnumSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.FileValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.ModifierAwareSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.MultiLanguagePropertyValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.PagingMetadataSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.SubmodelElementValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.SubmodelValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractRequestWithModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractSubmodelInterfaceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetOperationAsyncResultResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.BlobValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.FileValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementCollectionValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementListValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReflectionHelper;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


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
        this.wrapper = new SerializerWrapper(x -> modifyMapper(x));
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
     */
    public String write(Object obj) throws SerializationException {
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
     */
    public String write(Object obj, Level level) throws SerializationException {
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
     */
    public String write(Object obj, Extent extent) throws SerializationException {
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
     */
    public String write(Object obj, Level level, Extent extend) throws SerializationException {
        if (Objects.nonNull(obj) &&
                !ElementValueHelper.isValueOnlySupported(obj) &&
                !isExplicitelyAcceptedType(obj.getClass())) {
            throw new SerializationException(
                    String.format(
                            "Provided element is not supported by value-only serialization (type: %s). Supported types are: all subtypes of DataElement, SubmodelElementCollection, ReferenceElement, RelationshipElement, AnnotatedRelationshipElement, and Entity as well as all subtypes of ElementValue",
                            obj.getClass()));
        }
        try {
            return wrapper.getMapper().writer()
                    .withAttribute(ModifierAwareSerializer.LEVEL, level)
                    .withAttribute(ModifierAwareSerializer.EXTEND, extend)
                    .writeValueAsString(obj);
        }
        catch (JsonProcessingException e) {
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
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
        mapper.addMixIn(PropertyValue.class, PropertyValueMixin.class);
        mapper.addMixIn(SubmodelElementCollectionValue.class, SubmodelElementCollectionValueMixin.class);
        mapper.addMixIn(SubmodelElementListValue.class, SubmodelElementListValueMixin.class);
        mapper.addMixIn(TypedValue.class, TypedValueMixin.class);
        mapper.addMixIn(ReferenceElementValue.class, ReferenceElementValueMixin.class);
        mapper.addMixIn(Page.class, PageMixin.class);
        mapper.addMixIn(AbstractRequestWithModifier.class, AbstractRequestWithModifierMixin.class);
        mapper.addMixIn(AbstractSubmodelInterfaceRequest.class, AbstractSubmodelInterfaceRequestMixin.class);
        mapper.addMixIn(InvokeOperationRequest.class, InvokeOperationRequestValueMixin.class);
        mapper.addMixIn(Response.class, ResponseMixin.class);
        mapper.addMixIn(Result.class, ResultMixin.class);
        mapper.addMixIn(GetOperationAsyncResultResponse.class, GetOperationAsyncResultResponseValueMixin.class);
        mapper.addMixIn(OperationResult.class, OperationResultValueMixin.class);
        SimpleModule module = new SimpleModule();
        ReflectionHelper.ENUMS.forEach(x -> module.addSerializer(x, new EnumSerializer()));
        module.addSerializer(MultiLanguagePropertyValue.class, new MultiLanguagePropertyValueSerializer());
        module.addSerializer(FileValue.class, new FileValueSerializer());
        module.addSerializer(BlobValue.class, new BlobValueSerializer());
        module.addSerializer(AnnotatedRelationshipElementValue.class, new AnnotatedRelationshipElementValueSerializer());
        module.addSerializer(EntityValue.class, new EntityValueSerializer());
        module.addSerializer(SubmodelElement.class, new SubmodelElementValueSerializer());
        module.addSerializer(Submodel.class, new SubmodelValueSerializer());
        module.addSerializer(PagingMetadata.class, new PagingMetadataSerializer());
        ObjectMapper result = mapper.registerModule(module);
        result = result.registerModule(new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                context.addBeanSerializerModifier(new BeanSerializerModifier() {
                    @Override
                    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                                     BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                        beanProperties.removeIf(property -> !evalContainerType(property.getType()).stream()
                                .allMatch(
                                        x -> isJreType(x.getRawClass())
                                                || ElementValueHelper.isValueOnlySupported(x.getRawClass())
                                                || isExplicitelyAcceptedType(x.getRawClass())));
                        return beanProperties;
                    }
                });
            }
        });
        return (JsonMapper) result;
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
