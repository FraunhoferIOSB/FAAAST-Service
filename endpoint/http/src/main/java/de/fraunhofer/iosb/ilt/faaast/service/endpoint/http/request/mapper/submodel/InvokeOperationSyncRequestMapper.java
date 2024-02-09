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
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.AbstractSubmodelInterfaceRequestMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.InvokeOperationSyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;
import java.util.Map;


/**
 * class to map HTTP-POST-Request paths: submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/invoke,
 * shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/invoke.
 */
public class InvokeOperationSyncRequestMapper extends AbstractSubmodelInterfaceRequestMapper<InvokeOperationSyncRequest, InvokeOperationSyncResponse> {

    private static final String SUBMODEL_ELEMENT_PATH = RegExHelper.uniqueGroupName();
    private static final String PATTERN = String.format("submodel-elements/%s/invoke(/\\$value)?", pathElement(SUBMODEL_ELEMENT_PATH));

    public InvokeOperationSyncRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.POST, PATTERN);
    }


    @Override
    public InvokeOperationSyncRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) throws InvalidRequestException {
        InvokeOperationSyncRequest result;
        SubmodelElementIdentifier identifier = SubmodelElementIdentifier.builder()
                .submodelId(EncodingHelper.base64UrlDecode(urlParameters.get(SUBMODEL_ID)))
                .idShortPath(IdShortPath.parse(EncodingHelper.urlDecode(urlParameters.get(SUBMODEL_ELEMENT_PATH))))
                .build();
        if (outputModifier.getContent() == Content.VALUE) {
            try {
                result = deserializer.readValueOperationRequest(
                        httpRequest.getBodyAsString(),
                        InvokeOperationSyncRequest.class,
                        serviceContext,
                        identifier);
            }
            catch (DeserializationException e) {
                throw new InvalidRequestException(e);
            }
        }
        else {
            result = parseBody(httpRequest, InvokeOperationSyncRequest.class);
        }
        result.setSubmodelId(identifier.getSubmodelId());
        result.setPath(identifier.getIdShortPath().toString());
        return result;
    }
}
