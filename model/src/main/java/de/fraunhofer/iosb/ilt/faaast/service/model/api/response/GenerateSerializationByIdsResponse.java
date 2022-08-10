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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.response;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.AbstractResponseWithPayload;
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import java.util.Objects;


/**
 * Chapter 4.4.2
 */
public class GenerateSerializationByIdsResponse extends AbstractResponseWithPayload<AssetAdministrationShellEnvironment> {

    private DataFormat dataformat;

    public static GenerateSerializationByIdsResponse.Builder builder() {
        return new GenerateSerializationByIdsResponse.Builder();
    }


    public DataFormat getDataformat() {
        return dataformat;
    }


    public void setDataformat(DataFormat dataformat) {
        this.dataformat = dataformat;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        GenerateSerializationByIdsResponse that = (GenerateSerializationByIdsResponse) o;
        return super.equals(that)
                && Objects.equals(dataformat, that.dataformat);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dataformat);
    }

    public abstract static class AbstractBuilder<T extends GenerateSerializationByIdsResponse, B extends AbstractBuilder<T, B>>
            extends AbstractResponseWithPayload.AbstractBuilder<AssetAdministrationShellEnvironment, T, B> {

        public B datformat(DataFormat value) {
            getBuildingInstance().setDataformat(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<GenerateSerializationByIdsResponse, Builder> {

        @Override
        protected GenerateSerializationByIdsResponse.Builder getSelf() {
            return this;
        }


        @Override
        protected GenerateSerializationByIdsResponse newBuildingInstance() {
            return new GenerateSerializationByIdsResponse();
        }
    }

}
