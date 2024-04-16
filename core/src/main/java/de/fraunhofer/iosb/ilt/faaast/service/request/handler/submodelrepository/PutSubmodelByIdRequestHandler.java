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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.submodelrepository;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.PutSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.PutSubmodelByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValidationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ModelValidator;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.PutSubmodelByIdRequest} in the
 * service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.PutSubmodelByIdResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class PutSubmodelByIdRequestHandler extends AbstractRequestHandler<PutSubmodelByIdRequest, PutSubmodelByIdResponse> {

    public PutSubmodelByIdRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public PutSubmodelByIdResponse process(PutSubmodelByIdRequest request)
            throws ResourceNotFoundException, AssetConnectionException, ValueMappingException, MessageBusException, ValidationException, ResourceNotAContainerElementException {
        ModelValidator.validate(request.getSubmodel(), context.getCoreConfig().getValidationOnUpdate());
        //check if resource does exist
        context.getPersistence().getSubmodel(request.getSubmodel().getId(), QueryModifier.DEFAULT);
        context.getPersistence().save(request.getSubmodel());
        Reference reference = AasUtils.toReference(request.getSubmodel());
        syncWithAsset(reference, request.getSubmodel().getSubmodelElements(), !request.isInternal());
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementUpdateEventMessage.builder()
                    .element(reference)
                    .value(request.getSubmodel())
                    .build());
        }
        return PutSubmodelByIdResponse.builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
    }

}
