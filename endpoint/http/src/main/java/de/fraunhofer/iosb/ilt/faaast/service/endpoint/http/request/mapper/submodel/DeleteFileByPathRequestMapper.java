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

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.AbstractSubmodelInterfaceRequestMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.DeleteFileByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.DeleteFileByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;
import java.util.Map;


/**
 * class to map HTTP-DELETE-Request paths:
 * submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/attachment,
 * shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/attachment.
 */
public class DeleteFileByPathRequestMapper extends AbstractSubmodelInterfaceRequestMapper<DeleteFileByPathRequest, DeleteFileByPathResponse> {

    private static final String SUBMODEL_ELEMENT_PATH = RegExHelper.uniqueGroupName();
    private static final String PATTERN = String.format("submodel-elements/%s/attachment", pathElement(SUBMODEL_ELEMENT_PATH));

    public DeleteFileByPathRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.DELETE, PATTERN);
    }


    @Override
    public DeleteFileByPathRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) throws InvalidRequestException {
        return DeleteFileByPathRequest.builder()
                .path(EncodingHelper.urlDecode(urlParameters.get(SUBMODEL_ELEMENT_PATH)))
                .build();
    }
}
