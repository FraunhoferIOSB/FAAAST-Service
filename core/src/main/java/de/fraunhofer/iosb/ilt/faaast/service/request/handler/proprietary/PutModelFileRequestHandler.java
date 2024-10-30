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

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.environment.deserializer.AasxEnvironmentDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.environment.deserializer.JsonEnvironmentDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.EnvironmentContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.proprietary.PutModelFileRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.proprietary.PutModelFileResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;


/**
 * Class to handle a {@link PutModelFileRequest}.
 */
public class PutModelFileRequestHandler extends AbstractRequestHandler<PutModelFileRequest, PutModelFileResponse> {

    public PutModelFileRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public PutModelFileResponse process(PutModelFileRequest request)
            throws ResourceNotFoundException, MessageBusException, IOException, PersistenceException, DeserializationException {
        EnvironmentContext environmentContext = null;
        if (request.getContent().getContentType().equals("application/json")) {
            JsonEnvironmentDeserializer deserializer = new JsonEnvironmentDeserializer();
            environmentContext = deserializer.read(new ByteArrayInputStream(request.getContent().getContent()));
        }
        else {
            AasxEnvironmentDeserializer deserializer = new AasxEnvironmentDeserializer();
            environmentContext = deserializer.read(new ByteArrayInputStream(request.getContent().getContent()));
        }

        List<AssetAdministrationShell> aas = null;
        List<Submodel> submodel = null;
        List<ConceptDescription> concept = null;
        if (Objects.nonNull(environmentContext)) {
            aas = environmentContext.getEnvironment().getAssetAdministrationShells();
            aas.forEach(LambdaExceptionHelper.wrap(a -> context.getPersistence().save(a)));
            submodel = environmentContext.getEnvironment().getSubmodels();
            submodel.forEach(LambdaExceptionHelper.wrap(s -> context.getPersistence().save(s)));
            concept = environmentContext.getEnvironment().getConceptDescriptions();
            concept.forEach(LambdaExceptionHelper.wrap(c -> context.getPersistence().save(c)));
        }

        if (!request.isInternal()) {
            if (Objects.nonNull(aas)) {
                aas.forEach(LambdaExceptionHelper.rethrowConsumer(
                        x -> context.getMessageBus().publish(ElementCreateEventMessage.builder()
                                .element(x)
                                .value(x)
                                .build())));
            }
            if (Objects.nonNull(submodel)) {
                submodel.forEach(LambdaExceptionHelper.rethrowConsumer(
                        x -> context.getMessageBus().publish(ElementCreateEventMessage.builder()
                                .element(x)
                                .value(x)
                                .build())));
            }
            if (Objects.nonNull(concept)) {
                concept.forEach(LambdaExceptionHelper.rethrowConsumer(
                        x -> context.getMessageBus().publish(ElementCreateEventMessage.builder()
                                .element(x)
                                .value(x)
                                .build())));
            }
        }
        return PutModelFileResponse.builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
    }

}
