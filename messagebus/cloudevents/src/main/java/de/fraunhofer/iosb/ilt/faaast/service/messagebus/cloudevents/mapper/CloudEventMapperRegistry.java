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

import com.fasterxml.jackson.core.JsonProcessingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import io.cloudevents.CloudEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * A registry for different cloud event mappers.
 */
public class CloudEventMapperRegistry {

    private final List<CloudEventMapper> mappers;

    public CloudEventMapperRegistry() {
        this.mappers = new ArrayList<>();
    }


    /**
     * Register a CloudEventMapper to the registry.
     *
     * @param mapper The mapper to register
     */
    public void register(CloudEventMapper mapper) {
        this.mappers.add(mapper);
    }


    /**
     * Returns whether a registered mapper can handle this FA³ST event message.
     *
     * @param m The message to test
     * @return True if any mapper can handle the message, else false
     */
    public boolean canHandle(EventMessage m) {
        return mappers.stream().anyMatch(mapper -> mapper.canHandle(m));
    }


    /**
     * Map a FA³ST event message to a CloudEvent.
     *
     * @param message The FA³ST event message
     * @return The mapped CloudEvent
     * @throws JsonProcessingException Mapping AAS referable to JSON failed
     * @throws NoSuchElementException No mapper can handle this EventMessage type
     */
    public CloudEvent createCloudEvent(EventMessage message) throws JsonProcessingException {
        return mappers.stream()
                .filter(mapper -> mapper.canHandle(message))
                .findFirst().orElseThrow()
                .createCloudEvent(message);
    }
}
