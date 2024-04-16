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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PatchSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.PatchSubmodelResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import java.util.Map;


/**
 * class to map HTTP-PATCH-Request paths: submodels/{submodelIdentifier}/submodel,
 * shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel.
 */
public class PatchSubmodelRequestMapper extends AbstractSubmodelInterfaceRequestMapper<PatchSubmodelRequest, PatchSubmodelResponse> {

    private static final String PATTERN = "";

    public PatchSubmodelRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.PATCH, PATTERN);
    }


    @Override
    public PatchSubmodelRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) throws InvalidRequestException {
        return PatchSubmodelRequest.builder()
                .changes(parseMergePatch(httpRequest.getBodyAsString()))
                .build();
    }
}
