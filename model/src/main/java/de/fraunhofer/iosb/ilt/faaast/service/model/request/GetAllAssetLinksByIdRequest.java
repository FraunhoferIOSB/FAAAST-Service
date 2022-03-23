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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllAssetLinksByIdResponse;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.Objects;


/**
 * Chapter 7.2.3
 */
public class GetAllAssetLinksByIdRequest extends BaseRequest<GetAllAssetLinksByIdResponse> {

    private String aasIdentifier;

    public String getAasIdentifier() {
        return aasIdentifier;
    }


    public void setAasIdentifier(String aasIdentifier) {
        this.aasIdentifier = aasIdentifier;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetAllAssetLinksByIdRequest that = (GetAllAssetLinksByIdRequest) o;
        return Objects.equals(aasIdentifier, that.aasIdentifier);
    }


    @Override
    public int hashCode() {
        return Objects.hash(aasIdentifier);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends GetAllAssetLinksByIdRequest, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B aasIdentifier(String value) {
            getBuildingInstance().setAasIdentifier(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<GetAllAssetLinksByIdRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected GetAllAssetLinksByIdRequest newBuildingInstance() {
            return new GetAllAssetLinksByIdRequest();
        }
    }
}
