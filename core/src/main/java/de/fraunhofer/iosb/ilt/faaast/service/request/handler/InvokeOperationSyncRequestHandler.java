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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.ExecutionState;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.InvokeOperationSyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationFinishEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationInvokeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.InvokeOperationSyncRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.InvokeOperationSyncResponse}.
 * Is responsible for communication with the persistence and sends the
 * corresponding events to the message bus.
 */
public class InvokeOperationSyncRequestHandler extends RequestHandler<InvokeOperationSyncRequest, InvokeOperationSyncResponse> {

    public InvokeOperationSyncRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public InvokeOperationSyncResponse process(InvokeOperationSyncRequest request) throws ValueMappingException, ResourceNotFoundException, MessageBusException {
        Reference reference = ReferenceHelper.toReference(request.getPath(), request.getId(), Submodel.class);
        InvokeOperationSyncResponse response = new InvokeOperationSyncResponse();
        messageBus.publish(OperationInvokeEventMessage.builder()
                .element(reference)
                .input(ElementValueHelper.toValues(request.getInputArguments()))
                .inoutput(ElementValueHelper.toValues(request.getInoutputArguments()))
                .build());
        OperationResult operationResult = executeOperationSync(reference, request);
        response.setPayload(operationResult);
        response.setStatusCode(StatusCode.SUCCESS);
        messageBus.publish(OperationFinishEventMessage.builder()
                .element(reference)
                .inoutput(ElementValueHelper.toValues(response.getPayload().getInoutputArguments()))
                .output(ElementValueHelper.toValues(response.getPayload().getOutputArguments()))
                .build());
        return response;
    }


    public OperationResult executeOperationSync(Reference reference, InvokeOperationSyncRequest request) {
        if (!assetConnectionManager.hasOperationProvider(reference)) {
            throw new IllegalArgumentException(String.format(
                    "error executing operation - no operation provider defined for reference '%s' (requestId: %s)",
                    AasUtils.asString(reference),
                    request.getRequestId()));
        }

        AssetOperationProvider assetOperationProvider = assetConnectionManager.getOperationProvider(reference);
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
            OperationVariable[] outputVariables = future.get(request.getTimeout(), TimeUnit.MILLISECONDS);
            result = OperationResult.builder()
                    .requestId(request.getRequestId())
                    .executionState(ExecutionState.COMPLETED)
                    .inoutputArguments(request.getInoutputArguments())
                    .outputArguments(Arrays.asList(outputVariables))
                    .build();
        }
        catch (TimeoutException e) {
            future.cancel(true);
            result = OperationResult.builder()
                    .requestId(request.getRequestId())
                    .inoutputArguments(request.getInoutputArguments())
                    .executionState(ExecutionState.TIMEOUT)
                    .build();
            Thread.currentThread().interrupt();
        }
        catch (Exception e) {
            result = OperationResult.builder()
                    .requestId(request.getRequestId())
                    .inoutputArguments(request.getInoutputArguments())
                    .executionState(ExecutionState.FAILED)
                    .build();
            Thread.currentThread().interrupt();
        }
        finally {
            executor.shutdown();
        }
        return result;
    }
}
