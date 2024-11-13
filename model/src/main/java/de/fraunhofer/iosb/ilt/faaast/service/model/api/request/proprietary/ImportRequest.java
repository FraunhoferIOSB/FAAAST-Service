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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request.proprietary;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.proprietary.ImportResponse;
import java.util.Arrays;
import java.util.Objects;


/**
 * Request class for Import requests.
 */
public class ImportRequest extends Request<ImportResponse> {

    private byte[] content;
    private String contentType;

    public byte[] getContent() {
        return content;
    }


    public void setContent(byte[] content) {
        this.content = content;
    }


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
        ImportRequest that = (ImportRequest) o;
        return super.equals(that)
                && Arrays.equals(content, that.content)
                && Objects.equals(contentType, that.contentType);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), content, contentType);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends ImportRequest, B extends AbstractBuilder<T, B>> extends Request.AbstractBuilder<T, B> {

        public B content(byte[] value) {
            getBuildingInstance().setContent(value);
            return getSelf();
        }


        public B contentType(String value) {
            getBuildingInstance().setContentType(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<ImportRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected ImportRequest newBuildingInstance() {
            return new ImportRequest();
        }
    }

}
