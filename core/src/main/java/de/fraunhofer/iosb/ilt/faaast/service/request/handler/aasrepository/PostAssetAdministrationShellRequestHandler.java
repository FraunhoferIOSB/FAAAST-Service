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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.PostAssetAdministrationShellRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.PostAssetAdministrationShellResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ModelValidator;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.PostAssetAdministrationShellRequest} in
 * the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.PostAssetAdministrationShellResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class PostAssetAdministrationShellRequestHandler extends AbstractRequestHandler<PostAssetAdministrationShellRequest, PostAssetAdministrationShellResponse> {

    public PostAssetAdministrationShellRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public PostAssetAdministrationShellResponse process(PostAssetAdministrationShellRequest request) throws Exception {
        ModelValidator.validate(request.getAas(), context.getCoreConfig().getValidationOnCreate());
        if (context.getPersistence().assetAdministrationShellExists(request.getAas().getId())) {
            throw new ResourceAlreadyExistsException(request.getAas().getId(), AssetAdministrationShell.class);
        }
        context.getPersistence().save(request.getAas());
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementCreateEventMessage.builder()
                    .element(request.getAas())
                    .value(request.getAas())
                    .build());
        }
        return PostAssetAdministrationShellResponse.builder()
                .payload(request.getAas())
                .created()
                .build();
    }
}
