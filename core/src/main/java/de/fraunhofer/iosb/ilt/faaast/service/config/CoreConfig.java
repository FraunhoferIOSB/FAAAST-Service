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

import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.Objects;


/**
 * Central configuration class of a Service. This configuration is available in all parts of the service and therefore
 * should only contain general configuration properties.
 */
public class CoreConfig {

    public static final CoreConfig DEFAULT = builder().build();

    private static final long DEFAULT_ASSET_CONNECTION_RETRY_INTERVAL = 1000;
    private static final int DEFAULT_REQUEST_HANDLER_THREADPOOL_SIZE = 1;

    private long assetConnectionRetryInterval;
    private int requestHandlerThreadPoolSize;

    public CoreConfig() {
        this.assetConnectionRetryInterval = DEFAULT_ASSET_CONNECTION_RETRY_INTERVAL;
        this.requestHandlerThreadPoolSize = DEFAULT_REQUEST_HANDLER_THREADPOOL_SIZE;
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


    public int getRequestHandlerThreadPoolSize() {
        return requestHandlerThreadPoolSize;
    }


    public void setRequestHandlerThreadPoolSize(int requestHandlerThreadPoolSize) {
        this.requestHandlerThreadPoolSize = requestHandlerThreadPoolSize;
    }


    @Override
    public int hashCode() {
        return Objects.hash(assetConnectionRetryInterval, requestHandlerThreadPoolSize);
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
                && Objects.equals(this.requestHandlerThreadPoolSize, other.requestHandlerThreadPoolSize);
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
