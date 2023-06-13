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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.mqtt;

import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBusConfig;
import java.util.Objects;


/**
 * Configuration class for {@link MessageBusMqtt}.
 */
public class MessageBusMqttConfig extends MessageBusConfig<MessageBusMqtt> {

    private static final boolean DEFAULT_USE_INTERNAL_SERVER = true;
    private static final boolean DEFAULT_USE_WEBSOCKETS = false;
    private static final int DEFAULT_PORT = 1883;
    private static final int DEFAULT_SSL_PORT = 1884;
    private static final int DEFAULT_WEBSOCKET_PORT = 80;
    private static final int DEFAULT_SSL_WEBSOCKET_PORT = 443;
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String DEFAULT_SERVER_KEYSTORE_PASSWORD = "";
    private static final String DEFAULT_SERVER_KEYSTORE_PATH = "";
    private static final String DEFAULT_CLIENT_KEYSTORE_PASSWORD = "";
    private static final String DEFAULT_CLIENT_KEYSTORE_PATH = "";

    private boolean useInternalServer;
    private int port;
    private int sslPort;
    private String host;
    private int websocketPort;
    private int sslWebsocketPort;
    private boolean useWebsocket;
    private String serverKeystorePassword;
    private String serverKeystorePath;
    private String clientKeystorePath;
    private String clientKeystorePassword;
    private String passwordFile;
    private String username;
    private String password;

    public MessageBusMqttConfig() {
        this.useInternalServer = DEFAULT_USE_INTERNAL_SERVER;
        this.port = DEFAULT_PORT;
        this.sslPort = DEFAULT_SSL_PORT;
        this.host = DEFAULT_HOST;
        this.websocketPort = DEFAULT_WEBSOCKET_PORT;
        this.sslWebsocketPort = DEFAULT_SSL_WEBSOCKET_PORT;
        this.serverKeystorePassword = DEFAULT_SERVER_KEYSTORE_PASSWORD;
        this.serverKeystorePath = DEFAULT_SERVER_KEYSTORE_PATH;
        this.clientKeystorePassword = DEFAULT_CLIENT_KEYSTORE_PASSWORD;
        this.clientKeystorePath = DEFAULT_CLIENT_KEYSTORE_PATH;
        this.useWebsocket = DEFAULT_USE_WEBSOCKETS;
    }


    public int getPort() {
        return port;
    }


    public int getSslPort() {
        return sslPort;
    }


    public String getHost() {
        return host;
    }


    public int getWebsocketPort() {
        return websocketPort;
    }


    public int getSslWebsocketPort() {
        return sslWebsocketPort;
    }


    public String getServerKeystorePassword() {
        return serverKeystorePassword;
    }


    public String getClientKeystorePassword() {
        return clientKeystorePassword;
    }


    public boolean getUseInternalServer() {
        return useInternalServer;
    }


    public void setUseInternalServer(boolean useInternalServer) {
        this.useInternalServer = useInternalServer;
    }


    public String getServerKeystorePath() {
        return serverKeystorePath;
    }


    public String getClientKeystorePath() {
        return clientKeystorePath;
    }


    public void setPort(int port) {
        this.port = port;
    }


    public void setSslPort(int sslPort) {
        this.sslPort = sslPort;
    }


    public void setHost(String host) {
        this.host = host;
    }


    public void setWebsocketPort(int websocketPort) {
        this.websocketPort = websocketPort;
    }


    public void setSslWebsocketPort(int sslWebsocketPort) {
        this.sslWebsocketPort = sslWebsocketPort;
    }


    public void setServerKeystorePassword(String serverKeystorePassword) {
        this.serverKeystorePassword = serverKeystorePassword;
    }


    public void setClientKeystorePassword(String clientKeystorePassword) {
        this.clientKeystorePassword = clientKeystorePassword;
    }


    public void setServerKeystorePath(String serverKeystorePath) {
        this.serverKeystorePath = serverKeystorePath;
    }


    public void setClientKeystorePath(String clientKeystorePath) {
        this.clientKeystorePath = clientKeystorePath;
    }


    public String getPasswordFile() {
        return passwordFile;
    }


    public void setPasswordFile(String passwordFile) {
        this.passwordFile = passwordFile;
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


    public boolean getUseWebsocket() {
        return useWebsocket;
    }


    public void setUseWebsocket(boolean useWebsocket) {
        this.useWebsocket = useWebsocket;
    }


    public static Builder builder() {
        return new Builder();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MessageBusMqttConfig other = (MessageBusMqttConfig) o;
        return Objects.equals(useInternalServer, other.useInternalServer)
                && Objects.equals(port, other.port)
                && Objects.equals(sslPort, other.sslPort)
                && Objects.equals(host, other.host)
                && Objects.equals(websocketPort, other.websocketPort)
                && Objects.equals(sslWebsocketPort, other.sslWebsocketPort)
                && Objects.equals(serverKeystorePassword, other.serverKeystorePassword)
                && Objects.equals(serverKeystorePath, other.serverKeystorePath)
                && Objects.equals(clientKeystorePath, other.clientKeystorePath)
                && Objects.equals(clientKeystorePassword, other.clientKeystorePassword)
                && Objects.equals(passwordFile, other.passwordFile)
                && Objects.equals(username, other.username)
                && Objects.equals(password, other.password)
                && Objects.equals(useWebsocket, other.useWebsocket);

    }


    @Override
    public int hashCode() {
        return Objects.hash(useInternalServer,
                port,
                sslPort,
                host,
                websocketPort,
                sslWebsocketPort,
                serverKeystorePassword,
                serverKeystorePath,
                clientKeystorePath,
                clientKeystorePassword,
                passwordFile,
                username,
                password,
                useWebsocket);
    }

    private abstract static class AbstractBuilder<T extends MessageBusMqttConfig, B extends AbstractBuilder<T, B>> extends MessageBusConfig.AbstractBuilder<MessageBusMqtt, T, B> {

        public B internal(boolean value) {
            getBuildingInstance().setUseInternalServer(value);
            return getSelf();
        }


        public B port(int value) {
            getBuildingInstance().setPort(value);
            return getSelf();
        }


        public B sslPort(int value) {
            getBuildingInstance().setSslPort(value);
            return getSelf();
        }


        public B host(String value) {
            getBuildingInstance().setHost(value);
            return getSelf();
        }


        public B websocketPort(int value) {
            getBuildingInstance().setWebsocketPort(value);
            return getSelf();
        }


        public B sslWebsocketPort(int value) {
            getBuildingInstance().setSslWebsocketPort(value);
            return getSelf();
        }


        public B useWebsocket(boolean useWebsocket) {
            getBuildingInstance().setUseWebsocket(useWebsocket);
            return getSelf();
        }


        public B serverKeystorePassword(String value) {
            getBuildingInstance().setServerKeystorePassword(value);
            return getSelf();
        }


        public B clientKeystorePassword(String value) {
            getBuildingInstance().setClientKeystorePassword(value);
            return getSelf();
        }


        public B serverKeystorePath(String value) {
            getBuildingInstance().setServerKeystorePath(value);
            return getSelf();
        }


        public B clientKeystorePath(String value) {
            getBuildingInstance().setClientKeystorePath(value);
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


        public B passwordFile(String value) {
            getBuildingInstance().setPasswordFile(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<MessageBusMqttConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected MessageBusMqttConfig newBuildingInstance() {
            return new MessageBusMqttConfig();
        }

    }
}
