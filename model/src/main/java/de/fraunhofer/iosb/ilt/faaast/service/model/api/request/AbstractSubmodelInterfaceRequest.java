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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import java.util.Objects;


/**
 * Base class for requests that are part of the Submodel Interface and therefore can be executed as stand-alone or
 * within the context of an AAS.
 *
 * @param <T> actual type of the request
 */
public abstract class AbstractSubmodelInterfaceRequest<T extends Response> extends AbstractRequestWithModifier<T> {

    protected String aasId;
    protected String submodelId;

    protected AbstractSubmodelInterfaceRequest() {
        super();
    }


    protected AbstractSubmodelInterfaceRequest(OutputModifierConstraints outputModifierConstraints) {
        super(outputModifierConstraints);
    }


    public String getAasId() {
        return aasId;
    }


    public void setAasId(String aasId) {
        this.aasId = aasId;
    }


    public String getSubmodelId() {
        return submodelId;
    }


    public void setSubmodelId(String submodelId) {
        this.submodelId = submodelId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractSubmodelInterfaceRequest<T> that = (AbstractSubmodelInterfaceRequest<T>) o;
        return super.equals(o)
                && Objects.equals(aasId, that.aasId)
                && Objects.equals(submodelId, that.submodelId);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), aasId, submodelId);
    }

    public abstract static class AbstractBuilder<T extends AbstractSubmodelInterfaceRequest, B extends AbstractBuilder<T, B>>
            extends AbstractRequestWithModifier.AbstractBuilder<T, B> {

        public B aasId(String value) {
            getBuildingInstance().setAasId(value);
            return getSelf();
        }


        public B submodelId(String value) {
            getBuildingInstance().setSubmodelId(value);
            return getSelf();
        }
    }
}
