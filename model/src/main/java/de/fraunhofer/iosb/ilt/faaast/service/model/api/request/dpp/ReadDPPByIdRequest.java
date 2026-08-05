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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.dpp.ReadDPPByIdResponse;

import java.util.Objects;


/**
 * Request class for ReadDPPByIdRequest.
 */
public class ReadDPPByIdRequest extends AbstractDPPRequest<ReadDPPByIdResponse> {

    private String dppId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReadDPPByIdRequest that = (ReadDPPByIdRequest) o;
        return super.equals(that) &&
                this.dppId.equals(that.dppId);
    }


    public String getDppId() {
        return dppId;
    }


    public void setDppId(String dppId) {
        this.dppId = dppId;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<ReadDPPByIdRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        public Builder dppId(String value) {
            getBuildingInstance().setDppId(value);
            return getSelf();
        }


        @Override
        protected ReadDPPByIdRequest newBuildingInstance() {
            return new ReadDPPByIdRequest();
        }
    }
}
