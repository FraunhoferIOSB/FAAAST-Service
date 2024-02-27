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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractRequestWithId;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.PutAssetInformationResponse;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;


/**
 * Request class for PutAssetInformation requests.
 */
public class PutAssetInformationRequest extends AbstractRequestWithId<PutAssetInformationResponse> {

    private AssetInformation assetInfo;

    public AssetInformation getAssetInformation() {
        return assetInfo;
    }


    public void setAssetInformation(AssetInformation assetInfo) {
        this.assetInfo = assetInfo;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PutAssetInformationRequest that = (PutAssetInformationRequest) o;
        return super.equals(that)
                && Objects.equals(assetInfo, that.assetInfo);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), assetInfo);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends PutAssetInformationRequest, B extends AbstractBuilder<T, B>> extends AbstractRequestWithId.AbstractBuilder<T, B> {

        public B assetInformation(AssetInformation value) {
            getBuildingInstance().setAssetInformation(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<PutAssetInformationRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected PutAssetInformationRequest newBuildingInstance() {
            return new PutAssetInformationRequest();
        }
    }
}
