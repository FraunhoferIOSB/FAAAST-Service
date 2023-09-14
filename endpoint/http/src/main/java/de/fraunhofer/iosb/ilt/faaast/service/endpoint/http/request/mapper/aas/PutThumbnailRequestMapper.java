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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.aas;

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpConstants.HEADER_CONTENT_TYPE;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.AbstractRequestMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.FileContent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.PutThumbnailRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.BlobValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;
import java.util.Map;
import org.apache.http.entity.ContentType;


/**
 * class to map HTTP-PUT-Request path: shells/{aasIdentifier}/aas/asset-information/thumbnail.
 */
public class PutThumbnailRequestMapper extends AbstractRequestMapper {

    private static final String AAS_ID = RegExHelper.uniqueGroupName();
    private static final String PATTERN = String.format("shells/%s/aas/asset-information/thumbnail", pathElement(AAS_ID));

    public PutThumbnailRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.PUT, PATTERN);
    }


    @Override
    public Request doParse(HttpRequest httpRequest, Map<String, String> urlParameters) throws InvalidRequestException {
        ContentType contentType = ContentType.parse(httpRequest.getHeader(HEADER_CONTENT_TYPE));
        Map<String, BlobValue> multipart = parseMultiPartBody(httpRequest, contentType);
        return PutThumbnailRequest.builder()
                .id(EncodingHelper.base64UrlDecode(urlParameters.get(AAS_ID)))
                .content(FileContent.builder()
                        .content(multipart.get("file").getValue())
                        .path(new String(multipart.get("fileName").getValue()))
                        .build())
                .contentType(multipart.get("file").getContentType())
                .build();
    }
}
