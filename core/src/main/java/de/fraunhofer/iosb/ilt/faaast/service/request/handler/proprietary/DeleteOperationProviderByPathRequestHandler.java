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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.proprietary;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.proprietary.DeleteOperationProviderByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.proprietary.DeleteOperationProviderByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.LogHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.OperationProviderHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ResponseHelper;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.MessageType;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.proprietary.DeleteOperationProviderByPathRequest} in
 * the
 * service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.proprietary.DeleteOperationProviderByPathResponse}.
 * Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class DeleteOperationProviderByPathRequestHandler
        extends AbstractSubmodelInterfaceRequestHandler<DeleteOperationProviderByPathRequest, DeleteOperationProviderByPathResponse> {

    @Override
    protected DeleteOperationProviderByPathResponse doProcess(DeleteOperationProviderByPathRequest request, RequestExecutionContext context) throws Exception {
        Reference reference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(request.getPath())
                .build();
        context.getPersistence().getSubmodelElement(reference, QueryModifier.MINIMAL, Operation.class);
        if (!context.getAssetConnectionManager().hasOperationProvider(reference)) {
            throw new ResourceNotFoundException(String.format(
                    "no operation provider available for reference '%s'",
                    ReferenceHelper.asString(reference)));
        }

        AssetConnectionConfig<?, ?, ?, ?> config = OperationProviderHelper.convertBodyToAssetConnectionConfig(request.getBody(), reference);
        List<Message> result = context.getAssetConnectionManager().updateConnections(List.of(config), List.of()).stream()
                .filter(x -> x.getMessageType() == MessageType.ERROR || x.getMessageType() == MessageType.EXCEPTION)
                .toList();
        LogHelper.logMessages(result);
        return DeleteOperationProviderByPathResponse.builder()
                .statusCode(result.isEmpty()
                        ? StatusCode.SUCCESS_NO_CONTENT
                        : StatusCode.CLIENT_ERROR_BAD_REQUEST)
                .result(ResponseHelper.asResult(result))
                .build();
    }

}
