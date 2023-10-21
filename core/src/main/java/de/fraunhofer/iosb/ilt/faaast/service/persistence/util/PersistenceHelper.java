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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetID;


public class PersistenceHelper {
    private PersistenceHelper() {}


    private static long readCursor(String cursor) {
        return Long.parseLong(cursor);
    }


    private static String writeCursor(long index) {
        return Long.toString(index);
    }


    private static String nextCursor(PagingInfo paging, int resultCount) {
        return nextCursor(paging, paging.hasLimit() && resultCount > paging.getLimit());
    }


    private static String nextCursor(PagingInfo paging, boolean hasMoreData) {
        if (!hasMoreData) {
            return null;
        }
        if (!paging.hasLimit()) {
            throw new IllegalStateException("unable to generate next cursor for paging - there should not be more data available if previous request did not have a limit set");
        }
        if (Objects.isNull(paging.getCursor())) {
            return writeCursor(paging.getLimit());
        }
        return writeCursor(readCursor(paging.getCursor()) + paging.getLimit());
    }


    public static <T extends Referable> Page<T> preparePagedResult(Stream<T> input, QueryModifier modifier, PagingInfo paging) {
        Stream<T> result = input;
        if (Objects.nonNull(paging.getCursor())) {
            result = result.skip(readCursor(paging.getCursor()));
        }
        if (paging.hasLimit()) {
            result = result.limit(paging.getLimit() + 1);
        }
        List<T> temp = result.collect(Collectors.toList());
        return Page.<T> builder()
                .result(QueryModifierHelper.applyQueryModifier(
                        temp.stream()
                                .limit(paging.hasLimit() ? paging.getLimit() : temp.size())
                                .map(DeepCopyHelper::deepCopy)
                                .collect(Collectors.toList()),
                        modifier))
                .metadata(PagingMetadata.builder()
                        .cursor(nextCursor(paging, temp.size()))
                        .build())
                .build();
    }


    public static <T extends HasSemantics> Stream<T> filterBySemanticId(Stream<T> stream, Reference semanticId) {
        if (Objects.isNull(semanticId)) {
            return stream;
        }
        return stream.filter(x -> ReferenceHelper.equals(x.getSemanticID(), semanticId));
    }


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


    public static void splitAssetIdsIntoGlobalAndSpecificIds(List<AssetIdentification> assetIds,
                                                             List<String> globalIds,
                                                             List<SpecificAssetID> specificIds) {
        Ensure.requireNonNull(assetIds);
        Ensure.requireNonNull(globalIds);
        Ensure.requireNonNull(specificIds);

        globalIds.addAll(assetIds.stream()
                .filter(x -> GlobalAssetIdentification.class.isAssignableFrom(x.getClass()))
                .map(GlobalAssetIdentification.class::cast)
                .map(x -> x.getValue())
                .collect(Collectors.toList()));
        specificIds.addAll(assetIds.stream()
                .filter(x -> SpecificAssetIdentification.class.isAssignableFrom(x.getClass()))
                .map(x -> new DefaultSpecificAssetID.Builder()
                        .name(((SpecificAssetIdentification) x).getKey())
                        .value(((SpecificAssetIdentification) x).getValue())
                        .build())
                .collect(Collectors.toList()));
    }
}
