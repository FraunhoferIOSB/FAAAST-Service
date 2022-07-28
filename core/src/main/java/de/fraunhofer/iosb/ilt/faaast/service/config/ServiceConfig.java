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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.EndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBusConfig;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.PersistenceConfig;
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

    /**
     * Returns a new builder for this class.
     *
     * @return a new builder for this class
     */
    public static Builder builder() {
        return new Builder();
    }


    public ServiceConfig() {
        this.assetConnections = new ArrayList<>();
        this.endpoints = new ArrayList<>();
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
                && Objects.equals(this.persistence, other.persistence);
    }


    /**
     * Gets the configured asset connections.
     *
     * @return the configured asset connections
     */
    public List<AssetConnectionConfig> getAssetConnections() {
        return assetConnections;
    }


    /**
     * Sets the asset connections.
     *
     * @param assetConnections the asset connections to set
     */
    public void setAssetConnections(List<AssetConnectionConfig> assetConnections) {
        this.assetConnections = assetConnections;
    }


    /**
     * Gets the core configuration.
     *
     * @return the core configuration
     */
    public CoreConfig getCore() {
        return core;
    }


    /**
     * Sets the core configuration.
     *
     * @param core the core configuration to set
     */
    public void setCore(CoreConfig core) {
        this.core = core;
    }


    /**
     * Gets the configured endpoints.
     *
     * @return the configured endpoints
     */
    public List<EndpointConfig> getEndpoints() {
        return endpoints;
    }


    /**
     * Sets the endpoints.
     *
     * @param endpoints the endpoints to set
     */
    public void setEndpoints(List<EndpointConfig> endpoints) {
        this.endpoints = endpoints;
    }


    /**
     * Gets the persistence configuration
     *
     * @return the persistence configuration
     */
    public PersistenceConfig getPersistence() {
        return persistence;
    }


    /**
     * Sets the persistence.
     *
     * @param persistence the persistence to set
     */
    public void setPersistence(PersistenceConfig persistence) {
        this.persistence = persistence;
    }


    @Override
    public int hashCode() {
        return Objects.hash(core, assetConnections, persistence, endpoints);
    }

    /**
     * Builder class for ServiceConfig.
     */
    public static class Builder {

        private CoreConfig core;
        private List<AssetConnectionConfig> assetConnections;
        private List<EndpointConfig> endpoints;
        private PersistenceConfig persistence;
        private MessageBusConfig messageBus;

        public Builder() {
            this.core = new CoreConfig();
            this.persistence = new PersistenceConfig();
            this.assetConnections = new ArrayList<>();
            this.endpoints = new ArrayList<>();
        }


        /**
         * Sets the core config.
         *
         * @param value the core config
         * @return the builder
         */
        public Builder core(CoreConfig value) {
            this.core = value;
            return this;
        }


        /**
         * Sets the persistence config.
         *
         * @param value the persistence config
         * @return the builder
         */
        public Builder persistence(PersistenceConfig value) {
            this.persistence = value;
            return this;
        }


        /**
         * Sets the messageBus config.
         *
         * @param value the messageBus config
         * @return the builder
         */
        public Builder messageBus(MessageBusConfig value) {
            this.messageBus = value;
            return this;
        }


        /**
         * Sets the asset connections.
         *
         * @param value the asset connections
         * @return the builder
         */
        public Builder assetConnections(List<AssetConnectionConfig> value) {
            this.assetConnections = value;
            return this;
        }


        /**
         * Adds an asset connection to the current list of asset connections.
         *
         * @param value the asset connection to add
         * @return the builder
         */
        public Builder assetConnection(AssetConnectionConfig value) {
            this.assetConnections.add(value);
            return this;
        }


        /**
         * Sets the endpoints.
         *
         * @param value the endpoints
         * @return the builder
         */
        public Builder endpoints(List<EndpointConfig> value) {
            this.endpoints = value;
            return this;
        }


        /**
         * Adds an endpoint to the current list of endpoints.
         *
         * @param value the endpoint to add
         * @return the builder
         */
        public Builder endpoint(EndpointConfig value) {
            this.endpoints.add(value);
            return this;
        }


        /**
         * Builds a new instance of ServiceConfig as defined by the builder.
         *
         * @return a new instance of ServiceConfig as defined by the builder
         */
        public ServiceConfig build() {
            ServiceConfig result = new ServiceConfig();
            result.setAssetConnections(assetConnections);
            result.setCore(core);
            result.setEndpoints(endpoints);
            result.setPersistence(persistence);
            result.setMessageBus(messageBus);
            return result;
        }

    }

    public MessageBusConfig getMessageBus() {
        return messageBus;
    }


    public void setMessageBus(MessageBusConfig messageBus) {
        this.messageBus = messageBus;
    }
}
