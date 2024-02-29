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
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelElementByPathReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelElementByPathReferenceResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelElementByPathReferenceRequest} in
 * the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelElementByPathReferenceResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetSubmodelElementByPathReferenceRequestHandler
        extends AbstractSubmodelInterfaceRequestHandler<GetSubmodelElementByPathReferenceRequest, GetSubmodelElementByPathReferenceResponse> {

    public GetSubmodelElementByPathReferenceRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public GetSubmodelElementByPathReferenceResponse doProcess(GetSubmodelElementByPathReferenceRequest request)
            throws ResourceNotFoundException, ValueMappingException, AssetConnectionException, MessageBusException, ResourceNotAContainerElementException {
        Reference reference = resolveReferenceWithTypes(request.getSubmodelId(), request.getPath());
        SubmodelElement submodelElement = context.getPersistence().getSubmodelElement(reference, request.getOutputModifier());
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementReadEventMessage.builder()
                    .element(reference)
                    .value(submodelElement)
                    .build());
        }
        return GetSubmodelElementByPathReferenceResponse.builder()
                .payload(reference)
                .success()
                .build();
    }


    private Reference resolveReferenceWithTypes(String submodelId, String idShortPath) throws ResourceNotFoundException {
        ReferenceBuilder builder = new ReferenceBuilder();
        builder.submodel(submodelId);
        IdShortPath.Builder pathBuilder = IdShortPath.builder();
        for (String pathElement: IdShortPath.parse(idShortPath).getElements()) {
            IdShortPath subPath = pathBuilder.pathSegment(pathElement).build();
            SubmodelElement submodelElement = context.getPersistence().getSubmodelElement(
                    SubmodelElementIdentifier.builder()
                            .submodelId(submodelId)
                            .idShortPath(subPath)
                            .build(),
                    QueryModifier.MINIMAL);
            if (pathElement.startsWith("[") && pathElement.endsWith("]")) {
                builder.element(pathElement.substring(1, pathElement.length() - 1), submodelElement.getClass());
            }
            else {
                builder.element(submodelElement);
            }
        }
        return builder.build();
    }
}
