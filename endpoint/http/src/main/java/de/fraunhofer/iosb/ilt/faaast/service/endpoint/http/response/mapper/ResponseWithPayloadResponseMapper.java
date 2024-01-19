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
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.MessageType;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractRequestWithModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.AbstractResponseWithPayload;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Generic response mapper for any responses that contain a payload.It is used when no more specific mapper is present.
 *
 * @param <T> type of the payload
 * @param <U> type of the request
 */
public class ResponseWithPayloadResponseMapper<T extends AbstractResponseWithPayload, U extends Request<T>> extends AbstractResponseMapper<T, U> {

    public ResponseWithPayloadResponseMapper(ServiceContext serviceContext) {
        super(serviceContext);
    }


    @Override
    public void map(U apiRequest, T apiResponse, HttpServletResponse httpResponse) {
        try {
            HttpHelper.sendJson(httpResponse,
                    apiResponse.getStatusCode(),
                    new HttpJsonApiSerializer().write(
                            apiResponse.getPayload(),
                            AbstractRequestWithModifier.class.isAssignableFrom(apiRequest.getClass())
                                    ? ((AbstractRequestWithModifier) apiRequest).getOutputModifier()
                                    : OutputModifier.DEFAULT));
        }
        catch (SerializationException e) {
            HttpHelper.send(
                    httpResponse,
                    StatusCode.SERVER_INTERNAL_ERROR,
                    Result.builder()
                            .message(MessageType.EXCEPTION, e.getMessage())
                            .build());
        }
    }
}
