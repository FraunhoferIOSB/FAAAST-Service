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

import com.fasterxml.jackson.core.JsonProcessingException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.CloudEventMapper;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.CloudEventMapperConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;


/**
 * Mapping FA³ST Events to CloudEvents conformant to async-aas specification.
 */
public class DefaultCloudEventMapper extends CloudEventMapper {

    private final List<Class<? extends EventMessage>> handleableEventTypes = List.of(
            ElementCreateEventMessage.class,
            ElementUpdateEventMessage.class);

    /**
     * Class constructor.
     *
     * @param config Mapping configuration
     */
    public DefaultCloudEventMapper(CloudEventMapperConfig config) {
        super(config);
    }


    @Override
    protected List<Class<? extends EventMessage>> getSupportedEventTypes() {
        return handleableEventTypes;
    }


    @Override
    protected byte[] getData(EventMessage message) throws JsonProcessingException {
        ElementChangeEventMessage m = (ElementChangeEventMessage) message;
        return mapper.writeValueAsBytes(m.getValue());
    }


    @Override
    protected Referable getReferable(EventMessage message) {
        return ((ElementChangeEventMessage) message).getValue();
    }
}
