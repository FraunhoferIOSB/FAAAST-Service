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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;


/**
 * A context that can be applied to requests, e.g. when combining APIs as
 * defined in AAS specification Part 2.
 */
public interface RequestContext {

    public static final String PATTERN_LINE_START = "^";
    public static final String PATTERN_LINE_END = "$";

    /**
     * Initializes the context
     * 
     * @param serviceContext service context, e.g. to resolve/validate context-related arguments
     * @param baseUrlPattern base URL pattern of the vanilla request
     */
    public void init(ServiceContext serviceContext, String baseUrlPattern);


    /**
     * Checks if given URL matches the contextualized request
     * 
     * @param url the URL to match
     * @return true if matches, false otherweise
     */
    public boolean matches(String url);


    /**
     * Validates the contextualized part of an HTTP request, e.g. if provided parameters are valid.
     * 
     * @param httpRequest the request to validate
     * @throws InvalidRequestException if validation fails
     */
    public void validate(HttpRequest httpRequest) throws InvalidRequestException;


    /**
     * Decontextualizes a URL, meaning stripping the context-related part leaving only the part that the base request is
     * able to process.
     * 
     * @param url the contextualized URL
     * @return the de-contextualized URL
     */
    public String decontextualize(String url);
}
