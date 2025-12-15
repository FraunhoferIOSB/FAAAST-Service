package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.ModbusAssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.ModbusAssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.ModbusAssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusAssetOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusAssetSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusAssetValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;

import java.util.Objects;


public class ModbusAssetConnectionConfig extends AssetConnectionConfig<ModbusAssetConnection, ModbusAssetValueProviderConfig, ModbusAssetOperationProviderConfig,
        ModbusAssetSubscriptionProviderConfig> {

    public static final String DEFAULT_SERVER_URI = null;
    public static final int DEFAULT_PORT = 502;
    public static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    public static final boolean DEFAULT_CONNECT_PERSISTENT = true;
    public static final boolean DEFAULT_RECONNECT_LAZY = false;

    private String serverUri;
    private int port;
    private int connectTimeout;
    private boolean connectPersistent;
    private boolean reconnectLazy;


    public ModbusAssetConnectionConfig() {
        this.serverUri = DEFAULT_SERVER_URI;
        this.port = DEFAULT_PORT;
        this.connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        this.connectPersistent = DEFAULT_CONNECT_PERSISTENT;
        this.reconnectLazy = DEFAULT_RECONNECT_LAZY;
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
        return StringHelper.equalsNullOrEmpty(serverUri, that.serverUri)
                && Objects.equals(port, that.port)
                && Objects.equals(connectTimeout, that.connectTimeout)
                && Objects.equals(connectPersistent, that.connectPersistent)
                && Objects.equals(reconnectLazy, that.reconnectLazy);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ModbusAssetConnectionConfig that = (ModbusAssetConnectionConfig) obj;
        return StringHelper.equalsNullOrEmpty(serverUri, that.serverUri)
                && Objects.equals(port, that.port)
                && Objects.equals(connectTimeout, that.connectTimeout)
                && Objects.equals(connectPersistent, that.connectPersistent)
                && Objects.equals(reconnectLazy, that.reconnectLazy);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                serverUri,
                port,
                connectTimeout,
                connectPersistent,
                reconnectLazy);
    }


    public String getServerUri() {
        return serverUri;
    }


    public void setServerUri(String serverUri) {
        this.serverUri = serverUri;
    }


    public int getPort() {
        return port;
    }


    public void setPort(int port) {
        this.port = port;
    }


    public int getConnectTimeout() {
        return connectTimeout;
    }


    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
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


    public abstract static class AbstractBuilder<T extends ModbusAssetConnectionConfig, B extends AbstractBuilder<T, B>>
            extends
            AssetConnectionConfig.AbstractBuilder<ModbusAssetConnectionConfig, ModbusAssetValueProviderConfig, ModbusAssetValueProvider, ModbusOperationProviderConfig,
                    ModbusAssetOperationProvider, ModbusAssetSubscriptionProviderConfig, ModbusAssetSubscriptionProvider, ModbusAssetConnection, B> {

        public B serverUri(String serverUri) {
            getBuildingInstance().setServerUri(serverUri);
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


        public B connectTimeout(int connectTimeout) {
            getBuildingInstance().setConnectTimeout(connectTimeout);
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
