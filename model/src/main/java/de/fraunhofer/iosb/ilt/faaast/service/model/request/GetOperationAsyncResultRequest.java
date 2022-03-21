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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetOperationAsyncResultResponse;
import io.adminshell.aas.v3.model.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Chapter 4.3.13
 */
public class GetOperationAsyncResultRequest extends RequestWithModifier<GetOperationAsyncResultResponse> {

    private List<Key> path;
    private String handleId;

    public GetOperationAsyncResultRequest() {
        this.path = new ArrayList<>();
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
        GetOperationAsyncResultRequest that = (GetOperationAsyncResultRequest) o;
        return super.equals(that)
                && Objects.equals(path, that.path)
                && Objects.equals(handleId, that.handleId);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path, handleId);
    }


    public String getHandleId() {
        return handleId;
    }


    public void setHandleId(String handleId) {
        this.handleId = handleId;
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends GetOperationAsyncResultRequest, B extends AbstractBuilder<T, B>> extends RequestWithModifier.AbstractBuilder<T, B> {

        public B handleId(String value) {
            getBuildingInstance().setHandleId(value);
            return getSelf();
        }


        public B path(List<Key> value) {
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
