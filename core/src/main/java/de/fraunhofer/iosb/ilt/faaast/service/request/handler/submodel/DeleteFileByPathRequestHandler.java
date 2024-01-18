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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.DeleteFileByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.DeleteFileByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.io.IOException;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Class to handle a {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.DeleteFileByPathRequest}.
 */
public class DeleteFileByPathRequestHandler extends AbstractSubmodelInterfaceRequestHandler<DeleteFileByPathRequest, DeleteFileByPathResponse> {

    public DeleteFileByPathRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public DeleteFileByPathResponse doProcess(DeleteFileByPathRequest request)
            throws ResourceNotFoundException, ValueMappingException, AssetConnectionException, MessageBusException, ResourceNotAContainerElementException, IOException {
        Reference reference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(request.getPath())
                .build();
        File file = context.getPersistence().getSubmodelElement(reference, request.getOutputModifier(), File.class);
        context.getFileStorage().delete(file.getValue());
        File oldFile = file;
        file.setValue("");
        file.setContentType("");
        context.getPersistence().update(reference, file);
        if (!request.isInternal()) {
            context.getMessageBus().publish(ValueChangeEventMessage.builder()
                    .element(reference)
                    .oldValue(ElementValueMapper.toValue(oldFile))
                    .newValue(ElementValueMapper.toValue(file))
                    .build());
            context.getMessageBus().publish(ElementUpdateEventMessage.builder()
                    .element(reference)
                    .value(file)
                    .build());
        }
        return DeleteFileByPathResponse.builder()
                .success()
                .build();
    }
}
