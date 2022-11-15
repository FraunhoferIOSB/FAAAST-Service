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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.MqttOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.MqttSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.MqttValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.config.MqttOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.config.MqttSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.config.MqttValueProviderConfig;
import java.util.Objects;
import java.util.UUID;


/**
 * Config file for {@link MqttAssetConnection}.
 */
public class MqttAssetConnectionConfig extends AssetConnectionConfig<MqttAssetConnection, MqttValueProviderConfig, MqttOperationProviderConfig, MqttSubscriptionProviderConfig> {

    private String serverUri;
    private String clientId;
    private String username;
    private String password;

    public MqttAssetConnectionConfig() {
        clientId = UUID.randomUUID().toString().replace("-", "");
    }


    public String getServerUri() {
        return serverUri;
    }


    public void setServerUri(String serverUri) {
        this.serverUri = serverUri;
    }


    public String getClientId() {
        return clientId;
    }


    public void setClientId(String clientId) {
        this.clientId = clientId;
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


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), serverUri, clientId, username, password);
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
        final MqttAssetConnectionConfig other = (MqttAssetConnectionConfig) obj;
        return super.equals(other)
                && Objects.equals(this.serverUri, other.serverUri)
                && Objects.equals(this.clientId, other.clientId)
                && Objects.equals(this.username, other.username)
                && Objects.equals(this.password, other.password);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends MqttAssetConnectionConfig, B extends AbstractBuilder<T, B>>
            extends
            AssetConnectionConfig.AbstractBuilder<MqttAssetConnectionConfig, MqttValueProviderConfig, MqttValueProvider, MqttOperationProviderConfig, MqttOperationProvider, MqttSubscriptionProviderConfig, MqttSubscriptionProvider, MqttAssetConnection, B> {

        public B clientId(String value) {
            getBuildingInstance().setClientId(value);
            return getSelf();
        }


        public B serverUri(String value) {
            getBuildingInstance().setServerUri(value);
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

    }

    public static class Builder extends AbstractBuilder<MqttAssetConnectionConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected MqttAssetConnectionConfig newBuildingInstance() {
            return new MqttAssetConnectionConfig();
        }
    }

}
