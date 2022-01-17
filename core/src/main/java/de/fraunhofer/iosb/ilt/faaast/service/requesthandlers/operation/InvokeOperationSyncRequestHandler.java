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
package de.fraunhofer.iosb.ilt.faaast.service.requesthandlers.operation;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.ExecutionState;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.response.InvokeOperationSyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.requesthandlers.RequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.requesthandlers.Util;
import de.fraunhofer.iosb.ilt.faaast.service.util.DataElementValueMapper;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Reference;
import java.util.Arrays;
import java.util.stream.Collectors;


public class InvokeOperationSyncRequestHandler extends RequestHandler<InvokeOperationSyncRequest, InvokeOperationSyncResponse> {

    public InvokeOperationSyncRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public InvokeOperationSyncResponse process(InvokeOperationSyncRequest request) {
        InvokeOperationSyncResponse response = new InvokeOperationSyncResponse();

        try {
            Reference reference = Util.toReference(request.getPath());

            OperationResult operationResult = executeOperationSync(reference, request);
            response.setPayload(operationResult);
            response.setStatusCode(StatusCode.Success);
            publishOperationInvokeEventMessage(reference,
                    request.getInputArguments().stream()
                            .map(x -> (ElementValue) DataElementValueMapper.toDataElement(x.getValue()))
                            .collect(Collectors.toList()),
                    request.getInoutputArguments().stream()
                            .map(x -> (ElementValue) DataElementValueMapper.toDataElement(x.getValue()))
                            .collect(Collectors.toList()));
        }
        catch (Exception ex) {
            response.setStatusCode(StatusCode.ServerInternalError);
        }
        return response;
    }


    public OperationResult executeOperationSync(Reference reference, InvokeOperationSyncRequest request) {

        if (assetConnectionManager.hasOperationProvider(reference)) {
            OperationHandle operationHandle = this.persistence.putOperationContext(
                    null,
                    request.getRequestId(),
                    new OperationResult.Builder()
                            .requestId(request.getRequestId())
                            .inoutputArguments(request.getInoutputArguments())
                            .executionState(ExecutionState.Running)
                            .build());

            AssetOperationProvider assetOperationProvider = assetConnectionManager.getOperationProvider(reference);

            //TODO: Do async and abort after timeout
            OperationVariable[] outputVariables = assetOperationProvider.invoke(
                    request.getInputArguments().toArray(new OperationVariable[0]),
                    request.getInoutputArguments().toArray(new OperationVariable[0]));

            OperationResult operationResult = persistence.getOperationResult(operationHandle.getHandleId());
            //TODO: What about Failed / Timeout ...?
            operationResult.setExecutionState(ExecutionState.Completed);
            operationResult.setOutputArguments(Arrays.asList(outputVariables));

            persistence.putOperationContext(operationHandle.getHandleId(), operationHandle.getRequestId(), operationResult);
            publishOperationFinishEventMessage(reference,
                    Arrays.asList(outputVariables).stream()
                            .map(z -> (ElementValue) DataElementValueMapper.toDataElement(z.getValue()))
                            .collect(Collectors.toList()),
                    request.getInoutputArguments().stream()
                            .map(z -> (ElementValue) DataElementValueMapper.toDataElement(z.getValue()))
                            .collect(Collectors.toList()));

            return operationResult;
        }
        else {
            throw new RuntimeException("No assetconnection available for running operation with request id" + request.getRequestId());
        }
    }
}
