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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas;

import de.fraunhofer.iosb.ilt.faaast.service.model.TypedInMemoryFile;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.PutThumbnailResponse;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Request class for PutThumbnail requests.
 */
public class PutThumbnailRequest implements Request<PutThumbnailResponse> {

    private String id;
    private TypedInMemoryFile content;

    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public TypedInMemoryFile getContent() {
        return content;
    }


    public void setContent(TypedInMemoryFile content) {
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
        PutThumbnailRequest that = (PutThumbnailRequest) o;
        return Objects.equals(id, that.id)
                && Objects.equals(content, that.content);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, content);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends PutThumbnailRequest, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B content(TypedInMemoryFile value) {
            getBuildingInstance().setContent(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<PutThumbnailRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected PutThumbnailRequest newBuildingInstance() {
            return new PutThumbnailRequest();
        }
    }
}
