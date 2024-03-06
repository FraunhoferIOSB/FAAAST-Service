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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.DeleteConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.DeleteConceptDescriptionByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.DeleteConceptDescriptionByIdRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.DeleteConceptDescriptionByIdResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class DeleteConceptDescriptionByIdRequestHandler extends AbstractRequestHandler<DeleteConceptDescriptionByIdRequest, DeleteConceptDescriptionByIdResponse> {

    public DeleteConceptDescriptionByIdRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public DeleteConceptDescriptionByIdResponse process(DeleteConceptDescriptionByIdRequest request) throws ResourceNotFoundException, MessageBusException {
        DeleteConceptDescriptionByIdResponse response = new DeleteConceptDescriptionByIdResponse();
        ConceptDescription conceptDescription = context.getPersistence().getConceptDescription(request.getId(), QueryModifier.DEFAULT);
        context.getPersistence().deleteConceptDescription(request.getId());
        response.setStatusCode(StatusCode.SUCCESS_NO_CONTENT);
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementDeleteEventMessage.builder()
                    .element(conceptDescription)
                    .value(conceptDescription)
                    .build());
        }
        return response;
    }

}
