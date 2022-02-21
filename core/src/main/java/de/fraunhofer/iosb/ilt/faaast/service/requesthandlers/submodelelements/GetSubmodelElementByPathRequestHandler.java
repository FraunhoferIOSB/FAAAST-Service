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
package de.fraunhofer.iosb.ilt.faaast.service.requesthandlers.submodelelements;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.requesthandlers.RequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.requesthandlers.Util;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueMapper;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.util.Objects;


public class GetSubmodelElementByPathRequestHandler extends RequestHandler<GetSubmodelElementByPathRequest, GetSubmodelElementByPathResponse> {

    public GetSubmodelElementByPathRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public GetSubmodelElementByPathResponse process(GetSubmodelElementByPathRequest request) {
        GetSubmodelElementByPathResponse response = new GetSubmodelElementByPathResponse();
        try {
            Reference reference = Util.toReference(request.getPath(), request.getId(), Submodel.class);
            SubmodelElement submodelElement = persistence.get(reference, request.getOutputModifier());
            ElementValue oldValue = ElementValueMapper.toValue(submodelElement);

            //read value from AssetConnection if one exist
            //and update value in persistence if differs
            ElementValue valueFromAssetConnection = readDataElementValueFromAssetConnection(reference);
            if (valueFromAssetConnection != null
                    && !Objects.equals(valueFromAssetConnection, oldValue)) {
                submodelElement = ElementValueMapper.setValue(submodelElement, valueFromAssetConnection);
                persistence.put(null, reference, submodelElement);
                // TODO @Jens
                // better publishValueChangeEventMessage(reference, oldValue, oldValue) ???
                publishElementUpdateEventMessage(reference, submodelElement);
            }

            response.setPayload(submodelElement);
            response.setStatusCode(StatusCode.Success);
            publishElementReadEventMessage(reference, submodelElement);

        }
        catch (ResourceNotFoundException ex) {
            response.setStatusCode(StatusCode.ClientErrorResourceNotFound);
        }
        catch (Exception ex) {
            response.setStatusCode(StatusCode.ServerInternalError);
        }
        return response;
    }
}
