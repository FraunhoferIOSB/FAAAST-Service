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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.content.ContentFormat;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;


/**
 * * Config file for MQTT-based
 * {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider}.
 */
public class MqttValueProviderConfig implements AssetValueProviderConfig {

    private ContentFormat contentFormat;
    private String topic;
    private String query;

    public MqttValueProviderConfig() {
        this.contentFormat = ContentFormat.DEFAULT;
    }


    public ContentFormat getContentFormat() {
        return contentFormat;
    }


    public void setContentFormat(ContentFormat contentFormat) {
        this.contentFormat = contentFormat;
    }


    public String getTopic() {
        return topic;
    }


    public void setTopic(String topic) {
        this.topic = topic;
    }


    public String getQuery() {
        return query;
    }


    public void setQuery(String query) {
        this.query = query;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static abstract class AbstractBuilder<T extends MqttValueProviderConfig, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B query(String value) {
            getBuildingInstance().setQuery(value);
            return getSelf();
        }


        public B topic(String value) {
            getBuildingInstance().setTopic(value);
            return getSelf();
        }


        public B contentFormat(ContentFormat value) {
            getBuildingInstance().setContentFormat(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<MqttValueProviderConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected MqttValueProviderConfig newBuildingInstance() {
            return new MqttValueProviderConfig();
        }
    }
}
