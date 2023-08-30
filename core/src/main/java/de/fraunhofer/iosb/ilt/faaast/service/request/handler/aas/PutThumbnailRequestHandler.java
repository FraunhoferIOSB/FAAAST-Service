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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.aas;

import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.PutThumbnailRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.PutThumbnailResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;


/**
 * Class to handle a {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.PutThumbnailRequest}.
 */
public class PutThumbnailRequestHandler extends AbstractRequestHandler<PutThumbnailRequest, PutThumbnailResponse> {

    public PutThumbnailRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public PutThumbnailResponse process(PutThumbnailRequest request) throws ResourceNotFoundException, MessageBusException {
        AssetAdministrationShell aas = context.getPersistence().getAssetAdministrationShell(request.getId(), QueryModifier.DEFAULT);
        if (Objects.isNull(aas.getAssetInformation())
                || Objects.isNull(aas.getAssetInformation().getDefaultThumbnail())
                || StringHelper.isBlank(aas.getAssetInformation().getDefaultThumbnail().getPath())) {
            throw new ResourceNotFoundException(String.format("no thumbnail information set for AAS (id: %s)", request.getId()));
        }
        String path = aas.getAssetInformation().getDefaultThumbnail().getPath();
        context.getFileStorage().save(path, request.getContent());
        // maybe publish event on messageBus
        //     context.getMessageBus()publish();
        return PutThumbnailResponse.builder()
                .success()
                .build();
    }

}
