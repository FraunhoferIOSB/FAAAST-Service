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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Configuration class for {@link FileStorageInMemory}.
 */
public class FileStorageInMemoryConfig extends FileStorageConfig<FileStorageInMemory> {

    private Map<String, byte[]> files;

    public FileStorageInMemoryConfig() {
        this.files = new HashMap<>();
    }


    public Map<String, byte[]> getFiles() {
        return files;
    }


    public void setFiles(Map<String, byte[]> files) {
        this.files = files;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileStorageInMemoryConfig that = (FileStorageInMemoryConfig) o;
        if (files.size() != that.files.size()) {
            return false;
        }
        for (Map.Entry<String, byte[]> entry: files.entrySet()) {
            byte[] otherValue = that.files.get(entry.getKey());
            if (!Arrays.equals(entry.getValue(), otherValue)) {
                return false;
            }
        }
        return super.equals(o);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), files);
    }


    public static Builder builder() {
        return new Builder();
    }

    private abstract static class AbstractBuilder<T extends FileStorageInMemoryConfig, B extends AbstractBuilder<T, B>>
            extends FileStorageConfig.AbstractBuilder<FileStorageInMemory, T, B> {
        public B file(String name, byte[] value) {
            getBuildingInstance().getFiles().put(name, value);
            return getSelf();
        }


        public B files(Map<String, byte[]> value) {
            getBuildingInstance().setFiles(value);
            return getSelf();
        }
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
