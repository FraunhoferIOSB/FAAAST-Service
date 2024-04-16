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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.aasrepository;

import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.GetAllAssetAdministrationShellsByIdShortResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AssetAdministrationShellSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsByIdShortRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.GetAllAssetAdministrationShellsByIdShortResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetAllAssetAdministrationShellsByIdShortRequestHandler
        extends AbstractRequestHandler<GetAllAssetAdministrationShellsByIdShortRequest, GetAllAssetAdministrationShellsByIdShortResponse> {

    public GetAllAssetAdministrationShellsByIdShortRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public GetAllAssetAdministrationShellsByIdShortResponse process(GetAllAssetAdministrationShellsByIdShortRequest request) throws MessageBusException {
        Page<AssetAdministrationShell> page = context.getPersistence().findAssetAdministrationShells(
                AssetAdministrationShellSearchCriteria.builder()
                        .idShort(request.getIdShort())
                        .build(),
                request.getOutputModifier(),
                request.getPagingInfo());
        if (!request.isInternal() && Objects.nonNull(page.getContent())) {
            page.getContent().forEach(LambdaExceptionHelper.rethrowConsumer(
                    x -> context.getMessageBus().publish(ElementReadEventMessage.builder()
                            .element(x)
                            .value(x)
                            .build())));
        }
        return GetAllAssetAdministrationShellsByIdShortResponse.builder()
                .payload(page)
                .success()
                .build();
    }

}
