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


/**
 * Configuration class for {@link MessageBusMqtt}.
 */
public class MessageBusMqttConfig extends MessageBusConfig<MessageBusMqtt> {

    private boolean internalBroker = true;
    private Integer port = 1883;
    private Integer sslPort = 1884;
    private String host = "127.0.0.1";
    private Integer websocketPort = 1885;
    private Integer sslWebsocketPort = 1886;
    private String brokerKeystorePass = "password";
    private String brokerKeystorePath = "";
    private String brokerKeymanagerPass = "password";
    private String clientKeystorePath = "";
    private String clientKeystorePass = "password";
    private String passwordFile;
    private String username;
    private String password;

    public static Builder builder() {
        return new Builder();
    }

    private abstract static class AbstractBuilder<T extends MessageBusMqttConfig, B extends AbstractBuilder<T, B>> extends MessageBusConfig.AbstractBuilder<MessageBusMqtt, T, B> {

        public B internal(Boolean value) {
            getBuildingInstance().setInternalBroker(value);
            return getSelf();
        }


        public B port(Integer value) {
            getBuildingInstance().setPort(value);
            return getSelf();
        }


        public B sslPort(Integer value) {
            getBuildingInstance().setSslPort(value);
            return getSelf();
        }


        public B host(String value) {
            getBuildingInstance().setHost(value);
            return getSelf();
        }


        public B websocketPort(Integer value) {
            getBuildingInstance().setWebsocketPort(value);
            return getSelf();
        }


        public B sslWebsocketPort(Integer value) {
            getBuildingInstance().setSslWebsocketPort(value);
            return getSelf();
        }


        public B brokerKeystorePass(String value) {
            getBuildingInstance().setBrokerKeystorePass(value);
            return getSelf();
        }


        public B clientKeystorePass(String value) {
            getBuildingInstance().setClientKeystorePass(value);
            return getSelf();
        }


        public B brokerKeystorePath(String value) {
            getBuildingInstance().setBrokerKeystorePath(value);
            return getSelf();
        }


        public B clientKeystorePath(String value) {
            getBuildingInstance().setClientKeystorePath(value);
            return getSelf();
        }


        public B brokerKeymanagerPass(String value) {
            getBuildingInstance().setBrokerKeymanagerPass(value);
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

    public Integer getPort() {
        return port;
    }


    public Integer getSslPort() {
        return sslPort;
    }


    public String getHost() {
        return host;
    }


    public Integer getWebsocketPort() {
        return websocketPort;
    }


    public Integer getSslWebsocketPort() {
        return sslWebsocketPort;
    }


    public String getBrokerKeystorePass() {
        return brokerKeystorePass;
    }


    public String getClientKeystorePass() {
        return clientKeystorePass;
    }


    public boolean isInternalBroker() {
        return internalBroker;
    }


    public void setInternalBroker(boolean internalBroker) {
        this.internalBroker = internalBroker;
    }


    public String getBrokerKeystorePath() {
        return brokerKeystorePath;
    }


    public String getBrokerKeymanagerPass() {
        return brokerKeymanagerPass;
    }


    public String getClientKeystorePath() {
        return clientKeystorePath;
    }


    public void setPort(Integer port) {
        this.port = port;
    }


    public void setSslPort(Integer sslPort) {
        this.sslPort = sslPort;
    }


    public void setHost(String host) {
        this.host = host;
    }


    public void setWebsocketPort(Integer websocketPort) {
        this.websocketPort = websocketPort;
    }


    public void setSslWebsocketPort(Integer sslWebsocketPort) {
        this.sslWebsocketPort = sslWebsocketPort;
    }


    public void setBrokerKeystorePass(String brokerKeystorePass) {
        this.brokerKeystorePass = brokerKeystorePass;
    }


    public void setClientKeystorePass(String clientKeystorePass) {
        this.clientKeystorePass = clientKeystorePass;
    }


    public void setBrokerKeystorePath(String brokerKeystorePath) {
        this.brokerKeystorePath = brokerKeystorePath;
    }


    public void setClientKeystorePath(String clientKeystorePath) {
        this.clientKeystorePath = clientKeystorePath;
    }


    public void setBrokerKeymanagerPass(String brokerKeymanagerPass) {
        this.brokerKeymanagerPass = brokerKeymanagerPass;
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
}
