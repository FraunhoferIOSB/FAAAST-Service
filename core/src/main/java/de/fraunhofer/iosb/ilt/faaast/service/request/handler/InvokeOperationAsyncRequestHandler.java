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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.ExecutionState;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.InvokeOperationAsyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.InvokeOperationAsyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.InvokeOperationAsyncRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.InvokeOperationAsyncResponse}.
 * Is responsible for communication with the persistence and sends the
 * corresponding events to the message bus.
 */
public class InvokeOperationAsyncRequestHandler extends RequestHandler<InvokeOperationAsyncRequest, InvokeOperationAsyncResponse> {

    private static Logger LOGGER = LoggerFactory.getLogger(InvokeOperationAsyncRequestHandler.class);

    public InvokeOperationAsyncRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public InvokeOperationAsyncResponse process(InvokeOperationAsyncRequest request) throws ResourceNotFoundException, ValueMappingException, MessageBusException, Exception {
        InvokeOperationAsyncResponse response = new InvokeOperationAsyncResponse();
        Reference reference = ReferenceHelper.toReference(request.getPath(), request.getId(), Submodel.class);
        OperationHandle operationHandle = executeOperationAsync(reference, request);
        response.setPayload(operationHandle);
        response.setStatusCode(StatusCode.Success);
        publishOperationInvokeEventMessage(reference,
                toValues(request.getInputArguments()),
                toValues(request.getInoutputArguments()));
        return response;
    }


    public OperationHandle executeOperationAsync(Reference reference, InvokeOperationAsyncRequest request) throws MessageBusException, Exception {
        if (!assetConnectionManager.hasOperationProvider(reference)) {
            throw new IllegalArgumentException(String.format(
                    "error executing operation - no operation provider defined for reference '%s' (requestId: %s)",
                    AasUtils.asString(reference),
                    request.getRequestId()));
        }
        OperationHandle operationHandle = this.persistence.putOperationContext(
                null,
                request.getRequestId(),
                new OperationResult.Builder()
                        .requestId(request.getRequestId())
                        .inoutputArguments(request.getInoutputArguments())
                        .executionState(ExecutionState.Running)
                        .build());
        try {
            BiConsumer<OperationVariable[], OperationVariable[]> callback = LambdaExceptionHelper.rethrowBiConsumer((x, y) -> {
                OperationResult operationResult = persistence.getOperationResult(operationHandle.getHandleId());
                operationResult.setExecutionState(ExecutionState.Completed);
                operationResult.setOutputArguments(Arrays.asList(x));
                operationResult.setInoutputArguments(Arrays.asList(y));

                persistence.putOperationContext(operationHandle.getHandleId(), operationHandle.getRequestId(), operationResult);
                publishOperationFinishEventMessage(reference,
                        toValues(Arrays.asList(x)),
                        toValues(Arrays.asList(y)));
            });
            AssetOperationProvider assetOperationProvider = assetConnectionManager.getOperationProvider(reference);
            assetOperationProvider.invokeAsync(
                    request.getInputArguments().toArray(new OperationVariable[0]),
                    request.getInoutputArguments().toArray(new OperationVariable[0]),
                    callback);
        }
        catch (AssetConnectionException | ValueMappingException e) {
            OperationResult operationResult = persistence.getOperationResult(operationHandle.getHandleId());
            operationResult.setExecutionState(ExecutionState.Failed);
            operationResult.setInoutputArguments(request.getInoutputArguments());
            persistence.putOperationContext(operationHandle.getHandleId(), operationHandle.getRequestId(), operationResult);
            try {
                publishOperationFinishEventMessage(reference,
                        List.of(),
                        toValues(operationResult.getInoutputArguments()));
            }
            catch (ValueMappingException e2) {
                LOGGER.warn("could not publish operation finished event message because mapping result to value objects failed", e2);
            }
        }
        return operationHandle;
    }
}
