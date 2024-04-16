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
import de.fraunhofer.iosb.ilt.faaast.service.model.TypedInMemoryFile;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetFileByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetFileByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ValueReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Class to handle a {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetFileByPathRequest}.
 */
public class GetFileByPathRequestHandler extends AbstractSubmodelInterfaceRequestHandler<GetFileByPathRequest, GetFileByPathResponse> {

    public GetFileByPathRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public GetFileByPathResponse doProcess(GetFileByPathRequest request)
            throws ResourceNotFoundException, ValueMappingException, AssetConnectionException, MessageBusException, ResourceNotAContainerElementException {
        Reference reference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(request.getPath())
                .build();
        File file = context.getPersistence().getSubmodelElement(reference, request.getOutputModifier(), File.class);
        if (!request.isInternal()) {
            context.getMessageBus().publish(ValueReadEventMessage.builder()
                    .element(reference)
                    .build());
        }
        if (Objects.isNull(file.getValue())) {
            throw new ResourceNotFoundException(String.format("could not find file for path '%s'", request.getPath()));
        }
        return GetFileByPathResponse.builder()
                .payload(new TypedInMemoryFile.Builder()
                        .content(context.getFileStorage().get(file.getValue()))
                        .contentType(file.getContentType())
                        .path(file.getValue())
                        .build())
                .success()
                .build();
    }
}
