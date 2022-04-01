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

import java.util.Objects;


/**
 * Central configuration class of a Service. This configuration is available in all parts of the service and therefore
 * should only contain general configuration properties.
 */
public class CoreConfig {

    private int requestHandlerThreadPoolSize;

    /**
     * Returns a new builder for this class.
     * 
     * @return a new builder for this class
     */
    public static Builder builder() {
        return new Builder();
    }


    /**
     * Gets the number of threads used for executing async requests.
     * 
     * @return the the number of threads used for executing async requests
     */
    public int getRequestHandlerThreadPoolSize() {
        return requestHandlerThreadPoolSize;
    }


    /**
     * Sets the number of threads used for executing async requests.
     * 
     * @param requestHandlerThreadPoolSize the number of threads used for executing async requests
     */
    public void setRequestHandlerThreadPoolSize(int requestHandlerThreadPoolSize) {
        this.requestHandlerThreadPoolSize = requestHandlerThreadPoolSize;
    }


    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.requestHandlerThreadPoolSize);
        return hash;
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
        return Objects.equals(this.requestHandlerThreadPoolSize, other.requestHandlerThreadPoolSize);
    }

    /**
     * Builder for CoreConfig class.
     */
    public static class Builder {

        private int requestHandlerThreadPoolSize = 1;

        /**
         * Sets the number of threads used for executing async requests.
         * 
         * @param value the number of threads used for executing async requests
         * @return the builder
         */
        public Builder requestHandlerThreadPoolSize(int value) {
            this.requestHandlerThreadPoolSize = value;
            return this;
        }


        /**
         * Builds a new instance of CoreConfig as defined by the builder.
         *
         * @return a new instance of CoreConfig as defined by the builder
         */
        public CoreConfig build() {
            CoreConfig result = new CoreConfig();
            result.setRequestHandlerThreadPoolSize(requestHandlerThreadPoolSize);
            return result;
        }
    }

}
