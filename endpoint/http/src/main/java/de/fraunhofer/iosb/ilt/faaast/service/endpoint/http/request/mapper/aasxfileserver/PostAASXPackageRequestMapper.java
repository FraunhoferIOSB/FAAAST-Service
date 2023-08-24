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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.aasxfileserver;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.AbstractRequestMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import java.util.Map;


/**
 * class to map HTTP-POST-Request path: packages.
 */
public class PostAASXPackageRequestMapper extends AbstractRequestMapper {

    private static final String PATTERN = "packages";

    public PostAASXPackageRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.POST, PATTERN);
    }


    @Override
    public Request doParse(HttpRequest httpRequest, Map<String, String> urlParameters) throws InvalidRequestException {
        // PostAASXPackageRequest request = new PostAASXPackageRequest();
        throw new InvalidRequestException("PostAASXPackage currently not supported");
        // TODO: Needs specialy handling in HTTP server because it is 'multipart/form-data'
        // commented out for now
        // aasIds --> Identifier[]
        // file --> byte[]
        // filename --> String
        //        request.setFilename(IdGenerator.parseFilename(httpRequest.getBody()));
        //        request.setFile(IdGenerator.parseFile(httpRequest.getBody()));
        //        request.setAasIds(IdGenerator.parseAssetIds(httpRequest.getBody()));
        //return request;
    }
}
