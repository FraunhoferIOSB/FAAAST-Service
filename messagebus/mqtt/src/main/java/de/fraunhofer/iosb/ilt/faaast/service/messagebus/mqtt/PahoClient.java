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

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Objects;
import java.util.UUID;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wrapper for Eclipse Paho MQTT client.
 */
public class PahoClient {

    private static final String PROTOCOL_PREFIX = "tcp://";
    private static final String PROTOCOL_PREFIX_SSL = "ssl://";
    private static final String PROTOCOL_PREFIX_WSS = "wss://";
    private static final String PROTOCOL_PREFIX_WS = "ws://";
    private static final Logger logger = LoggerFactory.getLogger(PahoClient.class);
    private final MessageBusMqttConfig config;
    private MqttClient mqttClient;

    public PahoClient(MessageBusMqttConfig config) {
        this.config = config;
    }


    /**
     * Starts the client connection.
     */
    public void start() {
        String endpoint;
        if (config.getClientKeystorePath().isEmpty()) {
            if (config.getUseWebsocket()) {
                endpoint = config.getHost() + ":" + config.getWebsocketPort();
                if (!endpoint.startsWith(PROTOCOL_PREFIX_WS)) {
                    endpoint = PROTOCOL_PREFIX_WS + endpoint;
                }
            }
            else {
                endpoint = config.getHost() + ":" + config.getPort();
                if (!endpoint.startsWith(PROTOCOL_PREFIX)) {
                    endpoint = PROTOCOL_PREFIX + endpoint;
                }
            }
        }
        else {
            if (config.getUseWebsocket()) {
                endpoint = config.getHost() + ":" + config.getSslWebsocketPort();
                if (!endpoint.startsWith(PROTOCOL_PREFIX_WSS)) {
                    endpoint = PROTOCOL_PREFIX_WSS + endpoint;
                }
            }
            else {
                endpoint = config.getHost() + ":" + config.getSslPort();
                if (!endpoint.startsWith(PROTOCOL_PREFIX_SSL)) {
                    endpoint = PROTOCOL_PREFIX_SSL + endpoint;
                }
            }
        }
        String clientId = "FA³ST MQTT " + UUID.randomUUID().toString().replace("-", "");
        clientId = clientId.substring(0, 20); // MQTTv2 limited to 23 characters
        MqttConnectOptions options = new MqttConnectOptions();
        SSLSocketFactory ssl = getSSLSocketFactory(config.getClientKeystorePath(), config.getClientKeystorePassword());
        if (!Objects.isNull(ssl)) {
            options.setSocketFactory(ssl);
        }
        if (!Objects.isNull(config.getUsername())) {
            options.setUserName(config.getUsername());
            options.setPassword(config.getPassword() != null
                    ? config.getPassword().toCharArray()
                    : new char[0]);
        }
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        try {
            mqttClient = new MqttClient(
                    endpoint,
                    clientId,
                    new MemoryPersistence());
            mqttClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectionLost(Throwable throwable) {
                    logger.warn("MQTT message bus connection lost");
                }


                @Override
                public void deliveryComplete(IMqttDeliveryToken imdt) {
                    // intentionally left empty
                }


                @Override
                public void messageArrived(String string, MqttMessage mm) throws Exception {
                    // intentionally left empty

                }


                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    logger.debug("MQTT MessageBus Client connected to broker.");

                }

            });
            logger.trace("connecting to MQTT broker: {}", endpoint);
            mqttClient.connect(options);
            logger.debug("connected to MQTT broker: {}", endpoint);
        }
        catch (Exception ex) {
            logger.error("failed to connect to MQTT broker", ex);
        }
    }


    /**
     * Stops the client connection.
     */
    public void stop() {
        try {
            if (mqttClient == null) {
                return;
            }
            if (mqttClient.isConnected()) {
                logger.trace("disconnecting from MQTT broker...");
                mqttClient.disconnect();
                logger.info("disconnected from MQTT broker");
            }
            try {
                logger.trace("closing paho-client");
                mqttClient.close(true);
            }
            catch (MqttException ex) {
                logger.error("exception closing MQTT client.", ex);
            }
            mqttClient = null;
        }
        catch (MqttException ex) {
            logger.debug("error disconnecting MQTT", ex);
        }
    }


    private SSLSocketFactory getSSLSocketFactory(String keyStorePath, String password) {
        try (InputStream jksInputStream = new FileInputStream(keyStorePath)) {
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(jksInputStream, password.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, password.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keystore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);

            return sslContext.getSocketFactory();
        }
        catch (Exception e) {
            logger.error("MqttMessagebus SSL init error.");
            return null;
        }
    }


    /**
     * Publishes the message.
     *
     * @param topic the topic to publish on
     * @param content the message to publish
     */
    public void publish(String topic, String content) {
        if (!mqttClient.isConnected()) {
            logger.debug("received data but MQTT connection is closed, trying to connect...");
            start();
        }
        MqttMessage msg = new MqttMessage(content.getBytes());
        try {
            mqttClient.publish(topic, msg);
            logger.info("message published - topic: {}, data: {}", topic, content);
        }
        catch (MqttException e) {
            logger.warn("publishing mqtt message failed.");
        }
    }


    /**
     * Subscribe to a mqtt topic.
     *
     * @param topic the topic to subscribe to
     * @param listener the callback listener
     */
    public void subscribe(String topic, IMqttMessageListener listener) {
        try {
            mqttClient.subscribe(topic, listener);
        }
        catch (MqttException e) {
            logger.error(e.getMessage());
        }
    }


    /**
     * Unsubscribe from a mqtt topic.
     *
     * @param topic the topic to unsubscribe from
     */
    public void unsubscribe(String topic) {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.unsubscribe(topic);
            }
            catch (MqttException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
