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
package de.fraunhofer.iosb.ilt.faaast.service.model.request;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.BaseRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GenerateSerializationByIdsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Chapter 4.4.2
 */
public class GenerateSerializationByIdsRequest extends BaseRequest<GenerateSerializationByIdsResponse> {

    private List<Identifier> aasIds;
    private List<Identifier> submodelIds;
    private boolean includeConceptDescriptions;
    private DataFormat serializationFormat;

    public GenerateSerializationByIdsRequest() {
        this.aasIds = new ArrayList<>();
        this.submodelIds = new ArrayList<>();
        this.includeConceptDescriptions = false;
        this.serializationFormat = DataFormat.JSON;
    }


    public List<Identifier> getAasIds() {
        return aasIds;
    }


    public void setAasIds(List<Identifier> aasIds) {
        this.aasIds = aasIds;
    }


    public List<Identifier> getSubmodelIds() {
        return submodelIds;
    }


    public void setSubmodelIds(List<Identifier> submodelIds) {
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
        return Objects.equals(includeConceptDescriptions, that.includeConceptDescriptions)
                && Objects.equals(aasIds, that.aasIds)
                && Objects.equals(submodelIds, that.submodelIds)
                && Objects.equals(serializationFormat, that.serializationFormat);
    }


    @Override
    public int hashCode() {
        return Objects.hash(aasIds, submodelIds, includeConceptDescriptions, serializationFormat);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends GenerateSerializationByIdsRequest, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B aasIds(List<Identifier> value) {
            getBuildingInstance().setAasIds(value);
            return getSelf();
        }


        public B aasId(Identifier value) {
            getBuildingInstance().getAasIds().add(value);
            return getSelf();
        }


        public B submodelIds(List<Identifier> value) {
            getBuildingInstance().setSubmodelIds(value);
            return getSelf();
        }


        public B submodelId(Identifier value) {
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
