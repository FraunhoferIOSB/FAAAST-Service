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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.PostSubmodelResponse;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;


/**
 * Request class for PostSubmodel requests.
 */
public class PostSubmodelRequest extends Request<PostSubmodelResponse> {

    private Submodel submodel;

    public Submodel getSubmodel() {
        return submodel;
    }


    public void setSubmodel(Submodel submodel) {
        this.submodel = submodel;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PostSubmodelRequest that = (PostSubmodelRequest) o;
        return super.equals(that)
                && Objects.equals(submodel, that.submodel);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), submodel);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends PostSubmodelRequest, B extends AbstractBuilder<T, B>> extends Request.AbstractBuilder<T, B> {

        public B submodel(Submodel value) {
            getBuildingInstance().setSubmodel(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<PostSubmodelRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected PostSubmodelRequest newBuildingInstance() {
            return new PostSubmodelRequest();
        }
    }
}
