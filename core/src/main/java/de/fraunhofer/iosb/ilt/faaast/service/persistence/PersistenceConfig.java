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
package de.fraunhofer.iosb.ilt.faaast.service.persistence;

import de.fraunhofer.iosb.ilt.faaast.service.config.Config;
import de.fraunhofer.iosb.ilt.faaast.service.config.ExtendableBuilder;


/**
 * Generic persistance configuration. When implementing a custom persistence inherit from this class to create a custom
 * configuration.
 * 
 * @param <T> type of the persistence
 */
public class PersistenceConfig<T extends Persistence> extends Config<T> {
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PersistenceConfig other = (PersistenceConfig) obj;
        return true;
    }


    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    /**
     * Abstract builder class that should be used for builders of inheriting classes.
     * 
     * @param <T> type of the persistence of the config to build
     * @param <C> type of the config to build
     * @param <B> type of this builder, needed for inheritance builder pattern
     */
    public static abstract class AbstractBuilder<T extends Persistence, C extends PersistenceConfig<T>, B extends AbstractBuilder<T, C, B>> extends ExtendableBuilder<C, B> {

    }

    /**
     * Builder for PersistenceConfig class.
     * 
     * @param <T> type of the persistence of the config to build
     */
    public static class Builder<T extends Persistence> extends AbstractBuilder<T, PersistenceConfig<T>, Builder<T>> {

        @Override
        protected Builder<T> getSelf() {
            return this;
        }


        @Override
        protected PersistenceConfig<T> newBuildingInstance() {
            return new PersistenceConfig<>();
        }

    }
}
