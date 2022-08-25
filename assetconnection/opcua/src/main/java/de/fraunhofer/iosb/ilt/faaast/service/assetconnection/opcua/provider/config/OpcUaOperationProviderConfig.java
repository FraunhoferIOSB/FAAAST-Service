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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProviderConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * * Config file for OPC UA-based
 * {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider}.
 */
public class OpcUaOperationProviderConfig extends AbstractOpcUaProviderConfig implements AssetOperationProviderConfig {

    protected String parentNodeId;
    protected List<ArgumentMapping> inputArgumentMapping;

    public OpcUaOperationProviderConfig() {
        this.inputArgumentMapping = new ArrayList<>();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpcUaOperationProviderConfig that = (OpcUaOperationProviderConfig) o;
        return super.equals(o)
                && Objects.equals(parentNodeId, that.parentNodeId)
                && Objects.equals(inputArgumentMapping, that.inputArgumentMapping);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parentNodeId, inputArgumentMapping);
    }


    public String getParentNodeId() {
        return parentNodeId;
    }


    public void setParentNodeId(String parentNodeId) {
        this.parentNodeId = parentNodeId;
    }


    public List<ArgumentMapping> getInputArgumentMapping() {
        return inputArgumentMapping;
    }


    public void setInputArgumentMapping(List<ArgumentMapping> value) {
        this.inputArgumentMapping = value;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<OpcUaOperationProviderConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected OpcUaOperationProviderConfig newBuildingInstance() {
            return new OpcUaOperationProviderConfig();
        }
    }

    private abstract static class AbstractBuilder<T extends OpcUaOperationProviderConfig, B extends AbstractBuilder<T, B>>
            extends AbstractOpcUaProviderConfig.AbstractBuilder<T, B> {

        public B parentNodeId(String value) {
            getBuildingInstance().setParentNodeId(value);
            return getSelf();
        }


        public B inputArgumentMapping(ArgumentMapping value) {
            getBuildingInstance().getInputArgumentMapping().add(value);
            return getSelf();
        }


        public B inputArgumentMapping(List<ArgumentMapping> value) {
            getBuildingInstance().setInputArgumentMapping(value);
            return getSelf();
        }
    }
}
