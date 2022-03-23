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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PutAssetInformationResponse;
import io.adminshell.aas.v3.model.AssetInformation;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.Objects;


/**
 * Chapter 4.2.8
 */
public class PutAssetInformationRequest extends BaseRequest<PutAssetInformationResponse> {

    private Identifier id;
    private AssetInformation assetInfo;

    public Identifier getId() {
        return id;
    }


    public void setId(Identifier id) {
        this.id = id;
    }


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
        return Objects.equals(assetInfo, that.assetInfo);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, assetInfo);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends PutAssetInformationRequest, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B assetInformation(AssetInformation value) {
            getBuildingInstance().setAssetInformation(value);
            return getSelf();
        }


        public B id(Identifier value) {
            getBuildingInstance().setId(value);
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
