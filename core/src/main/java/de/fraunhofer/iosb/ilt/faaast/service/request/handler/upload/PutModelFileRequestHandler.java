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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.upload;

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.environment.deserializer.AasxEnvironmentDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.environment.deserializer.JsonEnvironmentDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.EnvironmentContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.upload.PutModelFileRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.upload.PutModelFileResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;


/**
 * Class to handle a {@link PutModelFile}.
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
        if (Objects.nonNull(environmentContext)) {
            environmentContext.getEnvironment().getAssetAdministrationShells().forEach(LambdaExceptionHelper.wrap(a -> context.getPersistence().save(a)));
            environmentContext.getEnvironment().getSubmodels().forEach(LambdaExceptionHelper.wrap(a -> context.getPersistence().save(a)));
            environmentContext.getEnvironment().getConceptDescriptions().forEach(LambdaExceptionHelper.wrap(a -> context.getPersistence().save(a)));
        }
        return PutModelFileResponse.builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
    }

}
