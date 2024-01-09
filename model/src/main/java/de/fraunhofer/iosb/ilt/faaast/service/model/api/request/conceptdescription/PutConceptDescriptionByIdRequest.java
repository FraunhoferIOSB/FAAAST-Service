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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.PutConceptDescriptionByIdResponse;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Request class for PutConceptDescriptionById requests.
 */
public class PutConceptDescriptionByIdRequest implements Request<PutConceptDescriptionByIdResponse> {

    private String id;
    private ConceptDescription conceptDescription;

    public String getId() {
        return id;
    }


    public void setId(String cdIdentifier) {
        this.id = cdIdentifier;
    }


    public ConceptDescription getConceptDescription() {
        return conceptDescription;
    }


    public void setConceptDescription(ConceptDescription conceptDescription) {
        this.conceptDescription = conceptDescription;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PutConceptDescriptionByIdRequest that = (PutConceptDescriptionByIdRequest) o;
        return Objects.equals(id, that.id)
                && Objects.equals(conceptDescription, that.conceptDescription);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, conceptDescription);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends PutConceptDescriptionByIdRequest, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B conceptDescription(ConceptDescription value) {
            getBuildingInstance().setConceptDescription(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<PutConceptDescriptionByIdRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected PutConceptDescriptionByIdRequest newBuildingInstance() {
            return new PutConceptDescriptionByIdRequest();
        }
    }
}