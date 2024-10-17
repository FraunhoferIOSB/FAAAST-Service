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

import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.HasSemantics;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;


/**
 * Helper class for complex persistence implementations.
 */
public class PersistenceHelper {
    private PersistenceHelper() {}


    /**
     * Filters the stream for a specific semanticId.
     *
     * @param stream
     * @param semanticId
     * @return result with matched semantic id
     * @param <T>
     */
    public static <T extends HasSemantics> Stream<T> filterBySemanticId(Stream<T> stream, Reference semanticId) {
        if (Objects.isNull(semanticId)) {
            return stream;
        }
        return stream.filter(x -> ReferenceHelper.equals(x.getSemanticId(), semanticId));
    }


    /**
     * Adds a submodel element from the parent to the collection.
     *
     * @param parent
     * @param submodelElementCollection
     */
    public static void addSubmodelElementsFromParentToCollection(Referable parent, Collection<SubmodelElement> submodelElementCollection) {
        if (Submodel.class.isAssignableFrom(parent.getClass())) {
            submodelElementCollection.addAll(((Submodel) parent).getSubmodelElements());
        }
        else if (SubmodelElementCollection.class.isAssignableFrom(parent.getClass())) {
            submodelElementCollection.addAll(((SubmodelElementCollection) parent).getValue());
        }
        else if (SubmodelElementList.class.isAssignableFrom(parent.getClass())) {
            submodelElementCollection.addAll(((SubmodelElementList) parent).getValue());
        }
    }


    /**
     * Splits the provided asset ids into groups global and specific ids.
     *
     * @param assetIds
     * @param globalIds
     * @param specificIds
     */
    public static void splitAssetIdsIntoGlobalAndSpecificIds(List<AssetIdentification> assetIds,
                                                             List<String> globalIds,
                                                             List<SpecificAssetId> specificIds) {
        Ensure.requireNonNull(assetIds);
        Ensure.requireNonNull(globalIds);
        Ensure.requireNonNull(specificIds);

        globalIds.addAll(assetIds.stream()
                .filter(x -> GlobalAssetIdentification.class.isAssignableFrom(x.getClass()))
                .map(GlobalAssetIdentification.class::cast)
                .map(x -> x.getValue())
                .toList());
        specificIds.addAll(assetIds.stream()
                .filter(x -> SpecificAssetIdentification.class.isAssignableFrom(x.getClass()))
                .map(x -> new DefaultSpecificAssetId.Builder()
                        .name(((SpecificAssetIdentification) x).getKey())
                        .value(x.getValue())
                        .build())
                .toList());
    }
}
