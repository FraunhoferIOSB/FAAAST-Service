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

public class MessageBusMqttInternalTest extends AbstractMessageBusMqttTest<Void> {

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
                .internal(true)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalWebsocketNoSslAsAnonymousSuccess() {
        return MessageBusMqttConfig.builder()
                .internal(true)
                .useWebsocket(true)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalTcpWithSslAsAnonymousSuccess() {
        return MessageBusMqttConfig.builder()
                .internal(true)
                .serverKeystorePath(SERVER_KEYSTORE_PATH)
                .serverKeystorePassword(SERVER_KEYSTORE_PASSWORD)
                .clientKeystorePath(CLIENT_KEYSTORE_PATH)
                .clientKeystorePassword(CLIENT_KEYSTORE_PASSWORD)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalWebsocketWithSslAsAnonymousSuccess() {
        return MessageBusMqttConfig.builder()
                .internal(true)
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
                .internal(true)
                .passwordFile(PASSWORD_FILE)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalWebsocketNoSslAsAnonymousFail() {
        return MessageBusMqttConfig.builder()
                .internal(true)
                .useWebsocket(true)
                .passwordFile(PASSWORD_FILE)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalTcpWithSslAsAnonymousFail() {
        return MessageBusMqttConfig.builder()
                .internal(true)
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
                .internal(true)
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
                .internal(true)
                .passwordFile(PASSWORD_FILE)
                .username(USER)
                .password(USER_PASSWORD_INVALID)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalWebsocketNoSslAsInvalidUser() {
        return MessageBusMqttConfig.builder()
                .internal(true)
                .useWebsocket(true)
                .passwordFile(PASSWORD_FILE)
                .username(USER)
                .password(USER_PASSWORD_INVALID)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalTcpWithSslAsInvalidUser() {
        return MessageBusMqttConfig.builder()
                .internal(true)
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
                .internal(true)
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
                .internal(true)
                .passwordFile(PASSWORD_FILE)
                .username(USER)
                .password(USER_PASSWORD_VALID)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalWebsocketNoSslAsValidUser() {
        return MessageBusMqttConfig.builder()
                .internal(true)
                .useWebsocket(true)
                .passwordFile(PASSWORD_FILE)
                .username(USER)
                .password(USER_PASSWORD_VALID)
                .build();
    }


    @Override
    protected MessageBusMqttConfig configureInternalTcpWithSslAsValidUser() {
        return MessageBusMqttConfig.builder()
                .internal(true)
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
                .internal(true)
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


    @Override
    protected Void startServer(MessageBusMqttConfig config) {
        return null;
    }


    @Override
    protected void stopServer(Void server) {
        // intentionally left empty
    }

}
