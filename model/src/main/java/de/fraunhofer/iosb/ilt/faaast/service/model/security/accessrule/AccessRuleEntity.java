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
package de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule;

/**
 * Interface describing an access rule entity, either a concrete instance or a placeholder referencing an entity by
 * name.
 *
 * @param <T> the concrete entity type
 */
public interface AccessRuleEntity<T> {

    /**
     * Returns whether this entity is a reference to another entity by name.
     *
     * @return true if this entity is a reference, otherwise false
     */
    default boolean isUse() {
        return false;
    }


    /**
     * Returns the name of the referenced entity.
     *
     * @return the name of the referenced entity
     */
    default String getUseName() {
        throw new IllegalArgumentException("Not supported");
    }


    /**
     * Returns the concrete entity instance.
     *
     * @return the concrete entity instance
     */
    default T getInstance() {
        throw new IllegalArgumentException("Not supported");
    }
}
