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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.HttpOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.HttpSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.HttpValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Config file for {@link HttpAssetConnection}.
 */
public class HttpAssetConnectionConfig extends AssetConnectionConfig<HttpAssetConnection, HttpValueProviderConfig, HttpOperationProviderConfig, HttpSubscriptionProviderConfig> {

    private URL baseUrl;
    private String username;
    private String password;
    private Map<String, String> headers;
    private CertificateConfig trustedCertificates;

    public HttpAssetConnectionConfig() {
        this.headers = new HashMap<>();
        this.trustedCertificates = CertificateConfig.builder().build();
    }


    public URL getBaseUrl() {
        return baseUrl;
    }


    public void setBaseUrl(URL baseUrl) {
        this.baseUrl = baseUrl;
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public Map<String, String> getHeaders() {
        return headers;
    }


    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }


    public CertificateConfig getTrustedCertificates() {
        return trustedCertificates;
    }


    public void setTrustedCertificates(CertificateConfig trustedCertificates) {
        this.trustedCertificates = trustedCertificates;
    }


    @Override
    public int hashCode() {
        return Objects.hash(baseUrl, username, password, headers, trustedCertificates);
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
        final HttpAssetConnectionConfig other = (HttpAssetConnectionConfig) obj;
        return super.equals(other)
                && Objects.equals(this.baseUrl, other.baseUrl)
                && Objects.equals(this.username, other.username)
                && Objects.equals(this.password, other.password)
                && Objects.equals(this.headers, other.headers)
                && Objects.equals(this.trustedCertificates, other.trustedCertificates);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends HttpAssetConnectionConfig, B extends AbstractBuilder<T, B>>
            extends
            AssetConnectionConfig.AbstractBuilder<HttpAssetConnectionConfig, HttpValueProviderConfig, HttpValueProvider, HttpOperationProviderConfig, HttpOperationProvider, HttpSubscriptionProviderConfig, HttpSubscriptionProvider, HttpAssetConnection, B> {

        public B baseUrl(URL value) {
            getBuildingInstance().setBaseUrl(value);
            return getSelf();
        }


        public B baseUrl(String value) throws MalformedURLException {
            getBuildingInstance().setBaseUrl(new URL(value));
            return getSelf();
        }


        public B username(String value) {
            getBuildingInstance().setUsername(value);
            return getSelf();
        }


        public B password(String value) {
            getBuildingInstance().setPassword(value);
            return getSelf();
        }


        public B headers(Map<String, String> value) {
            getBuildingInstance().setHeaders(value);
            return getSelf();
        }


        public B header(String name, String value) {
            getBuildingInstance().getHeaders().put(name, value);
            return getSelf();
        }


        public B trustedCertificates(CertificateConfig value) {
            getBuildingInstance().setTrustedCertificates(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<HttpAssetConnectionConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected HttpAssetConnectionConfig newBuildingInstance() {
            return new HttpAssetConnectionConfig();
        }
    }

}
