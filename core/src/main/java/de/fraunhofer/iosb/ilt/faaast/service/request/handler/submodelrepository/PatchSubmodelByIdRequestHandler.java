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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.submodelrepository;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.PatchSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.PatchSubmodelByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValidationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ModelValidator;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.PatchSubmodelByIdRequest}.
 */
public class PatchSubmodelByIdRequestHandler extends AbstractRequestHandler<PatchSubmodelByIdRequest, PatchSubmodelByIdResponse> {

    public PatchSubmodelByIdRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public PatchSubmodelByIdResponse process(PatchSubmodelByIdRequest request)
            throws ResourceNotFoundException, AssetConnectionException, ValueMappingException, MessageBusException, ValidationException, ResourceNotAContainerElementException,
            InvalidRequestException {
        Submodel current = context.getPersistence().getSubmodel(request.getId(), QueryModifier.DEFAULT);
        Submodel updated = applyMergePatch(request.getChanges(), current, Submodel.class);
        ModelValidator.validate(updated, context.getCoreConfig().getValidationOnUpdate());
        context.getPersistence().save(updated);
        Reference reference = ReferenceBuilder.forSubmodel(updated);
        cleanupDanglingAssetConnectionsForParent(reference, context.getPersistence());
        syncWithAsset(reference, updated.getSubmodelElements(), !request.isInternal());
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementUpdateEventMessage.builder()
                    .element(reference)
                    .value(updated)
                    .build());
        }
        return PatchSubmodelByIdResponse.builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
    }

}
