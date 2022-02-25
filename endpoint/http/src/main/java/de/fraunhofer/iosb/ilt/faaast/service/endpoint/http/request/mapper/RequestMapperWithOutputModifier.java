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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extend;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;


/**
 * Base class for mapping HTTP requests including output modifier information.
 */
public abstract class RequestMapperWithOutputModifier extends RequestMapper {

    private static final String PARAMETER_LEVEL = "level";
    private static final String PARAMETER_CONTENT = "content";
    private static final String PARAMETER_EXTEND = "extend";

    public RequestMapperWithOutputModifier(ServiceContext serviceContext) {
        super(serviceContext);
    }


    /**
     * Converts the HTTP request to protocol-agnostic request including output
     * modifier information
     *
     * @param httpRequest the HTTP request to convert
     * @param outputModifier output modifier for this request
     * @return the protocol-agnostic request
     * @throws InvalidRequestException if conversion fails
     */
    public abstract Request parse(HttpRequest httpRequest, OutputModifier outputModifier) throws InvalidRequestException;


    @Override
    public Request parse(HttpRequest httpRequest) throws InvalidRequestException {
        OutputModifier.Builder outputModifier = new OutputModifier.Builder();
        if (httpRequest.hasQueryParameter(PARAMETER_LEVEL)) {
            outputModifier.level(Level.fromString(httpRequest.getQueryParameter(PARAMETER_LEVEL)));
        }
        if (httpRequest.hasQueryParameter(PARAMETER_CONTENT)) {
            outputModifier.content(Content.fromString(httpRequest.getQueryParameter(PARAMETER_CONTENT)));
        }
        if (httpRequest.hasQueryParameter(PARAMETER_EXTEND)) {
            outputModifier.extend(Extend.fromString(httpRequest.getQueryParameter(PARAMETER_EXTEND)));
        }
        return parse(httpRequest, outputModifier.build());
    }
}
