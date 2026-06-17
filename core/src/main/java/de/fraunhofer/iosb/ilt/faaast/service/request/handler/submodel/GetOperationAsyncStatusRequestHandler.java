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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetOperationAsyncStatusRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetOperationAsyncStatusResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.OperationProviderHelper;
import org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetOperationAsyncStatusRequest}.
 */
public class GetOperationAsyncStatusRequestHandler extends AbstractRequestHandler<GetOperationAsyncStatusRequest, GetOperationAsyncStatusResponse> {

    @Override
    public GetOperationAsyncStatusResponse process(GetOperationAsyncStatusRequest request, RequestExecutionContext context) throws ResourceNotFoundException, PersistenceException {
        OperationResult result = context.getPersistence().getOperationResult(request.getHandle());
        // if request is running, only report each progress once
        if (result.getExecutionState() == ExecutionState.RUNNING) {
            OperationResult updatedResult = DeepCopyHelper.deepCopyAny(result, OperationResult.class);
            updatedResult.getMessages().removeIf(OperationProviderHelper::isProgressMessage);
            context.getPersistence().save(request.getHandle(), updatedResult);
        }
        return GetOperationAsyncStatusResponse.builder()
                .payload(result)
                .success()
                .build();
    }
}
