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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.HttpOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.HttpSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.HttpValueProviderConfig;
import java.util.Objects;


/**
 * Config file for {@link HttpAssetConnection}.
 */
public class HttpAssetConnectionConfig extends AssetConnectionConfig<HttpAssetConnection, HttpValueProviderConfig, HttpOperationProviderConfig, HttpSubscriptionProviderConfig> {

    private String serverUri;

    public String getServerUri() {
        return serverUri;
    }


    public void setServerUri(String serverUri) {
        this.serverUri = serverUri;
    }


    @Override
    public int hashCode() {
        return Objects.hash(serverUri);
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
        if (!Objects.equals(this.serverUri, other.serverUri)) {
            return false;
        }
        return Objects.equals(this.serverUri, other.serverUri);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends HttpAssetConnectionConfig, B extends AbstractBuilder<T, B>>
            extends
            AssetConnectionConfig.AbstractBuilder<HttpAssetConnectionConfig, HttpValueProviderConfig, HttpOperationProviderConfig, HttpSubscriptionProviderConfig, HttpAssetConnection, B> {

        public B serverUri(String value) {
            getBuildingInstance().setServerUri(value);
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
