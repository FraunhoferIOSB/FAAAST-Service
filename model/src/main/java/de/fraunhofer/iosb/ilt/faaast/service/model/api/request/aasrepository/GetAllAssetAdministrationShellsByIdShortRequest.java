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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractRequestWithModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractRequestWithModifierAndPaging;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.OutputModifierConstraints;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.GetAllAssetAdministrationShellsByIdShortResponse;
import java.util.Objects;


/**
 * Request class for GetAllAssetAdministrationShellsByIdShort requests.
 */
public class GetAllAssetAdministrationShellsByIdShortRequest extends AbstractRequestWithModifierAndPaging<GetAllAssetAdministrationShellsByIdShortResponse> {

    private String idShort;

    public GetAllAssetAdministrationShellsByIdShortRequest() {
        super(OutputModifierConstraints.ASSET_ADMINISTRATION_SHELL);
    }


    public String getIdShort() {
        return idShort;
    }


    public void setIdShort(String idShort) {
        this.idShort = idShort;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetAllAssetAdministrationShellsByIdShortRequest that = (GetAllAssetAdministrationShellsByIdShortRequest) o;
        return super.equals(that)
                && Objects.equals(idShort, that.idShort);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), idShort);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends GetAllAssetAdministrationShellsByIdShortRequest, B extends AbstractBuilder<T, B>>
            extends AbstractRequestWithModifier.AbstractBuilder<T, B> {

        public B idShort(String value) {
            getBuildingInstance().setIdShort(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<GetAllAssetAdministrationShellsByIdShortRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected GetAllAssetAdministrationShellsByIdShortRequest newBuildingInstance() {
            return new GetAllAssetAdministrationShellsByIdShortRequest();
        }
    }
}
