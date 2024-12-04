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
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.StreamHelper;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to handle a {@link ResetRequest}
 * in the service and to send the corresponding response
 * {@link ResetResponse}. Is responsible
 * for communication with the persistence.
 */
public class ResetRequestHandler extends AbstractRequestHandler<ResetRequest, ResetResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResetRequestHandler.class);

    @Override
    public ResetResponse process(ResetRequest request, RequestExecutionContext context) {
        try {
            context.getAssetConnectionManager().reset();
            context.getPersistence().deleteAll();
            context.getFileStorage().deleteAll();
            StreamHelper.concat(
                    context.getPersistence().getAllAssetAdministrationShells(QueryModifier.MINIMAL, PagingInfo.ALL).getContent().stream(),
                    context.getPersistence().getAllSubmodels(QueryModifier.MINIMAL, PagingInfo.ALL).getContent().stream(),
                    context.getPersistence().getAllConceptDescriptions(QueryModifier.MINIMAL, PagingInfo.ALL).getContent().stream())
                    .forEach(x -> {
                        try {
                            context.getMessageBus().publish(ElementDeleteEventMessage.builder()
                                    .element(x)
                                    .value(x)
                                    .build());
                        }
                        catch (MessageBusException e) {
                            LOGGER.warn("Publishing ElementDeleteEvent on message bus after reset failed (reference: {})", AasUtils.toReference(x));
                        }
                    });
            return ResetResponse.builder().statusCode(StatusCode.SUCCESS_NO_CONTENT).build();
        }
        catch (PersistenceException e) {
            throw new IllegalStateException("Error resetting FA³ST Service - the server might now be in an undefined and unstable state. It is recommended to restart the server",
                    e);
        }
    }

}
