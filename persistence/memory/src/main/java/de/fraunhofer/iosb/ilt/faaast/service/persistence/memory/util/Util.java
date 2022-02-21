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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.util;

import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShell;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Util {

    private Util() {}


    public static Identifiable findIdentifiableInListsById(Identifier id, Collection<? extends Identifiable>... requiredCollections) {
        Stream<? extends Identifiable> combinedStream = requiredCollections[0].stream();
        for (int i = 1; i < requiredCollections.length; i++) {
            combinedStream = Stream.concat(combinedStream, requiredCollections[i].stream());
        }

        return combinedStream.filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(id.getIdentifier())).findFirst().orElse(null);
    }


    public static List<AssetAdministrationShell> getDeepCopiedShells(Predicate<AssetAdministrationShell> filter, AssetAdministrationShellEnvironment aasEnvironment) {
        List<AssetAdministrationShell> shellList = aasEnvironment.getAssetAdministrationShells()
                .stream()
                .filter(filter)
                .collect(Collectors.toList());
        Class shellClass = shellList.size() > 0 ? shellList.get(0).getClass() : DefaultAssetAdministrationShell.class;
        return DeepCopyHelper.deepCopy(shellList,
                shellClass);
    }


    public static List<Submodel> getDeepCopiedSubmodels(Predicate<Submodel> filter, AssetAdministrationShellEnvironment aasEnvironment) {
        List<Submodel> submodelList = aasEnvironment.getSubmodels()
                .stream()
                .filter(filter)
                .collect(Collectors.toList());
        Class submodelClass = submodelList.size() > 0 ? submodelList.get(0).getClass() : DefaultSubmodel.class;
        return DeepCopyHelper.deepCopy(submodelList,
                submodelClass);
    }


    public static <T extends Identifiable> List<T> updateIdentifiableList(Class<T> identifiableClass, List<T> identifiableList, Identifiable identifiable) {
        List<T> newIdentifiableList = new ArrayList<>();
        identifiableList.forEach(x -> {
            if (!x.getIdentification().getIdentifier().equalsIgnoreCase(identifiable.getIdentification().getIdentifier())) {
                newIdentifiableList.add(x);
            }
        });
        newIdentifiableList.add((T) identifiable);
        return newIdentifiableList;
    }


    public static Method getGetListMethod(Class clazz, Object parent) {
        for (Method m: parent.getClass().getMethods()) {
            Type type = m.getGenericReturnType();
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;

                if (pt.getActualTypeArguments().length == 1) {
                    Type t = pt.getActualTypeArguments()[0];
                    if (Arrays.stream(clazz.getInterfaces()).anyMatch(x -> x.getName().equalsIgnoreCase(t.getTypeName()))) {
                        return m;
                    }
                    if (t.getTypeName().equalsIgnoreCase(clazz.getName())) {
                        return m;
                    }
                }
            }
            else if (m.getGenericParameterTypes().length > 0 && m.getGenericParameterTypes()[0].getTypeName().equalsIgnoreCase(clazz.getName())) {
                return m;
            }
        }
        return null;
    }

}
