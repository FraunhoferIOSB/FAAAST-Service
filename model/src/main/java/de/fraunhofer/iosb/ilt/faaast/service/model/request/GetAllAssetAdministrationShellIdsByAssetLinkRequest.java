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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllAssetAdministrationShellIdsByAssetLinkResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetID;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Request class for GetAllAssetAdministrationShellIdsByAssetLink requests.
 */
public class GetAllAssetAdministrationShellIdsByAssetLinkRequest implements Request<GetAllAssetAdministrationShellIdsByAssetLinkResponse> {

    private List<SpecificAssetID> assetIdentifierPairs;

    public GetAllAssetAdministrationShellIdsByAssetLinkRequest() {
        this.assetIdentifierPairs = new ArrayList<>();
    }


    public List<SpecificAssetID> getAssetIdentifierPairs() {
        return assetIdentifierPairs;
    }


    public void setAssetIdentifierPairs(List<SpecificAssetID> assetIdentifierPairs) {
        this.assetIdentifierPairs = assetIdentifierPairs;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetAllAssetAdministrationShellIdsByAssetLinkRequest that = (GetAllAssetAdministrationShellIdsByAssetLinkRequest) o;
        return Objects.equals(assetIdentifierPairs, that.assetIdentifierPairs);
    }


    @Override
    public int hashCode() {
        return Objects.hash(assetIdentifierPairs);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends GetAllAssetAdministrationShellIdsByAssetLinkRequest, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B assetIdentifierPair(SpecificAssetID value) {
            getBuildingInstance().getAssetIdentifierPairs().add(value);
            return getSelf();
        }


        public B assetIdentifierPairs(List<SpecificAssetID> value) {
            getBuildingInstance().setAssetIdentifierPairs(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<GetAllAssetAdministrationShellIdsByAssetLinkRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected GetAllAssetAdministrationShellIdsByAssetLinkRequest newBuildingInstance() {
            return new GetAllAssetAdministrationShellIdsByAssetLinkRequest();
        }
    }
}
