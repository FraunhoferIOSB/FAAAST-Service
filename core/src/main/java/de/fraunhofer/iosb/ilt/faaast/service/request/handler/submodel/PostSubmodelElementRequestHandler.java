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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.submodel;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PostSubmodelElementRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.PostSubmodelElementResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValidationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ModelValidator;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Class to handle a {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PostSubmodelElementRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.PostSubmodelElementResponse}. Is responsible
 * for communication with the persistence and sends the corresponding events to the message bus.
 */
public class PostSubmodelElementRequestHandler extends AbstractSubmodelInterfaceRequestHandler<PostSubmodelElementRequest, PostSubmodelElementResponse> {

    public PostSubmodelElementRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public PostSubmodelElementResponse doProcess(PostSubmodelElementRequest request)
            throws ResourceNotFoundException, ValueMappingException, ValidationException, ResourceNotAContainerElementException, AssetConnectionException, MessageBusException,
            ResourceAlreadyExistsException {
        ModelValidator.validate(request.getSubmodelElement(), context.getCoreConfig().getValidationOnCreate());
        Reference parentReference = ReferenceBuilder.forSubmodel(request.getSubmodelId());
        Reference childReference = ReferenceBuilder
                .with(parentReference)
                .element(request.getSubmodelElement().getIdShort())
                .build();
        if (context.getPersistence().submodelElementExists(childReference)) {
            throw new ResourceAlreadyExistsException(childReference);
        }
        context.getPersistence().insert(parentReference, request.getSubmodelElement());
        if (ElementValueHelper.isSerializableAsValue(request.getSubmodelElement().getClass())) {
            context.getAssetConnectionManager().setValue(childReference, ElementValueMapper.toValue(request.getSubmodelElement()));
        }
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementCreateEventMessage.builder()
                    .element(childReference)
                    .value(request.getSubmodelElement())
                    .build());
        }
        return PostSubmodelElementResponse.builder()
                .payload(request.getSubmodelElement())
                .created()
                .build();
    }
}
