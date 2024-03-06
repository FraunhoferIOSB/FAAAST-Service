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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.PostConceptDescriptionRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.PostConceptDescriptionResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ModelValidator;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.PostConceptDescriptionRequest} in
 * the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.PostConceptDescriptionResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class PostConceptDescriptionRequestHandler extends AbstractRequestHandler<PostConceptDescriptionRequest, PostConceptDescriptionResponse> {

    public PostConceptDescriptionRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public PostConceptDescriptionResponse process(PostConceptDescriptionRequest request) throws Exception {
        ModelValidator.validate(request.getConceptDescription(), context.getCoreConfig().getValidationOnCreate());
        if (context.getPersistence().conceptDescriptionExists(request.getConceptDescription().getId())) {
            throw new ResourceAlreadyExistsException(request.getConceptDescription().getId(), ConceptDescription.class);
        }
        context.getPersistence().save(request.getConceptDescription());
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementCreateEventMessage.builder()
                    .element(request.getConceptDescription())
                    .value(request.getConceptDescription())
                    .build());
        }
        return PostConceptDescriptionResponse.builder()
                .payload(request.getConceptDescription())
                .created()
                .build();
    }

}
