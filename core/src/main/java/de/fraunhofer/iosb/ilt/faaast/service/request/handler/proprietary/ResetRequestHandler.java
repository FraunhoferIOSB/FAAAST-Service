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

import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.proprietary.ResetRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.proprietary.ResetResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;


/**
 * Class to handle a {@link ResetRequest}
 * in the service and to send the corresponding response
 * {@link ResetResponse}. Is responsible
 * for communication with the persistence.
 */
public class ResetRequestHandler extends AbstractRequestHandler<ResetRequest, ResetResponse> {

    public ResetRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public ResetResponse process(ResetRequest request) throws ResourceNotFoundException, MessageBusException, PersistenceException {
        ResetResponse response = new ResetResponse();
        List<AssetAdministrationShell> aas = context.getPersistence().getAllAssetAdministrationShells(QueryModifier.DEFAULT, PagingInfo.ALL)
                .getContent();
        if (!request.isInternal() && Objects.nonNull(aas)) {
            aas.forEach(LambdaExceptionHelper.rethrowConsumer(
                    x -> context.getMessageBus().publish(ElementDeleteEventMessage.builder()
                            .element(x)
                            .value(x)
                            .build())));
        }
        aas.forEach(LambdaExceptionHelper.wrap(a -> context.getPersistence().deleteAssetAdministrationShell(a)));
        List<Submodel> submodel = context.getPersistence().getAllSubmodels(QueryModifier.DEFAULT, PagingInfo.ALL)
                .getContent();
        if (!request.isInternal() && Objects.nonNull(submodel)) {
            submodel.forEach(LambdaExceptionHelper.rethrowConsumer(
                    x -> context.getMessageBus().publish(ElementDeleteEventMessage.builder()
                            .element(x)
                            .value(x)
                            .build())));
        }
        submodel.forEach(LambdaExceptionHelper.wrap(s -> context.getPersistence().deleteSubmodel(s)));
        List<ConceptDescription> concept = context.getPersistence().getAllConceptDescriptions(QueryModifier.DEFAULT, PagingInfo.ALL)
                .getContent();
        if (!request.isInternal() && Objects.nonNull(concept)) {
            concept.forEach(LambdaExceptionHelper.rethrowConsumer(
                    x -> context.getMessageBus().publish(ElementDeleteEventMessage.builder()
                            .element(x)
                            .value(x)
                            .build())));
        }
        concept.forEach(LambdaExceptionHelper.wrap(c -> context.getPersistence().deleteConceptDescription(c)));
        response.setStatusCode(StatusCode.SUCCESS_NO_CONTENT);
        return response;
    }

}
