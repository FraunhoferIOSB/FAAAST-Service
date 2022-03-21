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
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GenerateSerializationByIdsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GenerateSerializationByIdsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;


/**
 * Not supported yet! Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.GenerateSerializationByIdsRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GenerateSerializationByIdsResponse}.
 * Is responsible for communication with the persistence and sends the
 * corresponding events to the message bus.
 */
public class GenerateSerializationByIdsRequestHandler extends RequestHandler<GenerateSerializationByIdsRequest, GenerateSerializationByIdsResponse> {

    public GenerateSerializationByIdsRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public GenerateSerializationByIdsResponse process(GenerateSerializationByIdsRequest request) {
        GenerateSerializationByIdsResponse response = new GenerateSerializationByIdsResponse();
        response.setContentType(request.getSerializationFormat().getContentType());
        //TODO implement Serialization
        // this requires uniform interface for de-/serializers, which can only be detected through reflection because otherwise we will have circular dependencies
        response.setStatusCode(StatusCode.SERVER_INTERNAL_ERROR);
        return response;
    }
}
