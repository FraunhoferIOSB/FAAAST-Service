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

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.TypeInstantiationException;
import io.adminshell.aas.v3.dataformat.core.ReflectionHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Helper class with methods to read/write element paths from/to
 * <ul>
 * <li>{@link io.adminshell.aas.v3.model.Reference}
 * <li>{@link io.adminshell.aas.v3.model.Key}
 * </ul>
 */
public class ElementPathHelper {

    private static final String ELEMENT_PATH_SEPARATOR = "\\.";

    private ElementPathHelper() {}


    /**
     * Create an element path out of a
     * {@link io.adminshell.aas.v3.model.Reference} to a
     * {@link io.adminshell.aas.v3.model.SubmodelElement}.
     *
     * @param submodelElementRef reference to the submodel element
     * @return values of the keys of the reference separated by a "."
     */
    public static String toElementPath(Reference submodelElementRef) {
        if (submodelElementRef == null || submodelElementRef.getKeys().isEmpty()) {
            return "";
        }
        return submodelElementRef.getKeys().stream()
                .filter(x -> SubmodelElement.class.isAssignableFrom(AasUtils.keyTypeToClass(x.getType())))
                .map(x -> x.getValue())
                .collect(Collectors.joining("."));
    }


    /**
     * Combines a reference and an element path to one reference
     *
     * @param parent reference of the parent
     * @param elementPath which should be added to the parent reference
     * @return the combined reference
     */
    public static Reference toReference(Reference parent, String elementPath) {
        Reference result;
        try {
            result = ReflectionHelper.getDefaultImplementation(Reference.class).getConstructor().newInstance();
            result.setKeys(Stream.concat(parent.getKeys().stream(), toKeys(elementPath).stream())
                    .collect(Collectors.toList()));
            return result;
        }
        catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            throw new TypeInstantiationException("error instantiating reference implementation class", e);
        }
    }


    /**
     * Converts an element path to a list of keys. Each key in the list have the
     * general key element "SUBMODEL_ELEMENT"
     *
     * @param elementPath a string with identifier values seperated by a "."
     * @return the list of keys
     */
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
                    catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        throw new IllegalArgumentException("error parsing reference - could not instantiate reference type", e);
                    }
                })
                .collect(Collectors.toList());
    }

}
