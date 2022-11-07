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
package de.fraunhofer.iosb.ilt.faaast.service.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.EndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBusConfig;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.PersistenceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.SubmodelTemplateProcessorConfig;
import de.fraunhofer.iosb.ilt.faaast.service.util.ImplementationManager;
import io.adminshell.aas.v3.model.builder.AbstractBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Config class for configuring a Service instance.
 */
public class ServiceConfig {

    private List<AssetConnectionConfig> assetConnections;
    private CoreConfig core;
    private List<EndpointConfig> endpoints;
    private PersistenceConfig persistence;
    private MessageBusConfig messageBus;
    private List<SubmodelTemplateProcessorConfig> submodelTemplateProcessors;

    private static ObjectMapper getMapper() {
        ObjectMapper mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setTypeFactory(mapper.getTypeFactory().withClassLoader(ImplementationManager.getClassLoader()));
        return mapper;
    }


    /**
     * Load the config from a given file.
     *
     * @param file the file to parse
     * @return the parsed config
     * @throws IOException if loading fails
     */
    public static ServiceConfig load(File file) throws IOException {
        return getMapper().readValue(file, ServiceConfig.class);
    }


    /**
     * Load the config from a given input stream.
     *
     * @param in the stream to parse
     * @return the parsed config
     * @throws IOException if loading fails
     */
    public static ServiceConfig load(InputStream in) throws IOException {
        return getMapper().readValue(in, ServiceConfig.class);
    }


    public static Builder builder() {
        return new Builder();
    }


    public ServiceConfig() {
        this.assetConnections = new ArrayList<>();
        this.endpoints = new ArrayList<>();
        this.submodelTemplateProcessors = new ArrayList<>();
    }


    public List<AssetConnectionConfig> getAssetConnections() {
        return assetConnections;
    }


    public void setAssetConnections(List<AssetConnectionConfig> assetConnections) {
        this.assetConnections = assetConnections;
    }


    public CoreConfig getCore() {
        return core;
    }


    public void setCore(CoreConfig core) {
        this.core = core;
    }


    public List<EndpointConfig> getEndpoints() {
        return endpoints;
    }


    public void setEndpoints(List<EndpointConfig> endpoints) {
        this.endpoints = endpoints;
    }


    public PersistenceConfig getPersistence() {
        return persistence;
    }


    public void setPersistence(PersistenceConfig persistence) {
        this.persistence = persistence;
    }


    public MessageBusConfig getMessageBus() {
        return messageBus;
    }


    public void setMessageBus(MessageBusConfig messageBus) {
        this.messageBus = messageBus;
    }


    public List<SubmodelTemplateProcessorConfig> getSubmodelTemplateProcessors() {
        return submodelTemplateProcessors;
    }


    public void setSubmodelTemplateProcessors(List<SubmodelTemplateProcessorConfig> submodelTemplateProcessors) {
        this.submodelTemplateProcessors = submodelTemplateProcessors;
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
        final ServiceConfig other = (ServiceConfig) obj;
        return Objects.equals(this.core, other.core)
                && Objects.equals(this.assetConnections, other.assetConnections)
                && Objects.equals(this.endpoints, other.endpoints)
                && Objects.equals(this.persistence, other.persistence)
                && Objects.equals(this.submodelTemplateProcessors, other.submodelTemplateProcessors);
    }


    @Override
    public int hashCode() {
        return Objects.hash(core, assetConnections, persistence, endpoints, submodelTemplateProcessors);
    }

    public static class Builder extends AbstractBuilder<ServiceConfig> {

        public Builder core(CoreConfig value) {
            getBuildingInstance().setCore(value);
            return this;
        }


        public Builder persistence(PersistenceConfig value) {
            getBuildingInstance().setPersistence(value);
            return this;
        }


        public Builder messageBus(MessageBusConfig value) {
            getBuildingInstance().setMessageBus(value);
            return this;
        }


        public Builder assetConnections(List<AssetConnectionConfig> value) {
            getBuildingInstance().setAssetConnections(value);
            return this;
        }


        public Builder assetConnection(AssetConnectionConfig value) {
            getBuildingInstance().getAssetConnections().add(value);
            return this;
        }


        public Builder endpoints(List<EndpointConfig> value) {
            getBuildingInstance().setEndpoints(value);
            return this;
        }


        public Builder endpoint(EndpointConfig value) {
            getBuildingInstance().getEndpoints().add(value);
            return this;
        }


        @Override
        protected ServiceConfig newBuildingInstance() {
            return new ServiceConfig();
        }

    }
}
