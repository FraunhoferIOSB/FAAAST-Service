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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.SubmodelInterfaceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import java.util.Objects;


/**
 * Abstract base class for handling request that are part of the Submodel
 * Interface. If request is made in the context of an AAS, this handler
 * validates that the AAS exists and the requested submodel is part of that AAS.
 *
 * @param <T> actual type of the request
 * @param <U> actual type of the response
 */
public abstract class SubmodelInterfaceRequestHandler<T extends SubmodelInterfaceRequest<U>, U extends Response> extends RequestHandler<T, U> {

    protected SubmodelInterfaceRequestHandler(Persistence<?> persistence, MessageBus<?> messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public U process(T request) throws Exception {
        Ensure.requireNonNull(request, "request must be non-null");
        Ensure.requireNonNull(request.getSubmodelId(), "request.submodelId must be non-null");
        validateSubmodelWithinAAS(request);
        return doProcess(request);
    }


    /**
     * Validates if the AAS exists and the submodel belongs to that AAS if this
     * request was made in the context of an AAS-
     *
     * @param request the request
     * @throws ResourceNotFoundException if AAS does not exist or submodel does
     *             not belong to AAS
     */
    protected void validateSubmodelWithinAAS(T request) throws ResourceNotFoundException {
        if (request.getAasId() != null) {
            Reference submodelRef = ReferenceHelper.toReference(request.getSubmodelId(), Submodel.class);
            if (((AssetAdministrationShell) persistence.get(
                    request.getAasId(),
                    new OutputModifier.Builder()
                            .level(Level.CORE)
                            .build())).getSubmodels().stream().noneMatch(x -> Objects.equals(x, submodelRef))) {
                throw new ResourceNotFoundException(String.format(
                        "AAS does not contain requested submodel (aasId: %s, submodelId: %s)",
                        request.getAasId().getIdentifier(),
                        request.getSubmodelId().getIdentifier()));
            }
        }
    }


    /**
     * Processes a request and returns the resulting response
     *
     * @param request the request
     * @return the response
     * @throws Exception if processing the request fails
     */
    protected abstract U doProcess(T request) throws Exception;
}
