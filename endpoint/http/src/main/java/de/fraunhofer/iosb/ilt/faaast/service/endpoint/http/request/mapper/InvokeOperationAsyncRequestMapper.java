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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.InvokeOperationAsyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.InvokeOperationAsyncRequest;


/**
 * class to map HTTP-POST-Request paths:
 * submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/invoke
 * <br>
 * shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/invoke
 */
public class InvokeOperationAsyncRequestMapper extends AbstractInvokeOperationRequestMapper<InvokeOperationAsyncRequest, InvokeOperationAsyncResponse> {

    public InvokeOperationAsyncRequestMapper(ServiceContext serviceContext) {
        super(serviceContext);
    }


    @Override
    public boolean matches(HttpRequest httpRequest) {
        return super.matches(httpRequest)
                && isAsync(httpRequest);
    }
}
