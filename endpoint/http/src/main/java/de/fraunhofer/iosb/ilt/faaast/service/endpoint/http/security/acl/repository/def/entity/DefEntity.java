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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.acl.repository.def.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A base class representing a map of named DEFINED entities.
 *
 * @param <T> the entity value type
 */
public abstract class DefEntity<T> extends ConcurrentHashMap<String, T> {

    public DefEntity(Map<String, T> entries) {
        super(entries);
    }


    /**
     * Creates a new instance of this entity from the given entries.
     *
     * @param entries the entries
     * @return the new instance
     */
    public DefEntity<T> from(Map<String, T> entries) {
        return getInstance(entries);
    }


    /**
     * Creates a new instance of this entity from the given entries.
     *
     * @param entries the entries
     * @return the new instance
     */
    public abstract DefEntity<T> getInstance(Map<String, T> entries);
}
