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
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.PageMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.metadata.AnnotatedRelationshipElementMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.metadata.AssetAdministrationShellMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.metadata.BlobMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.metadata.EntityMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.metadata.FileMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.metadata.MultiLanguagePropertyMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.metadata.PropertyMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.metadata.RangeMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.metadata.ReferenceElementMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.metadata.RelationshipElementMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.metadata.SubmodelElementCollectionMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.metadata.SubmodelElementListMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.metadata.SubmodelMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.PagingMetadataSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.util.CollectionHelper;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.RelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


/**
 * Serializer for content=metadata.
 */
public class MetadataJsonSerializer {

    private final SerializerWrapper wrapper;

    public MetadataJsonSerializer() {
        this.wrapper = new SerializerWrapper(this::modifyMapper);
    }


    /**
     * Modifies Jackson JsonMapper.
     *
     * @param mapper mapper to modify
     */
    protected void modifyMapper(JsonMapper mapper) {
        mapper.addMixIn(AssetAdministrationShell.class, AssetAdministrationShellMixin.class);
        mapper.addMixIn(Submodel.class, SubmodelMixin.class);
        mapper.addMixIn(Entity.class, EntityMixin.class);
        mapper.addMixIn(SubmodelElementCollection.class, SubmodelElementCollectionMixin.class);
        mapper.addMixIn(SubmodelElementList.class, SubmodelElementListMixin.class);
        mapper.addMixIn(Property.class, PropertyMixin.class);
        mapper.addMixIn(MultiLanguageProperty.class, MultiLanguagePropertyMixin.class);
        mapper.addMixIn(Range.class, RangeMixin.class);
        mapper.addMixIn(ReferenceElement.class, ReferenceElementMixin.class);
        mapper.addMixIn(RelationshipElement.class, RelationshipElementMixin.class);
        mapper.addMixIn(AnnotatedRelationshipElement.class, AnnotatedRelationshipElementMixin.class);
        mapper.addMixIn(Blob.class, BlobMixin.class);
        mapper.addMixIn(File.class, FileMixin.class);
        mapper.addMixIn(Page.class, PageMixin.class);
        SimpleModule module = new SimpleModule();
        module.addSerializer(PagingMetadata.class, new PagingMetadataSerializer());
        mapper.registerModule(module);
    }


    /**
     * Converts obj to JSON string.
     *
     * @param obj the object to serialize
     * @return the JSON representation
     * @throws SerializationException if serialization fails
     */
    public String write(Object obj) throws SerializationException {
        try {
            JsonMapper mapper = wrapper.getMapper();
            if (Objects.nonNull(obj)) {
                if (List.class.isAssignableFrom(obj.getClass()) && !((List) obj).isEmpty()) {
                    ObjectWriter objectWriter = mapper
                            .writerFor(mapper.getTypeFactory()
                                    .constructCollectionType(List.class, ((List<Object>) obj).get(0).getClass()));
                    return objectWriter.writeValueAsString(obj);
                }
                if (Page.class.isAssignableFrom(obj.getClass())) {
                    Class<?> contentType = CollectionHelper.findMostSpecificCommonType(((Page) obj).getContent());
                    ObjectWriter objectWriter = mapper
                            .writerFor(mapper.getTypeFactory()
                                    .constructParametricType(Page.class, contentType));
                    return objectWriter.writeValueAsString(obj);
                }
            }
            return mapper.writer()
                    .writeValueAsString(obj);
        }
        catch (JsonProcessingException e) {
            throw new SerializationException("serialization failed", e);
        }
    }

}
