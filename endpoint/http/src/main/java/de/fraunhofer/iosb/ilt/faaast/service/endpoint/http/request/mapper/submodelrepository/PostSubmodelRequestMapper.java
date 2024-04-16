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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.AbstractRequestMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.PostSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;


/**
 * class to map HTTP-POST-Request path: submodel.
 */
public class PostSubmodelRequestMapper extends AbstractRequestMapper {

    private static final String PATTERN = "submodels";

    public PostSubmodelRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.POST, PATTERN);
    }


    @Override
    public Request doParse(HttpRequest httpRequest, Map<String, String> urlParameters) throws InvalidRequestException {
        return PostSubmodelRequest.builder()
                .submodel(parseBody(httpRequest, Submodel.class))
                .build();
    }
}
