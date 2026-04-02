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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents;

import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBusConfig;
import java.util.Objects;
import java.util.UUID;


/**
 * Configuration class for {@link MessageBusCloudEvents}.
 */
public class MessageBusCloudEventsConfig extends MessageBusConfig<MessageBusCloudEvents> {

    private static final String DEFAULT_CLIENT_ID = "FA³ST CloudEvents" + UUID.randomUUID();
    private static final String DEFAULT_CLIENT_KEYSTORE_PASSWORD = "";
    private static final String DEFAULT_CLIENT_KEYSTORE_PATH = "";
    private static final String DEFAULT_HOST = "tcp://localhost:1883";
    private static final String DEFAULT_TOPIC_PREFIX = "noauth";
    private static final boolean DEFAULT_SLIM_EVENTS = true;
    private static final String DEFAULT_EVENT_CALLBACK_ADDRESS = "https://localhost";
    private static final String DEFAULT_EVENT_TYPE_PREFIX = "io.admin-shell.events.v1.";
    private static final String DEFAULT_DATA_SCHEMA_PREFIX = "https://api.swaggerhub.com/domains/Plattform_i40/Part1-MetaModel-Schemas/V3.1" +
            ".0#/components/schemas/";

    private String clientId;
    private CertificateConfig clientCertificate;
    private String host;
    private String user;
    private String password;
    private String topicPrefix;
    private boolean slimEvents;
    private String eventCallbackAddress;
    private String eventTypePrefix;
    private String dataSchemaPrefix;
    private String oauth2ClientId;
    private String oauth2ClientSecret;
    private String identityProviderUrl;

    public MessageBusCloudEventsConfig() {
        this.host = DEFAULT_HOST;
        this.clientCertificate = CertificateConfig.builder()
                .keyStorePath(DEFAULT_CLIENT_KEYSTORE_PATH)
                .keyStorePassword(DEFAULT_CLIENT_KEYSTORE_PASSWORD)
                .build();
        this.clientId = DEFAULT_CLIENT_ID;
        this.topicPrefix = DEFAULT_TOPIC_PREFIX;
        this.slimEvents = DEFAULT_SLIM_EVENTS;
        this.eventCallbackAddress = DEFAULT_EVENT_CALLBACK_ADDRESS;
        this.eventTypePrefix = DEFAULT_EVENT_TYPE_PREFIX;
        this.dataSchemaPrefix = DEFAULT_DATA_SCHEMA_PREFIX;
    }


    public String getClientId() {
        return clientId;
    }


    public void setClientId(String clientId) {
        this.clientId = clientId;
    }


    public String getOauth2ClientSecret() {
        return oauth2ClientSecret;
    }


    public void setOauth2ClientSecret(String oauth2ClientSecret) {
        this.oauth2ClientSecret = oauth2ClientSecret;
    }


    public String getOauth2ClientId() {
        return oauth2ClientId;
    }


    public void setOauth2ClientId(String oauth2ClientId) {
        this.oauth2ClientId = oauth2ClientId;
    }


    public String getIdentityProviderUrl() {
        return identityProviderUrl;
    }


    public void setIdentityProviderUrl(String identityProviderUrl) {
        this.identityProviderUrl = identityProviderUrl;
    }


    public String getTopicPrefix() {
        return topicPrefix;
    }


    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }


    public CertificateConfig getClientCertificate() {
        return clientCertificate;
    }


    public void setClientCertificate(CertificateConfig clientCertificate) {
        this.clientCertificate = clientCertificate;
    }


    public String getHost() {
        return host;
    }


    public void setHost(String host) {
        this.host = host;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public String getUser() {
        return user;
    }


    public void setUser(String user) {
        this.user = user;
    }


    public boolean isSlimEvents() {
        return slimEvents;
    }


    public void setSlimEvents(boolean slimEvents) {
        this.slimEvents = slimEvents;
    }


    public String getEventCallbackAddress() {
        return eventCallbackAddress;
    }


    public void setEventCallbackAddress(String eventCallbackAddress) {
        this.eventCallbackAddress = eventCallbackAddress;
    }


    public String getEventTypePrefix() {
        return eventTypePrefix;
    }


    public void setEventTypePrefix(String eventTypePrefix) {
        this.eventTypePrefix = eventTypePrefix;
    }


    public String getDataSchemaPrefix() {
        return dataSchemaPrefix;
    }


    public void setDataSchemaPrefix(String dataSchemaPrefix) {
        this.dataSchemaPrefix = dataSchemaPrefix;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MessageBusCloudEventsConfig other = (MessageBusCloudEventsConfig) o;
        return Objects.equals(host, other.host)
                && Objects.equals(clientCertificate, other.clientCertificate)
                && Objects.equals(password, other.password)
                && Objects.equals(clientId, other.clientId)
                && Objects.equals(topicPrefix, other.topicPrefix)
                && Objects.equals(slimEvents, other.slimEvents)
                && Objects.equals(eventCallbackAddress, other.eventCallbackAddress)
                && Objects.equals(eventTypePrefix, other.eventTypePrefix)
                && Objects.equals(dataSchemaPrefix, other.dataSchemaPrefix);
    }


    @Override
    public int hashCode() {
        return Objects.hash(
                host,
                clientCertificate,
                password,
                clientId,
                topicPrefix,
                slimEvents,
                eventCallbackAddress,
                eventTypePrefix,
                dataSchemaPrefix);
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<MessageBusCloudEventsConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected MessageBusCloudEventsConfig newBuildingInstance() {
            return new MessageBusCloudEventsConfig();
        }

    }

    private abstract static class AbstractBuilder<T extends MessageBusCloudEventsConfig, B extends AbstractBuilder<T, B>>
            extends MessageBusConfig.AbstractBuilder<MessageBusCloudEvents, T, B> {

        public B from(T base) {
            getBuildingInstance().setHost(base.getHost());
            getBuildingInstance().setClientCertificate(base.getClientCertificate());
            getBuildingInstance().setUser(base.getUser());
            getBuildingInstance().setPassword(base.getPassword());
            getBuildingInstance().setClientId(base.getClientId());
            getBuildingInstance().setTopicPrefix(base.getTopicPrefix());
            getBuildingInstance().setSlimEvents(base.isSlimEvents());
            getBuildingInstance().setEventCallbackAddress(base.getEventCallbackAddress());
            getBuildingInstance().setEventTypePrefix(base.getEventTypePrefix());
            getBuildingInstance().setDataSchemaPrefix(base.getDataSchemaPrefix());
            return getSelf();
        }


        public B host(String value) {
            getBuildingInstance().setHost(value);
            return getSelf();
        }


        public B clientCertificate(CertificateConfig value) {
            getBuildingInstance().setClientCertificate(value);
            return getSelf();
        }


        public B user(String value) {
            getBuildingInstance().setUser(value);
            return getSelf();
        }


        public B password(String value) {
            getBuildingInstance().setPassword(value);
            return getSelf();
        }


        public B clientId(String value) {
            getBuildingInstance().setClientId(value);
            return getSelf();
        }


        public B topicPrefix(String value) {
            getBuildingInstance().setTopicPrefix(value);
            return getSelf();
        }


        public B slimEvents(boolean value) {
            getBuildingInstance().setSlimEvents(value);
            return getSelf();
        }


        public B identityProviderUrl(String value) {
            getBuildingInstance().setIdentityProviderUrl(value);
            return getSelf();
        }


        public B oauth2ClientSecret(String value) {
            getBuildingInstance().setOauth2ClientSecret(value);
            return getSelf();
        }


        public B oauth2ClientId(String value) {
            getBuildingInstance().setOauth2ClientId(value);
            return getSelf();
        }


        public B eventCallbackAddress(String value) {
            getBuildingInstance().setEventCallbackAddress(value);
            return getSelf();
        }


        public B eventTypePrefix(String value) {
            getBuildingInstance().setEventTypePrefix(value);
            return getSelf();
        }


        public B dataSchemaPrefix(String value) {
            getBuildingInstance().setDataSchemaPrefix(value);
            return getSelf();
        }

    }
}
