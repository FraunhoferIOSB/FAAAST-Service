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

import java.util.Arrays;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Represents the content of a file.
 */
public class FileContent {

    private String contentType;
    private byte[] content;

    public String getContentType() {
        return contentType;
    }


    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    public byte[] getContent() {
        return content;
    }


    public void setContent(byte[] content) {
        this.content = content;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileContent that = (FileContent) o;
        return Objects.equals(contentType, that.contentType)
                && Arrays.equals(content, that.content);
    }


    @Override
    public int hashCode() {
        return Objects.hash(contentType, Arrays.hashCode(content));
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends FileContent, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B contentType(String value) {
            getBuildingInstance().setContentType(value);
            return getSelf();
        }


        public B content(byte[] value) {
            getBuildingInstance().setContent(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<FileContent, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected FileContent newBuildingInstance() {
            return new FileContent();
        }
    }

}
