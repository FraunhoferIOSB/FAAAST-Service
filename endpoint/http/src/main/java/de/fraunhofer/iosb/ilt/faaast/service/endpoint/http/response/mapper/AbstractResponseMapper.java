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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.response.mapper;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;


/**
 * Base class for mapping protocol-agnostic API responses to HTTP .
 *
 * @param <T> type of the response this class can be handled
 * @param <U> type of the request
 */
public abstract class AbstractResponseMapper<T extends Response, U extends Request<T>> {

    protected final ServiceContext serviceContext;

    protected AbstractResponseMapper(ServiceContext serviceContext) {
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        this.serviceContext = serviceContext;
    }


    /**
     * @param apiRequest the API request received
     * @param apiResponse the API response that shall be sent as a response to the apiRequest
     * @param httpResponse the HTTP response object to write to
     */
    public abstract void map(U apiRequest, T apiResponse, HttpServletResponse httpResponse);


    @Override
    public int hashCode() {
        return Objects.hash(serviceContext);
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
        final AbstractResponseMapper<T, U> other = (AbstractResponseMapper<T, U>) obj;
        return Objects.equals(this.serviceContext, other.serviceContext);
    }

}
