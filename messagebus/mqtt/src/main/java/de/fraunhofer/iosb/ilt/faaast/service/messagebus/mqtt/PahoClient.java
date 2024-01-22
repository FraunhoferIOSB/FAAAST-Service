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
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Objects;
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
    private static final String PROTOCOL_PREFIX_WEBSOCKET_SSL = "wss://";
    private static final String PROTOCOL_PREFIX_WEBSOCKET = "ws://";
    private static final Logger logger = LoggerFactory.getLogger(PahoClient.class);
    private final MessageBusMqttConfig config;
    private MqttClient mqttClient;

    public PahoClient(MessageBusMqttConfig config) {
        this.config = config;
    }


    private String buildEndpoint() {
        int port = config.getPort();
        String protocolPrefix = PROTOCOL_PREFIX;
        if (config.getClientCertificate().getKeyStorePath().isEmpty() && config.getUseWebsocket()) {
            port = config.getWebsocketPort();
            protocolPrefix = PROTOCOL_PREFIX_WEBSOCKET;
        }
        else if (!config.getClientCertificate().getKeyStorePath().isEmpty() && config.getUseWebsocket()) {
            port = config.getSslWebsocketPort();
            protocolPrefix = PROTOCOL_PREFIX_WEBSOCKET_SSL;
        }
        else if (!config.getClientCertificate().getKeyStorePath().isEmpty() && !config.getUseWebsocket()) {
            port = config.getSslPort();
            protocolPrefix = PROTOCOL_PREFIX_SSL;
        }
        return String.format("%s%s:%s", protocolPrefix, config.getHost(), port);
    }


    /**
     * Starts the client connection.
     *
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException if message bus fails to start
     */
    public void start() throws MessageBusException {
        String endpoint = buildEndpoint();
        MqttConnectOptions options = new MqttConnectOptions();
        try {
            if (Objects.nonNull(config.getClientCertificate())
                    && Objects.nonNull(config.getClientCertificate().getKeyStorePath())
                    && !config.getClientCertificate().getKeyStorePath().isEmpty()) {
                options.setSocketFactory(getSSLSocketFactory(config.getClientCertificate()));
            }
        }
        catch (GeneralSecurityException | IOException e) {
            throw new MessageBusException("error setting up SSL for MQTT message bus", e);
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
                    config.getClientId(),
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
        catch (MqttException e) {
            throw new MessageBusException("Failed to connect to MQTT server", e);
        }
    }


    /**
     * Stops the client connection.
     */
    public void stop() {
        if (mqttClient == null) {
            return;
        }
        try {
            if (mqttClient.isConnected()) {
                logger.trace("disconnecting from MQTT broker...");
                mqttClient.disconnect();
                logger.info("disconnected from MQTT broker");
            }
            logger.trace("closing paho-client");
            mqttClient.close(true);
            mqttClient = null;
        }
        catch (MqttException e) {
            logger.debug("MQTT message bus did not stop gracefully", e);
        }
    }


    private SSLSocketFactory getSSLSocketFactory(CertificateConfig certificate) throws GeneralSecurityException, IOException {
        try (InputStream keyStoreInputStream = new FileInputStream(certificate.getKeyStorePath())) {
            KeyStore keystore = KeyStore.getInstance(certificate.getKeyStoreType());
            keystore.load(keyStoreInputStream, certificate.getKeyStorePassword().toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, Objects.nonNull(certificate.getKeyPassword()) ? certificate.getKeyPassword().toCharArray() : new char[0]);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keystore);

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);

            return sslContext.getSocketFactory();
        }
    }


    /**
     * Publishes the message.
     *
     * @param topic the topic to publish on
     * @param content the message to publish
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException if publishing the message fails
     */
    public void publish(String topic, String content) throws MessageBusException {
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
            throw new MessageBusException("publishing message on MQTT message bus failed", e);
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
