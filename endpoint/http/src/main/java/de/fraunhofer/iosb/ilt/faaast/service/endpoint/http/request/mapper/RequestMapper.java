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
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.RequestContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Base class for mapping HTTP requests to protocl-agnostic requests.
 */
public abstract class RequestMapper {

    protected ServiceContext serviceContext;
    protected HttpJsonDeserializer deserializer;
    protected final HttpMethod method;
    protected String urlPattern;

    protected RequestMapper(ServiceContext serviceContext, HttpMethod method, String urlPattern, RequestContext... contextualizations) {
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        Ensure.requireNonNull(method, "method must be non-null");
        Ensure.requireNonNull(urlPattern, "urlPattern must be non-null");
        this.serviceContext = serviceContext;
        this.method = method;
        this.urlPattern = urlPattern;
        deserializer = new HttpJsonDeserializer();
        init();
    }


    private void init() {
        urlPattern = RegExHelper.ensureLineMatch(urlPattern);
    }


    /**
     * Decides if a given HTTP request matches this concrete protocl-agnostic
     * request.
     *
     * @param httpRequest the HTTP request to check
     * @return true if matches, otherwise false
     * @throws IllegalArgumentException if httpRequest is null
     */
    public boolean matches(HttpRequest httpRequest) {
        Ensure.requireNonNull(httpRequest, "httpRequest must be non-null");
        return httpRequest.getMethod().equals(method)
                && httpRequest.getPath().matches(urlPattern);
    }


    /**
     * Converts the HTTP request to protocol-agnostic request
     *
     * @param httpRequest the HTTP request to convert
     * @return the protocol-agnostic request
     * @throws InvalidRequestException if conversion fails
     * @throws IllegalArgumentException if httpRequest is null
     */
    public Request parse(HttpRequest httpRequest) throws InvalidRequestException {
        Ensure.requireNonNull(httpRequest, "httpRequest must be non-null");
        Matcher matcher = Pattern.compile(urlPattern).matcher(httpRequest.getPath());
        if (matcher.matches()) {
            return doParse(httpRequest, RegExHelper.getGroupValues(urlPattern, httpRequest.getPath()));
        }
        throw new IllegalStateException(String.format("request was matched but no suitable parser found (HTTP method: %s, URL pattern: %s", method, urlPattern));
    }


    /**
     * Converts the HTTP request to protocol-agnostic request
     *
     * @param httpRequest the HTTP request to convert
     * @param urlParameters map of named regex groups and their values
     * @return
     * @throws InvalidRequestException if conversion fails
     * @throws IllegalArgumentException if httpRequest is null
     */
    public abstract Request doParse(HttpRequest httpRequest, Map<String, String> urlParameters) throws InvalidRequestException;


    /**
     * Deserializes HTTP body to given type
     *
     * @param <T> expected type
     * @param httpRequest HTTP request
     * @param type expected type
     * @return deserialized payload
     * @throws InvalidRequestException if deserialization fails
     * @throws IllegalArgumentException if httpRequest is null
     */
    protected <T> T parseBody(HttpRequest httpRequest, Class<T> type) throws InvalidRequestException {
        Ensure.requireNonNull(httpRequest, "httpRequest must be non-null");
        try {
            return deserializer.read(httpRequest.getBody(), type);
        }
        catch (DeserializationException e) {
            throw new InvalidRequestException("error parsing body", e);
        }
    }


    /**
     * Deserializes HTTP body to a list of given type
     *
     * @param <T> expected type
     * @param httpRequest HTTP request
     * @param type expected type
     * @return deserialized payload as list of given type
     * @throws InvalidRequestException if deserialization fails
     * @throws IllegalArgumentException if httpRequest is null
     */
    protected <T> List<T> parseBodyAsList(HttpRequest httpRequest, Class<T> type) throws InvalidRequestException {
        Ensure.requireNonNull(httpRequest, "httpRequest must be non-null");
        try {
            return deserializer.readList(httpRequest.getBody(), type);
        }
        catch (DeserializationException e) {
            throw new InvalidRequestException("error parsing body", e);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(serviceContext, deserializer, method, urlPattern);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RequestMapper other = (RequestMapper) obj;
        return Objects.equals(this.urlPattern, other.urlPattern)
                && Objects.equals(this.serviceContext, other.serviceContext)
                && Objects.equals(this.deserializer, other.deserializer)
                && Objects.equals(this.method, other.method);
    }

}
