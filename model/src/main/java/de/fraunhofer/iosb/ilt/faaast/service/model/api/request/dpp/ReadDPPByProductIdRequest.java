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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.dpp.ReadDPPByProductIdResponse;

import java.util.Objects;


/**
 * Request class for ReadDPPByProductIdRequest.
 */
public class ReadDPPByProductIdRequest extends AbstractDPPRequest<ReadDPPByProductIdResponse> {

    private String productId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReadDPPByProductIdRequest that = (ReadDPPByProductIdRequest) o;
        return super.equals(that) &&
                this.productId.equals(that.productId);
    }


    public String getProductId() {
        return productId;
    }


    public void setProductId(String productId) {
        this.productId = productId;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<ReadDPPByProductIdRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        public Builder productId(String value) {
            getBuildingInstance().setProductId(value);
            return getSelf();
        }


        @Override
        protected ReadDPPByProductIdRequest newBuildingInstance() {
            return new ReadDPPByProductIdRequest();
        }
    }
}
