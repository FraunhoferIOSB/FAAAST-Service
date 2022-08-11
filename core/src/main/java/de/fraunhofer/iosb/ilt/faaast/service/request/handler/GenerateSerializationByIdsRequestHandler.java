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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GenerateSerializationByIdsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GenerateSerializationByIdsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Not supported yet! Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.GenerateSerializationByIdsRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GenerateSerializationByIdsResponse}.
 * Is responsible for communication with the persistence and sends the
 * corresponding events to the message bus.
 */
public class GenerateSerializationByIdsRequestHandler extends AbstractRequestHandler<GenerateSerializationByIdsRequest, GenerateSerializationByIdsResponse> {

    private static final OutputModifier OUTPUT_MODIFIER = new OutputModifier.Builder()
            .content(Content.NORMAL)
            .extend(Extent.WITH_BLOB_VALUE)
            .level(Level.DEEP)
            .build();

    public GenerateSerializationByIdsRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public GenerateSerializationByIdsResponse process(GenerateSerializationByIdsRequest request) throws ResourceNotFoundException {
        return GenerateSerializationByIdsResponse.builder()
                .dataformat(request.getSerializationFormat())
                .payload(new DefaultAssetAdministrationShellEnvironment.Builder()
                        .assetAdministrationShells(
                                request.getAasIds().stream()
                                        .map(LambdaExceptionHelper.rethrowFunction(x -> (AssetAdministrationShell) persistence.get(x, OUTPUT_MODIFIER)))
                                        .collect(Collectors.toList()))
                        .submodels(request.getSubmodelIds().stream()
                                .map(LambdaExceptionHelper.rethrowFunction(x -> (Submodel) persistence.get(x, OUTPUT_MODIFIER)))
                                .collect(Collectors.toList()))
                        .conceptDescriptions(request.getIncludeConceptDescriptions()
                                ? persistence.getEnvironment().getConceptDescriptions()
                                : List.of())
                        .build())
                .success()
                .build();
    }
}
