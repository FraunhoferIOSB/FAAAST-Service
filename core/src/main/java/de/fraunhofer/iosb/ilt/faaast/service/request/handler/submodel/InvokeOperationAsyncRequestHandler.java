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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.ExecutionState;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationAsyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.InvokeOperationAsyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationFinishEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationInvokeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.Arrays;
import java.util.function.BiConsumer;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationAsyncRequest} in the service
 * and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.InvokeOperationAsyncResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class InvokeOperationAsyncRequestHandler extends AbstractSubmodelInterfaceRequestHandler<InvokeOperationAsyncRequest, InvokeOperationAsyncResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvokeOperationAsyncRequestHandler.class);

    public InvokeOperationAsyncRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public InvokeOperationAsyncResponse doProcess(InvokeOperationAsyncRequest request) throws ResourceNotFoundException, ValueMappingException, MessageBusException, Exception {
        Reference reference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(request.getPath())
                .build();
        OperationHandle operationHandle = executeOperationAsync(reference, request);
        context.getMessageBus().publish(OperationInvokeEventMessage.builder()
                .element(reference)
                .input(ElementValueHelper.toValues(request.getInputArguments()))
                .inoutput(ElementValueHelper.toValues(request.getInoutputArguments()))
                .build());
        return InvokeOperationAsyncResponse.builder()
                .payload(operationHandle)
                .success()
                .build();
    }


    /**
     * Executes and operation asynchroniously.
     *
     * @param reference the reference to the AAS operation element
     * @param request the request
     * @return an handle that can be used to query the current state of the operation
     * @throws MessageBusException if publishing on the message bus failed
     * @throws Exception if executing the operation itself failed
     */
    public OperationHandle executeOperationAsync(Reference reference, InvokeOperationAsyncRequest request) throws MessageBusException, Exception {
        if (!context.getAssetConnectionManager().hasOperationProvider(reference)) {
            throw new IllegalArgumentException(String.format(
                    "error executing operation - no operation provider defined for reference '%s'",
                    ReferenceHelper.asString(reference)));
        }
        OperationHandle operationHandle = new OperationHandle();
        context.getPersistence().save(
                operationHandle,
                new OperationResult.Builder()
                        .inoutputArguments(request.getInoutputArguments())
                        .executionState(ExecutionState.RUNNING)
                        .build());
        try {
            BiConsumer<OperationVariable[], OperationVariable[]> callback = LambdaExceptionHelper.rethrowBiConsumer((x, y) -> {
                OperationResult operationResult = context.getPersistence().getOperationResult(operationHandle);
                operationResult.setExecutionState(ExecutionState.COMPLETED);
                operationResult.setOutputArguments(Arrays.asList(x));
                operationResult.setInoutputArguments(Arrays.asList(y));
                context.getPersistence().save(operationHandle, operationResult);
                context.getMessageBus().publish(OperationFinishEventMessage.builder()
                        .element(reference)
                        .inoutput(ElementValueHelper.toValues(Arrays.asList(x)))
                        .output(ElementValueHelper.toValues(Arrays.asList(y)))
                        .build());
            });
            AssetOperationProvider assetOperationProvider = context.getAssetConnectionManager().getOperationProvider(reference);
            assetOperationProvider.invokeAsync(
                    request.getInputArguments().toArray(new OperationVariable[0]),
                    request.getInoutputArguments().toArray(new OperationVariable[0]),
                    callback);
        }
        catch (AssetConnectionException | ValueMappingException e) {
            OperationResult operationResult = context.getPersistence().getOperationResult(operationHandle);
            operationResult.setExecutionState(ExecutionState.FAILED);
            operationResult.setInoutputArguments(request.getInoutputArguments());
            context.getPersistence().save(operationHandle, operationResult);
            try {
                context.getMessageBus().publish(OperationFinishEventMessage.builder()
                        .element(reference)
                        .inoutput(ElementValueHelper.toValues(operationResult.getInoutputArguments()))
                        .build());
            }
            catch (ValueMappingException e2) {
                LOGGER.warn("could not publish operation finished event message because mapping result to value objects failed", e2);
            }
        }
        return operationHandle;
    }
}
