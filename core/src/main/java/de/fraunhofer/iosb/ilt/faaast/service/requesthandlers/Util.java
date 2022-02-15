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
package de.fraunhofer.iosb.ilt.faaast.service.requesthandlers;

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueMapper;
import io.adminshell.aas.v3.dataformat.core.ReflectionHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Util {

    public static Reference toReference(List<Key> keys) {
        return new DefaultReference.Builder()
                .keys(keys)
                .build();
    }


    public static Reference toReference(List<Key> keys, Identifier parentId, Class<?> parentClass) {
        Reference parentReference = toReference(parentId, parentClass);
        Reference childReference = new DefaultReference.Builder()
                .keys(keys)
                .build();
        return toReference(parentReference, childReference);
    }


    public static Reference toReference(Reference parentReference, Reference childReference) {
        List<Key> keys = new ArrayList<>(parentReference.getKeys());
        childReference.getKeys().forEach(x -> {
            if (!keys.contains(x)) {
                keys.add(x);
            }
        });
        return new DefaultReference.Builder()
                .keys(keys)
                .build();
    }


    public static Reference toReference(Identifier id, Class<?> clazz) {
        return new DefaultReference.Builder()
                .keys(List.of(new DefaultKey.Builder()
                        .value(id.getIdentifier())
                        .type(referableToKeyType(clazz))
                        .idType(KeyType.IRI)
                        .build()))
                .build();
    }


    public static KeyElements referableToKeyType(Class<?> clazz) {
        Class<?> aasInterface = ReflectionHelper.getAasInterface(clazz);
        return aasInterface != null ? KeyElements.valueOf(AasUtils.deserializeEnumName(aasInterface.getSimpleName())) : null;
    }


    public static List<ElementValue> toValues(List<OperationVariable> variables) {
        return variables.stream()
                .map(x -> ElementValueMapper.<SubmodelElement, ElementValue> toValue(x.getValue()))
                .collect(Collectors.toList());
    }

}
