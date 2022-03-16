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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.response;

import de.fraunhofer.iosb.ilt.faaast.service.model.aasx.AASXPackageBase;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.BaseResponseWithPayload;
import java.util.Objects;


/**
 * Chapter 4.5.3
 */
public class GetAASXByPackageIdResponse extends BaseResponseWithPayload<AASXPackageBase> {

    private String filename;

    public String getFilename() {
        return filename;
    }


    public void setFilename(String filename) {
        this.filename = filename;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        GetAASXByPackageIdResponse that = (GetAASXByPackageIdResponse) o;
        return Objects.equals(filename, that.filename);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), filename);
    }


    public static GetAASXByPackageIdResponse.Builder builder() {
        return new GetAASXByPackageIdResponse.Builder();
    }

    public static class Builder extends AbstractBuilder<AASXPackageBase, GetAASXByPackageIdResponse, GetAASXByPackageIdResponse.Builder> {

        @Override
        protected GetAASXByPackageIdResponse.Builder getSelf() {
            return this;
        }


        public GetAASXByPackageIdResponse.Builder filename(String value) {
            getBuildingInstance().setFilename(value);
            return getSelf();
        }


        @Override
        protected GetAASXByPackageIdResponse newBuildingInstance() {
            return new GetAASXByPackageIdResponse();
        }
    }
}
