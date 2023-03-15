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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The mqtt server based on moquette to publish message bus events.
 *
 * @author jab
 */
public class MoquetteServer {
    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MoquetteServer.class);

    private Server mqttBroker;

    /**
     * The MQTT Id used by FA³ST service to connect to the MQTT broker.
     */
    private final String fastClientId;
    private MessageBusMqttConfig messageBusMqttConfig;

    public MoquetteServer(MessageBusMqttConfig config) {
        fastClientId = "FA³ST MQTT Server (" + UUID.randomUUID() + ")";
        this.messageBusMqttConfig = config;
    }


    /**
     * publish the message.
     *
     * @param topic
     * @param message
     * @param qos
     */
    public void publish(String topic, String message, int qos) {
        if (mqttBroker != null) {
            final ByteBuf payload = ByteBufUtil.writeUtf8(UnpooledByteBufAllocator.DEFAULT, message);
            MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.valueOf(qos), false, 0);
            MqttPublishVariableHeader varHeader = new MqttPublishVariableHeader(topic, 0);
            MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(fixedHeader, varHeader, payload);
            mqttBroker.internalPublish(mqttPublishMessage, fastClientId);
        }
    }


    /**
     * starts the broker.
     */
    public void start() {
        mqttBroker = new Server();
        IConfig config = new MemoryConfig(new Properties());
        // Ensure the immediate_flush property has a default of true.
        config.setProperty(BrokerConstants.IMMEDIATE_BUFFER_FLUSH_PROPERTY_NAME, "true");
        config.setProperty(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(messageBusMqttConfig.getPort()));
        config.setProperty(BrokerConstants.HOST_PROPERTY_NAME, messageBusMqttConfig.getHost());
        config.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.TRUE.toString());
        config.setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, Integer.toString(messageBusMqttConfig.getWebsocketPort()));
        //String keystorePath = messageBusMqttConfig.getKeystorePath();
        String keystorePath = "";
        if (!keystorePath.isEmpty()) {
            LOGGER.info("Configuring keystore for ssl");
            config.setProperty(BrokerConstants.JKS_PATH_PROPERTY_NAME, keystorePath);
            config.setProperty(BrokerConstants.KEY_STORE_PASSWORD_PROPERTY_NAME, messageBusMqttConfig.getKeystorePass());
            config.setProperty(BrokerConstants.KEY_MANAGER_PASSWORD_PROPERTY_NAME, messageBusMqttConfig.getKeymanagerPass());
            config.setProperty(BrokerConstants.SSL_PORT_PROPERTY_NAME, Integer.toString(messageBusMqttConfig.getSslPort()));
            config.setProperty(BrokerConstants.WSS_PORT_PROPERTY_NAME, Integer.toString(messageBusMqttConfig.getSslWebsocketPort()));
        }
        try {
            mqttBroker.startServer(config);
        }
        catch (IOException e) {
            LOGGER.warn("MQTT MessageBus could not be started.");
        }
    }


    /**
     * stops the broker.
     */
    public void stop() {
        if (mqttBroker != null) {
            mqttBroker.stopServer();
        }
    }
}
