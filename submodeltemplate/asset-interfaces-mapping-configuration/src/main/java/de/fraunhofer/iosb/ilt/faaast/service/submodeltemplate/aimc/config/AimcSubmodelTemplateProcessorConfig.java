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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.fraunhofer.iosb.ilt.faaast.service.config.serialization.ReferenceDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.config.serialization.ReferenceSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.SubmodelTemplateProcessorConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.AimcSubmodelTemplateProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Configuration for SMT Asset Interfaces Mapping Configuration processor.
 */
public class AimcSubmodelTemplateProcessorConfig extends SubmodelTemplateProcessorConfig<AimcSubmodelTemplateProcessor> {

    private Map<String, List<Credentials>> connectionLevelCredentials;

    @JsonSerialize(keyUsing = ReferenceSerializer.class)
    @JsonDeserialize(keyUsing = ReferenceDeserializer.class)
    private Map<Reference, List<Credentials>> interfaceLevelCredentials;

    private List<ExtendedConfiguration> globalExtendedConfiguration;
    private Map<String, List<ExtendedConfiguration>> connectionLevelExtendedConfiguration;

    @JsonSerialize(keyUsing = ReferenceSerializer.class)
    @JsonDeserialize(keyUsing = ReferenceDeserializer.class)
    private Map<Reference, List<ExtendedConfiguration>> propertyLevelExtendedConfiguration;

    public AimcSubmodelTemplateProcessorConfig() {
        connectionLevelCredentials = new HashMap<>();
        interfaceLevelCredentials = new HashMap<>();
        globalExtendedConfiguration = new ArrayList<>();
        connectionLevelExtendedConfiguration = new HashMap<>();
        propertyLevelExtendedConfiguration = new HashMap<>();
    }


    public Map<String, List<Credentials>> getConnectionLevelCredentials() {
        return connectionLevelCredentials;
    }


    public void setConnectionLevelCredentials(Map<String, List<Credentials>> connectionLevelCredentials) {
        this.connectionLevelCredentials = connectionLevelCredentials;
    }


    public Map<Reference, List<Credentials>> getInterfaceLevelCredentials() {
        return interfaceLevelCredentials;
    }


    public void setInterfaceLevelCredentials(Map<Reference, List<Credentials>> interfaceLevelCredentials) {
        this.interfaceLevelCredentials = interfaceLevelCredentials;
    }


    public List<ExtendedConfiguration> getGlobalExtendedConfiguration() {
        return globalExtendedConfiguration;
    }


    public void setGlobalExtendedConfiguration(List<ExtendedConfiguration> globalExtendedConfiguration) {
        this.globalExtendedConfiguration = globalExtendedConfiguration;
    }


    public Map<String, List<ExtendedConfiguration>> getConnectionLevelExtendedConfiguration() {
        return connectionLevelExtendedConfiguration;
    }


    public void setConnectionLevelExtendedConfiguration(Map<String, List<ExtendedConfiguration>> connectionLevelExtendedConfiguration) {
        this.connectionLevelExtendedConfiguration = connectionLevelExtendedConfiguration;
    }


    public Map<Reference, List<ExtendedConfiguration>> getPropertyLevelExtendedConfiguration() {
        return propertyLevelExtendedConfiguration;
    }


    public void setPropertyLevelExtendedConfiguration(Map<Reference, List<ExtendedConfiguration>> propertyLevelExtendedConfiguration) {
        this.propertyLevelExtendedConfiguration = propertyLevelExtendedConfiguration;
    }


    @Override
    public int hashCode() {
        return Objects.hash(connectionLevelCredentials, interfaceLevelCredentials, globalExtendedConfiguration, connectionLevelExtendedConfiguration,
                propertyLevelExtendedConfiguration);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AimcSubmodelTemplateProcessorConfig other = (AimcSubmodelTemplateProcessorConfig) obj;
        return super.equals(other)
                && Objects.equals(connectionLevelCredentials, other.connectionLevelCredentials)
                && Objects.equals(interfaceLevelCredentials, other.interfaceLevelCredentials)
                && Objects.equals(globalExtendedConfiguration, other.globalExtendedConfiguration)
                && Objects.equals(connectionLevelExtendedConfiguration, other.connectionLevelExtendedConfiguration)
                && Objects.equals(propertyLevelExtendedConfiguration, other.propertyLevelExtendedConfiguration);
    }

    protected abstract static class AbstractBuilder<C extends AimcSubmodelTemplateProcessorConfig, B extends AbstractBuilder<C, B>>
            extends ExtendableBuilder<C, B> {

        public B connectionLevelCredentials(Map<String, List<Credentials>> value) {
            getBuildingInstance().setConnectionLevelCredentials(value);
            return getSelf();
        }


        public B interfaceLevelCredentials(Map<Reference, List<Credentials>> value) {
            getBuildingInstance().setInterfaceLevelCredentials(value);
            return getSelf();
        }


        public B globalExtendedConfiguration(List<ExtendedConfiguration> value) {
            getBuildingInstance().setGlobalExtendedConfiguration(value);
            return getSelf();
        }


        public B connectionLevelExtendedConfiguration(Map<String, List<ExtendedConfiguration>> value) {
            getBuildingInstance().setConnectionLevelExtendedConfiguration(value);
            return getSelf();
        }


        public B propertyLevelExtendedConfiguration(Map<Reference, List<ExtendedConfiguration>> value) {
            getBuildingInstance().setPropertyLevelExtendedConfiguration(value);
            return getSelf();
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<AimcSubmodelTemplateProcessorConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected AimcSubmodelTemplateProcessorConfig newBuildingInstance() {
            return new AimcSubmodelTemplateProcessorConfig();
        }
    }

}
