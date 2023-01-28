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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception;

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;


/**
 * Exception to indicate there is no mapper for a path.
 */
public class RequestMapperNotFoundException extends InvalidRequestException {

    public RequestMapperNotFoundException(HttpRequest request) {
        super(String.format("no matching request mapper found for URL '%s'", request.getPath()));
    }
}
