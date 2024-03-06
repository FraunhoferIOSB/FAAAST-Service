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
package de.fraunhofer.iosb.ilt.faaast.service.typing;

import com.google.common.reflect.TypeToken;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.SubmodelElementListHelper;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.model.AnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


/**
 * Extracts type information from different types of objects.
 */
public class TypeExtractor {

    private static final Type COLLECTION_GENERIC_TOKEN;
    private static final Type MAP_GENERIC_TOKEN;

    static {
        try {
            COLLECTION_GENERIC_TOKEN = TypeToken.of(Collection.class.getMethod("iterator").getGenericReturnType()).resolveType(Iterator.class.getTypeParameters()[0]).getType();
            MAP_GENERIC_TOKEN = Map.class.getMethod("get", Object.class).getGenericReturnType();
        }
        catch (NoSuchMethodException e) {
            throw new IllegalStateException("static initialization of TypeExtractor failed", e);
        }
    }

    private TypeExtractor() {}


    private static TypeInfo extractTypeInfoForSubmodelElement(SubmodelElement submodelElement) {
        Class<?> type = submodelElement.getClass();
        ElementValueTypeInfo.Builder builder = ElementValueTypeInfo.builder();
        builder.type(ElementValueMapper.getValueClass(submodelElement.getClass()));
        if (AnnotatedRelationshipElement.class.isAssignableFrom(type)) {
            AnnotatedRelationshipElement annotatedRelationshipElement = (AnnotatedRelationshipElement) submodelElement;
            annotatedRelationshipElement.getAnnotations().forEach(x -> builder.element(x.getIdShort(), extractTypeInfo(x)));
        }
        else if (SubmodelElementCollection.class.isAssignableFrom(type)) {
            SubmodelElementCollection submodelElementCollection = (SubmodelElementCollection) submodelElement;
            submodelElementCollection.getValue().forEach(x -> builder.element(x.getIdShort(), extractTypeInfo(x)));
        }
        else if (SubmodelElementList.class.isAssignableFrom(type)) {
            SubmodelElementList submodelElementList = (SubmodelElementList) submodelElement;
            builder.element(null, ElementValueTypeInfo.builder()
                    .type(ElementValueMapper.getValueClass(SubmodelElementListHelper.getElementType(submodelElementList)))
                    .datatype(SubmodelElementListHelper.getDatatype(submodelElementList))
                    .build());
        }
        else if (Entity.class.isAssignableFrom(type)) {
            Entity entity = (Entity) submodelElement;
            entity.getStatements().forEach(x -> builder.element(x.getIdShort(), extractTypeInfo(x)));
        }
        else if (Property.class.isAssignableFrom(type)) {
            Property property = (Property) submodelElement;
            builder.datatype(Datatype.fromAas4jDatatype(property.getValueType()));
        }
        else if (Range.class.isAssignableFrom(type)) {
            Range range = (Range) submodelElement;
            builder.datatype(Datatype.fromAas4jDatatype(range.getValueType()));
        }
        return builder.build();
    }


    private static ContainerTypeInfo<?> extractTypeInfoForSubmodel(Submodel submodel) {
        ContainerTypeInfo.Builder<Object> builder = ContainerTypeInfo.<Object> builder();
        builder.type(Submodel.class);
        submodel.getSubmodelElements().forEach(x -> builder.element(x.getIdShort(), extractTypeInfo(x)));
        return builder.build();
    }


    private static ContainerTypeInfo<?> extractTypeInfoForCollection(Collection<?> collection) {
        ContainerTypeInfo.Builder<Integer> builder = ContainerTypeInfo.<Integer> builder();
        builder.type(Collection.class);
        builder.contentType(TypeToken.of(collection.getClass()).resolveType(COLLECTION_GENERIC_TOKEN).getRawType());
        Iterator<?> iterator = collection.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            builder.element(i, extractTypeInfo(iterator.next()));
            i++;
        }
        return builder.build();
    }


    private static ContainerTypeInfo<?> extractTypeInfoForMap(Map<?, ?> map) {
        ContainerTypeInfo.Builder<String> builder = ContainerTypeInfo.<String> builder();
        builder.type(Map.class);
        builder.contentType(TypeToken.of(map.getClass()).resolveType(MAP_GENERIC_TOKEN).getRawType());
        map.forEach((key, value) -> builder.element(key.toString(), extractTypeInfo(value)));
        return builder.build();
    }


    private static ContainerTypeInfo<?> extractTypeInfoForArray(Object[] array) {
        ContainerTypeInfo.Builder<Integer> builder = ContainerTypeInfo.<Integer> builder();
        builder.type(Array.class);
        builder.contentType(array.getClass().getComponentType());
        for (int i = 0; i < array.length; i++) {
            builder.element(i, extractTypeInfo(array[i]));
        }
        return builder.build();
    }


    /**
     * Extracts type information for a given object.
     *
     * @param obj the object to extract type information from
     * @return the extracted type information
     */
    public static TypeInfo extractTypeInfo(Object obj) {
        if (obj == null) {
            return null;
        }
        Class<?> type = obj.getClass();
        if (SubmodelElement.class.isAssignableFrom(type)) {
            return extractTypeInfoForSubmodelElement((SubmodelElement) obj);
        }
        if (Submodel.class.isAssignableFrom(type)) {
            return extractTypeInfoForSubmodel((Submodel) obj);
        }
        if (Collection.class.isAssignableFrom(type)) {
            return extractTypeInfoForCollection((Collection) obj);
        }
        if (Map.class.isAssignableFrom(type)) {
            return extractTypeInfoForMap((Map) obj);
        }
        if (type.isArray()) {
            return extractTypeInfoForArray((Object[]) obj);
        }
        return ContainerTypeInfo.<Object> builder().build();
    }
}
