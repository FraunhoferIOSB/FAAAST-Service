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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllAssetAdministrationShellsByAssetIdResponse;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Chapter 6.2.4
 */
public class GetAllAssetAdministrationShellsByAssetIdRequest extends AbstractGetAssetAdministrationShellRequest<GetAllAssetAdministrationShellsByAssetIdResponse> {

    private List<IdentifierKeyValuePair> assetIds;

    public GetAllAssetAdministrationShellsByAssetIdRequest() {
        this.assetIds = new ArrayList<>();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetAllAssetAdministrationShellsByAssetIdRequest that = (GetAllAssetAdministrationShellsByAssetIdRequest) o;
        return super.equals(that)
                && Objects.equals(assetIds, that.assetIds);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), assetIds);
    }


    public List<IdentifierKeyValuePair> getAssetIds() {
        return assetIds;
    }


    public void setAssetIds(List<IdentifierKeyValuePair> assetIds) {
        this.assetIds = assetIds;
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends GetAllAssetAdministrationShellsByAssetIdRequest, B extends AbstractBuilder<T, B>>
            extends RequestWithModifier.AbstractBuilder<T, B> {

        public B assetId(IdentifierKeyValuePair value) {
            getBuildingInstance().getAssetIds().add(value);
            return getSelf();
        }


        public B assetIds(List<IdentifierKeyValuePair> value) {
            getBuildingInstance().setAssetIds(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<GetAllAssetAdministrationShellsByAssetIdRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected GetAllAssetAdministrationShellsByAssetIdRequest newBuildingInstance() {
            return new GetAllAssetAdministrationShellsByAssetIdRequest();
        }
    }
}
