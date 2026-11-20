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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request.dpp;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.dpp.DppSerializationMode;

import java.util.Objects;


/**
 * Abstract request class for DPP requests with content. Cannot inherit from AbstractRequestWithId as this is a
 * multi-resource request (AAS, SM) and AbstractRequestWithId inherits
 * from DistinctResourceRequest.
 */
public abstract class AbstractDppRequest<T extends Response> extends Request<T> {

    private String id;
    private DppSerializationMode dppSerializationMode;

    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public DppSerializationMode getDppSerializationMode() {
        return dppSerializationMode;
    }


    public void setDppSerializationMode(DppSerializationMode dppSerializationMode) {
        this.dppSerializationMode = dppSerializationMode;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractDppRequest<T> that = (AbstractDppRequest<T>) o;
        return super.equals(that) &&
                Objects.equals(this.id, that.id) &&
                this.dppSerializationMode == that.dppSerializationMode;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    public abstract static class AbstractBuilder<T extends AbstractDppRequest, B extends AbstractBuilder<T, B>>
            extends Request.AbstractBuilder<T, B> {
        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B dppSerializationMode(DppSerializationMode value) {
            getBuildingInstance().setDppSerializationMode(value);
            return getSelf();
        }

    }
}
