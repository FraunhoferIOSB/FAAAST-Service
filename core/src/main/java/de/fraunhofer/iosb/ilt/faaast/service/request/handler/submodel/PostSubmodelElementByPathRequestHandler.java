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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.submodel;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PostSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.PostSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValidationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ModelValidator;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PostSubmodelElementByPathRequest} in the
 * service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.PostSubmodelElementByPathResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class PostSubmodelElementByPathRequestHandler extends AbstractSubmodelInterfaceRequestHandler<PostSubmodelElementByPathRequest, PostSubmodelElementByPathResponse> {

    @Override
    public PostSubmodelElementByPathResponse doProcess(PostSubmodelElementByPathRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, ValueMappingException, ValidationException, ResourceNotAContainerElementException, AssetConnectionException, MessageBusException,
            ResourceAlreadyExistsException, PersistenceException {
        ModelValidator.validate(request.getSubmodelElement(), context.getCoreConfig().getValidationOnCreate());
        IdShortPath idShortPath = IdShortPath.parse(request.getPath());
        Reference parentReference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(idShortPath)
                .build();
        ReferenceBuilder childSubReferenceBuilder = new ReferenceBuilder();
        if (idShortPath.isEmpty()) {
            childSubReferenceBuilder.element(request.getSubmodelElement().getIdShort());
        }
        else {
            SubmodelElement parent = context.getPersistence().getSubmodelElement(parentReference, QueryModifier.DEFAULT);
            KeyTypes keyType = ReferenceHelper.toKeyType(request.getSubmodelElement().getClass());
            if (SubmodelElementList.class.isAssignableFrom(parent.getClass())) {
                childSubReferenceBuilder.index(((SubmodelElementList) parent).getValue().size(), keyType);
                ReferenceHelper.getEffectiveKey(parentReference).setType(KeyTypes.SUBMODEL_ELEMENT_LIST);
            }
            else {
                childSubReferenceBuilder.element(request.getSubmodelElement().getIdShort(), keyType);
            }
        }
        Reference childReference = ReferenceHelper.combine(parentReference, childSubReferenceBuilder.build());
        if (context.getPersistence().submodelElementExists(childReference)) {
            throw new ResourceAlreadyExistsException(childReference);
        }
        context.getAssetConnectionManager().syncValueProvidersOnWrite(childReference, null, request.getSubmodelElement(), !request.isInternal());
        context.getPersistence().insert(parentReference, request.getSubmodelElement());
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementCreateEventMessage.builder()
                    .element(childReference)
                    .value(request.getSubmodelElement())
                    .build());
        }
        return PostSubmodelElementByPathResponse.builder()
                .payload(request.getSubmodelElement())
                .created()
                .build();
    }

}
