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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.aasserialization;

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.EnvironmentContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.FileContent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasserialization.GenerateSerializationByIdsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasserialization.GenerateSerializationByIdsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.ConceptDescriptionSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.InMemoryFile;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasserialization.GenerateSerializationByIdsRequest} in
 * the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasserialization.GenerateSerializationByIdsResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GenerateSerializationByIdsRequestHandler extends AbstractRequestHandler<GenerateSerializationByIdsRequest, GenerateSerializationByIdsResponse> {

    private static final OutputModifier OUTPUT_MODIFIER = new OutputModifier.Builder()
            .content(Content.NORMAL)
            .extend(Extent.WITH_BLOB_VALUE)
            .level(Level.DEEP)
            .build();

    public GenerateSerializationByIdsRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public GenerateSerializationByIdsResponse process(GenerateSerializationByIdsRequest request) throws ResourceNotFoundException, SerializationException {
        DefaultEnvironment environment = new DefaultEnvironment.Builder()
                .assetAdministrationShells(
                        request.getAasIds().stream()
                                .map(LambdaExceptionHelper.rethrowFunction(x -> context.getPersistence().getAssetAdministrationShell(x, OUTPUT_MODIFIER)))
                                .collect(Collectors.toList()))
                .submodels(request.getSubmodelIds().stream()
                        .map(LambdaExceptionHelper.rethrowFunction(x -> context.getPersistence().getSubmodel(x, OUTPUT_MODIFIER)))
                        .collect(Collectors.toList()))
                .conceptDescriptions(request.getIncludeConceptDescriptions()
                        ? context.getPersistence().findConceptDescriptions(
                                ConceptDescriptionSearchCriteria.NONE,
                                OUTPUT_MODIFIER,
                                PagingInfo.ALL)
                                .getContent()
                        : List.of())
                .build();
        List<InMemoryFile> files = new ArrayList<>();
        Map<String, FileContent> fileMap = context.getFileStorage().getAllFiles();
        fileMap.forEach((key, value) -> {
            files.add(new InMemoryFile(value.getContent(), key));
        });
        return GenerateSerializationByIdsResponse.builder()
                .dataformat(request.getSerializationFormat())
                .payload(EnvironmentContext.builder()
                        .environment(environment)
                        .files(files)
                        .build())
                .success()
                .build();
    }
}
