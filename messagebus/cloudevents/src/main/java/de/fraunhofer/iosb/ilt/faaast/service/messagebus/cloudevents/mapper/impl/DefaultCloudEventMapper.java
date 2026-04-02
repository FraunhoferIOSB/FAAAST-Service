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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.CloudEventMapper;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.CloudEventMapperConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationFinishEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationInvokeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import java.util.List;


/**
 * Mapping FA³ST Events to CloudEvents conformant to async-aas specification.
 */
public class DefaultCloudEventMapper extends CloudEventMapper {

    private final List<Class<? extends EventMessage>> handleableEventTypes = List.of(
            ValueChangeEventMessage.class,
            ElementCreateEventMessage.class,
            ElementUpdateEventMessage.class,
            OperationInvokeEventMessage.class,
            OperationFinishEventMessage.class);

    /**
     * Class constructor.
     *
     * @param config Mapping configuration
     * @param objectMapper AAS referable to JSON mapper
     */
    public DefaultCloudEventMapper(CloudEventMapperConfig config, ObjectMapper objectMapper) {
        super(config, objectMapper);
    }


    @Override
    protected List<Class<? extends EventMessage>> getHandleable() {
        return handleableEventTypes;
    }
}
