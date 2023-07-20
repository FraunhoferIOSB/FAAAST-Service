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

import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBusConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Configuration class for {@link MessageBusMqtt}.
 */
public class MessageBusMqttConfig extends MessageBusConfig<MessageBusMqtt> {

    private static final String DEFAULT_CLIENT_ID = "FAST MQTT MessageBus";
    private static final String DEFAULT_CLIENT_KEYSTORE_PASSWORD = "";
    private static final String DEFAULT_CLIENT_KEYSTORE_PATH = "";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 1883;
    private static final String DEFAULT_SERVER_KEYSTORE_PASSWORD = "";
    private static final String DEFAULT_SERVER_KEYSTORE_PATH = "";
    private static final int DEFAULT_SSL_PORT = 8883;
    private static final int DEFAULT_SSL_WEBSOCKET_PORT = 443;
    private static final String DEFAULT_TOPIC_PREFIX = "events/";
    private static final boolean DEFAULT_USE_INTERNAL_SERVER = true;
    private static final boolean DEFAULT_USE_WEBSOCKETS = false;
    private static final int DEFAULT_WEBSOCKET_PORT = 9001;

    private String clientId;
    private CertificateConfig clientCertificate;
    private CertificateConfig serverCertificate;
    private String host;
    private String password;
    private int port;
    private int sslPort;
    private int sslWebsocketPort;
    private String topicPrefix;
    private boolean useInternalServer;
    private boolean useWebsocket;
    private String username;
    private Map<String, String> users;
    private int websocketPort;

    public MessageBusMqttConfig() {
        this.useInternalServer = DEFAULT_USE_INTERNAL_SERVER;
        this.port = DEFAULT_PORT;
        this.sslPort = DEFAULT_SSL_PORT;
        this.host = DEFAULT_HOST;
        this.websocketPort = DEFAULT_WEBSOCKET_PORT;
        this.sslWebsocketPort = DEFAULT_SSL_WEBSOCKET_PORT;
        this.serverCertificate = CertificateConfig.builder()
                .keyStorePath(DEFAULT_SERVER_KEYSTORE_PATH)
                .keyStorePassword(DEFAULT_SERVER_KEYSTORE_PASSWORD)
                .build();
        this.clientCertificate = CertificateConfig.builder()
                .keyStorePath(DEFAULT_CLIENT_KEYSTORE_PATH)
                .keyStorePassword(DEFAULT_CLIENT_KEYSTORE_PASSWORD)
                .build();
        this.users = new HashMap<>();
        this.useWebsocket = DEFAULT_USE_WEBSOCKETS;
        this.clientId = DEFAULT_CLIENT_ID;
        this.topicPrefix = DEFAULT_TOPIC_PREFIX;
    }


    public String getClientId() {
        return clientId;
    }


    public void setClientId(String clientId) {
        this.clientId = clientId;
    }


    public CertificateConfig getServerCertificate() {
        return serverCertificate;
    }


    public void setServerCertificate(CertificateConfig serverCertificate) {
        this.serverCertificate = serverCertificate;
    }


    public CertificateConfig getClientCertificate() {
        return clientCertificate;
    }


    public void setClientCertificate(CertificateConfig clientCertificate) {
        this.clientCertificate = clientCertificate;
    }


    public String getHost() {
        return host;
    }


    public void setHost(String host) {
        this.host = host;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public int getPort() {
        return port;
    }


    public void setPort(int port) {
        this.port = port;
    }


    public int getSslPort() {
        return sslPort;
    }


    public void setSslPort(int sslPort) {
        this.sslPort = sslPort;
    }


    public int getSslWebsocketPort() {
        return sslWebsocketPort;
    }


    public void setSslWebsocketPort(int sslWebsocketPort) {
        this.sslWebsocketPort = sslWebsocketPort;
    }


    public String getTopicPrefix() {
        return topicPrefix;
    }


    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }


    public boolean getUseInternalServer() {
        return useInternalServer;
    }


    public void setUseInternalServer(boolean useInternalServer) {
        this.useInternalServer = useInternalServer;
    }


    public boolean getUseWebsocket() {
        return useWebsocket;
    }


    public void setUseWebsocket(boolean useWebsocket) {
        this.useWebsocket = useWebsocket;
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public Map<String, String> getUsers() {
        return users;
    }


    public void setUsers(Map<String, String> users) {
        this.users = users;
    }


    public int getWebsocketPort() {
        return websocketPort;
    }


    public void setWebsocketPort(int websocketPort) {
        this.websocketPort = websocketPort;
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
                && Objects.equals(serverCertificate, other.serverCertificate)
                && Objects.equals(clientCertificate, other.clientCertificate)
                && Objects.equals(users, other.users)
                && Objects.equals(username, other.username)
                && Objects.equals(password, other.password)
                && Objects.equals(useWebsocket, other.useWebsocket)
                && Objects.equals(clientId, other.clientId)
                && Objects.equals(topicPrefix, other.topicPrefix);

    }


    @Override
    public int hashCode() {
        return Objects.hash(useInternalServer,
                port,
                sslPort,
                host,
                websocketPort,
                sslWebsocketPort,
                serverCertificate,
                clientCertificate,
                users,
                username,
                password,
                useWebsocket,
                clientId,
                topicPrefix);
    }


    public static Builder builder() {
        return new Builder();
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

    private abstract static class AbstractBuilder<T extends MessageBusMqttConfig, B extends AbstractBuilder<T, B>> extends MessageBusConfig.AbstractBuilder<MessageBusMqtt, T, B> {

        public B from(T base) {
            getBuildingInstance().setUseInternalServer(base.getUseInternalServer());
            getBuildingInstance().setPort(base.getPort());
            getBuildingInstance().setSslPort(base.getSslPort());
            getBuildingInstance().setHost(base.getHost());
            getBuildingInstance().setWebsocketPort(base.getWebsocketPort());
            getBuildingInstance().setSslWebsocketPort(base.getSslWebsocketPort());
            getBuildingInstance().setUseWebsocket(base.getUseWebsocket());
            getBuildingInstance().setServerCertificate(base.getServerCertificate());
            getBuildingInstance().setClientCertificate(base.getClientCertificate());
            getBuildingInstance().setUsername(base.getUsername());
            getBuildingInstance().setPassword(base.getPassword());
            getBuildingInstance().setUsers(base.getUsers());
            getBuildingInstance().setClientId(base.getClientId());
            getBuildingInstance().setTopicPrefix(base.getTopicPrefix());
            return getSelf();
        }


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


        public B serverCertificate(CertificateConfig value) {
            getBuildingInstance().setServerCertificate(value);
            return getSelf();
        }


        public B clientCertificate(CertificateConfig value) {
            getBuildingInstance().setClientCertificate(value);
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


        public B users(Map<String, String> value) {
            getBuildingInstance().setUsers(value);
            return getSelf();
        }


        public B user(String username, String password) {
            getBuildingInstance().getUsers().put(username, password);
            return getSelf();
        }


        public B clientId(String value) {
            getBuildingInstance().setClientId(value);
            return getSelf();
        }


        public B topicPrefix(String value) {
            getBuildingInstance().setTopicPrefix(value);
            return getSelf();
        }

    }
}
