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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.dpp;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.dpp.ReadDPPByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.dpp.ReadDPPByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.dpp.DigitalProductPassport;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;


/**
 * Class to handle a {@link ReadDPPByIdRequest} in the service and to send the corresponding
 * {@link ReadDPPByIdResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class ReadDPPByIdRequestHandler extends AbstractDPPRequestHandler<ReadDPPByIdRequest, ReadDPPByIdResponse> {
    @Override
    public ReadDPPByIdResponse process(ReadDPPByIdRequest request, RequestExecutionContext context) throws Exception {
        AssetAdministrationShell aas = context.getPersistence().getAssetAdministrationShell(request.getDppId(), QueryModifier.MAXIMAL);

        DigitalProductPassport dpp = buildFrom(aas, context.getPersistence());

        if (!request.isInternal()) {
            distributeReadEvents(dpp, context.getMessageBus());
        }

        return ReadDPPByIdResponse.builder().payload(dpp).success().build();
    }

}
