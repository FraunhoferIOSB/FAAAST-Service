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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.PostSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.PostSubmodelResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValidationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ModelValidator;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.PostSubmodelRequest} in the service
 * and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.PostSubmodelResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class PostSubmodelRequestHandler extends AbstractRequestHandler<PostSubmodelRequest, PostSubmodelResponse> {

    public PostSubmodelRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public PostSubmodelResponse process(PostSubmodelRequest request)
            throws ResourceNotFoundException, AssetConnectionException, ValueMappingException, ValidationException, ResourceNotAContainerElementException, MessageBusException,
            ResourceAlreadyExistsException {
        ModelValidator.validate(request.getSubmodel(), context.getCoreConfig().getValidationOnCreate());
        if (context.getPersistence().submodelExists(request.getSubmodel().getId())) {
            throw new ResourceAlreadyExistsException(request.getSubmodel().getId(), Submodel.class);
        }
        context.getPersistence().save(request.getSubmodel());
        Reference reference = AasUtils.toReference(request.getSubmodel());
        syncWithAsset(reference, request.getSubmodel().getSubmodelElements(), !request.isInternal());
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementCreateEventMessage.builder()
                    .element(reference)
                    .value(request.getSubmodel())
                    .build());
        }
        return PostSubmodelResponse.builder()
                .payload(request.getSubmodel())
                .created()
                .build();
    }

}
