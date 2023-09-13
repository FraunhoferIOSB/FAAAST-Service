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
package de.fraunhofer.iosb.ilt.faaast.service.model;

import java.util.Objects;


/**
 * Represents a file loaded to memory.
 */
public class InMemoryFile extends AbstractFileContent {

    private String path;

    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }


    /**
     * Returns a {@link FileContent} with the same values as this instance.
     *
     * @return a {@link FileContent} representation of this instance
     */
    public FileContent asFileContent() {
        return FileContent.builder()
                .content(getContent())
                .path(getPath())
                .build();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InMemoryFile that = (InMemoryFile) o;
        return super.equals(that)
                && Objects.equals(path, that.path);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends InMemoryFile, B extends AbstractBuilder<T, B>> extends AbstractFileContent.AbstractBuilder<T, B> {

        public B path(String value) {
            getBuildingInstance().setPath(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<InMemoryFile, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected InMemoryFile newBuildingInstance() {
            return new InMemoryFile();
        }
    }

}
