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
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.ApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.AbstractRequestWithModifierMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.AbstractSubmodelInterfaceRequestMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.InvokeOperationRequestMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.PageMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.ResultMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.ServiceSpecificationProfileMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value.ReferenceElementValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.EnumSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.ModifierAwareSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.PagingMetadataSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.ServiceSpecificationProfile;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractRequestWithModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractSubmodelInterfaceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.CollectionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReflectionHelper;
import java.util.List;
import java.util.Objects;


/**
 * JSON API serializer for FA³ST supporting different output modifier as defined by specification.
 */
public class JsonApiSerializer implements ApiSerializer {

    private final PathJsonSerializer pathSerializer;
    private final ValueOnlyJsonSerializer valueOnlySerializer;
    private final MetadataJsonSerializer metadataJsonSerializer;
    private final SerializerWrapper wrapper;

    public JsonApiSerializer() {
        this.wrapper = new SerializerWrapper(this::modifyMapper);
        this.pathSerializer = new PathJsonSerializer();
        this.valueOnlySerializer = new ValueOnlyJsonSerializer();
        this.metadataJsonSerializer = new MetadataJsonSerializer();
    }


    /**
     * Modifies Jackson JsonMapper.
     *
     * @param mapper mapper to modify
     */
    protected void modifyMapper(JsonMapper mapper) {
        SimpleModule module = new SimpleModule();
        ReflectionHelper.ENUMS.forEach(x -> module.addSerializer(x, new EnumSerializer()));
        module.addSerializer(PagingMetadata.class, new PagingMetadataSerializer());
        mapper.registerModule(module);
        mapper.addMixIn(AbstractRequestWithModifier.class, AbstractRequestWithModifierMixin.class);
        mapper.addMixIn(AbstractSubmodelInterfaceRequest.class, AbstractSubmodelInterfaceRequestMixin.class);
        mapper.addMixIn(ReferenceElementValue.class, ReferenceElementValueMixin.class);
        mapper.addMixIn(Page.class, PageMixin.class);
        mapper.addMixIn(InvokeOperationRequest.class, InvokeOperationRequestMixin.class);
        mapper.addMixIn(Result.class, ResultMixin.class);
        mapper.addMixIn(ServiceSpecificationProfile.class, ServiceSpecificationProfileMixin.class);
    }


    @Override
    public String write(Object obj, OutputModifier modifier) throws SerializationException {
        Ensure.requireNonNull(modifier, "modifier must be non-null");
        switch (modifier.getContent()) {
            case VALUE:
                return valueOnlySerializer.write(obj, modifier.getLevel(), modifier.getExtent());
            case PATH:
                return pathSerializer.write(null, obj, modifier.getLevel());
            case METADATA:
                return metadataJsonSerializer.write(obj);
            case NORMAL:
            default: {
                return serializeNormal(obj, modifier);
            }
        }
    }


    private String serializeNormal(Object obj, OutputModifier modifier) throws SerializationException {
        if (obj != null && ElementValue.class.isAssignableFrom(obj.getClass())) {
            return valueOnlySerializer.write(obj, modifier.getLevel(), modifier.getExtent());
        }
        try {
            JsonMapper mapper = wrapper.getMapper();
            if (Objects.nonNull(obj)) {
                if (List.class.isAssignableFrom(obj.getClass()) && !((List) obj).isEmpty()) {
                    ObjectWriter objectWriter = mapper
                            .writerFor(mapper.getTypeFactory()
                                    .constructCollectionType(List.class, ((List<Object>) obj).get(0).getClass()))
                            .withAttribute(ModifierAwareSerializer.LEVEL, modifier);
                    return objectWriter.writeValueAsString(obj);
                }
                if (Page.class.isAssignableFrom(obj.getClass())) {
                    Class<?> contentType = CollectionHelper.findMostSpecificCommonType(((Page) obj).getContent());
                    ObjectWriter objectWriter = mapper
                            .writerFor(mapper.getTypeFactory()
                                    .constructParametricType(Page.class, contentType))
                            .withAttribute(ModifierAwareSerializer.LEVEL, modifier);
                    return objectWriter.writeValueAsString(obj);
                }
            }
            return mapper.writer()
                    .withAttribute(ModifierAwareSerializer.LEVEL, modifier)
                    .writeValueAsString(obj);
        }
        catch (JsonProcessingException e) {
            throw new SerializationException("serialization failed", e);
        }
    }

}
