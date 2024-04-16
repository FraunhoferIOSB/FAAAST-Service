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
package de.fraunhofer.iosb.ilt.faaast.service.filestorage.memory;

import de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorageConfig;


/**
 * Configuration class for {@link FileStorageInMemory}.
 */
public class FileStorageInMemoryConfig extends FileStorageConfig<FileStorageInMemory> {

    public static Builder builder() {
        return new Builder();
    }

    private abstract static class AbstractBuilder<T extends FileStorageInMemoryConfig, B extends AbstractBuilder<T, B>>
            extends FileStorageConfig.AbstractBuilder<FileStorageInMemory, T, B> {

    }

    public static class Builder extends AbstractBuilder<FileStorageInMemoryConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected FileStorageInMemoryConfig newBuildingInstance() {
            return new FileStorageInMemoryConfig();
        }
    }
}
