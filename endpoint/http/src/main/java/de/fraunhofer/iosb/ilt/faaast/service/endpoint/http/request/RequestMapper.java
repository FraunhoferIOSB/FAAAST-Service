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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.http.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.DeserializationException;
import java.util.List;


/**
 * Base class for implementing code to execute a given Request.
 */
public abstract class RequestMapper {

    protected ServiceContext serviceContext;
    protected HttpJsonDeserializer deserializer;

    protected RequestMapper(ServiceContext serviceContext) {
        deserializer = new HttpJsonDeserializer();
        this.serviceContext = serviceContext;
    }


    public abstract boolean matches(HttpRequest httpRequest);


    public abstract Request parse(HttpRequest httpRequest) throws InvalidRequestException;


    protected <T> T parseBody(HttpRequest httpRequest, Class<T> type) throws InvalidRequestException {
        try {
            return deserializer.read(httpRequest.getBody(), type);
        }
        catch (DeserializationException ex) {
            throw new InvalidRequestException("error parsing body", ex);
        }
    }


    protected <T> List<T> parseBodyAsList(HttpRequest httpRequest, Class<T> type) throws InvalidRequestException {
        try {
            return deserializer.readList(httpRequest.getBody(), type);
        }
        catch (DeserializationException ex) {
            throw new InvalidRequestException("error parsing body", ex);
        }
    }

}
