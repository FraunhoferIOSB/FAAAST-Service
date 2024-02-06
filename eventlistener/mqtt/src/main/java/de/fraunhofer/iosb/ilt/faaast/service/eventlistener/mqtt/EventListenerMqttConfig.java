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
package de.fraunhofer.iosb.ilt.faaast.service.eventlistener.mqtt;

import de.fraunhofer.iosb.ilt.faaast.service.eventlistener.EventListenerConfig;


/**
 * Configuration class for {@link EventListenerMqtt}.
 */
public class EventListenerMqttConfig extends EventListenerConfig<EventListenerMqtt> {

    public String getHost() {
        return host;
    }


    public void setHost(String host) {
        this.host = host;
    }

    private String host;

    public String getRule() {
        return rule;
    }


    public void setRule(String rule) {
        this.rule = rule;
    }

    private String rule;

    public static Builder builder() {
        return new Builder();
    }

    private abstract static class AbstractBuilder<T extends EventListenerMqttConfig, B extends AbstractBuilder<T, B>>
            extends EventListenerConfig.AbstractBuilder<EventListenerMqtt, T, B> {

        public B host(String value) {
            getBuildingInstance().setHost(value);
            return getSelf();
        }


        public B rule(String value) {
            getBuildingInstance().setRule(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<EventListenerMqttConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected EventListenerMqttConfig newBuildingInstance() {
            return new EventListenerMqttConfig();
        }
    }
}
