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

    public static final boolean DEFAULT_CORS_ENABLED = false;
    public static final boolean DEFAULT_CORS_ALLOW_CREDENTIALS = false;
    public static final String DEFAULT_CORS_ALLOWED_HEADERS = "*";
    public static final String DEFAULT_CORS_ALLOWED_METHODS = "GET, POST, HEAD";
    public static final String DEFAULT_CORS_ALLOWED_ORIGIN = "*";
    public static final String DEFAULT_CORS_EXPOSED_HEADERS = "";
    public static final long DEFAULT_CORS_MAX_AGE = 3600;
    public static final String DEFAULT_HOSTNAME = null;
    public static final boolean DEFAULT_INCLUDE_ERROR_DETAILS = false;
    public static final int DEFAULT_PORT = 443;
    public static final boolean DEFAULT_SNI_ENABLED = true;
    public static final boolean DEFAULT_SSL_ENABLED = true;

    public static Builder builder() {
        return new Builder();
    }

    private CertificateConfig certificate;
    private boolean corsEnabled;
    private boolean corsAllowCredentials;
    private String corsAllowedHeaders;
    private String corsAllowedMethods;
    private String corsAllowedOrigin;
    private String corsExposedHeaders;
    private long corsMaxAge;
    private String hostname;
    private boolean includeErrorDetails;
    private int port;
    private boolean sniEnabled;
    private boolean sslEnabled;
    private String jwkProvider;
    private String aclFolder;

    public HttpEndpointConfig() {
        certificate = CertificateConfig.builder()
                .build();
        corsEnabled = DEFAULT_CORS_ENABLED;
        corsAllowCredentials = DEFAULT_CORS_ALLOW_CREDENTIALS;
        corsAllowedHeaders = DEFAULT_CORS_ALLOWED_HEADERS;
        corsAllowedMethods = DEFAULT_CORS_ALLOWED_METHODS;
        corsAllowedOrigin = DEFAULT_CORS_ALLOWED_ORIGIN;
        corsExposedHeaders = DEFAULT_CORS_EXPOSED_HEADERS;
        corsMaxAge = DEFAULT_CORS_MAX_AGE;
        hostname = DEFAULT_HOSTNAME;
        includeErrorDetails = DEFAULT_INCLUDE_ERROR_DETAILS;
        port = DEFAULT_PORT;
        sniEnabled = DEFAULT_SNI_ENABLED;
        sslEnabled = DEFAULT_SSL_ENABLED;
    }


    public CertificateConfig getCertificate() {
        return certificate;
    }


    public void setCertificate(CertificateConfig certificate) {
        this.certificate = certificate;
    }


    public boolean isCorsEnabled() {
        return corsEnabled;
    }


    public void setCorsEnabled(boolean corsEnabled) {
        this.corsEnabled = corsEnabled;
    }


    public boolean isCorsAllowCredentials() {
        return corsAllowCredentials;
    }


    public void setCorsAllowCredentials(boolean corsAllowCredentials) {
        this.corsAllowCredentials = corsAllowCredentials;
    }


    public String getCorsAllowedHeaders() {
        return corsAllowedHeaders;
    }


    public void setCorsAllowedHeaders(String corsAllowedHeaders) {
        this.corsAllowedHeaders = corsAllowedHeaders;
    }


    public String getCorsAllowedMethods() {
        return corsAllowedMethods;
    }


    public void setCorsAllowedMethods(String corsAllowedMethods) {
        this.corsAllowedMethods = corsAllowedMethods;
    }


    public String getCorsAllowedOrigin() {
        return corsAllowedOrigin;
    }


    public void setCorsAllowedOrigin(String corsAllowedOrigin) {
        this.corsAllowedOrigin = corsAllowedOrigin;
    }


    public String getCorsExposedHeaders() {
        return corsExposedHeaders;
    }


    public void setCorsExposedHeaders(String corsExposedHeaders) {
        this.corsExposedHeaders = corsExposedHeaders;
    }


    public long getCorsMaxAge() {
        return corsMaxAge;
    }


    public void setCorsMaxAge(long corsMaxAge) {
        this.corsMaxAge = corsMaxAge;
    }


    public String getHostname() {
        return hostname;
    }


    public void setHostname(String hostname) {
        this.hostname = hostname;
    }


    public boolean isIncludeErrorDetails() {
        return includeErrorDetails;
    }


    public void setIncludeErrorDetails(boolean includeErrorDetails) {
        this.includeErrorDetails = includeErrorDetails;
    }


    public int getPort() {
        return port;
    }


    public void setPort(int port) {
        this.port = port;
    }


    public boolean isSniEnabled() {
        return sniEnabled;
    }


    public void setSniEnabled(boolean sniEnabled) {
        this.sniEnabled = sniEnabled;
    }


    public boolean isSslEnabled() {
        return sslEnabled;
    }


    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }


    public String getJwkProvider() {
        return jwkProvider;
    }


    public void setJwkProvider(String jwkProvider) {
        this.jwkProvider = jwkProvider;
    }


    public String getAclFolder() {
        return aclFolder;
    }


    public void setAclFolder(String aclFolder) {
        this.aclFolder = aclFolder;
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
        return super.equals(o)
                && Objects.equals(certificate, that.certificate)
                && Objects.equals(corsEnabled, that.corsEnabled)
                && Objects.equals(corsAllowCredentials, that.corsAllowCredentials)
                && Objects.equals(corsAllowedHeaders, that.corsAllowedHeaders)
                && Objects.equals(corsAllowedMethods, that.corsAllowedMethods)
                && Objects.equals(corsAllowedOrigin, that.corsAllowedOrigin)
                && Objects.equals(corsExposedHeaders, that.corsExposedHeaders)
                && Objects.equals(corsMaxAge, that.corsMaxAge)
                && Objects.equals(hostname, that.hostname)
                && Objects.equals(includeErrorDetails, that.includeErrorDetails)
                && Objects.equals(port, that.port)
                && Objects.equals(sniEnabled, that.sniEnabled)
                && Objects.equals(sslEnabled, that.sslEnabled)
                && Objects.equals(certificate, that.certificate)
                && Objects.equals(hostname, that.hostname)
                && Objects.equals(jwkProvider, that.jwkProvider)
                && Objects.equals(aclFolder, that.aclFolder)
                && Objects.equals(profiles, that.profiles);
    }


    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                certificate,
                corsEnabled,
                corsAllowCredentials,
                corsAllowedHeaders,
                corsAllowedMethods,
                corsAllowedOrigin,
                corsExposedHeaders,
                corsMaxAge,
                hostname,
                includeErrorDetails,
                port,
                sniEnabled,
                sslEnabled,
                jwkProvider,
                aclFolder,
                profiles);
    }

    private abstract static class AbstractBuilder<T extends HttpEndpointConfig, B extends AbstractBuilder<T, B>> extends EndpointConfig.AbstractBuilder<HttpEndpoint, T, B> {

        public B certificate(CertificateConfig value) {
            getBuildingInstance().setCertificate(value);
            return getSelf();
        }


        public B cors() {
            getBuildingInstance().setCorsEnabled(true);
            return getSelf();
        }


        public B cors(boolean value) {
            getBuildingInstance().setCorsEnabled(value);
            return getSelf();
        }


        public B corsAllowCredentials() {
            getBuildingInstance().setCorsAllowCredentials(true);
            return getSelf();
        }


        public B corsAllowCredentials(boolean value) {
            getBuildingInstance().setCorsAllowCredentials(value);
            return getSelf();
        }


        public B corsAllowedHeaders(String value) {
            getBuildingInstance().setCorsAllowedHeaders(value);
            return getSelf();
        }


        public B corsAllowedMethods(String value) {
            getBuildingInstance().setCorsAllowedMethods(value);
            return getSelf();
        }


        public B corsAllowedOrigin(String value) {
            getBuildingInstance().setCorsAllowedOrigin(value);
            return getSelf();
        }


        public B corsExposedHeaders(String value) {
            getBuildingInstance().setCorsExposedHeaders(value);
            return getSelf();
        }


        public B corsMaxAge(long value) {
            getBuildingInstance().setCorsMaxAge(value);
            return getSelf();
        }


        public B hostname(String value) {
            getBuildingInstance().setHostname(value);
            return getSelf();
        }


        public B jwkProvider(String value) {
            getBuildingInstance().setJwkProvider(value);
            return getSelf();
        }


        public B includeErrorDetails() {
            getBuildingInstance().setIncludeErrorDetails(true);
            return getSelf();
        }


        public B includeErrorDetails(boolean value) {
            getBuildingInstance().setIncludeErrorDetails(value);
            return getSelf();
        }


        public B port(int value) {
            getBuildingInstance().setPort(value);
            return getSelf();
        }


        public B sni() {
            getBuildingInstance().setSniEnabled(true);
            return getSelf();
        }


        public B sni(boolean value) {
            getBuildingInstance().setSniEnabled(value);
            return getSelf();
        }


        public B ssl() {
            getBuildingInstance().setSslEnabled(true);
            return getSelf();
        }


        public B ssl(boolean value) {
            getBuildingInstance().setSslEnabled(value);
            return getSelf();
        }


        public B aclFolder(String value) {
            getBuildingInstance().setAclFolder(value);
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
