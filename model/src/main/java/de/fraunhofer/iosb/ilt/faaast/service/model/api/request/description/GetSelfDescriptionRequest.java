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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request.description;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.description.GetSelfDescriptionResponse;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Request class for GetSelfDescription requests.
 */
public class GetSelfDescriptionRequest implements Request<GetSelfDescriptionResponse> {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return Objects.nonNull(o) &&
                Objects.equals(getClass(), o.getClass());
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends GetSelfDescriptionRequest, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

    }

    public static class Builder extends AbstractBuilder<GetSelfDescriptionRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected GetSelfDescriptionRequest newBuildingInstance() {
            return new GetSelfDescriptionRequest();
        }
    }
}
