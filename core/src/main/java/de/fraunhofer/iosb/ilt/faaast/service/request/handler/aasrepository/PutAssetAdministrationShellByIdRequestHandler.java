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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.aasrepository;

import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.PutAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.PutAssetAdministrationShellByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValidationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ModelValidator;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.PutAssetAdministrationShellByIdRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.PutAssetAdministrationShellByIdResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class PutAssetAdministrationShellByIdRequestHandler extends AbstractRequestHandler<PutAssetAdministrationShellByIdRequest, PutAssetAdministrationShellByIdResponse> {

    public PutAssetAdministrationShellByIdRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public PutAssetAdministrationShellByIdResponse process(PutAssetAdministrationShellByIdRequest request)
            throws ResourceNotFoundException, MessageBusException, ValidationException {
        ModelValidator.validate(request.getAas(), context.getCoreConfig().getValidationOnUpdate());
        context.getPersistence().getAssetAdministrationShell(request.getAas().getId(), QueryModifier.DEFAULT);
        context.getPersistence().save(request.getAas());
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementUpdateEventMessage.builder()
                    .element(request.getAas())
                    .value(request.getAas())
                    .build());
        }
        return PutAssetAdministrationShellByIdResponse.builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
    }

}
