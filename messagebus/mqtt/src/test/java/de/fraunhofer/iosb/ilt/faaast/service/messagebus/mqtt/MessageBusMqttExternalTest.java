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

import io.moquette.BrokerConstants;
import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;


public class MessageBusMqttExternalTest extends AbstractMessageBusMqttTest<Server> {

    private static final String LOCALHOST = "127.0.0.1";

    @Override
    protected Server startServer(MessageBusMqttConfig config) throws IOException {
        Server result = new Server();
        result.startServer(getMqttServerConfig(config), null);
        return result;
    }


    @Override
    protected void stopServer(Server server) {
        server.stopServer();
    }


    private static IConfig getMqttServerConfig(MessageBusMqttConfig config) {
        MemoryConfig result = new MemoryConfig(new Properties());
        result.setProperty(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(config.getPort()));
        result.setProperty(BrokerConstants.HOST_PROPERTY_NAME, LOCALHOST);
        if (Objects.isNull(config.getPasswordFile())) {
            result.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.toString(true));
        }
        else {
            result.setProperty(BrokerConstants.PASSWORD_FILE_PROPERTY_NAME, config.getPasswordFile());
            result.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.toString(false));
        }
        if (config.getUseWebsocket()) {
            result.setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, Integer.toString(config.getWebsocketPort()));
        }
        if (Objects.nonNull(config.getServerKeystorePath())) {
            result.setProperty(BrokerConstants.JKS_PATH_PROPERTY_NAME, config.getServerKeystorePath());
            if (Objects.nonNull(config.getServerKeystorePassword())) {
                result.setProperty(BrokerConstants.KEY_STORE_PASSWORD_PROPERTY_NAME, config.getServerKeystorePassword());
                result.setProperty(BrokerConstants.KEY_MANAGER_PASSWORD_PROPERTY_NAME, config.getServerKeystorePassword());
            }
            result.setProperty(BrokerConstants.SSL_PORT_PROPERTY_NAME, Integer.toString(config.getSslPort()));
            if (config.getUseWebsocket()) {
                result.setProperty(BrokerConstants.WSS_PORT_PROPERTY_NAME, Integer.toString(config.getSslWebsocketPort()));
            }
        }

        return result;
    }


    @Override
    protected int getTcpPort() {
        return findFreePort();
    }


    @Override
    protected int getTcpSslPort() {
        return findFreePort();
    }


    @Override
    protected int getWebsocketPort() {
        return findFreePort();
    }


    @Override
    protected int getWebsocketSslPort() {
        return findFreePort();
    }


    @Override
    protected MessageBusMqttConfig configureInternalTcpNoSslAsAnonymousSuccess() {
        return MessageBusMqttConfig.builder()
                .internal(false)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalWebsocketNoSslAsAnonymousSuccess() {
        return MessageBusMqttConfig.builder()
                .internal(false)
                .useWebsocket(true)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalTcpWithSslAsAnonymousSuccess() {
        return MessageBusMqttConfig.builder()
                .internal(false)
                .serverKeystorePath(SERVER_KEYSTORE_PATH)
                .serverKeystorePassword(SERVER_KEYSTORE_PASSWORD)
                .clientKeystorePath(CLIENT_KEYSTORE_PATH)
                .clientKeystorePassword(CLIENT_KEYSTORE_PASSWORD)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalWebsocketWithSslAsAnonymousSuccess() {
        return MessageBusMqttConfig.builder()
                .internal(false)
                .useWebsocket(true)
                .serverKeystorePath(SERVER_KEYSTORE_PATH)
                .serverKeystorePassword(SERVER_KEYSTORE_PASSWORD)
                .clientKeystorePath(CLIENT_KEYSTORE_PATH)
                .clientKeystorePassword(CLIENT_KEYSTORE_PASSWORD)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalTcpNoSslAsAnonymousFail() {
        return MessageBusMqttConfig.builder()
                .internal(false)
                .passwordFile(PASSWORD_FILE)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalWebsocketNoSslAsAnonymousFail() {
        return MessageBusMqttConfig.builder()
                .internal(false)
                .useWebsocket(true)
                .passwordFile(PASSWORD_FILE)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalTcpWithSslAsAnonymousFail() {
        return MessageBusMqttConfig.builder()
                .internal(false)
                .passwordFile(PASSWORD_FILE)
                .serverKeystorePath(SERVER_KEYSTORE_PATH)
                .serverKeystorePassword(SERVER_KEYSTORE_PASSWORD)
                .clientKeystorePath(CLIENT_KEYSTORE_PATH)
                .clientKeystorePassword(CLIENT_KEYSTORE_PASSWORD)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalWebsocketWithSslAsAnonymousFail() {
        return MessageBusMqttConfig.builder()
                .internal(false)
                .useWebsocket(true)
                .passwordFile(PASSWORD_FILE)
                .serverKeystorePath(SERVER_KEYSTORE_PATH)
                .serverKeystorePassword(SERVER_KEYSTORE_PASSWORD)
                .clientKeystorePath(CLIENT_KEYSTORE_PATH)
                .clientKeystorePassword(CLIENT_KEYSTORE_PASSWORD)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalTcpNoSslAsInvalidUser() {
        return MessageBusMqttConfig.builder()
                .internal(false)
                .passwordFile(PASSWORD_FILE)
                .username(USER)
                .password(USER_PASSWORD_INVALID)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalWebsocketNoSslAsInvalidUser() {
        return MessageBusMqttConfig.builder()
                .internal(false)
                .useWebsocket(true)
                .passwordFile(PASSWORD_FILE)
                .username(USER)
                .password(USER_PASSWORD_INVALID)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalTcpWithSslAsInvalidUser() {
        return MessageBusMqttConfig.builder()
                .internal(false)
                .passwordFile(PASSWORD_FILE)
                .username(USER)
                .password(USER_PASSWORD_INVALID)
                .serverKeystorePath(SERVER_KEYSTORE_PATH)
                .serverKeystorePassword(SERVER_KEYSTORE_PASSWORD)
                .clientKeystorePath(CLIENT_KEYSTORE_PATH)
                .clientKeystorePassword(CLIENT_KEYSTORE_PASSWORD)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalWebsocketWithSslAsInvalidUser() {
        return MessageBusMqttConfig.builder()
                .internal(false)
                .useWebsocket(true)
                .passwordFile(PASSWORD_FILE)
                .username(USER)
                .password(USER_PASSWORD_INVALID)
                .serverKeystorePath(SERVER_KEYSTORE_PATH)
                .serverKeystorePassword(SERVER_KEYSTORE_PASSWORD)
                .clientKeystorePath(CLIENT_KEYSTORE_PATH)
                .clientKeystorePassword(CLIENT_KEYSTORE_PASSWORD)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalTcpNoSslAsValidUser() {
        return MessageBusMqttConfig.builder()
                .internal(false)
                .passwordFile(PASSWORD_FILE)
                .username(USER)
                .password(USER_PASSWORD_VALID)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalWebsocketNoSslAsValidUser() {
        return MessageBusMqttConfig.builder()
                .internal(false)
                .useWebsocket(true)
                .passwordFile(PASSWORD_FILE)
                .username(USER)
                .password(USER_PASSWORD_VALID)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalTcpWithSslAsValidUser() {
        return MessageBusMqttConfig.builder()
                .internal(false)
                .passwordFile(PASSWORD_FILE)
                .username(USER)
                .password(USER_PASSWORD_VALID)
                .serverKeystorePath(SERVER_KEYSTORE_PATH)
                .serverKeystorePassword(SERVER_KEYSTORE_PASSWORD)
                .clientKeystorePath(CLIENT_KEYSTORE_PATH)
                .clientKeystorePassword(CLIENT_KEYSTORE_PASSWORD)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalWebsocketWithSslAsValidUser() {
        return MessageBusMqttConfig.builder()
                .internal(false)
                .useWebsocket(true)
                .passwordFile(PASSWORD_FILE)
                .username(USER)
                .password(USER_PASSWORD_VALID)
                .serverKeystorePath(SERVER_KEYSTORE_PATH)
                .serverKeystorePassword(SERVER_KEYSTORE_PASSWORD)
                .clientKeystorePath(CLIENT_KEYSTORE_PATH)
                .clientKeystorePassword(CLIENT_KEYSTORE_PASSWORD)
                .build();
    }

}
