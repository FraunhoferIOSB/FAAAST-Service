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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.ArgumentValidationMode;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProviderConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * * Config file for OPC UA-based {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider}.
 */
public class OpcUaOperationProviderConfig extends AbstractOpcUaProviderConfig implements AssetOperationProviderConfig {

    protected String parentNodeId;
    protected List<ArgumentMapping> inputArgumentMapping;
    protected List<ArgumentMapping> outputArgumentMapping;
    protected ArgumentValidationMode inputValidationMode;
    protected ArgumentValidationMode inoutputValidationMode;
    protected ArgumentValidationMode outputValidationMode;

    public OpcUaOperationProviderConfig() {
        this.inputArgumentMapping = new ArrayList<>();
        this.outputArgumentMapping = new ArrayList<>();
        this.inputValidationMode = ArgumentValidationMode.DEFAULT;
        this.inoutputValidationMode = ArgumentValidationMode.DEFAULT;
        this.outputValidationMode = ArgumentValidationMode.DEFAULT;
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


    public List<ArgumentMapping> getOutputArgumentMapping() {
        return outputArgumentMapping;
    }


    public void setOutputArgumentMapping(List<ArgumentMapping> value) {
        this.outputArgumentMapping = value;
    }


    @Override
    public ArgumentValidationMode getInputValidationMode() {
        return inputValidationMode;
    }


    @Override
    public void setInputValidationMode(ArgumentValidationMode mode) {
        this.inputValidationMode = mode;
    }


    @Override
    public ArgumentValidationMode getInoutputValidationMode() {
        return inoutputValidationMode;
    }


    @Override
    public void setInoutputValidationMode(ArgumentValidationMode mode) {
        this.inoutputValidationMode = mode;
    }


    @Override
    public ArgumentValidationMode getOutputValidationMode() {
        return outputValidationMode;
    }


    @Override
    public void setOutputValidationMode(ArgumentValidationMode mode) {
        this.outputValidationMode = mode;
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
                && Objects.equals(inputArgumentMapping, that.inputArgumentMapping)
                && Objects.equals(outputArgumentMapping, that.outputArgumentMapping)
                && Objects.equals(inputValidationMode, that.inputValidationMode)
                && Objects.equals(inoutputValidationMode, that.inoutputValidationMode)
                && Objects.equals(outputValidationMode, that.outputValidationMode);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parentNodeId, inputArgumentMapping, outputArgumentMapping, inputValidationMode, inoutputValidationMode, outputValidationMode);
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


        public B inputArgumentMappings(List<ArgumentMapping> value) {
            getBuildingInstance().setInputArgumentMapping(value);
            return getSelf();
        }


        public B outputArgumentMapping(ArgumentMapping value) {
            getBuildingInstance().getOutputArgumentMapping().add(value);
            return getSelf();
        }


        public B outputArgumentMappings(List<ArgumentMapping> value) {
            getBuildingInstance().setOutputArgumentMapping(value);
            return getSelf();
        }


        public B inputValidationMode(ArgumentValidationMode value) {
            getBuildingInstance().setInputValidationMode(value);
            return getSelf();
        }


        public B inoutputValidationMode(ArgumentValidationMode value) {
            getBuildingInstance().setInoutputValidationMode(value);
            return getSelf();
        }


        public B outputValidationMode(ArgumentValidationMode value) {
            getBuildingInstance().setOutputValidationMode(value);
            return getSelf();
        }
    }
}
