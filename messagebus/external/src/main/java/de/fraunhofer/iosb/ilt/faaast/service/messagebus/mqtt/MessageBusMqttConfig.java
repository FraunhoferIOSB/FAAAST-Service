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
    private String host = "0.0.0.0";
    private Integer websocketPort = 1885;
    private Integer sslWebsocketPort = 1886;
    private String keystorePass;
    private String keystorePath;
    private String keymanagerPass;

    public static Builder builder() {
        return new Builder();
    }

    private abstract static class AbstractBuilder<T extends MessageBusMqttConfig, B extends AbstractBuilder<T, B>>
            extends MessageBusConfig.AbstractBuilder<MessageBusMqtt, T, B> {

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


        public B keystorePass(String value) {
            getBuildingInstance().setKeystorePass(value);
            return getSelf();
        }


        public B keystorePath(String value) {
            getBuildingInstance().setKeystorePath(value);
            return getSelf();
        }


        public B keymanagerPass(String value) {
            getBuildingInstance().setKeymanagerPass(value);
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


    public String getKeystorePass() {
        return keystorePass;
    }


    public boolean isInternalBroker() {
        return internalBroker;
    }


    public void setInternalBroker(boolean internalBroker) {
        this.internalBroker = internalBroker;
    }


    public String getKeystorePath() {
        return keystorePath;
    }


    public String getKeymanagerPass() {
        return keymanagerPass;
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


    public void setKeystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
    }


    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }


    public void setKeymanagerPass(String keymanagerPass) {
        this.keymanagerPass = keymanagerPass;
    }
}
