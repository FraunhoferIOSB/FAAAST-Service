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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper;

import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.MessageBusCloudEventsConfig;


/**
 * Configuration for a FA³ST event message to CloudEvent mapper.
 *
 * @param callbackAddress Callback address for subscribers of the CloudEvents
 * @param dataSchemaPrefix Data schema prefix used to assign semantic information to data elements
 * @param eventTypePrefix Prefix for event types
 * @param slimEvents If true, the data field in CloudEvents will be empty
 */
public record CloudEventMapperConfig(String callbackAddress, String dataSchemaPrefix, String eventTypePrefix, boolean slimEvents) {

    /**
     * Build a CloudEventMapperConfig from MessageBusCloudEventsConfig and a referable supplier.
     *
     * @param messageBusConfig The MessageBusCloudEventsConfig
     * @param callbackAddress The callback address for event subscribers.
     * @return The CloudEventMapperConfig
     */
    public static CloudEventMapperConfig from(MessageBusCloudEventsConfig messageBusConfig, String callbackAddress) {
        return new CloudEventMapperConfig(callbackAddress,
                messageBusConfig.getDataSchemaPrefix(),
                messageBusConfig.getEventTypePrefix(),
                messageBusConfig.isSlimEvents());
    }
}
