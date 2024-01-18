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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.ExecutionState;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.InvokeOperationSyncResponse;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Class to handle a {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationSyncRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.InvokeOperationSyncResponse}. Is responsible
 * for communication with the persistence and sends the corresponding events to the message bus.
 */
public class InvokeOperationSyncRequestHandler extends AbstractSubmodelInterfaceRequestHandler<InvokeOperationSyncRequest, InvokeOperationSyncResponse> {

    public InvokeOperationSyncRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public InvokeOperationSyncResponse doProcess(InvokeOperationSyncRequest request) throws ValueMappingException, ResourceNotFoundException, MessageBusException {
        Reference reference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(request.getPath())
                .build();
        if (!request.isInternal()) {
            context.getMessageBus().publish(OperationInvokeEventMessage.builder()
                    .element(reference)
                    .input(ElementValueHelper.toValueMap(request.getInputArguments()))
                    .inoutput(ElementValueHelper.toValueMap(request.getInoutputArguments()))
                    .build());
        }
        OperationResult operationResult = executeOperationSync(reference, request);
        if (!request.isInternal()) {
            context.getMessageBus().publish(OperationFinishEventMessage.builder()
                    .element(reference)
                    .inoutput(ElementValueHelper.toValueMap(operationResult.getInoutputArguments()))
                    .output(ElementValueHelper.toValueMap(operationResult.getOutputArguments()))
                    .build());
        }
        return InvokeOperationSyncResponse.builder()
                .payload(operationResult)
                .success()
                .build();
    }


    /**
     * Executes and operation synchroniously.
     *
     * @param reference the reference to the AAS operation element
     * @param request the request
     * @return the operation result
     */
    public OperationResult executeOperationSync(Reference reference, InvokeOperationSyncRequest request) {
        if (!context.getAssetConnectionManager().hasOperationProvider(reference)) {
            throw new IllegalArgumentException(String.format(
                    "error executing operation - no operation provider defined for reference '%s'",
                    ReferenceHelper.toString(reference)));
        }
        AssetOperationProvider assetOperationProvider = context.getAssetConnectionManager().getOperationProvider(reference);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<OperationVariable[]> future = executor.submit(new Callable<OperationVariable[]>() {
            @Override
            public OperationVariable[] call() throws Exception {
                return assetOperationProvider.invoke(
                        request.getInputArguments().toArray(new OperationVariable[0]),
                        request.getInoutputArguments().toArray(new OperationVariable[0]));
            }
        });
        OperationResult result;
        try {
            OperationVariable[] outputVariables = future.get(request.getTimeout().getTimeInMillis(Calendar.getInstance()), TimeUnit.MILLISECONDS);
            result = new OperationResult.Builder()
                    .executionState(ExecutionState.COMPLETED)
                    .inoutputArguments(request.getInoutputArguments())
                    .outputArguments(Arrays.asList(outputVariables))
                    .success(true)
                    .build();
        }
        catch (TimeoutException e) {
            future.cancel(true);
            result = new OperationResult.Builder()
                    .inoutputArguments(request.getInoutputArguments())
                    .executionState(ExecutionState.TIMEOUT)
                    .success(false)
                    .build();
            Thread.currentThread().interrupt();
        }
        catch (InterruptedException | ExecutionException e) {
            result = new OperationResult.Builder()
                    .inoutputArguments(request.getInoutputArguments())
                    .executionState(ExecutionState.FAILED)
                    .success(false)
                    .build();
            Thread.currentThread().interrupt();
        }
        finally {
            executor.shutdown();
        }
        return result;
    }
}
