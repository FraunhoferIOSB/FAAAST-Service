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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request.dpp;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractRequestWithPaging;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.dpp.ReadDPPIdsByProductIdsResponse;

import java.util.List;
import java.util.Objects;


/**
 * Request class for ReadDPPIdsByProductIdsRequest.
 */
public class ReadDppIdsByProductIdsRequest extends AbstractRequestWithPaging<ReadDPPIdsByProductIdsResponse> {

    private List<String> productIds;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReadDppIdsByProductIdsRequest that = (ReadDppIdsByProductIdsRequest) o;
        return super.equals(that) &&
                this.productIds.equals(that.productIds);
    }


    public List<String> getProductIds() {
        return productIds;
    }


    public void setProductIds(List<String> productId) {
        this.productIds = productId;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<ReadDppIdsByProductIdsRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        public Builder productIds(List<String> value) {
            getBuildingInstance().setProductIds(value);
            return getSelf();
        }


        @Override
        protected ReadDppIdsByProductIdsRequest newBuildingInstance() {
            return new ReadDppIdsByProductIdsRequest();
        }
    }
}
