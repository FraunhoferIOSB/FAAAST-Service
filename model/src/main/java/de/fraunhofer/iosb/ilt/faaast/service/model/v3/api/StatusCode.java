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
package de.fraunhofer.iosb.ilt.faaast.service.model.v3.api;

/**
 * Enum StatusCode
 * Also maps internal status enum to http status code
 */
public enum StatusCode {
    Success(200),
    SuccessCreated(201),
    SuccessNoContent(204),
    ClientForbidden(403),
    ClientErrorBadRequest(400),
    ClientMethodNotAllowed(405),
    ClientErrorResourceNotFound(404),
    ServerInternalError(500),
    ServerErrorBadGateway(502);

    int statusCode;

    StatusCode(int statusCode) {
        this.statusCode = statusCode;
    }


    public int getStatusCode() {
        return this.statusCode;
    }
}
