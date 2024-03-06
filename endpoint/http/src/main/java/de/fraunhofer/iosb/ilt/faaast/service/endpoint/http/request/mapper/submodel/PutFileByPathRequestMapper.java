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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.submodel;

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpConstants.HEADER_CONTENT_TYPE;

import com.google.common.net.MediaType;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.AbstractSubmodelInterfaceRequestMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.TypedInMemoryFile;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PutFileByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.PutFileByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;
import java.util.Map;


/**
 * class to map HTTP-PUT-Request paths:
 * submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/attachment,
 * shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/attachment.
 */
public class PutFileByPathRequestMapper extends AbstractSubmodelInterfaceRequestMapper<PutFileByPathRequest, PutFileByPathResponse> {

    private static final String SUBMODEL_ELEMENT_PATH = RegExHelper.uniqueGroupName();
    private static final String PATTERN = String.format("submodel-elements/%s/attachment", pathElement(SUBMODEL_ELEMENT_PATH));

    public PutFileByPathRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.PUT, PATTERN);
    }


    @Override
    public PutFileByPathRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) throws InvalidRequestException {
        MediaType contentType = MediaType.parse(httpRequest.getHeader(HEADER_CONTENT_TYPE));
        Map<String, TypedInMemoryFile> multipart = parseMultiPartBody(httpRequest, contentType);
        return PutFileByPathRequest.builder()
                .path(EncodingHelper.urlDecode(urlParameters.get(SUBMODEL_ELEMENT_PATH)))
                .content(new TypedInMemoryFile.Builder()
                        .content(multipart.get("file").getContent())
                        .contentType(multipart.get("file").getContentType())
                        .path(new String(multipart.get("fileName").getContent()))
                        .build())
                .build();
    }
}
