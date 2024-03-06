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
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.deser.std.ObjectArrayDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.ApiDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.AnnotatedRelationshipElementValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.ContextAwareElementValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.ElementValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.EntityValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.EnumDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.MultiLanguagePropertyValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.PagingMetadataDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.PropertyValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.RangeValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.SubmodelElementCollectionValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.SubmodelElementListValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.TypedValueDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.ValueArrayDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.ValueCollectionDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer.ValueMapDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.AbstractRequestWithModifierMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.AbstractSubmodelInterfaceRequestMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.InvokeOperationRequestMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.PageMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.PropertyValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.ReferenceElementValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractRequestWithModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractSubmodelInterfaceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementCollectionValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementListValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ContainerTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeExtractor;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReflectionHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.datatype.DatatypeFactory;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;


/**
 * JSON API deserializer for FA³ST.
 */
public class JsonApiDeserializer implements ApiDeserializer {

    private final DeserializerWrapper wrapper;
    private static final String ERROR_MSG_DESERIALIZATION_FAILED = "deserialization failed";
    private static final String ERROR_MSG_TYPEINFO_MUST_BE_CONATINERTYPEINFO = "typeInfo must be of type ContainerTypeInfo";
    private static final String ERROR_MSG_ROOT_TYPE_INFO_MUST_BE_NON_NULL = "root type information must be non-null";
    private static final String ERROR_MSG_CONTENT_TYPE_MUST_BE_NON_NULL = "content type must be non-null";
    private static final String ERROR_MSG_TYPE_INFO_MUST_BE_NON_NULL = "typeInfo must be non-null";

    private static final String KEY_OPERATION_INPUT_ARGUMENTS = "inputArguments";
    private static final String KEY_OPERATION_INOUTPUT_ARGUMENTS = "inoutputArguments";
    private static final String KEY_OPERATION_CLIENT_TIMEOUT_DURATION = "clientTimeoutDuration";

    public JsonApiDeserializer() {
        this.wrapper = new DeserializerWrapper(this::modifyMapper);
    }


    @Override
    public <T> T read(String json, JavaType type) throws DeserializationException {
        try {
            return wrapper.getMapper().readValue(json, type);
        }
        catch (JsonProcessingException e) {
            throw new DeserializationException(ERROR_MSG_DESERIALIZATION_FAILED, e);
        }
    }


    @Override
    public <T> List<T> readList(String json, JavaType type) throws DeserializationException {
        try {
            return wrapper.getMapper().readValue(
                    json,
                    wrapper.getMapper().getTypeFactory().constructCollectionType(
                            List.class,
                            wrapper.getMapper().getTypeFactory().constructType(type)));
        }
        catch (JsonProcessingException e) {
            throw new DeserializationException(ERROR_MSG_DESERIALIZATION_FAILED, e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if typeInfo is null
     * @throws DeserializationException if typeInfo does not contain type information
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
                    .readValue(json, typeInfo.getType());
        }
        catch (IOException e) {
            throw new DeserializationException(ERROR_MSG_DESERIALIZATION_FAILED, e);
        }
    }


    @Override
    public <T extends ElementValue> T readValue(String json, Class<T> type) throws DeserializationException {
        try {
            return wrapper.getMapper().readValue(json, type);
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
     * @throws DeserializationException if typeInfo does not contain type information
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
            return (ElementValue[]) wrapper.getMapper()
                    .readerForArrayOf(containerTypeInfo.getContentType())
                    .withAttribute(ContextAwareElementValueDeserializer.VALUE_TYPE_CONTEXT, typeInfo)
                    .forType(wrapper.getMapper().getTypeFactory().constructArrayType(containerTypeInfo.getContentType()))
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
     * @throws DeserializationException if typeInfo does not contain type information
     * @throws DeserializationException if typeInfo does contain content type information
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
     * @throws DeserializationException if typeInfo does not contain type information
     * @throws DeserializationException if typeInfo does contain content type information
     */
    public <T extends ElementValue> Page<T> readValuePage(String json, TypeInfo typeInfo) throws DeserializationException {
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
            return (Page<T>) wrapper.getMapper().reader()
                    .withAttribute(ContextAwareElementValueDeserializer.VALUE_TYPE_CONTEXT, typeInfo)
                    .forType(wrapper.getMapper().getTypeFactory().constructParametricType(Page.class, ElementValue.class))
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
     * @throws DeserializationException if typeInfo does not contain type information
     * @throws DeserializationException if typeInfo does contain content type information
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


    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if typeInfo is null
     * @throws IllegalArgumentException if typeInfo is not a
     *             {@link de.fraunhofer.iosb.ilt.faaast.service.typing.ContainerTypeInfo}
     * @throws DeserializationException if typeInfo does not contain type information
     * @throws DeserializationException if typeInfo does contain content type information
     */
    @Override
    public <K, V extends ElementValue> Map<K, V> readValueMap(JsonNode json, TypeInfo typeInfo) throws DeserializationException {
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


    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends InvokeOperationRequest> T readValueOperationRequest(File file, Class<T> type, ServiceContext serviceContext, SubmodelElementIdentifier operationIdentifier)
            throws DeserializationException, IOException {
        return readValueOperationRequest(Files.readString(file.toPath()), type, serviceContext, operationIdentifier);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends InvokeOperationRequest> T readValueOperationRequest(String json, Class<T> type, ServiceContext serviceContext, SubmodelElementIdentifier operationIdentifier)
            throws DeserializationException {
        GetSubmodelElementByPathRequest metadataRequest = GetSubmodelElementByPathRequest.builder()
                .internal()
                .submodelId(operationIdentifier.getSubmodelId())
                .path(operationIdentifier.getIdShortPath().toString())
                .outputModifier(OutputModifier.DEFAULT)
                .build();
        GetSubmodelElementByPathResponse metadataResponse = (GetSubmodelElementByPathResponse) serviceContext.execute(metadataRequest);
        if (metadataResponse.getStatusCode() == StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND
                || Objects.isNull(metadataResponse.getPayload())) {
            throw new DeserializationException(String.format(
                    "error deserializing valueOnly operation invocation payload - no metadata found for operation with reference %s",
                    ReferenceHelper.asString(operationIdentifier.toReference())));
        }
        if (!Operation.class.isAssignableFrom(metadataResponse.getPayload().getClass())) {
            throw new DeserializationException(String.format(
                    "error deserializing valueOnly operation invocation payload - reference not pointing to an operation %s",
                    ReferenceHelper.asString(operationIdentifier.toReference())));
        }
        Operation operation = (Operation) metadataResponse.getPayload();

        T result;
        try {
            result = type.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new DeserializationException(String.format(
                    "instantiation of class failed (class: %s)",
                    type), e);
        }
        JsonNode node;
        try {
            node = wrapper.getMapper().readTree(json);
        }
        catch (IOException e) {
            throw new DeserializationException(String.format(
                    "error deserializing valueOnly operation invocation payload - unable to parse payload (reference: %s, payload: %s)",
                    ReferenceHelper.asString(operationIdentifier.toReference()),
                    json),
                    e);
        }
        result.setInputArguments(parseOperationVariables(node, KEY_OPERATION_INPUT_ARGUMENTS, operation.getInputVariables()));
        result.setInoutputArguments(parseOperationVariables(node, KEY_OPERATION_INOUTPUT_ARGUMENTS, operation.getInoutputVariables()));
        if (node.hasNonNull(KEY_OPERATION_CLIENT_TIMEOUT_DURATION)) {
            try {
                result.setTimeout(DatatypeFactory.newDefaultInstance().newDuration(node.get(KEY_OPERATION_CLIENT_TIMEOUT_DURATION).asText()));
            }
            catch (DateTimeParseException e) {
                throw new DeserializationException(String.format(
                        "Invalid value '%s' for property '%s', expected ISO8601 duration",
                        node.get(KEY_OPERATION_CLIENT_TIMEOUT_DURATION).asText(),
                        KEY_OPERATION_CLIENT_TIMEOUT_DURATION));
            }
        }
        result.setSubmodelId(operationIdentifier.getSubmodelId());
        result.setPath(operationIdentifier.getIdShortPath().toString());
        return result;

    }


    private List<OperationVariable> parseOperationVariables(JsonNode root, String propertyName, List<OperationVariable> template) throws DeserializationException {
        if (!root.hasNonNull(propertyName)) {
            return List.of();
        }
        JsonNode node = root.get(propertyName);
        Map<String, SubmodelElement> parameterTemplate = template.stream().collect(Collectors.toMap(x -> x.getValue().getIdShort(), x -> x.getValue()));
        TypeInfo typeInfo = TypeExtractor.extractTypeInfo(parameterTemplate);
        Map<String, ElementValue> providedArguments;
        try {
            providedArguments = readValueMap(node, typeInfo);
        }
        catch (DeserializationException e) {
            throw new DeserializationException(String.format("error reading property '%s'", propertyName), e);
        }
        List<OperationVariable> result = new ArrayList<>();
        for (var entry: providedArguments.entrySet()) {
            if (!parameterTemplate.containsKey(entry.getKey())) {
                throw new DeserializationException(
                        String.format("provided argument '%s' in '%s' not defined for operation",
                                entry.getKey(),
                                propertyName));
            }
            SubmodelElement temp = DeepCopyHelper.deepCopy(parameterTemplate.get(entry.getKey()));
            try {
                ElementValueMapper.setValue(temp, entry.getValue());
            }
            catch (ValueMappingException ex) {
                throw new DeserializationException(
                        String.format("error deserializing value for operation variable '%s'", propertyName));
            }
            result.add(new DefaultOperationVariable.Builder()
                    .value(temp)
                    .build());
        }
        return result;
    }


    @Override
    public <T> void useImplementation(Class<T> interfaceType, Class<? extends T> implementationType) {
        wrapper.useImplementation(interfaceType, implementationType);
    }


    /**
     * Modifies Jackson JsonMapper.
     *
     * @param mapper mapper to modify
     */
    protected void modifyMapper(JsonMapper mapper) {
        mapper.addMixIn(PropertyValue.class, PropertyValueMixin.class);
        mapper.addMixIn(AbstractRequestWithModifier.class, AbstractRequestWithModifierMixin.class);
        mapper.addMixIn(AbstractSubmodelInterfaceRequest.class, AbstractSubmodelInterfaceRequestMixin.class);
        mapper.addMixIn(ReferenceElementValue.class, ReferenceElementValueMixin.class);
        mapper.addMixIn(Page.class, PageMixin.class);
        mapper.addMixIn(InvokeOperationRequest.class, InvokeOperationRequestMixin.class);
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
        module.addDeserializer(SubmodelElementCollectionValue.class, new SubmodelElementCollectionValueDeserializer());
        module.addDeserializer(SubmodelElementListValue.class, new SubmodelElementListValueDeserializer());
        module.addDeserializer(MultiLanguagePropertyValue.class, new MultiLanguagePropertyValueDeserializer());
        module.addDeserializer(EntityValue.class, new EntityValueDeserializer());
        module.addDeserializer(ElementValue.class, new ElementValueDeserializer());
        module.addDeserializer(RangeValue.class, new RangeValueDeserializer());
        module.addDeserializer(PagingMetadata.class, new PagingMetadataDeserializer());
        ReflectionHelper.ENUMS.forEach(x -> module.addDeserializer(x, new EnumDeserializer(x)));
        mapper.registerModule(module);
        mapper.registerModule(new JavaTimeModule());
    }

}
