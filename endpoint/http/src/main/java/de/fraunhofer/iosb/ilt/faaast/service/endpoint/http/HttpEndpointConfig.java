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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http;

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.EndpointConfig;
import java.util.Objects;


/**
 * Configuration class for {@link HttpEndpoint}
 */
public class HttpEndpointConfig extends EndpointConfig<HttpEndpoint> {

    public static final int DEFAULT_PORT = 8080;
    private int port;
    private boolean corsEnabled;

    public HttpEndpointConfig() {
        this.port = DEFAULT_PORT;
    }


    public int getPort() {
        return port;
    }


    public void setPort(int port) {
        this.port = port;
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
        HttpEndpointConfig that = (HttpEndpointConfig) o;
        return port == that.port;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), port);
    }


    public static Builder builder() {
        return new Builder();
    }

    private abstract static class AbstractBuilder<T extends HttpEndpointConfig, B extends AbstractBuilder<T, B>> extends EndpointConfig.AbstractBuilder<HttpEndpoint, T, B> {

        public B port(int value) {
            getBuildingInstance().setPort(value);
            return getSelf();
        }


        public B cors(boolean value) {
            getBuildingInstance().setCorsEnabled(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<HttpEndpointConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected HttpEndpointConfig newBuildingInstance() {
            return new HttpEndpointConfig();
        }
    }

    public boolean isCorsEnabled() {
        return corsEnabled;
    }


    public void setCorsEnabled(boolean corsEnabled) {
        this.corsEnabled = corsEnabled;
    }
}
