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

import java.util.Arrays;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Value class for Blob.
 */
public class BlobValue extends DataElementValue {

    private String contentType;
    private byte[] value;

    public String getContentType() {
        return contentType;
    }


    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    public byte[] getValue() {
        return value;
    }


    public void setValue(byte[] value) {
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
        BlobValue blobValue = (BlobValue) o;
        return Objects.equals(contentType, blobValue.contentType) && Arrays.equals(value, blobValue.value);
    }


    @Override
    public int hashCode() {
        int result = Objects.hash(contentType);
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends BlobValue, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B mimeType(String value) {
            getBuildingInstance().setContentType(value);
            return getSelf();
        }


        public B value(String value) {
            getBuildingInstance().setValue(value != null ? value.getBytes() : null);
            return getSelf();
        }


        public B value(byte[] value) {
            getBuildingInstance().setValue(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<BlobValue, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected BlobValue newBuildingInstance() {
            return new BlobValue();
        }
    }
}
