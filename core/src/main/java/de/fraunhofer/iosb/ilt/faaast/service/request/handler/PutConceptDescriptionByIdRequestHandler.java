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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PutConceptDescriptionByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PutConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;


/**
 * Class to handle a {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.PutConceptDescriptionByIdRequest} in the
 * service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PutConceptDescriptionByIdResponse}. Is responsible
 * for communication with the persistence and sends the corresponding events to the message bus.
 */
public class PutConceptDescriptionByIdRequestHandler extends AbstractRequestHandler<PutConceptDescriptionByIdRequest, PutConceptDescriptionByIdResponse> {

    public PutConceptDescriptionByIdRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public PutConceptDescriptionByIdResponse process(PutConceptDescriptionByIdRequest request) throws ResourceNotFoundException, MessageBusException {
        persistence.get(request.getConceptDescription().getIdentification(), QueryModifier.DEFAULT, ConceptDescription.class);
        ConceptDescription conceptDescription = persistence.put(request.getConceptDescription());
        messageBus.publish(ElementUpdateEventMessage.builder()
                .element(conceptDescription)
                .value(conceptDescription)
                .build());
        return PutConceptDescriptionByIdResponse.builder()
                .payload(conceptDescription)
                .success()
                .build();
    }

}
