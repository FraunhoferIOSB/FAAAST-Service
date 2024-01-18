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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.response.mapper;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationAsyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.InvokeOperationAsyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;


/**
 * HTTP response mapper for {@link InvokeOperationAsyncResponse}.
 */
public class InvokeOperationAsyncResponseMapper extends AbstractResponseMapper<InvokeOperationAsyncResponse, InvokeOperationAsyncRequest> {

    public InvokeOperationAsyncResponseMapper(ServiceContext serviceContext) {
        super(serviceContext);
    }


    @Override
    public void map(InvokeOperationAsyncRequest apiRequest, InvokeOperationAsyncResponse apiResponse, HttpServletResponse httpResponse) {
        HttpHelper.sendContent(
                httpResponse,
                apiResponse.getStatusCode(),
                null,
                null,
                Map.of("Location",
                        String.format(
                                "%soperation-status/%s",
                                apiRequest.getOutputModifier().getContent() == Content.VALUE
                                        ? "../"
                                        : "",
                                EncodingHelper.base64UrlEncode(apiResponse.getPayload().getHandleId()))));
    }
}
