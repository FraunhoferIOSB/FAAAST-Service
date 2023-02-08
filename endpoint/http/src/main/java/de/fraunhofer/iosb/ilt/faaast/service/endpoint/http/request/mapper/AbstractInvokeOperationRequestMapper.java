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
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;
import java.util.Map;


/**
 * Abstract base class for invoke operation requests mappings. Supports the following HTTP-POST-Request paths
 * 
 * <p><ul>
 * <li>submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/invoke</li>
 * <li>shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/invoke</li>
 * </ul>
 *
 * @param <T> actual request type
 * @param <U> actual response type
 */
public abstract class AbstractInvokeOperationRequestMapper<T extends InvokeOperationRequest<U>, U extends Response> extends AbstractSubmodelInterfaceRequestMapper<T, U> {

    protected static final String SUBMODEL_ELEMENT_PATH = RegExHelper.uniqueGroupName();
    protected static final String PATTERN = String.format("submodel-elements/%s/invoke", pathElement(SUBMODEL_ELEMENT_PATH));

    protected AbstractInvokeOperationRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.POST, PATTERN);
    }


    /**
     * Checks if a request indicates that the operation should be executed asynchronously.
     *
     * @param httpRequest the HTTP request
     * @return true if is async, false otherwise
     */
    protected boolean isAsync(HttpRequest httpRequest) {
        return httpRequest.hasQueryParameter(QueryParameters.ASYNC)
                && Boolean.parseBoolean(httpRequest.getQueryParameter(QueryParameters.ASYNC));
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
