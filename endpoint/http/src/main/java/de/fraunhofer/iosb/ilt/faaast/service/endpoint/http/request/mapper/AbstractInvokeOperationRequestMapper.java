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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.InvokeOperationAsyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.InvokeOperationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementPathHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import java.util.Map;


/**
 * Abstract base class for invoke operation requests mapping to the following
 * HTTP-POST-Request paths:
 * submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/invoke
 * <br>
 * shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/invoke
 *
 * @param <T> actual request type
 * @param <U> actual response type
 */
public abstract class AbstractInvokeOperationRequestMapper<T extends InvokeOperationRequest<U>, U extends Response> extends SubmodelInterfaceRequestMapper<T, U> {

    protected static final String SUBMODEL_ELEMENT_PATH = "submodelElementPath";
    protected static final String PATTERN = String.format("submodel-elements/(?<%s>.*)/invoke", SUBMODEL_ELEMENT_PATH);
    protected static final String QUERY_PARAMETER_ASYNC = "async";

    protected AbstractInvokeOperationRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.POST, PATTERN);
    }


    /**
     * Checks if a request indicates that the operation should be executed
     * asynchronously
     *
     * @param httpRequest the HTTP request
     * @return true if is async, false otherwise
     */
    protected boolean isAsync(HttpRequest httpRequest) {
        return httpRequest.hasQueryParameter(QUERY_PARAMETER_ASYNC)
                && Boolean.parseBoolean(httpRequest.getQueryParameter(QUERY_PARAMETER_ASYNC));
    }


    @Override
    public T doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) throws InvalidRequestException {
        T request = isAsync(httpRequest)
                ? (T) parseBody(httpRequest, InvokeOperationAsyncRequest.class)
                : (T) parseBody(httpRequest, InvokeOperationSyncRequest.class);
        request.setPath(ElementPathHelper.toKeys(EncodingHelper.urlDecode(urlParameters.get(SUBMODEL_ELEMENT_PATH))));
        return request;
    }
}
