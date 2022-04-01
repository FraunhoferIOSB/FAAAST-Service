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
package de.fraunhofer.iosb.ilt.faaast.service.model.request;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.BaseRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.DeleteSubmodelElementByPathResponse;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Chapter 4.3.10
 */
public class DeleteSubmodelElementByPathRequest extends BaseRequest<DeleteSubmodelElementByPathResponse> {

    private Identifier id;
    private List<Key> path;

    public DeleteSubmodelElementByPathRequest() {
        this.path = new ArrayList<>();
    }


    public Identifier getId() {
        return id;
    }


    public void setId(Identifier id) {
        this.id = id;
    }


    public List<Key> getPath() {
        return path;
    }


    public void setPath(List<Key> path) {
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
        DeleteSubmodelElementByPathRequest that = (DeleteSubmodelElementByPathRequest) o;
        return Objects.equals(id, that.id) && Objects.equals(path, that.path);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, path);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends DeleteSubmodelElementByPathRequest, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B id(Identifier value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B path(List<Key> value) {
            getBuildingInstance().setPath(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<DeleteSubmodelElementByPathRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected DeleteSubmodelElementByPathRequest newBuildingInstance() {
            return new DeleteSubmodelElementByPathRequest();
        }
    }
}
