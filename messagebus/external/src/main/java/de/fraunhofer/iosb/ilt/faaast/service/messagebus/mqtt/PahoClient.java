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
import javax.net.ssl.*;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * the mqtt client to publish to brokers.
 *
 * @author Michael Jacoby
 */
public class PahoClient {

    private static final String PROTOCOL_PREFIX = "tcp://";
    private static final String PROTOCOL_PREFIX_SSL = "ssl://";
    private static final Logger logger = LoggerFactory.getLogger(MqttClient.class);
    private MqttClient mqttClient;
    private final MessageBusMqttConfig messageBusMqttConfig;

    public PahoClient(MessageBusMqttConfig messageBusMqttConfig) {
        this.messageBusMqttConfig = messageBusMqttConfig;
    }


    /**
     * starts the client connection.
     */
    public void start() {
        mqttConnect();
    }


    /**
     * stops the client connection.
     */
    public void stop() {
        try {
            mqttDisconnect();
        }
        catch (MqttException ex) {
            logger.debug("error disconnecting MQTT", ex);
        }
    }


    private void mqttConnect() {
        String endpoint;
        if (messageBusMqttConfig.getClientKeystorePath().isEmpty()) {
            endpoint = messageBusMqttConfig.getHost() + ":" + messageBusMqttConfig.getPort();
            if (!endpoint.startsWith(PROTOCOL_PREFIX)) {
                endpoint = PROTOCOL_PREFIX + endpoint;
            }
        }
        else {
            endpoint = messageBusMqttConfig.getHost() + ":" + messageBusMqttConfig.getSslPort();
            if (!endpoint.startsWith(PROTOCOL_PREFIX_SSL)) {
                endpoint = PROTOCOL_PREFIX_SSL + endpoint;
            }
        }
        String clientId = "FA³ST MQTT MessageBus Client " + UUID.randomUUID().toString().replace("-", "");
        clientId = clientId.substring(0, 20); // MQTTv2 limited to 23 characters
        MqttConnectOptions options = new MqttConnectOptions();
        SSLSocketFactory ssl = getSSLSocketFactory(messageBusMqttConfig.getClientKeystorePath(), messageBusMqttConfig.getClientKeystorePass());
        if (!Objects.isNull(ssl)) {
            options.setSocketFactory(ssl);
        }
        if (!Objects.isNull(messageBusMqttConfig.getUsername())) {
            options.setUserName(messageBusMqttConfig.getUsername());
            options.setPassword(messageBusMqttConfig.getPassword() != null
                    ? messageBusMqttConfig.getPassword().toCharArray()
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
                    logger.info("MQTT MessageBus Client connected to broker.");

                }

            });
            logger.trace("connecting to MQTT broker: {}", endpoint);
            mqttClient.connect(options);
            logger.info("connected to MQTT broker: {}", endpoint);
        }
        catch (Exception ex) {
            logger.error("failed to connect to MQTT broker", ex);
        }
    }


    private SSLSocketFactory getSSLSocketFactory(String keyStorePath, String password) {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            InputStream jksInputStream = new FileInputStream(keyStorePath);
            ks.load(jksInputStream, password.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            SSLContext sc = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = tmf.getTrustManagers();
            sc.init(kmf.getKeyManagers(), trustManagers, null);

            SSLSocketFactory ssf = sc.getSocketFactory();
            return ssf;
        }
        catch (Exception e) {
            logger.error("MqttMessagebus SSL init error.");
            return null;
        }

    }


    private void mqttDisconnect() throws MqttException {
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


    /**
     * publishes the message.
     *
     * @param topic
     * @param content
     */
    public void publish(String topic, String content) {
        if (!mqttClient.isConnected()) {
            logger.warn("received data but MQTT connection is closed, trying to connect...");
            mqttConnect();
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
     * subscribe to a mqtt topic.
     *
     * @param topic
     * @param listener
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
     * unsubscribe from a mqtt topic.
     *
     * @param topic
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
