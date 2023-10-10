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
 * Represents a typed in-memory file with path, content and contentType.
 */
public class TypedInMemoryFile extends AbstractFileContent {

    private String contentType;

    public String getContentType() {
        return contentType;
    }


    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    private String path;

    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TypedInMemoryFile that = (TypedInMemoryFile) o;
        return super.equals(that)
                && Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path);
    }


    public static TypedInMemoryFile.Builder builder() {
        return new TypedInMemoryFile.Builder();
    }

    public abstract static class AbstractBuilder<T extends TypedInMemoryFile, B extends TypedInMemoryFile.AbstractBuilder<T, B>> extends AbstractFileContent.AbstractBuilder<T, B> {

        public B path(String value) {
            getBuildingInstance().setPath(value);
            return getSelf();
        }

        public B contentType(String value) {
            getBuildingInstance().setContentType(value);
            return getSelf();
        }
    }

    public static class Builder extends TypedInMemoryFile.AbstractBuilder<TypedInMemoryFile, TypedInMemoryFile.Builder> {

        @Override
        protected TypedInMemoryFile.Builder getSelf() {
            return this;
        }


        @Override
        protected TypedInMemoryFile newBuildingInstance() {
            return new TypedInMemoryFile();
        }
    }

}
