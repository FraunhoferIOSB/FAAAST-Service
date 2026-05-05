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
package de.fraunhofer.iosb.ilt.faaast.service.request.assetconnection;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractSubmodelInterfaceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.response.assetconnection.DeleteOperationProviderByPathResponse;
import java.util.Objects;


/**
 * Request class for DeleteOperationProviderByPath requests.
 */
public class DeleteOperationProviderByPathRequest extends AbstractSubmodelInterfaceRequest<DeleteOperationProviderByPathResponse> {

    private String path;
    private String body;

    public String getPath() {
        return path;
    }


    public void setPath(String key) {
        this.path = key;
    }


    public String getBody() {
        return body;
    }


    public void setBody(String body) {
        this.body = body;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeleteOperationProviderByPathRequest that = (DeleteOperationProviderByPathRequest) o;
        return super.equals(that)
                && Objects.equals(path, that.path)
                && Objects.equals(body, that.body);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path, body);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends DeleteOperationProviderByPathRequest, B extends AbstractBuilder<T, B>>
            extends AbstractSubmodelInterfaceRequest.AbstractBuilder<T, B> {

        public B path(String value) {
            getBuildingInstance().setPath(value);
            return getSelf();
        }


        public B body(String value) {
            getBuildingInstance().setBody(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<DeleteOperationProviderByPathRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected DeleteOperationProviderByPathRequest newBuildingInstance() {
            return new DeleteOperationProviderByPathRequest();
        }
    }
}
