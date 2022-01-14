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
package de.fraunhofer.iosb.ilt.faaast.service.requesthandlers.conceptdescriptions;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllConceptDescriptionsByIsCaseOfRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.response.GetAllConceptDescriptionsByIsCaseOfResponse;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.requesthandlers.RequestHandler;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.ConceptDescription;
import java.util.List;


public class GetAllConceptDescriptionsByIsCaseOfRequestHandler extends RequestHandler<GetAllConceptDescriptionsByIsCaseOfRequest, GetAllConceptDescriptionsByIsCaseOfResponse> {

    public GetAllConceptDescriptionsByIsCaseOfRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public GetAllConceptDescriptionsByIsCaseOfResponse process(GetAllConceptDescriptionsByIsCaseOfRequest request) {
        GetAllConceptDescriptionsByIsCaseOfResponse response = new GetAllConceptDescriptionsByIsCaseOfResponse();

        try {
            List<ConceptDescription> conceptDescriptions = persistence.get(null, request.getIsCaseOf(), null, request.getOutputModifier());
            response.setPayload(conceptDescriptions);
            response.setStatusCode(StatusCode.Success);
            if (conceptDescriptions != null) {
                conceptDescriptions.forEach(x -> publishElementReadEventMessage(AasUtils.toReference(x), x));
            }
        }
        catch (Exception ex) {
            response.setStatusCode(StatusCode.ServerInternalError);
        }
        return response;
    }
}
