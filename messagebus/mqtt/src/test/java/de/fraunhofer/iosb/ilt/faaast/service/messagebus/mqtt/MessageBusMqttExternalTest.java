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

import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;


public class MessageBusMqttExternalTest extends AbstractMessageBusMqttTest<Server> {

    private static final String LOCALHOST = "127.0.0.1";

    @Override
    protected MessageBusMqttConfig getBaseConfig() {
        return MessageBusMqttConfig.builder()
                .internal(false)
                .host(LOCALHOST)
                .build();
    }


    @Override
    protected Server startServer(MessageBusMqttConfig config) throws IOException {
        Server result = new Server();
        MoquetteAuthenticator authenticator = new MoquetteAuthenticator(config);
        result.startServer(getMqttServerConfig(config), null, null, authenticator, authenticator);
        return result;
    }


    @Override
    protected void stopServer(Server server) {
        server.stopServer();
    }


    private static IConfig getMqttServerConfig(MessageBusMqttConfig config) {
        MemoryConfig result = new MemoryConfig(new Properties());
        result.setProperty(IConfig.PERSISTENCE_ENABLED_PROPERTY_NAME, Boolean.toString(false));
        result.setProperty(IConfig.PORT_PROPERTY_NAME, Integer.toString(config.getPort()));
        result.setProperty(IConfig.HOST_PROPERTY_NAME, LOCALHOST);
        result.setProperty(IConfig.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.toString(config.getUsers().isEmpty()));
        if (config.getUseWebsocket()) {
            result.setProperty(IConfig.WEB_SOCKET_PORT_PROPERTY_NAME, Integer.toString(config.getWebsocketPort()));
        }
        if (Objects.nonNull(config.getServerCertificate())
                && Objects.nonNull(config.getServerCertificate().getKeyStorePath())) {
            result.setProperty(IConfig.SSL_PORT_PROPERTY_NAME, Integer.toString(config.getSslPort()));
            result.setProperty(IConfig.SSL_PROVIDER, "JDK");
            result.setProperty(IConfig.JKS_PATH_PROPERTY_NAME, config.getServerCertificate().getKeyStorePath());
            result.setProperty(IConfig.KEY_STORE_TYPE, config.getServerCertificate().getKeyStoreType());
            if (Objects.nonNull(config.getServerCertificate().getKeyStorePassword())) {
                result.setProperty(IConfig.KEY_STORE_PASSWORD_PROPERTY_NAME, config.getServerCertificate().getKeyStorePassword());
            }
            if (Objects.nonNull(config.getServerCertificate().getKeyPassword())) {
                result.setProperty(IConfig.KEY_MANAGER_PASSWORD_PROPERTY_NAME, config.getServerCertificate().getKeyPassword());
            }
            if (config.getUseWebsocket()) {
                result.setProperty(IConfig.WSS_PORT_PROPERTY_NAME, Integer.toString(config.getSslWebsocketPort()));
            }
        }
        return result;
    }
}
