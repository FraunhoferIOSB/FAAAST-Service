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

import java.util.Collection;
import java.util.List;


/**
 * Helper class for actions on collections
 */
public class CollectionHelper {

    private CollectionHelper() {}


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
        //TODO: expand with other implementations
        else {
            collection.add(element);
        }
    }


    /**
     * Replaces or adds the element in the collection. If the concrete collection supports indexes
     * the element will be replaced at the same index.
     * If not, the element is added at the end of the collection and the old element is deleted.
     *
     * @param newElement to replace the old element
     * @param collection which contains the elements
     * @param oldElement the element to replace can be null
     * @param <T> type of the objects
     */
    public static <T> void put(Collection<T> collection, T oldElement, T newElement) {
        Ensure.requireNonNull(newElement, "Element must be non-null");
        int idx = List.class.isAssignableFrom(collection.getClass()) ? ((List<T>) collection)
                .indexOf(oldElement)
                : collection.size();
        collection.remove(oldElement);
        add(collection, idx, newElement);
    }

}
