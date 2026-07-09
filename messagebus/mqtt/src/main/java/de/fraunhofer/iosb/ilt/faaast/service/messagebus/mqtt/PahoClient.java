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
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
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

    private final BlockingQueue<MqttEvent> queue = new java.util.concurrent.LinkedBlockingQueue<>();
    private final ConcurrentMap<String, CopyOnWriteArrayList<BiConsumer<String, MqttMessage>>> handlersByTopic = new ConcurrentHashMap<>();
    private ExecutorService messageHandlerExecutor;

    public PahoClient(MessageBusMqttConfig config) {
        this.config = config;
    }


    private String buildEndpoint() {
        int port = config.getPort();
        String protocolPrefix = PROTOCOL_PREFIX;
        boolean hasKeyStore = Objects.nonNull(config.getClientCertificate()) && !StringHelper.isBlank(config.getClientCertificate().getKeyStorePath());
        if (!hasKeyStore && config.getUseWebsocket()) {
            port = config.getWebsocketPort();
            protocolPrefix = PROTOCOL_PREFIX_WEBSOCKET;
        }
        else if (hasKeyStore && config.getUseWebsocket()) {
            port = config.getSslWebsocketPort();
            protocolPrefix = PROTOCOL_PREFIX_WEBSOCKET_SSL;
        }
        else if (hasKeyStore && !config.getUseWebsocket()) {
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
                    && !StringHelper.isEmpty(config.getClientCertificate().getKeyStorePath())) {
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
                    // all message handling is done via per-topic listeners that enqueue into 'queue'
                }


                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    logger.debug("MQTT MessageBus Client connected to broker.");
                }

            });
            logger.trace("connecting to MQTT broker: {}", endpoint);
            mqttClient.connect(options);
            logger.debug("connected to MQTT broker: {}", endpoint);
            startDispatch();
        }
        catch (MqttException e) {
            throw new MessageBusException("Failed to connect to MQTT server", e);
        }
    }


    private void startDispatch() {
        if (messageHandlerExecutor == null || messageHandlerExecutor.isShutdown()) {
            int threads = 1;
            messageHandlerExecutor = Executors.newFixedThreadPool(threads);
            for (int i = 0; i < threads; i++) {
                messageHandlerExecutor.submit(() -> {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            MqttEvent event = queue.take();
                            dispatch(event);
                        }
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }
    }


    /**
     * Stops the client connection.
     */
    public void stop() {
        if (messageHandlerExecutor != null) {
            messageHandlerExecutor.shutdownNow();
            messageHandlerExecutor = null;
        }
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
        handlersByTopic.clear();
        queue.clear();
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
        if (mqttClient == null || !mqttClient.isConnected()) {
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


    private void dispatch(MqttEvent event) {
        List<BiConsumer<String, MqttMessage>> listeners = handlersByTopic.get(event.topic);
        if (listeners == null || listeners.isEmpty()) {
            return;
        }
        for (var listener: listeners) {
            try {
                listener.accept(event.topic, event.message);
            }
            catch (Exception ex) {
                logger.warn("Error in MQTT handler for topic {}: {}", event.topic, ex.getMessage(), ex);
            }
        }
    }


    /**
     * Subscribe to a mqtt topic.
     * Multiple handlers per topic are supported. Only ONE underlying Paho
     * subscription per topic is created; messages are put into a queue and
     * processed by worker threads which call all registered handlers.
     *
     * @param topic the topic to subscribe to
     * @param listener the callback listener
     */
    public void subscribe(String topic, BiConsumer<String, MqttMessage> listener) {
        if (topic == null || listener == null) {
            return;
        }
        if (mqttClient == null || !mqttClient.isConnected()) {
            logger.error("MQTT client not started or not connected; cannot subscribe to {}", topic);
        }
        // synchronize to avoid race when subscribing same topic concurrent
        synchronized (handlersByTopic) {
            List<BiConsumer<String, MqttMessage>> handlers = handlersByTopic.computeIfAbsent(topic, x -> new CopyOnWriteArrayList<>());
            handlers.add(listener);
            if (handlers.size() == 1) {
                try {
                    mqttClient.subscribe(topic, (t, message) -> {
                        try {
                            queue.put(new MqttEvent(t, message));
                        }
                        catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            logger.error("Interrupted while enqueuing MQTT message", e);
                        }
                    });
                    logger.debug("Subscribed to topic: {}", topic);
                }
                catch (MqttException e) {
                    logger.error("Error subscribing to topic {}: {}", topic, e.getMessage(), e);
                }
            }
        }
    }


    /**
     * Unsubscribe a specific listener from a mqtt topic.
     * If the last listener is removed, the underlying Paho subscription
     * is also removed.
     *
     * @param topic the topic to unsubscribe from
     * @param listener the listener to remove
     */
    public void unsubscribe(String topic, BiConsumer<String, MqttMessage> listener) {
        if (topic == null || listener == null) {
            return;
        }
        synchronized (handlersByTopic) {
            List<BiConsumer<String, MqttMessage>> list = handlersByTopic.get(topic);
            if (list == null) {
                return;
            }
            list.remove(listener);
            if (list.isEmpty()) {
                handlersByTopic.remove(topic);
                if (mqttClient != null && mqttClient.isConnected()) {
                    try {
                        mqttClient.unsubscribe(topic);
                        logger.debug("Unsubscribed (Paho) from topic: {}", topic);
                    }
                    catch (MqttException e) {
                        logger.error("Error unsubscribing from topic {}: {}", topic, e.getMessage(), e);
                    }
                }
            }
        }
    }


    /**
     * Unsubscribe all listeners from a mqtt topic.
     *
     * @param topic the topic to unsubscribe from
     */
    public void unsubscribe(String topic) {
        if (topic == null) {
            return;
        }
        synchronized (handlersByTopic) {
            handlersByTopic.remove(topic);
        }
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.unsubscribe(topic);
                logger.debug("Unsubscribed (Paho) from topic (all listeners): {}", topic);
            }
            catch (MqttException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private static class MqttEvent {
        final String topic;
        final MqttMessage message;

        MqttEvent(String topic, MqttMessage message) {
            this.topic = topic;
            this.message = message;
        }
    }
}
