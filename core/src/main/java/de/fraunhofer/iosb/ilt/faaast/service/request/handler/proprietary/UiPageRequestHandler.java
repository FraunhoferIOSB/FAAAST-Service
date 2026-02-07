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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.proprietary;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.proprietary.UiPageRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.proprietary.UiPageResponse;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a {@link UiPageRequest} in the service and to send the corresponding response
 * {@link UiPageResponse}. The actual HTML rendering is performed by the HTTP-specific response mapper.
 */
public class UiPageRequestHandler extends AbstractRequestHandler<UiPageRequest, UiPageResponse> {

    @Override
    public UiPageResponse process(UiPageRequest request, RequestExecutionContext context) {
        return UiPageResponse.builder().statusCode(StatusCode.SUCCESS).build();
    }
}
