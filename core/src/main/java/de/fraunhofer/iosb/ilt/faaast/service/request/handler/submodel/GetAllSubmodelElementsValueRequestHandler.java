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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetAllSubmodelElementsValueRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetAllSubmodelElementsValueResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetAllSubmodelElementsRequest} in the service
 * and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetAllSubmodelElementsResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetAllSubmodelElementsValueRequestHandler extends AbstractSubmodelInterfaceRequestHandler<GetAllSubmodelElementsValueRequest, GetAllSubmodelElementsValueResponse> {

    public GetAllSubmodelElementsValueRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public GetAllSubmodelElementsValueResponse doProcess(GetAllSubmodelElementsValueRequest request)
            throws AssetConnectionException, ValueMappingException, ResourceNotFoundException, MessageBusException, ResourceNotAContainerElementException {
        Reference reference = ReferenceBuilder.forSubmodel(request.getSubmodelId());
        Page<SubmodelElement> page = context.getPersistence().getSubmodelElementsValueOnly(reference, request.getOutputModifier(), request.getPagingInfo());
        syncWithAsset(reference, page.getContent(), !request.isInternal());
        if (!request.isInternal() && Objects.nonNull(page.getContent())) {
            page.getContent().forEach(LambdaExceptionHelper.rethrowConsumer(
                    x -> context.getMessageBus().publish(ElementReadEventMessage.builder()
                            .element(AasUtils.toReference(reference, x))
                            .value(x)
                            .build())));
        }
        return GetAllSubmodelElementsValueResponse.builder()
                .payload(page)
                .success()
                .build();
    }

}
