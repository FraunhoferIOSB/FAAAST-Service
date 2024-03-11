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

import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.EndpointConfig;
import java.util.Objects;


/**
 * Configuration class for {@link HttpEndpoint}.
 */
public class HttpEndpointConfig extends EndpointConfig<HttpEndpoint> {

    public static final int DEFAULT_PORT = 443;
    public static final boolean DEFAULT_CORS_ENABLED = false;
    public static final boolean DEFAULT_SNI_ENABLED = true;
    private int port;
    private boolean corsEnabled;
    private boolean sniEnabled;
    private CertificateConfig certificate;

    public HttpEndpointConfig() {
        port = DEFAULT_PORT;
        corsEnabled = DEFAULT_CORS_ENABLED;
        sniEnabled = DEFAULT_SNI_ENABLED;
        certificate = CertificateConfig.builder()
                .build();
    }


    public int getPort() {
        return port;
    }


    public void setPort(int port) {
        this.port = port;
    }


    public boolean isCorsEnabled() {
        return corsEnabled;
    }


    public void setCorsEnabled(boolean corsEnabled) {
        this.corsEnabled = corsEnabled;
    }


    public boolean isSniEnabled() {
        return sniEnabled;
    }


    public void setSniEnabled(boolean sniEnabled) {
        this.sniEnabled = sniEnabled;
    }


    public CertificateConfig getCertificate() {
        return certificate;
    }


    public void setCertificate(CertificateConfig certificate) {
        this.certificate = certificate;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HttpEndpointConfig that = (HttpEndpointConfig) o;
        return Objects.equals(port, that.port)
                && Objects.equals(corsEnabled, that.corsEnabled)
                && Objects.equals(sniEnabled, that.sniEnabled)
                && Objects.equals(certificate, that.certificate);
    }


    @Override
    public int hashCode() {
        return Objects.hash(port, corsEnabled, sniEnabled, certificate);
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


        public B sni(boolean value) {
            getBuildingInstance().setSniEnabled(value);
            return getSelf();
        }


        public B certificate(CertificateConfig value) {
            getBuildingInstance().setCertificate(value);
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

}
