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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.description;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.ServiceDescription;
import de.fraunhofer.iosb.ilt.faaast.service.model.ServiceSpecificationProfile;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.description.GetSelfDescriptionRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.description.GetSelfDescriptionResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.GetSelfDescriptionRequest}.
 */
public class GetSelfDescriptionRequestHandler extends AbstractRequestHandler<GetSelfDescriptionRequest, GetSelfDescriptionResponse> {

    public GetSelfDescriptionRequestHandler(CoreConfig coreConfig, Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(coreConfig, persistence, messageBus, assetConnectionManager);
    }


    @Override
    public GetSelfDescriptionResponse process(GetSelfDescriptionRequest request) throws ResourceNotFoundException, AssetConnectionException, ValueMappingException, Exception {
        return GetSelfDescriptionResponse.builder()
                .payload(ServiceDescription.builder()
                        .profile(ServiceSpecificationProfile.AAS_FULL)
                        .profile(ServiceSpecificationProfile.AAS_REPOSITORY_FULL)
                        .profile(ServiceSpecificationProfile.CONCEPT_DESCRIPTION_FULL)
                        .profile(ServiceSpecificationProfile.DISCOVERY_FULL)
                        .profile(ServiceSpecificationProfile.SUBMODEL_FULL)
                        .profile(ServiceSpecificationProfile.SUBMODEL_REPOSITORY_FULL)
                        .build())
                .build();
    }

}
