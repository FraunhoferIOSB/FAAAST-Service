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

import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Utility class for querying and manipulating AAS objects.
 */
public class AasHelper {

    private AasHelper() {}


    /**
     * Applies/Copies basic properties from one {@link io.adminshell.aas.v3.model.SubmodelElementCollection} to another.
     *
     * @param src the source to copy from
     * @param target the target to copy to
     */
    public static void applyBasicProperties(SubmodelElementCollection src, SubmodelElementCollection target) {
        target.setAllowDuplicates(src.getAllowDuplicates());
        target.setCategory(src.getCategory());
        target.setDescriptions(src.getDescriptions());
        target.setDisplayNames(src.getDisplayNames());
        target.setEmbeddedDataSpecifications(src.getEmbeddedDataSpecifications());
        target.setExtensions(src.getExtensions());
        target.setIdShort(src.getIdShort());
        target.setKind(src.getKind());
        target.setOrdered(src.getOrdered());
        target.setQualifiers(src.getQualifiers());
        target.setSemanticId(src.getSemanticId());
    }


    /**
     * Applies/Copies basic properties from one {@link io.adminshell.aas.v3.model.Submodel} to another.
     *
     * @param src the source to copy from
     * @param target the target to copy to
     */
    public static void applyBasicProperties(Submodel src, Submodel target) {
        target.setAdministration(src.getAdministration());
        target.setCategory(src.getCategory());
        target.setDescriptions(src.getDescriptions());
        target.setDisplayNames(src.getDisplayNames());
        target.setEmbeddedDataSpecifications(src.getEmbeddedDataSpecifications());
        target.setExtensions(src.getExtensions());
        target.setIdShort(src.getIdShort());
        target.setIdentification(src.getIdentification());
        target.setKind(src.getKind());
        target.setQualifiers(src.getQualifiers());
        target.setSemanticId(src.getSemanticId());
    }


    /**
     * Gets elements from a collection of elements by idShort.
     *
     * @param collection the collection to search
     * @param idShort the idShort of the elements to get
     * @return a list of elements with the given idShort
     */
    public static List<SubmodelElement> getElementsByIdShort(Collection<SubmodelElement> collection, String idShort) {
        if (collection == null) {
            return List.of();
        }
        return collection.stream()
                .filter(x -> Objects.equals(idShort, x.getIdShort())).collect(Collectors.toList());
    }


    /**
     * Gets elements from a collection of elements by idShort and type.
     *
     * @param <T> expected type of the elements
     * @param collection the collection to search
     * @param idShort the idShort of the elements to get
     * @param type expected type of the elements
     * @return a list of elements with the given idShort and type
     */
    public static <T extends SubmodelElement> List<T> getElementsByIdShort(Collection<SubmodelElement> collection, String idShort, Class<T> type) {
        if (collection == null) {
            return List.of();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .filter(x -> Objects.equals(idShort, x.getIdShort()))
                .filter(x -> type.isAssignableFrom(x.getClass()))
                .map(x -> (T) x)
                .collect(Collectors.toList());
    }


    /**
     * Gets elements from a collection of elements by semanticId and type.
     *
     * @param <T> expected type of the elements
     * @param collection the collection to search
     * @param semanticId the semanticId of the elements to get
     * @param type expected type of the elements
     * @return a list of elements with the given idShort and type
     */
    public static <T extends SubmodelElement> List<T> getElementsBySemanticId(Collection<SubmodelElement> collection, String semanticId, Class<T> type) {
        if (collection == null) {
            return List.of();
        }
        return collection.stream()
                .filter(x -> Objects.equals(ReferenceHelper.globalReference(semanticId), x.getSemanticId()))
                .filter(Objects::nonNull)
                .filter(x -> type.isAssignableFrom(x.getClass()))
                .map(x -> (T) x)
                .collect(Collectors.toList());
    }


    /**
     * Gets a single element from a collection of elements by semanticId and type.
     *
     * @param <T> expected type of the element
     * @param collection the collection to search
     * @param semanticId the semanticId of the elements to get
     * @param type expected type of the element
     * @return the elements with the given idShort
     * @throws IllegalArgumentException if no matching element is found
     */
    public static <T extends SubmodelElement> T getElementBySemanticId(Collection<SubmodelElement> collection, String semanticId, Class<T> type) {
        List<T> matches = getElementsBySemanticId(collection, semanticId, type);
        if (matches.isEmpty()) {
            return null;
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException(String.format("expected at most one SubmodelElement with semanticId '%s' in collection, but found %d",
                    semanticId,
                    matches.size()));
        }
        return matches.get(0);
    }


    /**
     * Gets a single element from a collection of elements by idShort.
     *
     * @param collection the collection to search
     * @param idShort the idShort of the element to get
     * @return the elements with the given idShort
     * @throws IllegalArgumentException if no matching element is found
     */
    public static SubmodelElement getElementByIdShort(Collection<SubmodelElement> collection, String idShort) {
        List<SubmodelElement> matches = getElementsByIdShort(collection, idShort);
        if (matches.isEmpty()) {
            return null;
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException(String.format("expected at most one SubmodelElement with idShort '%s' in collection, but found %d",
                    idShort,
                    matches.size()));
        }
        return matches.get(0);
    }


    /**
     * Gets a single element from a collection of elements by idShort and type.
     *
     * @param <T> expected type of the element
     * @param collection the collection to search
     * @param idShort the idShort of the element to get
     * @param type expected type of the element
     * @return the elements with the given idShort
     * @throws IllegalArgumentException if no matching element is found
     */
    public static <T extends SubmodelElement> T getElementByIdShort(Collection<SubmodelElement> collection, String idShort, Class<T> type) {
        List<T> matches = getElementsByIdShort(collection, idShort, type);
        if (matches.isEmpty()) {
            return null;
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException(String.format("expected at most one SubmodelElement with idShort '%s' in collection, but found %d",
                    idShort,
                    matches.size()));
        }
        return matches.get(0);
    }
}
