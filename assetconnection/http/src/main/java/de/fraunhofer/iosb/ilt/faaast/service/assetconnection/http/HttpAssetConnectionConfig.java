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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.*;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;


/**
 * Config file for {@link HttpAssetConnection}.
 */
public class HttpAssetConnectionConfig extends AssetConnectionConfig<HttpAssetConnection, HttpValueProviderConfig, HttpOperationProviderConfig, HttpSubscriptionProviderConfig> {

    private URL baseUrl;

    public URL getBaseUrl() {
        return baseUrl;
    }


    public void setBaseUrl(URL baseUrl) {
        this.baseUrl = baseUrl;
    }


    @Override
    public int hashCode() {
        return Objects.hash(baseUrl);
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
        if (!Objects.equals(this.baseUrl, other.baseUrl)) {
            return false;
        }
        return Objects.equals(this.baseUrl, other.baseUrl);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends HttpAssetConnectionConfig, B extends AbstractBuilder<T, B>>
            extends
            AssetConnectionConfig.AbstractBuilder<HttpAssetConnectionConfig, HttpValueProviderConfig, HttpOperationProviderConfig, HttpSubscriptionProviderConfig, HttpAssetConnection, B> {

        public B baseUrl(URL value) {
            getBuildingInstance().setBaseUrl(value);
            return getSelf();
        }


        public B baseUrl(String value) throws MalformedURLException {
            getBuildingInstance().setBaseUrl(new URL(value));
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
