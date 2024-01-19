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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasserialization;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasserialization.GenerateSerializationByIdsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Request class for GenerateSerializationByIds requests.
 */
public class GenerateSerializationByIdsRequest extends Request<GenerateSerializationByIdsResponse> {

    private List<String> aasIds;
    private List<String> submodelIds;
    private boolean includeConceptDescriptions;
    private DataFormat serializationFormat;

    public GenerateSerializationByIdsRequest() {
        this.aasIds = new ArrayList<>();
        this.submodelIds = new ArrayList<>();
        this.includeConceptDescriptions = false;
        this.serializationFormat = DataFormat.JSON;
    }


    public List<String> getAasIds() {
        return aasIds;
    }


    public void setAasIds(List<String> aasIds) {
        this.aasIds = aasIds;
    }


    public List<String> getSubmodelIds() {
        return submodelIds;
    }


    public void setSubmodelIds(List<String> submodelIds) {
        this.submodelIds = submodelIds;
    }


    public boolean getIncludeConceptDescriptions() {
        return includeConceptDescriptions;
    }


    public void setIncludeConceptDescriptions(boolean includeConceptDescriptions) {
        this.includeConceptDescriptions = includeConceptDescriptions;
    }


    public DataFormat getSerializationFormat() {
        return serializationFormat;
    }


    public void setSerializationFormat(DataFormat serializationFormat) {
        this.serializationFormat = serializationFormat;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GenerateSerializationByIdsRequest that = (GenerateSerializationByIdsRequest) o;
        return super.equals(that)
                && Objects.equals(includeConceptDescriptions, that.includeConceptDescriptions)
                && Objects.equals(aasIds, that.aasIds)
                && Objects.equals(submodelIds, that.submodelIds)
                && Objects.equals(serializationFormat, that.serializationFormat);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), aasIds, submodelIds, includeConceptDescriptions, serializationFormat);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends GenerateSerializationByIdsRequest, B extends AbstractBuilder<T, B>> extends Request.AbstractBuilder<T, B> {

        public B aasIds(List<String> value) {
            getBuildingInstance().setAasIds(value);
            return getSelf();
        }


        public B aasId(String value) {
            getBuildingInstance().getAasIds().add(value);
            return getSelf();
        }


        public B submodelIds(List<String> value) {
            getBuildingInstance().setSubmodelIds(value);
            return getSelf();
        }


        public B submodelId(String value) {
            getBuildingInstance().getSubmodelIds().add(value);
            return getSelf();
        }


        public B serializationFormat(DataFormat value) {
            getBuildingInstance().setSerializationFormat(value);
            return getSelf();
        }


        public B includeConceptDescriptions(boolean value) {
            getBuildingInstance().setIncludeConceptDescriptions(value);
            return getSelf();
        }


        public B includeConceptDescriptions() {
            getBuildingInstance().setIncludeConceptDescriptions(true);
            return getSelf();
        }


        public B excludeConceptDescriptions() {
            getBuildingInstance().setIncludeConceptDescriptions(false);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<GenerateSerializationByIdsRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected GenerateSerializationByIdsRequest newBuildingInstance() {
            return new GenerateSerializationByIdsRequest();
        }
    }
}
