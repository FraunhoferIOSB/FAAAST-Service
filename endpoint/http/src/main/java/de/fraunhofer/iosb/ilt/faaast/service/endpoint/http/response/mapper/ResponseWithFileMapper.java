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

import com.google.common.net.MediaType;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.*;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.AbstractResponseWithFile;
import de.fraunhofer.iosb.ilt.faaast.service.util.FileHelper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;


/**
 * Response mapper for any responses that contain a file.
 *
 */
public class ResponseWithFileMapper extends AbstractResponseMapper<AbstractResponseWithFile, Request<AbstractResponseWithFile>> {

    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

    public ResponseWithFileMapper(ServiceContext serviceContext) {
        super(serviceContext);
    }


    @Override
    public void map(Request<AbstractResponseWithFile> apiRequest, AbstractResponseWithFile apiResponse, HttpServletResponse httpResponse) {
        HttpHelper.sendContent(
                httpResponse,
                apiResponse.getStatusCode(),
                apiResponse.getPayload().getContent(),
                MediaType.parse(apiResponse.getPayload().getContentType()),
                Map.of(HEADER_CONTENT_DISPOSITION, String.format(
                        "attachment; filename=\"%s\"",
                        FileHelper.getFilenameFromPath(apiResponse.getPayload().getPath()))));

    }
}
