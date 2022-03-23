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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllAASXPackageIdsResponse;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Chapter 4.5.2
 */
public class GetAllAASXPackageIdsRequest extends BaseRequest<GetAllAASXPackageIdsResponse> {

    private List<Identifier> aasId;

    public GetAllAASXPackageIdsRequest() {
        this.aasId = new ArrayList<>();
    }


    public List<Identifier> getAasId() {
        return aasId;
    }


    public void setAasId(List<Identifier> aasId) {
        this.aasId = aasId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetAllAASXPackageIdsRequest that = (GetAllAASXPackageIdsRequest) o;
        return Objects.equals(aasId, that.aasId);
    }


    @Override
    public int hashCode() {
        return Objects.hash(aasId);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends GetAllAASXPackageIdsRequest, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B aasId(Identifier value) {
            getBuildingInstance().getAasId().add(value);
            return getSelf();
        }


        public B aasIds(List<Identifier> value) {
            getBuildingInstance().setAasId(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<GetAllAASXPackageIdsRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected GetAllAASXPackageIdsRequest newBuildingInstance() {
            return new GetAllAASXPackageIdsRequest();
        }
    }
}
