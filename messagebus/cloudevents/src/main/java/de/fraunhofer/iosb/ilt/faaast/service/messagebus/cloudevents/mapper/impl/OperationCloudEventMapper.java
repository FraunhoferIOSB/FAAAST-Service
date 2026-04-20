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
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationFinishEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationInvokeEventMessage;
import java.util.List;
import java.util.function.Function;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Mapping FA³ST Operation Invoke/Finish Events to CloudEvents conformant to async-aas specification.
 */
public class OperationCloudEventMapper extends CloudEventMapper {
    private final List<Class<? extends EventMessage>> handleableEventTypes = List.of(
            OperationInvokeEventMessage.class,
            OperationFinishEventMessage.class);

    private final Function<Reference, Referable> referableResolver;

    /**
     * Class constructor.
     *
     * @param config Mapping config.
     * @param referableResolver Supplier attached to the FA³ST persistence layer to get referable information for the
     *            semantic id and data fields
     */
    public OperationCloudEventMapper(CloudEventMapperConfig config, Function<Reference, Referable> referableResolver) {
        super(config);
        this.referableResolver = referableResolver;
    }


    @Override
    protected List<Class<? extends EventMessage>> getSupportedEventTypes() {
        return handleableEventTypes;
    }


    @Override
    protected byte[] getData(EventMessage message) throws JsonProcessingException {
        String serializedData = "{\"inoutputArguments\":{";
        if (message instanceof OperationInvokeEventMessage invoke) {
            serializedData = serializedData.concat(mapper.writeValueAsString(invoke.getInoutput()));
            serializedData = serializedData.concat("},\"inputArguments\":{");
            serializedData = serializedData.concat(mapper.writeValueAsString(invoke.getInput()));
            serializedData = serializedData.concat("}");
        }
        else if (message instanceof OperationFinishEventMessage finish) {
            serializedData = serializedData.concat(mapper.writeValueAsString(finish.getInoutput()));
            serializedData = serializedData.concat("},\"outputArguments\":{");
            serializedData = serializedData.concat(mapper.writeValueAsString(finish.getOutput()));
            serializedData = serializedData.concat("}");
        }

        return serializedData.getBytes();
    }


    @Override
    protected Referable getReferable(EventMessage message) {
        return referableResolver.apply(message.getElement());
    }
}
