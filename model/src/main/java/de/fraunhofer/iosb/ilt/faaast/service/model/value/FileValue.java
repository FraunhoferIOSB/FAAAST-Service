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
package de.fraunhofer.iosb.ilt.faaast.service.model.value;

import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.Objects;


public class FileValue extends DataElementValue {

    private String mimeType;
    private String value;

    public FileValue() {}


    public FileValue(String mimeType, String value) {
        this.mimeType = mimeType;
        this.value = value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileValue fileValue = (FileValue) o;
        return Objects.equals(mimeType, fileValue.mimeType) && Objects.equals(value, fileValue.value);
    }


    public String getMimeType() {
        return mimeType;
    }


    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }


    public String getValue() {
        return value;
    }


    public void setValue(String value) {
        this.value = value;
    }


    @Override
    public int hashCode() {
        return Objects.hash(mimeType, value);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends FileValue, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B mimeType(String value) {
            getBuildingInstance().setMimeType(value);
            return getSelf();
        }


        public B value(String value) {
            getBuildingInstance().setValue(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<FileValue, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected FileValue newBuildingInstance() {
            return new FileValue();
        }
    }
}
