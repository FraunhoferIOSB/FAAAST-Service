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
public class TypedInMemoryFile extends InMemoryFile {

    private String contentType;

    public String getContentType() {
        return contentType;
    }


    public void setContentType(String contentType) {
        this.contentType = contentType;
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
        return Objects.hash(super.hashCode(), contentType);
    }

    public abstract static class AbstractBuilder<T extends TypedInMemoryFile, B extends AbstractBuilder<T, B>> extends InMemoryFile.AbstractBuilder<T, B> {

        public B contentType(String value) {
            getBuildingInstance().setContentType(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<TypedInMemoryFile, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected TypedInMemoryFile newBuildingInstance() {
            return new TypedInMemoryFile();
        }
    }

}
