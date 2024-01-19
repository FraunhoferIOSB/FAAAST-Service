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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.conceptdescription;

import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.GetConceptDescriptionByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetConceptDescriptionByIdRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.GetConceptDescriptionByIdResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetConceptDescriptionByIdRequestHandler extends AbstractRequestHandler<GetConceptDescriptionByIdRequest, GetConceptDescriptionByIdResponse> {

    public GetConceptDescriptionByIdRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public GetConceptDescriptionByIdResponse process(GetConceptDescriptionByIdRequest request) throws ResourceNotFoundException, MessageBusException {
        ConceptDescription conceptDescription = context.getPersistence().getConceptDescription(request.getId(), request.getOutputModifier());
        if (!request.isInternal() && Objects.nonNull(conceptDescription)) {
            context.getMessageBus().publish(ElementReadEventMessage.builder()
                    .element(conceptDescription)
                    .value(conceptDescription)
                    .build());
        }
        return GetConceptDescriptionByIdResponse.builder()
                .payload(conceptDescription)
                .success()
                .build();
    }
}
