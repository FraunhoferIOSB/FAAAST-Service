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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractRequestWithModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractRequestWithModifierAndPaging;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.GetAllConceptDescriptionsByDataSpecificationReferenceResponse;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Request class for GetAllConceptDescriptionsByDataSpecificationReference requests.
 */
public class GetAllConceptDescriptionsByDataSpecificationReferenceRequest
        extends AbstractRequestWithModifierAndPaging<GetAllConceptDescriptionsByDataSpecificationReferenceResponse> {

    private Reference dataSpecificationReference;

    public Reference getDataSpecificationReference() {
        return dataSpecificationReference;
    }


    public void setDataSpecificationReference(Reference dataSpecificationReference) {
        this.dataSpecificationReference = dataSpecificationReference;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetAllConceptDescriptionsByDataSpecificationReferenceRequest that = (GetAllConceptDescriptionsByDataSpecificationReferenceRequest) o;
        return super.equals(that)
                && Objects.equals(dataSpecificationReference, that.dataSpecificationReference);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dataSpecificationReference);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends GetAllConceptDescriptionsByDataSpecificationReferenceRequest, B extends AbstractBuilder<T, B>>
            extends AbstractRequestWithModifier.AbstractBuilder<T, B> {

        public B dataSpecification(Reference value) {
            getBuildingInstance().setDataSpecificationReference(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<GetAllConceptDescriptionsByDataSpecificationReferenceRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected GetAllConceptDescriptionsByDataSpecificationReferenceRequest newBuildingInstance() {
            return new GetAllConceptDescriptionsByDataSpecificationReferenceRequest();
        }
    }
}
