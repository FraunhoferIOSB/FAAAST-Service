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
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.EnvironmentSerializationManager;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.MessageType;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasserialization.GenerateSerializationByIdsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasserialization.GenerateSerializationByIdsResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;


/**
 * HTTP response mapper for {@link GenerateSerializationByIdsResponse}, serializing the requested content according to
 * the desired data format.
 */
public class GenerateSerializationByIdsResponseMapper extends AbstractResponseMapper<GenerateSerializationByIdsResponse, GenerateSerializationByIdsRequest> {

    public GenerateSerializationByIdsResponseMapper(ServiceContext serviceContext) {
        super(serviceContext);
    }


    @Override
    public void map(GenerateSerializationByIdsRequest apiRequest, GenerateSerializationByIdsResponse apiResponse, HttpServletResponse httpResponse) {
        try {
            HttpHelper.sendContent(httpResponse,
                    apiResponse.getStatusCode(),
                    EnvironmentSerializationManager.serializerFor(apiResponse.getDataformat()).write(apiResponse.getPayload()),
                    apiResponse.getDataformat().getContentType(),
                    Map.of("Content-Disposition",
                            String.format(
                                    "attachment; filename=\"download.%s\"",
                                    apiResponse.getDataformat().getFileExtensions().get(0))));
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
