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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.http;

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.StatusCode;
import org.eclipse.jetty.http.HttpStatus;


public class HttpStatusCode {

    private HttpStatusCode() {}


    public static int getHttpStatusCode(StatusCode statusCode) {
        switch (statusCode) {
            case Success:
                return HttpStatus.OK_200;
            case SuccessCreated:
                return HttpStatus.CREATED_201;
            case SuccessNoContent:
                return HttpStatus.NO_CONTENT_204;
            case ClientForbidden:
                return HttpStatus.FORBIDDEN_403;
            case ClientErrorBadRequest:
                return HttpStatus.BAD_REQUEST_400;
            case ClientMethodNotAllowed:
                return HttpStatus.METHOD_NOT_ALLOWED_405;
            case ClientErrorResourceNotFound:
                return HttpStatus.NOT_FOUND_404;
            case ServerInternalError:
                return HttpStatus.INTERNAL_SERVER_ERROR_500;
            case ServerErrorBadGateway:
                return HttpStatus.BAD_GATEWAY_502;
            default:
                throw new IllegalStateException(String.format("unsupported status code '%s'", statusCode.name()));
        }
    }
}
