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

import io.adminshell.aas.v3.dataformat.core.ReflectionHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ElementPathHelper {

    private static final String ELEMENT_PATH_SEPARATOR = "\\.";

    private ElementPathHelper() {}


    public static String toElementPath(Reference submodelElementRef) {
        if (submodelElementRef == null || submodelElementRef.getKeys().isEmpty()) {
            return "";
        }
        return submodelElementRef.getKeys().stream()
                .filter(x -> SubmodelElement.class.isAssignableFrom(AasUtils.keyTypeToClass(x.getType())))
                .map(x -> x.getValue())
                .collect(Collectors.joining("."));
    }


    public static List<Key> extractElementPath(Reference submodelElementRef) {
        return submodelElementRef.getKeys().stream()
                .filter(x -> SubmodelElement.class.isAssignableFrom(AasUtils.keyTypeToClass(x.getType())))
                .map(x -> {
                    x.setType(KeyElements.SUBMODEL_ELEMENT);
                    return x;
                })
                .collect(Collectors.toList());
    }


    public static Reference toReference(Reference parent, String elementPath) {
        Reference result;
        try {
            result = ReflectionHelper.getDefaultImplementation(Reference.class).getConstructor().newInstance();
            result.setKeys(Stream.concat(parent.getKeys().stream(), toKeys(elementPath).stream())
                    .collect(Collectors.toList()));
            return result;
        }
        catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            throw new RuntimeException("error instantiating reference implementation class", ex);
        }
    }


    public static List<Key> toKeys(String elementPath) {
        return Stream.of(elementPath.split(ELEMENT_PATH_SEPARATOR))
                .map(x -> {
                    try {
                        Key key = ReflectionHelper.getDefaultImplementation(Key.class).getConstructor().newInstance();
                        key.setIdType(IdentifierHelper.guessKeyType(x));
                        key.setType(KeyElements.SUBMODEL_ELEMENT);
                        key.setValue(x);
                        return key;
                    }
                    catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        throw new IllegalArgumentException("error parsing reference - could not instantiate reference type", ex);
                    }
                })
                .collect(Collectors.toList());
    }


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
}
