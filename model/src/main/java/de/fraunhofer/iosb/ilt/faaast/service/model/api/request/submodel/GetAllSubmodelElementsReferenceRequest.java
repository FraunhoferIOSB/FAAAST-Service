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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractSubmodelInterfaceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractSubmodelInterfaceRequestWithPaging;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.OutputModifierConstraints;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetAllSubmodelElementsReferenceResponse;
import java.util.Objects;


/**
 * Request class for GetAllSubmodelElements requests with content modifier Reference.
 */
public class GetAllSubmodelElementsReferenceRequest extends AbstractSubmodelInterfaceRequestWithPaging<GetAllSubmodelElementsReferenceResponse> {

    public GetAllSubmodelElementsReferenceRequest() {
        super(OutputModifierConstraints.builder()
                .supportedContentModifiers(Content.REFERENCE)
                .supportsExtent(false)
                .supportsLevel(true)
                .build());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetAllSubmodelElementsReferenceRequest that = (GetAllSubmodelElementsReferenceRequest) o;
        return super.equals(that);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends GetAllSubmodelElementsReferenceRequest, B extends AbstractBuilder<T, B>>
            extends AbstractSubmodelInterfaceRequest.AbstractBuilder<T, B> {

    }

    public static class Builder extends AbstractBuilder<GetAllSubmodelElementsReferenceRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected GetAllSubmodelElementsReferenceRequest newBuildingInstance() {
            return new GetAllSubmodelElementsReferenceRequest();
        }
    }
}
