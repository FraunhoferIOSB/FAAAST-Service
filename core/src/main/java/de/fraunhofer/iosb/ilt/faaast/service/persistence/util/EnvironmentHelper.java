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

import de.fraunhofer.iosb.ilt.faaast.service.util.CollectionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;


/**
 * Helper class for the persistence to handle with an AAS Environment.
 */
public class EnvironmentHelper {

    private EnvironmentHelper() {}


    /**
     * Get a list of deep copied asset administration shell objects which matches a filter.
     *
     * @param filter which should applied to the search of asset administration shells
     * @param aasEnvironment which contains the asset administration shells
     * @return a filtered list of deep copied asset administration shells
     */
    public static List<AssetAdministrationShell> getDeepCopiedShells(Predicate<AssetAdministrationShell> filter, Environment aasEnvironment) {
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
     * @param list to which the given identifiable should be added/updated
     * @param identifiable which should be added or updated
     * @param <T> type of the identifiable
     */
    public static <T extends Identifiable> void updateIdentifiableList(List<T> list, Identifiable identifiable) {
        Identifiable actualIdentifiable = list.stream()
                .filter(x -> x.getId().equalsIgnoreCase(identifiable.getId()))
                .findFirst()
                .orElse(null);

        int index = list.indexOf(actualIdentifiable);
        list.remove(actualIdentifiable);
        CollectionHelper.add(list, index, (T) identifiable);
    }

}
