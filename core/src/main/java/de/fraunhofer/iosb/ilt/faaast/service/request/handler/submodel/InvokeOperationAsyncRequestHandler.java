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

import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.MessageType;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
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
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
        return InvokeOperationAsyncResponse.builder()
                .payload(operationHandle)
                .statusCode(StatusCode.SUCCESS_ACCEPTED)
                .build();
    }


    private void handleOperationSuccess(Reference reference, OperationHandle operationHandle, OperationVariable[] inoutput, OperationVariable[] output) {
        handleOperationResult(
                reference,
                operationHandle,
                new OperationResult.Builder()
                        .executionState(ExecutionState.COMPLETED)
                        .inoutputArguments(Arrays.asList(inoutput))
                        .outputArguments(Arrays.asList(output))
                        .success(true)
                        .build());
    }


    private void handleOperationFailure(Reference reference, InvokeOperationAsyncRequest request, OperationHandle operationHandle, Throwable error) {
        handleOperationResult(
                reference,
                operationHandle,
                new OperationResult.Builder()
                        .executionState(ExecutionState.FAILED)
                        .inoutputArguments(request.getInoutputArguments())
                        .outputArguments(List.of())
                        .message(MessageType.ERROR, String.format(
                                "operation failed to execute (reason: %s)",
                                error.getMessage()))
                        .success(false)
                        .build());
    }


    private void handleOperationTimeout(Reference reference, InvokeOperationAsyncRequest request, OperationHandle operationHandle) {
        handleOperationResult(
                reference,
                operationHandle,
                new OperationResult.Builder()
                        .executionState(ExecutionState.TIMEOUT)
                        .inoutputArguments(request.getInoutputArguments())
                        .outputArguments(List.of())
                        .message(MessageType.WARNING, String.format(
                                "operation execution timed out after %s ms",
                                request.getTimeout()))
                        .success(false)
                        .build());
    }


    private void handleOperationResult(Reference reference,
                                       OperationHandle operationHandle,
                                       OperationResult operationResult) {
        context.getPersistence().save(operationHandle, operationResult);
        try {
            context.getMessageBus().publish(OperationFinishEventMessage.builder()
                    .element(reference)
                    .inoutput(ElementValueHelper.toValueMap(operationResult.getInoutputArguments()))
                    .output(ElementValueHelper.toValueMap(operationResult.getOutputArguments()))
                    .build());
        }
        catch (ValueMappingException | MessageBusException e) {
            LOGGER.warn("could not publish OperationFinishedEventMessage on messagebus", e);
        }
    }


    private void handleOperationInvoke(Reference reference,
                                       OperationHandle operationHandle,
                                       InvokeOperationAsyncRequest request) {
        context.getPersistence().save(
                operationHandle,
                new OperationResult.Builder()
                        .inoutputArguments(request.getInoutputArguments())
                        .executionState(ExecutionState.RUNNING)
                        .build());
        try {
            context.getMessageBus().publish(OperationInvokeEventMessage.builder()
                    .element(reference)
                    .input(ElementValueHelper.toValueMap(request.getInputArguments()))
                    .inoutput(ElementValueHelper.toValueMap(request.getInoutputArguments()))
                    .build());
        }
        catch (ValueMappingException | MessageBusException e) {
            LOGGER.warn("could not publish OperationFinishedEventMessage on messagebus", e);
        }
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
                    ReferenceHelper.toString(reference)));
        }
        OperationHandle operationHandle = new OperationHandle();
        handleOperationInvoke(reference, operationHandle, request);

        AtomicBoolean timeoutOccured = new AtomicBoolean(false);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            context.getAssetConnectionManager().getOperationProvider(reference).invokeAsync(
                    request.getInputArguments().toArray(new OperationVariable[0]),
                    request.getInoutputArguments().toArray(new OperationVariable[0]),
                    (output, inoutput) -> {
                        if (timeoutOccured.get()) {
                            return;
                        }
                        handleOperationSuccess(reference, operationHandle, inoutput, output);
                    },
                    error -> handleOperationFailure(reference, request, operationHandle, error));
            executor.schedule(() -> {
                timeoutOccured.set(true);
                handleOperationTimeout(reference, request, operationHandle);
            }, request.getTimeout().getTimeInMillis(Calendar.getInstance()), TimeUnit.MILLISECONDS);
        }
        catch (Exception e) {
            handleOperationFailure(reference, request, operationHandle, e);
        }
        finally {
            executor.shutdown();
        }
        return operationHandle;
    }
}
