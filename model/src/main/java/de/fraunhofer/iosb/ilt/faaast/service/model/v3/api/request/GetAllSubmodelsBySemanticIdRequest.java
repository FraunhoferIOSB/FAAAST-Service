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
package de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request;

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.BaseRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.response.GetAllSubmodelsBySemanticIdResponse;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.Objects;


/**
 * Chapter 6.3.4
 */
public class GetAllSubmodelsBySemanticIdRequest extends BaseRequest<GetAllSubmodelsBySemanticIdResponse> {
    private OutputModifier outputModifier;
    private Reference semanticId;

    public GetAllSubmodelsBySemanticIdRequest() {
        this.outputModifier = OutputModifier.DEFAULT;
    }


    public OutputModifier getOutputModifier() {
        return outputModifier;
    }


    public void setOutputModifier(OutputModifier outputModifier) {
        this.outputModifier = outputModifier;
    }


    public Reference getSemanticId() {
        return semanticId;
    }


    public void setSemanticId(Reference semanticId) {
        this.semanticId = semanticId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GetAllSubmodelsBySemanticIdRequest that = (GetAllSubmodelsBySemanticIdRequest) o;
        return Objects.equals(outputModifier, that.outputModifier)
                && Objects.equals(semanticId, that.semanticId);
    }


    @Override
    public int hashCode() {
        return Objects.hash(outputModifier, semanticId);
    }


    public static Builder builder() {
        return new Builder();
    }

    public static abstract class AbstractBuilder<T extends GetAllSubmodelsBySemanticIdRequest, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {
        public B outputModifier(OutputModifier value) {
            getBuildingInstance().setOutputModifier(value);
            return getSelf();
        }


        public B semanticId(Reference value) {
            getBuildingInstance().setSemanticId(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<GetAllSubmodelsBySemanticIdRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected GetAllSubmodelsBySemanticIdRequest newBuildingInstance() {
            return new GetAllSubmodelsBySemanticIdRequest();
        }
    }
}
