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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.ModbusOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.ModbusSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.ModbusValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.util.Objects;


/**
 * Modbus asset connection config.
 */
public class ModbusAssetConnectionConfig
        extends AssetConnectionConfig<ModbusAssetConnection, ModbusValueProviderConfig, ModbusOperationProviderConfig, ModbusSubscriptionProviderConfig> {

    public static final int DEFAULT_PORT = 502;
    public static final long DEFAULT_CONNECT_TIMEOUT_MILLIS = 5000;
    public static final long DEFAULT_REQUEST_TIMEOUT_MILLIS = 5000;
    public static final boolean DEFAULT_CONNECT_PERSISTENT = false;
    public static final boolean DEFAULT_RECONNECT_LAZY = false;
    public static final boolean DEFAULT_TLS_ENABLED = false;
    public static final CertificateConfig DEFAULT_CERTIFICATE_CONFIG = CertificateConfig.builder().build();

    private String hostname;
    private int port;
    private long connectTimeoutMillis;
    private long requestTimeoutMillis;
    private boolean connectPersistent;
    private boolean reconnectLazy;
    private boolean tlsEnabled;
    private CertificateConfig keyCertificateConfig;
    private CertificateConfig trustCertificateConfig;

    public ModbusAssetConnectionConfig() {
        this.port = DEFAULT_PORT;
        this.connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT_MILLIS;
        this.requestTimeoutMillis = DEFAULT_REQUEST_TIMEOUT_MILLIS;
        this.connectPersistent = DEFAULT_CONNECT_PERSISTENT;
        this.reconnectLazy = DEFAULT_RECONNECT_LAZY;
        this.tlsEnabled = DEFAULT_TLS_ENABLED;
        this.keyCertificateConfig = DEFAULT_CERTIFICATE_CONFIG;
        this.trustCertificateConfig = DEFAULT_CERTIFICATE_CONFIG;
    }


    @Override
    public boolean equalsIgnoringProviders(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ModbusAssetConnectionConfig that = (ModbusAssetConnectionConfig) obj;
        return StringHelper.equalsNullOrEmpty(hostname, that.hostname) &&
                Objects.equals(port, that.port) &&
                Objects.equals(connectPersistent, that.connectPersistent) &&
                Objects.equals(reconnectLazy, that.reconnectLazy) &&
                Objects.equals(tlsEnabled, that.tlsEnabled) &&
                Objects.equals(connectTimeoutMillis, that.connectTimeoutMillis) &&
                Objects.equals(requestTimeoutMillis, that.requestTimeoutMillis) &&
                Objects.equals(keyCertificateConfig, that.keyCertificateConfig) &&
                Objects.equals(trustCertificateConfig, that.trustCertificateConfig);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        ModbusAssetConnectionConfig that = (ModbusAssetConnectionConfig) o;
        return StringHelper.equalsNullOrEmpty(hostname, that.hostname) &&
                Objects.equals(port, that.port) &&
                Objects.equals(connectPersistent, that.connectPersistent) &&
                Objects.equals(reconnectLazy, that.reconnectLazy) &&
                Objects.equals(tlsEnabled, that.tlsEnabled) &&
                Objects.equals(connectTimeoutMillis, that.connectTimeoutMillis) &&
                Objects.equals(requestTimeoutMillis, that.requestTimeoutMillis) &&
                Objects.equals(keyCertificateConfig, that.keyCertificateConfig) &&
                Objects.equals(trustCertificateConfig, that.trustCertificateConfig);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                hostname,
                port,
                connectTimeoutMillis,
                requestTimeoutMillis,
                connectPersistent,
                reconnectLazy,
                tlsEnabled,
                keyCertificateConfig,
                trustCertificateConfig);
    }


    public String getHostname() {
        return hostname;
    }


    public void setHostname(String hostname) {
        this.hostname = hostname;
    }


    public int getPort() {
        return port;
    }


    public void setPort(int port) {
        this.port = port;
    }


    public long getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }


    public void setConnectTimeoutMillis(long connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }


    public boolean isConnectPersistent() {
        return connectPersistent;
    }


    public void setConnectPersistent(boolean connectPersistent) {
        this.connectPersistent = connectPersistent;
    }


    public boolean isReconnectLazy() {
        return reconnectLazy;
    }


    public void setReconnectLazy(boolean reconnectLazy) {
        this.reconnectLazy = reconnectLazy;
    }


    public static Builder builder() {
        return new Builder();
    }


    public boolean isTlsEnabled() {
        return tlsEnabled;
    }


    public void setTlsEnabled(boolean tlsEnabled) {
        this.tlsEnabled = tlsEnabled;
    }


    public CertificateConfig getKeyCertificateConfig() {
        return keyCertificateConfig;
    }


    public void setKeyCertificateConfig(CertificateConfig keyCertificateConfig) {
        this.keyCertificateConfig = keyCertificateConfig;
    }


    public CertificateConfig getTrustCertificateConfig() {
        return trustCertificateConfig;
    }


    public void setTrustCertificateConfig(CertificateConfig trustCertificateConfig) {
        this.trustCertificateConfig = trustCertificateConfig;
    }


    public long getRequestTimeoutMillis() {
        return requestTimeoutMillis;
    }


    public void setRequestTimeoutMillis(long requestTimeoutMillis) {
        this.requestTimeoutMillis = requestTimeoutMillis;
    }

    public abstract static class AbstractBuilder<T extends ModbusAssetConnectionConfig, B extends AbstractBuilder<T, B>>
            extends
            AssetConnectionConfig.AbstractBuilder<ModbusAssetConnectionConfig, ModbusValueProviderConfig, ModbusValueProvider, ModbusOperationProviderConfig, ModbusOperationProvider, ModbusSubscriptionProviderConfig, ModbusSubscriptionProvider, ModbusAssetConnection, B> {

        public B hostname(String hostname) {
            getBuildingInstance().setHostname(hostname);
            return getSelf();
        }


        public B port(int port) {
            getBuildingInstance().setPort(port);
            return getSelf();
        }


        public B connectPersistent(boolean connectPersistent) {
            getBuildingInstance().setConnectPersistent(connectPersistent);
            return getSelf();
        }


        public B reconnectLazy(boolean reconnectLazy) {
            getBuildingInstance().setReconnectLazy(reconnectLazy);
            return getSelf();
        }


        public B connectTimeout(long connectTimeoutMillis) {
            getBuildingInstance().setConnectTimeoutMillis(connectTimeoutMillis);
            return getSelf();
        }


        public B requestTimeout(long requestTimeoutMillis) {
            getBuildingInstance().setRequestTimeoutMillis(requestTimeoutMillis);
            return getSelf();
        }


        public B tlsEnabled(boolean tlsEnabled) {
            getBuildingInstance().setTlsEnabled(tlsEnabled);
            return getSelf();
        }


        public B keyCertificateConfig(CertificateConfig keyCertificateConfig) {
            getBuildingInstance().setKeyCertificateConfig(keyCertificateConfig);
            return getSelf();
        }


        public B trustCertificateConfig(CertificateConfig trustCertificateConfig) {
            getBuildingInstance().setTrustCertificateConfig(trustCertificateConfig);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<ModbusAssetConnectionConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected ModbusAssetConnectionConfig newBuildingInstance() {
            return new ModbusAssetConnectionConfig();
        }
    }
}
