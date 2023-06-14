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

import io.moquette.broker.security.IAuthenticator;
import io.moquette.broker.security.IAuthorizatorPolicy;
import io.moquette.broker.subscriptions.Topic;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


class MoquetteAuthenticator implements IAuthenticator, IAuthorizatorPolicy {

    private final MessageBusMqttConfig config;

    MoquetteAuthenticator(MessageBusMqttConfig config) {
        this.config = config;
    }


    @Override
    public boolean canRead(Topic topic, String user, String client) {
        return true;
    }


    @Override
    public boolean canWrite(Topic topic, String user, String client) {
        return Objects.equals(client, config.getClientId());
    }


    @Override
    public boolean checkValid(String clientId, String username, byte[] password) {
        return config.getUsers().isEmpty()
                || Objects.equals(config.getUsers().get(username), new String(password, StandardCharsets.UTF_8));
    }

}
