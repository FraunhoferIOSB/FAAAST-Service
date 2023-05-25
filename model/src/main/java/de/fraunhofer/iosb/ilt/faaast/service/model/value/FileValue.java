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

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Value class for File.
 */
public class FileValue extends DataElementValue {

    private String contentType;
    private String value;

    public FileValue() {}


    public FileValue(String mimeType, String value) {
        this.contentType = mimeType;
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
        return Objects.equals(contentType, fileValue.contentType) && Objects.equals(value, fileValue.value);
    }


    public String getContentType() {
        return contentType;
    }


    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    public String getValue() {
        return value;
    }


    public void setValue(String value) {
        this.value = value;
    }


    @Override
    public int hashCode() {
        return Objects.hash(contentType, value);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends FileValue, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B mimeType(String value) {
            getBuildingInstance().setContentType(value);
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
