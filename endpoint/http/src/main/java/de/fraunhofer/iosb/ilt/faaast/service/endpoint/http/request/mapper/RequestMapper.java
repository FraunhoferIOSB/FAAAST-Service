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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import java.util.List;


/**
 * Base class for mapping HTTP requests to protocl-agnostic requests.
 */
public abstract class RequestMapper {

    protected ServiceContext serviceContext;
    protected HttpJsonDeserializer deserializer;

    protected RequestMapper(ServiceContext serviceContext) {
        deserializer = new HttpJsonDeserializer();
        this.serviceContext = serviceContext;
    }


    /**
     * Decides if a given HTTP request matches this concrete protocl-agnostic
     * request.
     *
     * @param httpRequest the HTTP request to check
     * @return true if matches, otherwise false
     */
    public abstract boolean matches(HttpRequest httpRequest);


    /**
     * Converts the HTTP request to protocol-agnostic request
     *
     * @param httpRequest the HTTP request to convert
     * @return the protocol-agnostic request
     * @throws InvalidRequestException if conversion fails
     */
    public abstract Request parse(HttpRequest httpRequest) throws InvalidRequestException;


    /**
     * Deserializes HTTP body to given type
     *
     * @param <T> expected type
     * @param httpRequest HTTP request
     * @param type expected type
     * @return deserialized payload
     * @throws InvalidRequestException if deserialization fails
     */
    protected <T> T parseBody(HttpRequest httpRequest, Class<T> type) throws InvalidRequestException {
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
     */
    protected <T> List<T> parseBodyAsList(HttpRequest httpRequest, Class<T> type) throws InvalidRequestException {
        try {
            return deserializer.readList(httpRequest.getBody(), type);
        }
        catch (DeserializationException e) {
            throw new InvalidRequestException("error parsing body", e);
        }
    }

}
