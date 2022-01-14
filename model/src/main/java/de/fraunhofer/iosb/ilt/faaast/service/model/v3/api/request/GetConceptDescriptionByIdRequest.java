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
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.response.GetConceptDescriptionByIdResponse;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.Objects;


/**
 * Chapter 6.4.3
 */
public class GetConceptDescriptionByIdRequest extends BaseRequest<GetConceptDescriptionByIdResponse> {
    private OutputModifier outputModifier;
    private Identifier id;

    public GetConceptDescriptionByIdRequest() {
        this.outputModifier = OutputModifier.DEFAULT;
    }


    public OutputModifier getOutputModifier() {
        return outputModifier;
    }


    public void setOutputModifier(OutputModifier outputModifier) {
        this.outputModifier = outputModifier;
    }


    public Identifier getId() {
        return id;
    }


    public void setId(Identifier cdIdentifier) {
        this.id = cdIdentifier;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GetConceptDescriptionByIdRequest that = (GetConceptDescriptionByIdRequest) o;
        return Objects.equals(outputModifier, that.outputModifier)
                && Objects.equals(id, that.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(outputModifier, id);
    }


    public static Builder builder() {
        return new Builder();
    }

    public static abstract class AbstractBuilder<T extends GetConceptDescriptionByIdRequest, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {
        public B id(Identifier value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B outputModifier(OutputModifier value) {
            getBuildingInstance().setOutputModifier(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<GetConceptDescriptionByIdRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected GetConceptDescriptionByIdRequest newBuildingInstance() {
            return new GetConceptDescriptionByIdRequest();
        }
    }
}
