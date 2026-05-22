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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mqtt.client;

import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.util.HostnameUtil;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.broker.security.IAuthenticator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public abstract class AbstractPahoClientTest<T extends PahoClient> {

    protected static final String USERNAME = "test-user";
    protected static final String PASSWORD = "test-password";

    private static final Server MQTT_BROKER = new Server();
    private static final int MQTT_BROKER_PORT = PortHelper.findFreePort();
    protected static final String MQTT_BROKER_URL = String.format("tcp://%s:%d", HostnameUtil.LOCALHOST_IP, MQTT_BROKER_PORT);
    private static final String TOPIC = "test-topic";
    private static MqttClient mqttClient;

    @BeforeClass
    public static void init() throws IOException, MqttException {
        mqttClient = new MqttClient(MQTT_BROKER_URL, UUID.randomUUID().toString());

        IConfig config = new MemoryConfig(new Properties());
        Path tempDir = Files.createTempDirectory("moquette-test-");
        config.setProperty(IConfig.DATA_PATH_PROPERTY_NAME, tempDir.toString() + File.separator);
        config.setProperty(IConfig.PERSISTENCE_ENABLED_PROPERTY_NAME, "false");
        config.setProperty(IConfig.PERSISTENT_QUEUE_TYPE_PROPERTY_NAME, "inmemory");

        config.setProperty(IConfig.ALLOW_ANONYMOUS_PROPERTY_NAME, "false");
        config.setProperty(IConfig.PORT_PROPERTY_NAME, String.valueOf(MQTT_BROKER_PORT));

        IAuthenticator authenticator = (clientId, username, password) -> USERNAME.equals(username) && PASSWORD.equals(new String(password));

        MQTT_BROKER.startServer(config, null, null, authenticator, null);

        // Client
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setUserName(USERNAME);
        connectOptions.setPassword(PASSWORD.toCharArray());

        mqttClient.connect(connectOptions);
    }


    @AfterClass
    public static void cleanUp() {
        MQTT_BROKER.stopServer();
    }


    @Test
    public void testPublish() throws MessageBusException, MqttException, InterruptedException {
        String payload = UUID.randomUUID().toString();
        CountDownLatch latch = new CountDownLatch(1);
        var client = getInstance();

        mqttClient.subscribe(TOPIC, (IMqttMessageListener) (s, mqttMessage) -> {
            Assert.assertEquals(Arrays.toString(payload.getBytes()), Arrays.toString(mqttMessage.getPayload()));
            latch.countDown();
        });

        client.prepareConnect();
        client.connect();

        client.publish(TOPIC, payload);

        boolean received = latch.await(5, TimeUnit.SECONDS);
        Assert.assertTrue("Message was not received within timeout", received);

        client.disconnect();
    }


    protected abstract T getInstance();

}
