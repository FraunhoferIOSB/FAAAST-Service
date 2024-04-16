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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractSubmodelInterfaceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetOperationAsyncResultResponse;
import java.util.Objects;


/**
 * Request class for GetOperationAsyncResult requests.
 */
public class GetOperationAsyncResultRequest extends AbstractSubmodelInterfaceRequest<GetOperationAsyncResultResponse> {

    private String path;
    private OperationHandle handle;

    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }


    public OperationHandle getHandle() {
        return handle;
    }


    public void setHandle(OperationHandle handle) {
        this.handle = handle;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetOperationAsyncResultRequest that = (GetOperationAsyncResultRequest) o;
        return super.equals(that)
                && Objects.equals(path, that.path)
                && Objects.equals(handle, that.handle);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path, handle);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends GetOperationAsyncResultRequest, B extends AbstractBuilder<T, B>>
            extends AbstractSubmodelInterfaceRequest.AbstractBuilder<T, B> {

        public B handle(OperationHandle value) {
            getBuildingInstance().setHandle(value);
            return getSelf();
        }


        public B path(String value) {
            getBuildingInstance().setPath(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<GetOperationAsyncResultRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected GetOperationAsyncResultRequest newBuildingInstance() {
            return new GetOperationAsyncResultRequest();
        }
    }
}
