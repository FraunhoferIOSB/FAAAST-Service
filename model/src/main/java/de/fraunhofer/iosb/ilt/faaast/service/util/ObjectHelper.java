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
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;


/**
 * Generic helper functions operating on any object.
 */
public class ObjectHelper {

    private ObjectHelper() {}


    /**
     * Checks if an object is a collection-like type ({@link Collection}, {@link Map} or array).
     *
     * @param obj The object to check
     * @return true if object is collection-like type, false otherwise. If input is null, false is returned.
     */
    public static boolean isCollectionLikeType(Object obj) {
        if (obj == null) {
            return false;
        }
        return Collection.class.isAssignableFrom(obj.getClass())
                || Map.class.isAssignableFrom(obj.getClass())
                || obj.getClass().isArray();
    }


    /**
     * If {@code obj} is a collection-like type, {@code consumer} is applied to each element, otherwise only to
     * {@code obj} itself.
     *
     * @param obj The object to apply the {@code consumer} to
     * @param consumer The consumer to apply
     */
    public static void forEach(Object obj, Consumer<Object> consumer) {
        if (obj == null) {
            return;
        }
        if (Collection.class.isAssignableFrom(obj.getClass())) {
            ((Collection) obj).forEach(consumer::accept);
            return;
        }
        if (Map.class.isAssignableFrom(obj.getClass())) {
            ((Map) obj).values().forEach(consumer::accept);
            return;
        }
        if (obj.getClass().isArray()) {
            for (var x: ((Object[]) obj)) {
                consumer.accept(x);
            }
            return;
        }
        consumer.accept(obj);
    }


    /**
     * Checks if two lists are equal ignoring the order of the lists.
     *
     * @param list1 first list
     * @param list2 second list
     * @return true if lists are equal ignoring order, false otherwise
     */
    public static boolean equalsIgnoreOrder(List<?> list1, List<?> list2) {
        if (Objects.isNull(list1) && Objects.isNull(list2)) {
            return true;
        }
        if (Objects.isNull(list1) || Objects.isNull(list2)) {
            return false;
        }
        return list1.size() == list2.size() && list1.containsAll(list2);
    }
}
