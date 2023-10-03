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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.AbstractResponseWithPayload;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;


/**
 * Response class for GetAssetInformation requests.
 */
public class GetAssetInformationResponse extends AbstractResponseWithPayload<AssetInformation> {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<AssetInformation, GetAssetInformationResponse, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected GetAssetInformationResponse newBuildingInstance() {
            return new GetAssetInformationResponse();
        }
    }
}
