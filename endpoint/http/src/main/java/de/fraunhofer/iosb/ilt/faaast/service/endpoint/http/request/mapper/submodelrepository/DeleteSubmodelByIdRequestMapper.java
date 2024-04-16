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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.submodelrepository;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.AbstractSubmodelInterfaceRequestMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.DeleteSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.DeleteSubmodelByIdResponse;
import java.util.Map;


/**
 * class to map HTTP-DELETE-Request paths: submodels/{submodelIdentifier},
 * shells/{aasIdentifier}/submodels/{submodelIdentifier}.
 */
public class DeleteSubmodelByIdRequestMapper extends AbstractSubmodelInterfaceRequestMapper<DeleteSubmodelByIdRequest, DeleteSubmodelByIdResponse> {

    private static final String PATTERN = "";

    public DeleteSubmodelByIdRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.DELETE, PATTERN, Content.METADATA, Content.PATH, Content.REFERENCE, Content.VALUE);
    }


    @Override
    public DeleteSubmodelByIdRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) {
        return DeleteSubmodelByIdRequest.builder().build();
    }
}
