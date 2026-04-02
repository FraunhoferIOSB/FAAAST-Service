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

import static org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes.ASSET_ADMINISTRATION_SHELL;
import static org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes.SUBMODEL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationFinishEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationInvokeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonFormat;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.digitaltwin.aas4j.v3.model.HasSemantics;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base class for mapping FA³ST Event types to CloudEvents conformant to async-aas specification.
 */
public abstract class CloudEventMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudEventMapper.class);

    private static final String APPLICATION_JSON = "application/json";
    private static final String SEMANTIC_ID_KEY = "semanticid";

    private final Map<Class<? extends EventMessage>, String> internalToCloudEventMap = Map.of(
            ValueChangeEventMessage.class, "valueChanged",
            ElementCreateEventMessage.class, "created",
            ElementUpdateEventMessage.class, "updated",
            ElementDeleteEventMessage.class, "deleted",
            OperationInvokeEventMessage.class, "invoked",
            OperationFinishEventMessage.class, "finished");
    private final CloudEventMapperConfig config;
    protected final ObjectMapper objectMapper;

    /**
     * Class constructor.
     *
     * @param config Mapping config.
     */
    protected CloudEventMapper(CloudEventMapperConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY)
                .registerModule(JsonFormat.getCloudEventJacksonModule());
    }


    /**
     * Returns whether this mapper can handle a FA³ST event message.
     *
     * @param m The message to test
     * @return True if the mapper can handle the message, else false
     */

    public boolean canHandle(EventMessage m) {
        if (getHandleable().stream().noneMatch(c -> c.isAssignableFrom(m.getClass()))) {
            return false;
        }

        KeyTypes rootType = Optional.ofNullable(ReferenceHelper.getRoot(m.getElement()))
                .orElseThrow()
                .getType();

        return rootType == ASSET_ADMINISTRATION_SHELL || rootType == SUBMODEL;
    }


    /**
     * Map a FA³ST event message to a CloudEvent.
     *
     * @param message The FA³ST event message
     * @return The mapped CloudEvent
     */
    public CloudEvent createCloudEvent(EventMessage message) {
        CloudEventBuilder cloudEventBuilder = createCloudEventBaseBuilder(message);
        appendData(cloudEventBuilder, message);
        cloudEventBuilder.withDataContentType(APPLICATION_JSON); // data content type
        return cloudEventBuilder.build();
    }


    /**
     * Get event classes this concrete implementation can handle.
     *
     * @return The classes this implementation can handle.
     */
    protected abstract List<Class<? extends EventMessage>> getHandleable();


    /**
     * Creates the basis for all cloudevents with the required fields.
     *
     * @param message The FA³ST event message to map into the base cloud event
     * @return The base cloud event builder
     */
    protected CloudEventBuilder createCloudEventBaseBuilder(EventMessage message) {
        CloudEventBuilder builder = CloudEventBuilder
                .v1() // spec version
                .withId(UUID.randomUUID().toString()) // id
                .withSource(getSourceUri(message.getElement())) // source
                .withDataSchema(URI.create(config.dataSchemaPrefix() + getSpecificElementName(message.getElement()))) // dataschema
                .withType(config.eventTypePrefix().concat(getEventType(message.getClass()))) // type
                .withTime(OffsetDateTime.now()); // time

        appendSemanticId(builder, message);

        return builder;
    }


    /**
     * Returns the content of the cloud event's data field, if any.
     *
     * @param message The message to get the data field from
     * @return The content of the data field or null if no data is meant to be sent.
     * @throws JsonProcessingException On serialization of the data.
     */
    protected abstract byte[] getData(EventMessage message) throws JsonProcessingException;


    /**
     * Returns the referable associated with this event message.
     *
     * @param message The message to get the referable from
     * @return The referable or null if no referable is available.
     */
    protected abstract Referable getReferable(EventMessage message);


    private void appendSemanticId(CloudEventBuilder cloudEventBuilder, EventMessage message) {
        Optional.ofNullable(getReferable(message))
                .map(this::getSemanticId)
                .ifPresent(s -> cloudEventBuilder.withExtension(SEMANTIC_ID_KEY, s));
    }


    private String getEventType(Class<? extends EventMessage> messageClass) {
        String eventType = internalToCloudEventMap.get(messageClass);

        if (eventType == null) {
            throw new IllegalArgumentException(String.format("EventMessage type not supported: %s", messageClass));
        }
        return eventType;
    }


    private void appendData(CloudEventBuilder cloudEventBuilder, EventMessage message) {
        if (!config.slimEvents()) {
            try {
                Optional.ofNullable(getData(message)).ifPresent(cloudEventBuilder::withData);
            }
            catch (JsonProcessingException e) {
                LOGGER.warn("{} when trying to write cloud event data field: {}", e.getClass().getName(), e.getMessage());
            }
        }
    }


    private String getSemanticId(Referable referable) {
        if (!(referable instanceof HasSemantics semanticElement) || ReferenceHelper.getRoot(semanticElement.getSemanticId()) == null) {
            return null;
        }
        // If the referable is changed in between the if statement and this one, throw nullpointer
        return Optional.ofNullable(ReferenceHelper.getRoot(semanticElement.getSemanticId()))
                .map(Key::getValue)
                .orElse(null);
    }


    private String getSpecificElementName(Reference reference) {
        KeyTypes effectiveKeyType = Optional.ofNullable(ReferenceHelper.getEffectiveKeyType(reference)).orElseThrow();

        String[] elementNameParts = effectiveKeyType.toString().split("_");
        StringBuilder elementNameBuilder = new StringBuilder();

        for (String elementNamePart: elementNameParts) {
            elementNameBuilder.append(elementNamePart.charAt(0));
            elementNameBuilder.append(elementNamePart.substring(1).toLowerCase());
        }

        return elementNameBuilder.toString();
    }


    private URI getSourceUri(Reference reference) {
        // base
        String uriString = config.eventCallbackAddress().endsWith("/")
                ? config.eventCallbackAddress().substring(0, config.eventCallbackAddress().length() - 1)
                : config.eventCallbackAddress();

        Key root = ReferenceHelper.getRoot(reference);
        if (root == null || root.getValue() == null) {
            throw new IllegalArgumentException(String.format("Event reference malformed: %s", root));
        }

        // identifiable
        uriString = uriString.concat(switch (root.getType()) {
            case ASSET_ADMINISTRATION_SHELL -> "/shells/";
            case SUBMODEL -> "/submodels/";
            default -> throw new IllegalArgumentException(String.format("CloudEvents message bus only supports %s or %s but was %s",
                    ASSET_ADMINISTRATION_SHELL, SUBMODEL, root.getType()));
        })
                .concat(EncodingHelper.base64UrlEncode(root.getValue()));

        // referable
        if (reference.getKeys().size() > 1) {
            // SubmodelElement
            uriString = uriString.concat("/submodel-elements/")
                    .concat(IdShortPath.fromReference(reference).toString());
        }

        return URI.create(String.join("/", uriString));
    }

}
