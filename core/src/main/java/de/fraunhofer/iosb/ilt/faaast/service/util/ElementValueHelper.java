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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import com.google.common.reflect.TypeToken;
import de.fraunhofer.iosb.ilt.faaast.service.model.valuedata.ElementValue;
import io.adminshell.aas.v3.model.AnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.DataElement;
import io.adminshell.aas.v3.model.Entity;
import io.adminshell.aas.v3.model.ReferenceElement;
import io.adminshell.aas.v3.model.RelationshipElement;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;


public class ElementValueHelper {

    private static final Type COLLECTION_GENERIC_TOKEN;
    private static final Type MAP_GENERIC_TOKEN;

    static {
        try {
            COLLECTION_GENERIC_TOKEN = TypeToken.of(Collection.class.getMethod("iterator").getGenericReturnType()).resolveType(Iterator.class.getTypeParameters()[0]).getType();
            MAP_GENERIC_TOKEN = Map.class.getMethod("get", Object.class).getGenericReturnType();
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private ElementValueHelper() {}


    public static boolean isValueOnlySupported(Object obj) {
        if (obj == null) {
            return true;
        }
        Class type = obj.getClass();
        if (type.isArray()) {
            return Stream.of((Object[]) obj).allMatch(x -> isValueOnlySupported(x));
        }
        if (Collection.class.isAssignableFrom(type)) {
            return ((Collection) obj).stream().allMatch(x -> isValueOnlySupported(x));
        }
        if (Map.class.isAssignableFrom(type)) {
            return ((Map) obj).values().stream().allMatch(x -> isValueOnlySupported(x));
        }
        return isValueOnlySupported(type);
    }


    public static boolean isValueOnlySupported(Class<?> type) {
        if (isSerializableAsValue(type) || Submodel.class.isAssignableFrom(type) || ElementValue.class.isAssignableFrom(type)) {
            return true;
        }
        if (type.isArray()) {
            return isValueOnlySupported(TypeToken.of(type).getComponentType());
        }
        if (Collection.class.isAssignableFrom(type)) {
            return isValueOnlySupported(TypeToken.of(type).resolveType(COLLECTION_GENERIC_TOKEN).getRawType());
        }
        if (Map.class.isAssignableFrom(type)) {
            return isValueOnlySupported(TypeToken.of(type).resolveType(MAP_GENERIC_TOKEN).getRawType());
        }
        return false;
    }


    public static boolean isSerializableAsValue(Class<?> type) {
        return DataElement.class.isAssignableFrom(type)
                || SubmodelElementCollection.class.isAssignableFrom(type)
                || ReferenceElement.class.isAssignableFrom(type)
                || RelationshipElement.class.isAssignableFrom(type)
                || AnnotatedRelationshipElement.class.isAssignableFrom(type)
                || Entity.class.isAssignableFrom(type);
    }
}
