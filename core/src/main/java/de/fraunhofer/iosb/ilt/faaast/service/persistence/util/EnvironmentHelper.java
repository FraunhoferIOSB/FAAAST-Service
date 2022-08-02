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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.util;

import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShell;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Helper class for the persistence to handle with an AAS Environment
 */
public class EnvironmentHelper {

    private EnvironmentHelper() {}


    /**
     * Get a list of deep copied asset administration shell objects which
     * matches a filter
     *
     * @param filter which should applied to the search of asset administration
     *            shells
     * @param aasEnvironment which contains the asset administration shells
     * @return a filtered list of deep copied asset administration shells
     */
    public static List<AssetAdministrationShell> getDeepCopiedShells(Predicate<AssetAdministrationShell> filter, AssetAdministrationShellEnvironment aasEnvironment) {
        List<AssetAdministrationShell> shellList = aasEnvironment.getAssetAdministrationShells()
                .stream()
                .filter(filter)
                .collect(Collectors.toList());
        Class<? extends AssetAdministrationShell> shellClass = !shellList.isEmpty() ? shellList.get(0).getClass() : DefaultAssetAdministrationShell.class;
        return DeepCopyHelper.deepCopy(shellList,
                shellClass);
    }


    /**
     * Adds or updates an identifiable to a list.
     *
     * @param list to which the given identifiable should be
     *            added/updated
     * @param identifiable which should be added or updated
     * @param <T> type of the identifiable
     */
    public static <T extends Identifiable> void updateIdentifiableList(List<T> list, Identifiable identifiable) {
        Identifiable actualIdentifiable = list.stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(identifiable.getIdentification().getIdentifier()))
                .findFirst()
                .orElse(null);

        int index = list.indexOf(actualIdentifiable);
        list.remove(actualIdentifiable);
        add(list, index, (T) identifiable);
    }


    /**
     * Adds the element to the collection. If the concrete collection supports adding an element at a specific index
     * the element will be added at the given index.
     * If not, the element is added at the end of the collection.
     *
     * @param collection to add the element
     * @param index where to add the element in the list. Ignored when negative.
     * @param element to add
     * @param <T> type of the element
     */
    public static <T> void add(Collection<T> collection, int index, T element) {
        Ensure.requireNonNull(element, "Element must be non-null");
        if (List.class.isAssignableFrom(collection.getClass())) {
            ((List<T>) collection).add(index >= 0 ? index : collection.size(), element);
        }
        else {
            collection.add(element);
        }
    }

}
