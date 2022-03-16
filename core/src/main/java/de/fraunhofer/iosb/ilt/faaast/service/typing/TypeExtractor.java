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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import io.adminshell.aas.v3.model.AnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.Entity;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


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

    public static TypeInfo extractTypeInfo(Object obj) {
        if (obj == null) {
            return null;
        }
        Class<?> type = obj.getClass();
        if (SubmodelElement.class.isAssignableFrom(type)) {
            ElementValueTypeInfo.Builder builder = ElementValueTypeInfo.builder();
            SubmodelElement submodelElement = (SubmodelElement) obj;
            builder.type(ElementValueMapper.getValueClass(submodelElement.getClass()));
            if (AnnotatedRelationshipElement.class.isAssignableFrom(type)) {
                AnnotatedRelationshipElement annotatedRelationshipElement = (AnnotatedRelationshipElement) obj;
                annotatedRelationshipElement.getAnnotations().forEach(x -> builder.element(x.getIdShort(), extractTypeInfo(x)));
            }
            else if (SubmodelElementCollection.class.isAssignableFrom(type)) {
                SubmodelElementCollection submodelElementCollection = (SubmodelElementCollection) obj;
                submodelElementCollection.getValues().forEach(x -> builder.element(x.getIdShort(), extractTypeInfo(x)));
            }
            else if (Entity.class.isAssignableFrom(type)) {
                Entity entity = (Entity) obj;
                entity.getStatements().forEach(x -> builder.element(x.getIdShort(), extractTypeInfo(x)));
            }
            else if (Property.class.isAssignableFrom(obj.getClass())) {
                Property property = (Property) obj;
                builder.datatype(Datatype.fromName(property.getValueType()));
            }
            else if (Range.class.isAssignableFrom(obj.getClass())) {
                Range range = (Range) obj;
                builder.datatype(Datatype.fromName(range.getValueType()));
            }
            return builder.build();
        }
        if (Submodel.class.isAssignableFrom(type)) {
            Submodel submodel = (Submodel) obj;
            ContainerTypeInfo.Builder<Object> builder = ContainerTypeInfo.<Object> builder();
            builder.type(Submodel.class);
            submodel.getSubmodelElements().forEach(x -> builder.element(x.getIdShort(), extractTypeInfo(x)));
            return builder.build();
        }
        if (Collection.class.isAssignableFrom(type)) {
            Collection collection = (Collection) obj;
            ContainerTypeInfo.Builder<Integer> builder = ContainerTypeInfo.<Integer> builder();
            builder.type(Collection.class);
            builder.contentType(TypeToken.of(type).resolveType(COLLECTION_GENERIC_TOKEN).getRawType());
            Iterator iterator = collection.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                builder.element(i, extractTypeInfo(iterator.next()));
                i++;
            }
            return builder.build();
        }
        if (Map.class.isAssignableFrom(type)) {
            Map map = (Map) obj;
            ContainerTypeInfo.Builder<String> builder = ContainerTypeInfo.<String> builder();
            builder.type(Map.class);
            builder.contentType(TypeToken.of(type).resolveType(MAP_GENERIC_TOKEN).getRawType());
            map.forEach((key, value) -> builder.element(key.toString(), extractTypeInfo(value)));
            return builder.build();
        }
        if (type.isArray()) {
            Object[] array = (Object[]) obj;
            ContainerTypeInfo.Builder<Integer> builder = ContainerTypeInfo.<Integer> builder();
            builder.type(Array.class);
            builder.contentType(type.getComponentType());
            for (int i = 0; i < array.length; i++) {
                builder.element(i, extractTypeInfo(array[i]));
            }
            return builder.build();
        }
        return ContainerTypeInfo.<Object> builder().build();
    }
}
