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

import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ModelValidatorConfig;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Central configuration class of a Service. This configuration is available in all parts of the service and therefore
 * should only contain general configuration properties.
 */
public class CoreConfig {

    public static final CoreConfig DEFAULT = builder().build();

    private static final long DEFAULT_ASSET_CONNECTION_RETRY_INTERVAL = 1000;
    private static final int DEFAULT_REQUEST_HANDLER_THREADPOOL_SIZE = 1;
    private static final String DEFAULT_REGISTRY_HOST = "localhost";
    private static final int DEFAULT_REGISTRY_PORT = 8090;
    private static final String DEFAULT_AAS_REGISTRY_BASE_PATH = "/api/v3.0/shell-descriptors";
    private static final String DEFAULT_SUBMODEL_REGISTRY_BASE_PATH = "/api/v3.0/submodel-descriptors";
    private static final String DEFAULT_AAS_REGISTRY_PROTOCOL = "HTTPS";

    private long assetConnectionRetryInterval;
    private int requestHandlerThreadPoolSize;
    private ModelValidatorConfig validationOnLoad;
    private ModelValidatorConfig validationOnCreate;
    private ModelValidatorConfig validationOnUpdate;
    private String registryHost;
    private int registryPort;
    private String aasRegistryBasePath;
    private String submodelRegistryBasePath;

    public String getRegistryProtocol() {
        return registryProtocol;
    }


    public void setRegistryProtocol(String registryProtocol) {
        this.registryProtocol = registryProtocol;
    }

    private String registryProtocol;

    public CoreConfig() {
        this.assetConnectionRetryInterval = DEFAULT_ASSET_CONNECTION_RETRY_INTERVAL;
        this.requestHandlerThreadPoolSize = DEFAULT_REQUEST_HANDLER_THREADPOOL_SIZE;
        this.validationOnLoad = ModelValidatorConfig.builder()
                .validateConstraints(true)
                .validateIdShortUniqueness(true)
                .validateIdentifierUniqueness(true)
                .build();
        this.validationOnCreate = ModelValidatorConfig.builder()
                .validateConstraints(false)
                .validateIdShortUniqueness(true)
                .validateIdentifierUniqueness(true)
                .build();
        this.validationOnUpdate = ModelValidatorConfig.builder()
                .validateConstraints(false)
                .validateIdShortUniqueness(true)
                .validateIdentifierUniqueness(true)
                .build();
        this.registryHost = DEFAULT_REGISTRY_HOST;
        this.registryPort = DEFAULT_REGISTRY_PORT;
        this.aasRegistryBasePath = DEFAULT_AAS_REGISTRY_BASE_PATH;
        this.submodelRegistryBasePath = DEFAULT_SUBMODEL_REGISTRY_BASE_PATH;
        this.registryProtocol = DEFAULT_AAS_REGISTRY_PROTOCOL;
    }


    public static Builder builder() {
        return new Builder();
    }


    public long getAssetConnectionRetryInterval() {
        return assetConnectionRetryInterval;
    }


    public void setAssetConnectionRetryInterval(long assetConnectionRetryInterval) {
        this.assetConnectionRetryInterval = assetConnectionRetryInterval;
    }


    public String getRegistryHost() {
        return registryHost;
    }


    public void setRegistryHost(String registryHost) {
        this.registryHost = registryHost;
    }


    public int getRegistryPort() {
        return registryPort;
    }


    public void setRegistryPort(int registryPort) {
        this.registryPort = registryPort;
    }


    public String getAasRegistryBasePath() {
        return aasRegistryBasePath;
    }


    public void setAasRegistryBasePath(String aasRegistryBasePath) {
        this.aasRegistryBasePath = aasRegistryBasePath;
    }


    public String getSubmodelRegistryBasePath() {
        return submodelRegistryBasePath;
    }


    public void setSubmodelRegistryBasePath(String submodelRegistryBasePath) {
        this.submodelRegistryBasePath = submodelRegistryBasePath;
    }


    public int getRequestHandlerThreadPoolSize() {
        return requestHandlerThreadPoolSize;
    }


    public void setValidationOnLoad(ModelValidatorConfig validationOnLoad) {
        this.validationOnLoad = validationOnLoad;
    }


    public ModelValidatorConfig getValidationOnLoad() {
        return validationOnLoad;
    }


    public void setValidationOnCreate(ModelValidatorConfig validationOnCreate) {
        this.validationOnCreate = validationOnCreate;
    }


    public ModelValidatorConfig getValidationOnCreate() {
        return validationOnCreate;
    }


    public void setValidationOnUpdate(ModelValidatorConfig validationOnUpdate) {
        this.validationOnUpdate = validationOnUpdate;
    }


    public ModelValidatorConfig getValidationOnUpdate() {
        return validationOnUpdate;
    }


    public void setRequestHandlerThreadPoolSize(int requestHandlerThreadPoolSize) {
        this.requestHandlerThreadPoolSize = requestHandlerThreadPoolSize;
    }


    @Override
    public int hashCode() {
        return Objects.hash(assetConnectionRetryInterval,
                requestHandlerThreadPoolSize,
                validationOnLoad,
                validationOnCreate,
                validationOnUpdate);
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
        final CoreConfig other = (CoreConfig) obj;
        return Objects.equals(this.assetConnectionRetryInterval, other.assetConnectionRetryInterval)
                && Objects.equals(this.requestHandlerThreadPoolSize, other.requestHandlerThreadPoolSize)
                && Objects.equals(this.validationOnLoad, other.validationOnLoad)
                && Objects.equals(this.validationOnCreate, other.validationOnCreate)
                && Objects.equals(this.validationOnUpdate, other.validationOnUpdate);
    }

    public static class Builder extends ExtendableBuilder<CoreConfig, Builder> {

        public Builder requestHandlerThreadPoolSize(int value) {
            getBuildingInstance().setRequestHandlerThreadPoolSize(value);
            return getSelf();
        }


        public Builder assetConnectionRetryInterval(long value) {
            getBuildingInstance().setAssetConnectionRetryInterval(value);
            return getSelf();
        }


        public Builder validationOnLoad(ModelValidatorConfig value) {
            getBuildingInstance().setValidationOnLoad(value);
            return getSelf();
        }


        public Builder validationOnCreate(ModelValidatorConfig value) {
            getBuildingInstance().setValidationOnCreate(value);
            return getSelf();
        }


        public Builder registryHost(String value) {
            getBuildingInstance().setRegistryHost(value);
            return getSelf();
        }


        public Builder registryPort(int value) {
            getBuildingInstance().setRegistryPort(value);
            return getSelf();
        }


        public Builder registryProtocol(String value) {
            getBuildingInstance().setRegistryProtocol(value);
            return getSelf();
        }


        public Builder aasRegistryBasePath(String value) {
            getBuildingInstance().setAasRegistryBasePath(value);
            return getSelf();
        }


        public Builder submodelRegistryBasePath(String value) {
            getBuildingInstance().setSubmodelRegistryBasePath(value);
            return getSelf();
        }


        public Builder validationOnUpdate(ModelValidatorConfig value) {
            getBuildingInstance().setValidationOnUpdate(value);
            return getSelf();
        }


        public Builder validateConstraints(boolean value) {
            getBuildingInstance().getValidationOnLoad().setValidateConstraints(value);
            getBuildingInstance().getValidationOnCreate().setValidateConstraints(value);
            getBuildingInstance().getValidationOnUpdate().setValidateConstraints(value);
            return getSelf();
        }


        public Builder validateIdShortUniqueness(boolean value) {
            getBuildingInstance().getValidationOnLoad().setValidateIdShortUniqueness(value);
            getBuildingInstance().getValidationOnCreate().setValidateIdShortUniqueness(value);
            getBuildingInstance().getValidationOnUpdate().setValidateIdShortUniqueness(value);
            return getSelf();
        }


        public Builder validateIdentifierUniqueness(boolean value) {
            getBuildingInstance().getValidationOnLoad().setValidateIdentifierUniqueness(value);
            getBuildingInstance().getValidationOnCreate().setValidateIdentifierUniqueness(value);
            getBuildingInstance().getValidationOnUpdate().setValidateIdentifierUniqueness(value);
            return getSelf();
        }


        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected CoreConfig newBuildingInstance() {
            return new CoreConfig();
        }
    }

}
