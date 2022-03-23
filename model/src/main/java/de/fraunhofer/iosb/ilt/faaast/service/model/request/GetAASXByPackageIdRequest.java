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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAASXByPackageIdResponse;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.Objects;


/**
 * Chapter 4.5.3
 */
public class GetAASXByPackageIdRequest extends BaseRequest<GetAASXByPackageIdResponse> {

    private String packageId;

    public String getPackageId() {
        return packageId;
    }


    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetAASXByPackageIdRequest that = (GetAASXByPackageIdRequest) o;
        return Objects.equals(packageId, that.packageId);
    }


    @Override
    public int hashCode() {
        return Objects.hash(packageId);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends GetAASXByPackageIdRequest, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B packageId(String value) {
            getBuildingInstance().setPackageId(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<GetAASXByPackageIdRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected GetAASXByPackageIdRequest newBuildingInstance() {
            return new GetAASXByPackageIdRequest();
        }
    }
}
