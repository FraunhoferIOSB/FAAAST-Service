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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.memory;

import de.fraunhofer.iosb.ilt.faaast.service.persistence.PersistenceConfig;


/**
 * Configuration class for {@link PersistenceInMemory}.
 */
public class PersistenceInMemoryConfig extends PersistenceConfig<PersistenceInMemory> {

    public static Builder builder() {
        return new Builder();
    }

    private abstract static class AbstractBuilder<T extends PersistenceInMemoryConfig, B extends AbstractBuilder<T, B>>
            extends PersistenceConfig.AbstractBuilder<PersistenceInMemory, T, B> {

    }

    public static class Builder extends AbstractBuilder<PersistenceInMemoryConfig, Builder> {
        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected PersistenceInMemoryConfig newBuildingInstance() {
            return new PersistenceInMemoryConfig();
        }
    }
}
